<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Project;
use App\Models\Report;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

class ReportController extends Controller
{
    use ApiResponse;

    /**
     * GET /api/projects/{project}/reports — الأستاذ المشرف أو أعضاء الفريق
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

        $isProfessor = $project->professor_id === $request->user()->id;

        $reports = $project->reports()->with('task')->latest()->get();

        return $this->success([
            'project' => [
                'id' => $project->id,
                'title' => $project->title,
                'is_professor' => $isProfessor,
            ],
            'tasks' => $project->tasks()->get(['id', 'title']),
            'reports' => $reports->map(fn ($r) => [
                'id' => $r->id,
                'project_id' => $r->project_id,
                'task_id' => $r->task_id,
                'task_title' => $r->task?->title,
                'file_name' => basename($r->file_path),
                'feedback' => $r->feedback,
                'submitted_at' => $r->submitted_at?->toIso8601String(),
                'download_url' => route('api.reports.download', $r),
            ])->values(),
        ]);
    }

    /**
     * POST /api/projects/{project}/reports — رفع ملف (multipart) بعضو الفريق
     */
    public function store(Request $request, Project $project): JsonResponse
    {
        // فقط أعضاء الفريق يرفعون التقارير (وليس الأستاذ)
        $isMember = $project->group->members->contains('id', $request->user()->id);
        if (! $isMember) {
            return $this->failure(
                message: 'فقط اعضای تیم می‌توانند گزارش آپلود کنند.',
                errorCode: 'FORBIDDEN',
                status: 403,
            );
        }

        $validated = $request->validate([
            'task_id' => 'nullable|exists:tasks,id',
            'file' => 'required|file|mimes:pdf,doc,docx,jpg,jpeg,png|max:10240',
        ]);

        if ($validated['task_id'] ?? null) {
            $taskBelongsToProject = $project->tasks()->where('id', $validated['task_id'])->exists();
            if (! $taskBelongsToProject) {
                return $this->failure(
                    message: 'این وظیفه مربوط به این پروژه نیست.',
                    errorCode: 'TASK_NOT_IN_PROJECT',
                    status: 422,
                );
            }
        }

        $path = $request->file('file')->store('reports', 'public');

        $report = $project->reports()->create([
            'task_id' => $validated['task_id'] ?? null,
            'file_path' => $path,
            'submitted_at' => now(),
        ]);

        return $this->success([
            'id' => $report->id,
            'task_id' => $report->task_id,
            'file_name' => basename($report->file_path),
            'submitted_at' => $report->submitted_at?->toIso8601String(),
        ], status: 201);
    }

    /**
     * GET /api/reports/{report}/download — تدفق الملف مع ترويسة Authorization
     */
    public function download(Request $request, Report $report)
    {
        if (! $this->canAccess($request->user(), $report->project)) {
            return $this->failure(
                message: 'دسترسی به این فایل برای شما مجاز نیست.',
                errorCode: 'FORBIDDEN',
                status: 403,
            );
        }

        if (! Storage::disk('public')->exists($report->file_path)) {
            return $this->failure(
                message: 'فایل یافت نشد.',
                errorCode: 'FILE_NOT_FOUND',
                status: 404,
            );
        }

        return Storage::disk('public')->download($report->file_path);
    }

    /**
     * PATCH /api/reports/{report}/feedback — الأستاذ المشرف فقط
     */
    public function feedback(Request $request, Report $report): JsonResponse
    {
        if ($report->project->professor_id !== $request->user()->id) {
            return $this->failure(
                message: 'فقط استاد ناظر می‌تواند بازخورد ثبت کند.',
                errorCode: 'FORBIDDEN',
                status: 403,
            );
        }

        $validated = $request->validate([
            'feedback' => 'required|string',
        ]);

        $report->update(['feedback' => $validated['feedback']]);

        return $this->success([
            'id' => $report->id,
            'feedback' => $report->feedback,
        ]);
    }

    private function canAccess($user, Project $project): bool
    {
        $isProfessor = $project->professor_id === $user->id;
        $isMember = $project->group->members->contains('id', $user->id);

        return $isProfessor || $isMember;
    }
}
