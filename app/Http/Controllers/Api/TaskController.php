<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Project;
use App\Models\Task;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;

class TaskController extends Controller
{
    use ApiResponse;

    /**
     * GET /api/projects/{project}/tasks — الأستاذ المشرف أو أعضاء الفريق فقط
     */
    public function index(Request $request, Project $project): JsonResponse
    {
        if (! $this->canAccess($request->user(), $project)) {
            return $this->failure(
                message: 'دسترسی به این پروژه برای شما مجاز نیست.',
                errorCode: 'FORBIDDEN',
                status: 403,
            );
        }

        $tasks = $project->tasks()->with('assignee')->latest()->get();

        return $this->success([
            'project' => [
                'id' => $project->id,
                'title' => $project->title,
                'status' => $project->status,
                'progress_percent' => $project->progress_percent,
                'is_professor' => $project->professor_id === $request->user()->id,
            ],
            'team_members' => $project->group->members->map(fn ($m) => [
                'id' => $m->id,
                'name' => $m->name,
            ])->values(),
            'tasks' => $tasks->map(fn ($t) => $this->taskData($t))->values(),
        ]);
    }

    /**
     * POST /api/projects/{project}/tasks — الأستاذ فقط، والمشروع معتمد أو قيد التنفيذ
     */
    public function store(Request $request, Project $project): JsonResponse
    {
        if ($project->professor_id !== $request->user()->id) {
            return $this->failure(
                message: 'فقط استاد ناظر می‌تواند وظیفه ایجاد کند.',
                errorCode: 'FORBIDDEN',
                status: 403,
            );
        }

        if (! in_array($project->status, ['approved', 'in_progress'], true)) {
            return $this->failure(
                message: 'ابتدا باید پروژه تأیید شود سپس می‌توانید وظیفه اضافه کنید.',
                errorCode: 'PROJECT_NOT_APPROVED',
                status: 422,
            );
        }

        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'nullable|string',
            'deadline' => 'nullable|date',
            'assigned_to' => 'required|exists:users,id',
        ]);

        // إسناد المهمة يجب أن يكون لعضو في فريق المشروع
        if (! $project->group->members->contains('id', $validated['assigned_to'])) {
            return $this->failure(
                message: 'کاربر انتخاب‌شده عضو تیم این پروژه نیست.',
                errorCode: 'NOT_TEAM_MEMBER',
                status: 422,
            );
        }

        $task = $project->tasks()->create($validated);

        // أول مهمة تنقل المشروع إلى قيد التنفيذ
        if ($project->status === 'approved') {
            $project->update(['status' => 'in_progress']);
        }

        $project->recalculateProgress();

        return $this->success($this->taskData($task->fresh()->load('assignee')), status: 201);
    }

    /**
     * PATCH /api/tasks/{task}/status — الطالب المسؤول عن المهمة فقط
     */
    public function updateStatus(Request $request, Task $task): JsonResponse
    {
        if ($task->assigned_to !== $request->user()->id) {
            return $this->failure(
                message: 'فقط دانشجو مسئول این وظیفه می‌تواند وضعیت آن را تغییر دهد.',
                errorCode: 'FORBIDDEN',
                status: 403,
            );
        }

        $validated = $request->validate([
            'status' => 'required|in:todo,in_progress,done',
        ]);

        $task->update(['status' => $validated['status']]);

        $task->project->recalculateProgress();

        return $this->success([
            'task' => $this->taskData($task->load('assignee')),
            'project_progress_percent' => $task->project->fresh()->progress_percent,
        ]);
    }

    private function taskData(Task $task): array
    {
        return [
            'id' => $task->id,
            'project_id' => $task->project_id,
            'title' => $task->title,
            'description' => $task->description,
            'deadline' => $task->deadline ? Carbon::parse($task->deadline)->format('Y-m-d') : null,
            'status' => $task->status,
            'assigned_to' => $task->assigned_to,
            'assignee_name' => $task->assignee?->name,
        ];
    }

    private function canAccess($user, Project $project): bool
    {
        $isProfessor = $project->professor_id === $user->id;
        $isMember = $project->group->members->contains('id', $user->id);

        return $isProfessor || $isMember;
    }
}
