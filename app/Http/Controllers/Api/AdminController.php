<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Group;
use App\Models\Project;
use App\Models\User;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class AdminController extends Controller
{
    use ApiResponse;

    /**
     * GET /api/admin/users — كل المستخدمين مع أدوارهم
     */
    public function users(): JsonResponse
    {
        $users = User::orderBy('role')->orderBy('name')->get();

        return $this->success($users->map(fn ($u) => [
            'id' => $u->id,
            'name' => $u->name,
            'email' => $u->email,
            'role' => $u->role,
            'created_at' => $u->created_at?->toIso8601String(),
        ])->values());
    }

    /**
     * POST /api/admin/users — الأدمن ينشئ مستخدمًا جديدًا (طالب/أستاذ/أدمن)
     */
    public function storeUser(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email',
            'password' => 'required|string|min:6',
            'role' => 'required|in:student,professor,admin',
        ]);

        $user = User::create([
            'name' => $validated['name'],
            'email' => $validated['email'],
            'password' => Hash::make($validated['password']),
            'role' => $validated['role'],
        ]);

        return $this->success([
            'id' => $user->id,
            'name' => $user->name,
            'email' => $user->email,
            'role' => $user->role,
        ], status: 201);
    }

    /**
     * PATCH /api/admin/users/{user} — تعديل اسم/ريد/دور/كلمة مرور مستخدم
     */
    public function updateUser(Request $request, User $user): JsonResponse
    {
        $validated = $request->validate([
            'name' => 'sometimes|required|string|max:255',
            'email' => 'sometimes|required|email|unique:users,email,' . $user->id,
            'password' => 'nullable|string|min:6',
            'role' => 'sometimes|required|in:student,professor,admin',
        ]);

        if (isset($validated['name'])) $user->name = $validated['name'];
        if (isset($validated['email'])) $user->email = $validated['email'];
        if (isset($validated['role'])) $user->role = $validated['role'];
        if (! empty($validated['password'])) $user->password = Hash::make($validated['password']);
        $user->save();

        return $this->success([
            'id' => $user->id,
            'name' => $user->name,
            'email' => $user->email,
            'role' => $user->role,
        ]);
    }

    /**
     * DELETE /api/admin/users/{user} — حذف مستخدم (يمنع حذف نفسه)
     */
    public function destroyUser(Request $request, User $user): JsonResponse
    {
        if ($request->user()->id === $user->id) {
            return $this->failure(
                message: 'لا يمكنك حذف حسابك الحالي.',
                errorCode: 'SELF_DELETE',
                status: 422,
            );
        }

        $user->delete();

        return $this->success(['deleted' => true]);
    }

    /**
     * GET /api/admin/projects — كل المشاريع في النظام
     */
    public function projects(): JsonResponse
    {
        $projects = Project::with(['group', 'professor', 'evaluation'])
            ->latest()
            ->get();

        return $this->success($projects->map(fn ($p) => [
            'id' => $p->id,
            'title' => $p->title,
            'description' => $p->description,
            'status' => $p->status,
            'progress_percent' => $p->progress_percent,
            'group_id' => $p->group_id,
            'professor_id' => $p->professor_id,
            'group_name' => $p->group?->name,
            'professor_name' => $p->professor?->name,
            'score' => $p->evaluation?->score,
            'start_date' => $p->start_date?->format('Y-m-d'),
            'end_date' => $p->end_date?->format('Y-m-d'),
        ])->values());
    }

    /**
     * POST /api/admin/projects — الأدمن ينشئ مشروعًا مباشرة (يتطلب فريقًا موجودًا أو يُنشئ واحدًا)
     */
    public function storeProject(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'nullable|string',
            'group_id' => 'nullable|exists:groups,id',
            'professor_id' => 'nullable|exists:users,id',
            'start_date' => 'nullable|date',
            'end_date' => 'nullable|date|after_or_equal:start_date',
            'status' => 'nullable|in:pending,approved,rejected,in_progress,submitted,evaluated',
        ]);

        // If no group is provided, create an empty placeholder group so the project has one.
        $groupId = $validated['group_id'] ?? null;
        if (! $groupId) {
            $tmpGroup = Group::create([
                'name' => 'گروه بدون عضو - ' . $validated['title'],
                'created_by' => $request->user()->id,
            ]);
            $groupId = $tmpGroup->id;
        }

        $project = Project::create([
            'group_id' => $groupId,
            'professor_id' => $validated['professor_id'] ?? null,
            'title' => $validated['title'],
            'description' => $validated['description'] ?? null,
            'status' => $validated['status'] ?? 'pending',
            'start_date' => $validated['start_date'] ?? null,
            'end_date' => $validated['end_date'] ?? null,
            'progress_percent' => 0,
        ]);

        return $this->success([
            'id' => $project->id,
            'title' => $project->title,
            'status' => $project->status,
        ], status: 201);
    }

    /**
     * PATCH /api/admin/projects/{project} — تعديل عنوان/وصف/حالة/تواريخ/مشرف
     */
    public function updateProject(Request $request, Project $project): JsonResponse
    {
        $validated = $request->validate([
            'title' => 'sometimes|required|string|max:255',
            'description' => 'nullable|string',
            'group_id' => 'sometimes|nullable|exists:groups,id',
            'professor_id' => 'sometimes|nullable|exists:users,id',
            'start_date' => 'sometimes|nullable|date',
            // Nullable on update so the admin can extend/clear the deadline.
            'end_date' => 'sometimes|nullable|date|after_or_equal:start_date',
            'status' => 'sometimes|required|in:pending,approved,rejected,in_progress,submitted,evaluated',
            'progress_percent' => 'sometimes|integer|between:0,100',
        ]);

        foreach (['title', 'description', 'group_id', 'professor_id', 'start_date', 'end_date', 'status', 'progress_percent'] as $k) {
            if (array_key_exists($k, $validated)) {
                $project->{$k} = $validated[$k];
            }
        }
        $project->save();

        return $this->success([
            'id' => $project->id,
            'title' => $project->title,
            'status' => $project->status,
            'progress_percent' => $project->progress_percent,
            'start_date' => $project->start_date?->format('Y-m-d'),
            'end_date' => $project->end_date?->format('Y-m-d'),
        ]);
    }

    /**
     * DELETE /api/admin/projects/{project} — حذف مشروع
     */
    public function destroyProject(Project $project): JsonResponse
    {
        $project->delete();

        return $this->success(['deleted' => true]);
    }

    /**
     * PATCH /api/admin/projects/{project}/extend — تمديد مهلة المشروع فقط
     */
    public function extendDeadline(Request $request, Project $project): JsonResponse
    {
        $validated = $request->validate([
            'end_date' => 'required|date|after_or_equal:' . ($project->start_date?->format('Y-m-d') ?? 'today'),
        ]);

        $project->update(['end_date' => $validated['end_date']]);

        return $this->success([
            'id' => $project->id,
            'title' => $project->title,
            'end_date' => $project->end_date?->format('Y-m-d'),
        ]);
    }
}
