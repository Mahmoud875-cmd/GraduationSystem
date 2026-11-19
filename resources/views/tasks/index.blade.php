<x-app-layout>
    <div class="space-y-4">

        @if (session('success'))
            <div class="mat-card !bg-emerald-50 border border-emerald-600" style="color:#065F46">{{ session('success') }}</div>
        @endif
        @if (session('error'))
            <div class="mat-card !bg-red-50 text-error">{{ session('error') }}</div>
        @endif

        <!-- Project header + progress -->
        <div class="mat-card">
            <div class="flex items-center justify-between gap-3 flex-wrap">
                <div class="min-w-0">
                    <h1 class="text-[22px] font-bold text-on-surface truncate">{{ $project->title }}</h1>
                    <p class="text-sm text-on-surface-variant mt-1">مهام المشروع</p>
                </div>
                @auth
                    @if (auth()->user()->id === $project->professor_id)
                        <a href="{{ route('tasks.create', $project) }}" class="mat-btn-primary">+ إضافة مهمة</a>
                    @endif
                @endauth
            </div>
            <div class="mt-4">
                <div class="flex justify-between text-xs text-on-surface-variant mb-1">
                    <span class="font-medium">تقدم المهام</span>
                    <span>{{ $project->fresh()->progress_percent }}%</span>
                </div>
                <div class="mat-progress-bar">
                    <div class="mat-progress-fill" style="width: {{ $project->fresh()->progress_percent }}%;"></div>
                </div>
            </div>
        </div>

        <!-- Tasks list -->
        @forelse ($tasks as $task)
            <div class="mat-card">
                <div class="flex items-start justify-between gap-4">
                    <div class="flex-1 min-w-0">
                        <h3 class="font-bold text-on-surface">{{ $task->title }}</h3>
                        @if ($task->description)
                            <p class="text-sm text-on-surface-variant mt-1 leading-relaxed">{{ $task->description }}</p>
                        @endif
                        <p class="text-xs text-on-surface-variant mt-2">
                            👤 مسندة إلى: {{ $task->assignee->name ?? '—' }}
                            @if ($task->deadline)
                                · 📅 الموعد النهائي: {{ $task->deadline->format('Y-m-d') }}
                            @endif
                        </p>
                    </div>
                    <span @class([
                        'status-chip shrink-0',
                        'chip-pending' => $task->status === 'todo',
                        'chip-inprogress' => $task->status === 'in_progress',
                        'chip-approved' => $task->status === 'done',
                    ])>
                        {{ ['todo' => 'لم تبدأ', 'in_progress' => 'قيد التنفيذ', 'done' => 'منجزة'][$task->status] ?? $task->status }}
                    </span>
                </div>

                @auth
                    @if (auth()->user()->id === $task->assigned_to)
                        <form method="POST" action="{{ route('tasks.update-status', $task) }}" class="mt-3 flex items-center gap-2">
                            @csrf
                            @method('PATCH')
                            <label class="text-xs text-on-surface-variant">تحديث الحالة:</label>
                            <select name="status" onchange="this.form.submit()" class="mat-input !py-2 !w-auto text-sm">
                                <option value="todo" @selected($task->status === 'todo')>لم تبدأ</option>
                                <option value="in_progress" @selected($task->status === 'in_progress')>قيد التنفيذ</option>
                                <option value="done" @selected($task->status === 'done')>منجزة</option>
                            </select>
                        </form>
                    @endif
                @endauth
            </div>
        @empty
            <div class="mat-card text-center py-12">
                <p class="material-symbols-outlined text-5xl text-outline mb-2">checklist</p>
                <p class="text-on-surface-variant">لا توجد مهام حتى الآن.</p>
            </div>
        @endforelse
    </div>
</x-app-layout>
