<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class GroupController extends Controller
{
    use ApiResponse;

    /**
     * POST /api/groups — الطالب ينشئ فريقًا واحدًا فقط
     */
    public function store(Request $request): JsonResponse
    {
        $user = $request->user();

        if ($user->groups()->exists()) {
            return $this->failure(
                message: 'شما از قبل عضو یک تیم هستید، نمی‌توانید تیم دیگری ایجاد کنید.',
                errorCode: 'ALREADY_IN_GROUP',
                status: 422,
            );
        }

        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'member_emails' => 'nullable|string',
        ]);

        $group = $user->createdGroups()->create([
            'name' => $validated['name'],
        ]);

        // المنشئ يصبح أول عضو تلقائيًا
        $group->members()->attach($user->id);

        $addedEmails = [];
        $notFoundEmails = [];

        if (! empty($validated['member_emails'])) {
            $emails = array_filter(array_map('trim', explode(',', $validated['member_emails'])));

            $found = User::where('role', 'student')
                ->whereIn('email', $emails)
                ->where('id', '!=', $user->id)
                ->pluck('email')
                ->all();

            $memberIds = User::where('role', 'student')
                ->whereIn('email', $found)
                ->pluck('id');

            $group->members()->syncWithoutDetaching($memberIds);

            $addedEmails = $found;
            $notFoundEmails = array_values(array_diff($emails, $found));
        }

        return $this->success([
            'id' => $group->id,
            'name' => $group->name,
            'members' => $group->load('members')->members->map(fn ($m) => [
                'id' => $m->id,
                'name' => $m->name,
                'email' => $m->email,
            ])->values(),
            'added_emails' => $addedEmails,
            'not_found_emails' => $notFoundEmails,
        ], status: 201);
    }
}
