<x-app-layout>
    <div class="space-y-4">

        <h1 class="text-[26px] font-bold text-on-surface">مراجعة مواضيع المشاريع</h1>

        @if (session('success'))
            <div class="mat-card !bg-emerald-50 border border-emerald-600" style="color:#065F46">{{ session('success') }}</div>
        @endif

        @if ($errors->any())
            <div class="mat-card !bg-red-50 text-error text-sm space-y-1">
                @foreach ($errors->all() as $error)
                    <div>{{ $error }}</div>
                @endforeach
            </div>
        @endif

        @forelse ($projects as $project)
            <div class="mat-card">
                <div class="flex items-start justify-between gap-6 flex-wrap">
                    <div class="flex-1 min-w-[240px]">
                        <div class="flex items-center gap-3 flex-wrap">
                            <h3 class="font-bold text-lg text-on-surface">{{ $project->title }}</h3>
                            <span @class([
                                'status-chip',
                                'chip-pending' => $project->status === 'pending',
                                'chip-approved' => $project->status === 'approved',
                                'chip-inprogress' => $project->status === 'in_progress',
                                'chip-rejected' => $project->status === 'rejected',
                            ])>
                                {{ ['pending' => 'بانتظار الموافقة', 'approved' => 'معتمد', 'in_progress' => 'قيد التنفيذ', 'submitted' => 'مُقدَّم', 'evaluated' => 'مُقيَّم', 'rejected' => 'مرفوض'][$project->status] ?? $project->status }}
                            </span>
                        </div>
                        @if ($project->description)
                            <p class="text-sm text-on-surface-variant mt-2 leading-relaxed">{{ $project->description }}</p>
                        @endif
                        <p class="text-xs text-on-surface-variant mt-2">
                            👥 الفريق: {{ $project->group->name }} · الأعضاء: {{ $project->group->members->pluck('name')->join('، ') }}
                        </p>
                    </div>

                    @if ($project->status === 'pending')
                        <div class="w-72 shrink-0 rounded-xl bg-surface-container-low p-4">
                            <form method="POST" action="{{ route('projects.approve', $project) }}" class="space-y-3">
                                @csrf
                                <div>
                                    <label class="block text-xs text-on-surface-variant mb-1">تاريخ بداية المشروع</label>
                                    <input type="date" name="start_date" required class="mat-input !py-2.5 text-sm" />
                                </div>
                                <div>
                                    <label class="block text-xs text-on-surface-variant mb-1">تاريخ نهاية المشروع المتوقع</label>
                                    <input type="date" name="end_date" required class="mat-input !py-2.5 text-sm" />
                                </div>
                                <button type="submit" class="mat-btn-primary w-full justify-center">اعتماد الموضوع</button>
                            </form>

                            <form method="POST" action="{{ route('projects.reject', $project) }}" class="mt-2">
                                @csrf
                                <button type="submit" class="mat-btn-danger w-full justify-center">رفض</button>
                            </form>
                        </div>
                    @endif
                </div>
            </div>
        @empty
            <div class="mat-card text-center py-12">
                <p class="material-symbols-outlined text-5xl text-outline mb-2">inbox</p>
                <p class="text-on-surface-variant">لا توجد مواضيع مرسلة إليك حتى الآن.</p>
            </div>
        @endforelse
    </div>
</x-app-layout>
