<?php

namespace App\Http\Controllers;

use App\Models\Group;
use App\Models\Project;
use App\Models\User;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\Rule;
use Illuminate\View\View;

class AdminController extends Controller
{
    /**
     * عرض كل المستخدمين مصنّفين حسب الدور
     */
    public function users(): View
    {
        $users = User::orderBy('role')->orderBy('name')->get();

        return view('admin.users', compact('users'));
    }

    /**
     * إضافة مستخدم جديد
     */
    public function storeUser(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'name' => ['required', 'string', 'max:255'],
            'email' => ['required', 'string', 'email', 'max:255', 'unique:users,email'],
            'role' => ['required', Rule::in(['student', 'professor', 'admin'])],
            'password' => ['required', 'string', 'min:8'],
        ]);

        $validated['password'] = Hash::make($validated['password']);
        User::create($validated);

        return back()->with('success', 'تمت إضافة المستخدم بنجاح.');
    }

    /**
     * تعديل مستخدم
     */
    public function updateUser(Request $request, User $user): RedirectResponse
    {
        $validated = $request->validate([
            'name' => ['required', 'string', 'max:255'],
            'email' => ['required', 'string', 'email', 'max:255', Rule::unique('users', 'email')->ignore($user->id)],
            'role' => ['required', Rule::in(['student', 'professor', 'admin'])],
            'password' => ['nullable', 'string', 'min:8'],
        ]);

        $user->fill($validated);
        if ($request->filled('password')) {
            $user->password = Hash::make($request->input('password'));
        }
        $user->save();

        return back()->with('success', 'تم تحديث المستخدم.');
    }

    /**
     * حذف مستخدم (مع منع حذف الذات)
     */
    public function destroyUser(Request $request, User $user): RedirectResponse
    {
        if ($request->user()->id === $user->id) {
            return back()->with('error', 'لا يمكنك حذف حسابك الحالي.');
        }

        $user->delete();

        return back()->with('success', 'تم حذف المستخدم.');
    }

    /**
     * عرض كل المشاريع في النظام بغض النظر عن الأستاذ المشرف
     */
    public function projects(): View
    {
        $projects = Project::with(['group', 'professor', 'evaluation'])
            ->latest()
            ->get();

        $groups = Group::with('members')->get();
        $professors = User::where('role', 'professor')->orderBy('name')->get();

        return view('admin.projects', compact('projects', 'groups', 'professors'));
    }

    /**
     * إضافة مشروع جديد
     */
    public function storeProject(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'title' => ['required', 'string', 'max:255'],
            'description' => ['nullable', 'string', 'max:5000'],
            'status' => ['required', Rule::in(['pending', 'approved', 'in_progress', 'submitted', 'evaluated', 'rejected'])],
            'professor_id' => ['nullable', 'exists:users,id'],
            'group_id' => ['nullable', 'exists:groups,id'],
            'start_date' => ['nullable', 'date'],
            'end_date' => ['nullable', 'date', 'after_or_equal:start_date'],
        ]);

        // ربط بفريق موجود أو إنشاء فريق placeholder
        if (! empty($validated['group_id'])) {
            $groupId = (int) $validated['group_id'];
        } else {
            $groupId = Group::create([
                'name' => $validated['title'] . ' — تیم',
                'created_by' => $request->user()->id,
            ])->id;
        }
        unset($validated['group_id']);

        $validated['group_id'] = $groupId;
        $project = Project::create($validated);
        $project->recalculateProgress();

        return back()->with('success', 'تمت إضافة المشروع بنجاح.');
    }

    /**
     * تعديل مشروع
     */
    public function updateProject(Request $request, Project $project): RedirectResponse
    {
        $validated = $request->validate([
            'title' => ['sometimes', 'required', 'string', 'max:255'],
            'description' => ['nullable', 'string', 'max:5000'],
            'status' => ['sometimes', 'required', Rule::in(['pending', 'approved', 'in_progress', 'submitted', 'evaluated', 'rejected'])],
            'professor_id' => ['nullable', 'exists:users,id'],
            'start_date' => ['nullable', 'date'],
            'end_date' => ['nullable', 'date', 'after_or_equal:start_date'],
        ]);

        $project->update($validated);
        $project->recalculateProgress();

        return back()->with('success', 'تم تحديث المشروع.');
    }

    /**
     * تمديد موعد نهاية المشروع
     */
    public function extendDeadline(Request $request, Project $project): RedirectResponse
    {
        $validated = $request->validate([
            'end_date' => ['required', 'date', 'after_or_equal:' . ($project->start_date?->format('Y-m-d') ?? 'today')],
        ]);

        $project->update(['end_date' => $validated['end_date']]);

        return back()->with('success', 'تم تمديد الموعد النهائي.');
    }

    /**
     * حذف مشروع
     */
    public function destroyProject(Request $request, Project $project): RedirectResponse
    {
        $project->delete();

        return back()->with('success', 'تم حذف المشروع.');
    }
}
