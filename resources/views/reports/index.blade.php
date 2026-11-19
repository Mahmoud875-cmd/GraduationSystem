<x-app-layout>
    <div class="space-y-4">

        @if (session('success'))
            <div class="mat-card !bg-emerald-50 border border-emerald-600" style="color:#065F46">{{ session('success') }}</div>
        @endif

        <!-- Header -->
        <div class="mat-card flex items-center justify-between gap-3 flex-wrap">
            <div class="min-w-0">
                <h1 class="text-[22px] font-bold text-on-surface truncate">تقارير مشروع: {{ $project->title }}</h1>
                <p class="text-sm text-on-surface-variant mt-1">التقارير المرفوعة</p>
            </div>
            @auth
                @if (auth()->user()->id !== $project->professor_id)
                    <a href="{{ route('reports.create', $project) }}" class="mat-btn-primary">+ رفع تقرير جديد</a>
                @endif
            @endauth
        </div>

        <!-- Reports list -->
        @forelse ($reports as $report)
            <div class="mat-card">
                <div class="flex items-start justify-between gap-4">
                    <div class="flex-1 min-w-0">
                        <div class="flex items-center gap-2">
                            <span class="material-symbols-outlined text-primary">description</span>
                            <p class="font-bold text-on-surface">
                                {{ $report->task ? 'مرتبط بمهمة: ' . $report->task->title : 'تقرير عام عن المشروع' }}
                            </p>
                        </div>
                        <p class="text-xs text-on-surface-variant mt-2">📤 تاريخ الرفع: {{ $report->submitted_at->format('Y-m-d H:i') }}</p>
                        <a href="{{ route('reports.download', $report) }}" class="inline-flex items-center gap-1 text-sm text-primary font-medium mt-2 hover:underline">
                            ⬇ تحميل الملف
                        </a>
                    </div>
                </div>

                <!-- Feedback box -->
                <div class="mt-3 rounded-xl bg-surface-container-low p-4">
                    @if ($report->feedback)
                        <p class="text-sm text-on-surface">
                            <span class="font-semibold">📝 ملاحظة الأستاذ:</span> {{ $report->feedback }}
                        </p>
                    @else
                        <p class="text-sm text-outline mb-2">لا توجد ملاحظة بعد.</p>
                    @endif

                    @auth
                        @if (auth()->user()->id === $project->professor_id)
                            <form method="POST" action="{{ route('reports.feedback', $report) }}" class="mt-3 flex gap-2">
                                @csrf
                                @method('PATCH')
                                <input type="text" name="feedback" value="{{ $report->feedback }}"
                                    placeholder="اكتب ملاحظتك هنا..." class="mat-input flex-1 !py-2.5" required />
                                <button type="submit" class="mat-btn-primary whitespace-nowrap">حفظ</button>
                            </form>
                        @endif
                    @endauth
                </div>
            </div>
        @empty
            <div class="mat-card text-center py-12">
                <p class="material-symbols-outlined text-5xl text-outline mb-2">folder_open</p>
                <p class="text-on-surface-variant">لا توجد تقارير مرفوعة حتى الآن.</p>
            </div>
        @endforelse
    </div>
</x-app-layout>
