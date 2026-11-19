<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class GroupController extends Controller
{
    /**
     * نموذج إنشاء فريق جديد (طالب فقط، ولمرة واحدة إذا لم يكن عضوًا في فريق مسبقًا)
     */
    public function create(Request $request): View|RedirectResponse
    {
        $user = $request->user();

        if ($user->groups()->exists()) {
            return redirect()->route('dashboard')
                ->with('error', 'شما از قبل عضو یک فریق هستید نمی توانید تیم دیگری ایجاد کنید');
        }

        return view('groups.create');
    }

    /**
     * حفظ الفريق الجديد، وإضافة المنشئ + الأعضاء المدعوين (بالبريد الإلكتروني)
     */
    public function store(Request $request): RedirectResponse
    {
        $user = $request->user();

        if ($user->groups()->exists()) {
            return redirect()->route('dashboard')
                ->with('error', 'أنت عضو بالفعل في فريق.');
        }

        $validated = $request->validate([
            'name' => 'required|string|max:255',
            // يكتب الطالب إيميلات زملائه مفصولة بفاصلة، مثال: a@x.com, b@x.com
            'member_emails' => 'nullable|string',
        ]);

        $group = $user->createdGroups()->create([
            'name' => $validated['name'],
        ]);

        // المنشئ نفسه يصبح أول عضو تلقائيًا
        $group->members()->attach($user->id);

        // إضافة بقية الأعضاء إن وُجدوا كطلاب مسجّلين فعليًا في النظام
        if (! empty($validated['member_emails'])) {
            $emails = array_map('trim', explode(',', $validated['member_emails']));

            $memberIds = User::where('role', 'student')
                ->whereIn('email', $emails)
                ->where('id', '!=', $user->id)
                ->pluck('id');

            $group->members()->syncWithoutDetaching($memberIds);
        }

        return redirect()->route('dashboard')
            ->with('success', 'تم إنشاء الفريق بنجاح.');
    }
}
