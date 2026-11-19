<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Project;
use App\Models\User;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class DashboardController extends Controller
{
    use ApiResponse;

    /**
     * GET /api/dashboard — بيانات اللوحة حسب دور المستخدم
     */
    public function index(Request $request): JsonResponse
    {
        $user = $request->user();

        return match ($user->role) {
            'student' => $this->success($this->studentData($user)),
            'professor' => $this->success($this->professorData($user)),
            'admin' => $this->success($this->adminData($user)),
            default => $this->failure(
                message: 'نوع کاربر نامشخص است.',
                errorCode: 'UNKNOWN_ROLE',
                status: 403,
            ),
        };
    }

    private function studentData($user): array
    {
        $group = $user->groups()->with(['project.evaluation', 'members', 'project.professor'])->first();

        $project = null;
        if ($group && $group->project) {
            $p = $group->project;
            $project = [
                'id' => $p->id,
                'title' => $p->title,
                'description' => $p->description,
                'status' => $p->status,
                'progress_percent' => $p->progress_percent,
                'start_date' => $p->start_date?->format('Y-m-d'),
                'end_date' => $p->end_date?->format('Y-m-d'),
                'professor_name' => $p->professor?->name,
                'evaluation' => $p->evaluation ? [
                    'score' => $p->evaluation->score,
                    'comments' => $p->evaluation->comments,
                    'evaluated_at' => $p->evaluation->evaluated_at?->toIso8601String(),
                ] : null,
            ];
        }

        return [
            'role' => 'student',
            'name' => $user->name,
            'has_group' => (bool) $group,
            'group' => $group ? [
                'id' => $group->id,
                'name' => $group->name,
                'members' => $group->members->map(fn ($m) => [
                    'id' => $m->id,
                    'name' => $m->name,
                    'email' => $m->email,
                ])->values(),
            ] : null,
            'can_create_group' => ! (bool) $group,
            'can_register_project' => $group && ! $group->project,
            'project' => $project,
        ];
    }

    private function professorData($user): array
    {
        $pendingCount = $user->supervisedProjects()->where('status', 'pending')->count();

        $activeProjects = $user->supervisedProjects()
            ->whereIn('status', ['approved', 'in_progress'])
            ->with(['evaluation', 'group.members'])
            ->latest()
            ->get();

        return [
            'role' => 'professor',
            'name' => $user->name,
            'pending_count' => $pendingCount,
            'active_projects' => $activeProjects->map(fn ($p) => [
                'id' => $p->id,
                'title' => $p->title,
                'status' => $p->status,
                'progress_percent' => $p->progress_percent,
                'group_name' => $p->group?->name,
                'team_members' => $p->group->members->map(fn ($m) => [
                    'id' => $m->id,
                    'name' => $m->name,
                ])->values(),
                'has_evaluation' => (bool) $p->evaluation,
            ])->values(),
        ];
    }

    private function adminData($user): array
    {
        return [
            'role' => 'admin',
            'name' => $user->name,
            'users_count' => User::count(),
            'projects_count' => Project::count(),
            'pending_projects_count' => Project::where('status', 'pending')->count(),
        ];
    }
}
