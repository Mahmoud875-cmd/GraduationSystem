<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Project;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class EvaluationController extends Controller
{
    use ApiResponse;

    /**
     * GET /api/projects/{project}/evaluate — عرض حالة التقييم للأستاذ
     */
    public function show(Request $request, Project $project): JsonResponse
    {
        if ($project->professor_id !== $request->user()->id) {
            return $this->failure(
                message: 'فقط استاد ناظر می‌تواند این پروژه را ارزیابی کند.',
                errorCode: 'FORBIDDEN',
                status: 403,
            );
        }

        return $this->success([
            'project' => [
                'id' => $project->id,
                'title' => $project->title,
                'group_name' => $project->group?->name,
            ],
            'already_evaluated' => (bool) $project->evaluation,
            'evaluation' => $project->evaluation ? [
                'score' => $project->evaluation->score,
                'comments' => $project->evaluation->comments,
                'evaluated_at' => $project->evaluation->evaluated_at?->toIso8601String(),
            ] : null,
        ]);
    }

    /**
     * POST /api/projects/{project}/evaluate — الأستاذ فقط، ومرة واحدة لكل مشروع
     */
    public function store(Request $request, Project $project): JsonResponse
    {
        if ($project->professor_id !== $request->user()->id) {
            return $this->failure(
                message: 'فقط استاد ناظر می‌تواند این پروژه را ارزیابی کند.',
                errorCode: 'FORBIDDEN',
                status: 403,
            );
        }

        if ($project->evaluation) {
            return $this->failure(
                message: 'این پروژه قبلاً ارزیابی شده است.',
                errorCode: 'ALREADY_EVALUATED',
                status: 422,
            );
        }

        $validated = $request->validate([
            'score' => 'required|numeric|min:0|max:20',
            'comments' => 'nullable|string',
        ]);

        $evaluation = $project->evaluation()->create([
            'professor_id' => $request->user()->id,
            'score' => $validated['score'],
            'comments' => $validated['comments'] ?? null,
            'evaluated_at' => now(),
        ]);

        $project->update(['status' => 'evaluated']);

        // Final evaluation means the project is fully complete → force 100% progress.
        $project->recalculateProgress();

        return $this->success([
            'id' => $evaluation->id,
            'score' => $evaluation->score,
            'comments' => $evaluation->comments,
            'project_status' => $project->fresh()->status,
            'progress_percent' => $project->fresh()->progress_percent,
        ], status: 201);
    }
}
