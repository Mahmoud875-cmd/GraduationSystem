<x-app-layout>
    <x-slot name="header">
        <h2 class="text-2xl font-bold" style="color: #1A1C1C;">داشبورد استاد راهنما</h2>
    </x-slot>

    <div class="space-y-6 max-w-4xl mx-auto">

        <!-- Welcome + Stats -->
        <div class="mat-card">
            <p class="text-lg font-semibold mb-4">سلام د. {{ $user->name }} 👋</p>

            @php
                $pendingCount = $user->supervisedProjects()->where('status', 'pending')->count();
                $activeProjects = $user->supervisedProjects()->whereIn('status', ['approved', 'in_progress'])->with('evaluation')->get();
            @endphp

            <div class="grid grid-cols-2 gap-4 mb-6">
                <div class="stat-card">
                    <div class="stat-icon" style="background-color: #F59E0B;">{{ $pendingCount }}</div>
                    <div>
                        <p class="font-semibold text-sm">در انتظار بررسی</p>
                        <p class="text-xs" style="color: #464555;">موضوعات جدید</p>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon">{{ $activeProjects->count() }}</div>
                    <div>
                        <p class="font-semibold text-sm">پروژه‌های فعال</p>
                        <p class="text-xs" style="color: #464555;">در حال انجام</p>
                    </div>
                </div>
            </div>

            <a href="{{ route('projects.professor-index') }}" class="mat-btn-primary">
                📋 مرور مباحث
            </a>
        </div>

        <!-- Active Projects -->
        @if ($activeProjects->isNotEmpty())
            <div class="section-title">پروژه‌های تأیید شده</div>
            <div class="space-y-4">
                @foreach ($activeProjects as $project)
                    <div class="mat-card">
                        <div class="flex items-start justify-between gap-4">
                            <div class="flex-1">
                                <h3 class="font-bold mb-1">{{ $project->title }}</h3>
                                <div class="flex items-center gap-3 mt-2">
                                    <span class="status-chip chip-inprogress">در حال انجام</span>
                                    <div class="flex-1 max-w-xs">
                                        <div class="flex justify-between text-xs mb-1">
                                            <span style="color: #464555;">پیشرفت</span>
                                            <span class="font-bold" style="color: #3525CD;">{{ $project->progress_percent }}٪</span>
                                        </div>
                                        <div class="mat-progress-bar">
                                            <div class="mat-progress-fill" style="width: {{ $project->progress_percent }}%;"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="flex flex-wrap gap-2 mt-4 pt-4 border-t" style="border-color: #E2E2E2;">
                            <a href="{{ route('tasks.index', $project) }}" class="mat-btn-secondary text-xs px-4 py-2">
                                📋 وظایف
                            </a>
                            <a href="{{ route('reports.index', $project) }}" class="mat-btn-outline text-xs px-4 py-2">
                                📂 گزارش‌ها
                            </a>
                            @if (! $project->evaluation)
                                <a href="{{ route('evaluations.create', $project) }}" class="mat-btn-primary text-xs px-4 py-2"
                                   style="background-color: #059669;">
                                    ⭐ ارزیابی نهایی
                                </a>
                            @endif
                        </div>
                    </div>
                @endforeach
            </div>
        @endif
    </div>
</x-app-layout>
