<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Project;
use App\Models\User;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ProjectController extends Controller
{
    use ApiResponse;

    /**
     * GET /api/professors — قائمة الأساتذة لاختيار المشرف
     */
    public function professors(): JsonResponse
    {
        $professors = User::where('role', 'professor')
            ->orderBy('name')
            ->get(['id', 'name', 'email']);

        return $this->success($professors);
    }

    /**
     * POST /api/projects — الطالب يسجل موضوعًا (فريق بدون مشروع فقط)
     */
    public function store(Request $request): JsonResponse
    {
        $user = $request->user();
        $group = $user->groups()->first();

        if (! $group) {
            return $this->failure(
                message: 'ابتدا باید یک تیم ایجاد کنید یا به تیم بپیوندید.',
                errorCode: 'NO_GROUP',
                status: 422,
            );
        }

        if ($group->project) {
            return $this->failure(
                message: 'تیم شما قبلاً یک موضوع ثبت کرده است.',
                errorCode: 'GROUP_HAS_PROJECT',
                status: 422,
            );
        }

        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'nullable|string',
            'professor_id' => 'required|exists:users,id',
        ]);

        $professor = User::find($validated['professor_id']);
        if (! $professor || ! $professor->isProfessor()) {
            return $this->failure(
                message: 'کاربر انتخاب‌شده استاد راهنما نیست.',
                errorCode: 'NOT_A_PROFESSOR',
                status: 422,
            );
        }

        $project = $group->project()->create([
            'professor_id' => $validated['professor_id'],
            'title' => $validated['title'],
            'description' => $validated['description'] ?? null,
            'status' => 'pending',
        ]);

        return $this->success([
            'id' => $project->id,
            'title' => $project->title,
            'status' => $project->status,
        ], status: 201);
    }

    /**
     * GET /api/professor/projects — كل مواضيع التي يشرف عليها الأستاذ الحالي
     */
    public function professorIndex(Request $request): JsonResponse
    {
        $projects = $request->user()
            ->supervisedProjects()
            ->with(['group.members', 'evaluation'])
            ->latest()
            ->get();

        return $this->success($projects->map(fn ($p) => [
            'id' => $p->id,
            'title' => $p->title,
            'description' => $p->description,
            'status' => $p->status,
            'progress_percent' => $p->progress_percent,
            'start_date' => $p->start_date?->format('Y-m-d'),
            'end_date' => $p->end_date?->format('Y-m-d'),
            'group_name' => $p->group?->name,
            'team_members' => $p->group->members->map(fn ($m) => [
                'id' => $m->id,
                'name' => $m->name,
            ])->values(),
            'has_evaluation' => (bool) $p->evaluation,
        ])->values());
    }

    /**
     * POST /api/projects/{project}/approve — اعتماد مع تحديد الجدول الزمني
     */
    public function approve(Request $request, Project $project): JsonResponse
    {
        if ($project->professor_id !== $request->user()->id) {
            return $this->failure(
                message: 'فقط استاد ناظر این پروژه می‌تواند آن را تأیید کند.',
                errorCode: 'FORBIDDEN',
                status: 403,
            );
        }

        $validated = $request->validate([
            'start_date' => 'required|date',
            'end_date' => 'required|date|after:start_date',
        ]);

        $project->update([
            'status' => 'approved',
            'start_date' => $validated['start_date'],
            'end_date' => $validated['end_date'],
        ]);

        return $this->success([
            'id' => $project->id,
            'status' => $project->status,
            'start_date' => $project->start_date->format('Y-m-d'),
            'end_date' => $project->end_date->format('Y-m-d'),
        ]);
    }

    /**
     * POST /api/projects/{project}/reject — رفض الموضوع
     */
    public function reject(Request $request, Project $project): JsonResponse
    {
        if ($project->professor_id !== $request->user()->id) {
            return $this->failure(
                message: 'فقط استاد ناظر این پروژه می‌تواند آن را رد کند.',
                errorCode: 'FORBIDDEN',
                status: 403,
            );
        }

        $project->update(['status' => 'rejected']);

        return $this->success([
            'id' => $project->id,
            'status' => $project->status,
        ]);
    }
}
