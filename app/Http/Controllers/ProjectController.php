<?php

namespace App\Http\Controllers;

use App\Models\Project;
use App\Models\User;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class ProjectController extends Controller
{
    /*
    |--------------------------------------------------------------------
    | الطالب: تسجيل موضوع المشروع
    |--------------------------------------------------------------------
    */

    public function create(Request $request): View|RedirectResponse
    {
        $user = $request->user();
        $group = $user->groups()->first();

        if (! $group) {
            return redirect()->route('dashboard')
                ->with('error', 'يجب أن تنشئ أو تنضم إلى فريق أولاً قبل تسجيل موضوع.');
        }

        if ($group->project) {
            return redirect()->route('dashboard')
                ->with('error', 'فريقك سجّل موضوعًا بالفعل.');
        }

        $professors = User::where('role', 'professor')->orderBy('name')->get();

        return view('projects.create', compact('professors'));
    }

    public function store(Request $request): RedirectResponse
    {
        $user = $request->user();
        $group = $user->groups()->first();

        if (! $group || $group->project) {
            return redirect()->route('dashboard')
                ->with('error', 'لا يمكن تسجيل موضوع الآن.');
        }

        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'nullable|string',
            'professor_id' => 'required|exists:users,id',
        ]);

        $group->project()->create([
            'professor_id' => $validated['professor_id'],
            'title' => $validated['title'],
            'description' => $validated['description'] ?? null,
            'status' => 'pending',
        ]);

        return redirect()->route('dashboard')
            ->with('success', 'تم إرسال موضوع مشروعك بانتظار موافقة الأستاذ.');
    }

    /*
    |--------------------------------------------------------------------
    | الأستاذ: مراجعة المواضيع المُرسلة إليه
    |--------------------------------------------------------------------
    */

    public function professorIndex(Request $request): View
    {
        $projects = $request->user()
            ->supervisedProjects()
            ->with('group.members')
            ->latest()
            ->get();

        return view('projects.professor-index', compact('projects'));
    }

    /**
     * اعتماد الموضوع + تحديد تاريخ بداية ونهاية المشروع
     * (هذان التاريخان يُستخدمان لاحقًا في وحدة تحليل Python للتنبؤ بالتأخير)
     */
    public function approve(Request $request, Project $project): RedirectResponse
    {
        abort_unless($project->professor_id === $request->user()->id, 403);

        $validated = $request->validate([
            'start_date' => 'required|date',
            'end_date' => 'required|date|after:start_date',
        ]);

        $project->update([
            'status' => 'approved',
            'start_date' => $validated['start_date'],
            'end_date' => $validated['end_date'],
        ]);

        return back()->with('success', 'تم اعتماد الموضوع بنجاح، مع تحديد الجدول الزمني للمشروع.');
    }

    public function reject(Request $request, Project $project): RedirectResponse
    {
        abort_unless($project->professor_id === $request->user()->id, 403);

        $project->update(['status' => 'rejected']);

        return back()->with('success', 'تم رفض الموضوع.');
    }
}
