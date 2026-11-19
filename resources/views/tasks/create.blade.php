<x-app-layout>
    <div class="max-w-2xl mx-auto space-y-4">

        <a href="{{ route('tasks.index', $project) }}" class="inline-flex items-center gap-1 text-sm text-primary font-medium hover:underline">
            → العودة إلى المهام
        </a>

        <div class="mat-card">
            <h1 class="text-[22px] font-bold text-on-surface mb-1">إضافة مهمة جديدة</h1>
            <p class="text-sm text-on-surface-variant mb-5">{{ $project->title }}</p>

            @if ($errors->any())
                <div class="mb-4 p-4 rounded-xl bg-red-50 text-error text-sm space-y-1">
                    @foreach ($errors->all() as $error)
                        <div>{{ $error }}</div>
                    @endforeach
                </div>
            @endif

            <form method="POST" action="{{ route('tasks.store', $project) }}" class="space-y-4">
                @csrf

                <div>
                    <label for="title" class="block text-sm font-medium text-on-surface-variant mb-1.5">عنوان المهمة</label>
                    <input id="title" name="title" type="text" class="mat-input" value="{{ old('title') }}" required autofocus />
                </div>

                <div>
                    <label for="description" class="block text-sm font-medium text-on-surface-variant mb-1.5">وصف المهمة</label>
                    <textarea id="description" name="description" rows="3" class="mat-input resize-none">{{ old('description') }}</textarea>
                </div>

                <div>
                    <label for="deadline" class="block text-sm font-medium text-on-surface-variant mb-1.5">الموعد النهائي</label>
                    <input id="deadline" name="deadline" type="date" class="mat-input" value="{{ old('deadline') }}" />
                </div>

                <div>
                    <label for="assigned_to" class="block text-sm font-medium text-on-surface-variant mb-1.5">إسناد المهمة إلى</label>
                    <select id="assigned_to" name="assigned_to" class="mat-input" required>
                        <option value="">— اختر طالبًا —</option>
                        @foreach ($members as $member)
                            <option value="{{ $member->id }}" @selected(old('assigned_to') == $member->id)>{{ $member->name }}</option>
                        @endforeach
                    </select>
                </div>

                <button type="submit" class="mat-btn-primary w-full justify-center mt-2">
                    إضافة المهمة
                </button>
            </form>
        </div>
    </div>
</x-app-layout>
