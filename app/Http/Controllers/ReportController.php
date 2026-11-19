<?php

namespace App\Http\Controllers;

use App\Models\Project;
use App\Models\Report;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\View\View;

class ReportController extends Controller
{
    /**
     * عرض كل التقارير المرفوعة لمشروع معيّن (يراها الأستاذ وأعضاء الفريق)
     */
    public function index(Request $request, Project $project): View
    {
        $this->authorizeAccess($request->user(), $project);

        $reports = $project->reports()->with('task')->latest()->get();

        return view('reports.index', compact('project', 'reports'));
    }

    /**
     * نموذج رفع تقرير جديد (عضو الفريق فقط)
     */
    public function create(Request $request, Project $project): View
    {
        $this->authorizeAccess($request->user(), $project);

        $tasks = $project->tasks;

        return view('reports.create', compact('project', 'tasks'));
    }

    public function store(Request $request, Project $project): RedirectResponse
    {
        $this->authorizeAccess($request->user(), $project);

        $validated = $request->validate([
            'task_id' => 'nullable|exists:tasks,id',
            // نقبل PDF أو Word أو صور، بحد أقصى 10 ميجابايت
            'file' => 'required|file|mimes:pdf,doc,docx,jpg,jpeg,png|max:10240',
        ]);

        // تخزين الملف داخل storage/app/public/reports (رابط عام لاحقًا عبر storage:link)
        $path = $request->file('file')->store('reports', 'public');

        $project->reports()->create([
            'task_id' => $validated['task_id'] ?? null,
            'file_path' => $path,
            'submitted_at' => now(),
        ]);

        return redirect()->route('reports.index', $project)
            ->with('success', 'تم رفع التقرير بنجاح.');
    }

    /**
     * تحميل الملف (لأي طرف مصرّح له بالوصول للمشروع)
     */
    public function download(Request $request, Report $report)
    {
        $this->authorizeAccess($request->user(), $report->project);

        abort_unless(Storage::disk('public')->exists($report->file_path), 404);

        return Storage::disk('public')->download($report->file_path);
    }

    /**
     * الأستاذ يضيف ملاحظة نصية على تقرير معيّن
     */
    public function feedback(Request $request, Report $report): RedirectResponse
    {
        abort_unless($report->project->professor_id === $request->user()->id, 403);

        $validated = $request->validate([
            'feedback' => 'required|string',
        ]);

        $report->update(['feedback' => $validated['feedback']]);

        return redirect()->route('reports.index', $report->project)
            ->with('success', 'تم حفظ ملاحظتك.');
    }

    /**
     * السماح فقط للأستاذ المشرف أو أعضاء الفريق صاحب المشروع
     */
    private function authorizeAccess($user, Project $project): void
    {
        $isProfessor = $project->professor_id === $user->id;
        $isMember = $project->group->members->contains('id', $user->id);

        abort_unless($isProfessor || $isMember, 403);
    }
}
