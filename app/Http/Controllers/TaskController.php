<?php

namespace App\Http\Controllers;

use App\Models\Project;
use App\Models\Task;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class TaskController extends Controller
{
    /**
     * عرض كل مهام مشروع معيّن — يراها كل من الأستاذ المشرف وأعضاء الفريق
     */
    public function index(Request $request, Project $project): View
    {
        $user = $request->user();
        $this->authorizeAccess($user, $project);

        $tasks = $project->tasks()->with('assignee')->latest()->get();

        return view('tasks.index', compact('project', 'tasks'));
    }

    /**
     * نموذج إنشاء مهمة جديدة (الأستاذ فقط، وفقط إذا كان المشروع معتمدًا)
     */
    public function create(Request $request, Project $project): View|RedirectResponse
    {
        abort_unless($project->professor_id === $request->user()->id, 403);

        if ($project->status !== 'approved' && $project->status !== 'in_progress') {
            return redirect()->route('tasks.index', $project)
                ->with('error', 'يجب اعتماد المشروع أولاً قبل إضافة مهام.');
        }

        $members = $project->group->members;

        return view('tasks.create', compact('project', 'members'));
    }

    public function store(Request $request, Project $project): RedirectResponse
    {
        abort_unless($project->professor_id === $request->user()->id, 403);

        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'nullable|string',
            'deadline' => 'nullable|date',
            'assigned_to' => 'required|exists:users,id',
        ]);

        $project->tasks()->create($validated);

        // بمجرد إضافة أول مهمة، ننقل المشروع لحالة "قيد التنفيذ"
        if ($project->status === 'approved') {
            $project->update(['status' => 'in_progress']);
        }

        $project->recalculateProgress();

        return redirect()->route('tasks.index', $project)
            ->with('success', 'تمت إضافة المهمة بنجاح.');
    }

    /**
     * الطالب المسؤول عن المهمة يغيّر حالتها
     */
    public function updateStatus(Request $request, Task $task): RedirectResponse
    {
        abort_unless($task->assigned_to === $request->user()->id, 403);

        $validated = $request->validate([
            'status' => 'required|in:todo,in_progress,done',
        ]);

        $task->update(['status' => $validated['status']]);

        // تحديث نسبة تقدم المشروع تلقائيًا بعد أي تغيير في حالة مهمة
        $task->project->recalculateProgress();

        return redirect()->route('tasks.index', $task->project)
            ->with('success', 'تم تحديث حالة المهمة.');
    }

    /**
     * السماح بالوصول فقط للأستاذ المشرف أو أعضاء الفريق صاحب المشروع
     */
    private function authorizeAccess($user, Project $project): void
    {
        $isProfessor = $project->professor_id === $user->id;
        $isMember = $project->group->members->contains('id', $user->id);

        abort_unless($isProfessor || $isMember, 403);
    }
}
