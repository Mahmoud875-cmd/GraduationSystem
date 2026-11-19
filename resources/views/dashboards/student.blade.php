<x-app-layout>
    <x-slot name="header">
        <h2 class="text-2xl font-bold" style="color: #1A1C1C;">داشبورد دانشجو</h2>
    </x-slot>

    <div class="space-y-6 max-w-4xl mx-auto">

        @if (session('success'))
            <div class="p-4 rounded-2xl text-sm font-medium" style="background-color: #DCFCE7; color: #166534;">{{ session('success') }}</div>
        @endif

        @if (session('error'))
            <div class="p-4 rounded-2xl text-sm font-medium" style="background-color: #FEE2E2; color: #991B1B;">{{ session('error') }}</div>
        @endif

        <!-- Welcome Card -->
        <div class="mat-card">
            <p class="text-lg font-semibold mb-4">سلام {{ $user->name }} 👋</p>

            @php $group = $user->groups()->with(['project.evaluation'])->first(); @endphp

            @if (! $group)
                <p class="mb-4" style="color: #464555;">هنوز به هیچ تیمی نپیوسته‌اید.</p>
                <a href="{{ route('groups.create') }}" class="mat-btn-primary">
                    + ایجاد تیم جدید
                </a>
            @else
                <!-- Team Info -->
                <div class="mb-4 p-4 rounded-2xl" style="background-color: #F3F3F4;">
                    <h3 class="font-semibold mb-1">تیم شما: {{ $group->name }}</h3>
                    <p class="text-sm" style="color: #464555;">
                        اعضا: {{ $group->members->pluck('name')->join('، ') }}
                    </p>
                </div>

                @if (! $group->project)
                    <a href="{{ route('projects.create') }}" class="mat-btn-primary">
                        📝 ثبت موضوع پروژه
                    </a>
                @else
                    @php $project = $group->project; @endphp

                    <!-- Project Card -->
                    <div class="border-t pt-4 mt-4" style="border-color: #E2E2E2;">
                        <h3 class="text-lg font-bold mb-2">{{ $project->title }}</h3>
                        @if($project->description)
                            <p class="text-sm mb-3" style="color: #464555;">{{ $project->description }}</p>
                        @endif

                        <!-- Status Chip -->
                        <div class="flex items-center gap-3 mb-4">
                            <span class="status-chip {{
                                $project->status === 'pending' ? 'chip-pending' :
                                ($project->status === 'in_progress' || $project->status === 'approved' ? 'chip-inprogress' :
                                ($project->status === 'evaluated' ? 'chip-approved' :
                                ($project->status === 'rejected' ? 'chip-rejected' : 'chip-submitted')))
                            }}">
                                @switch($project->status)
                                    @case('pending') در انتظار بررسی @break
                                    @case('approved') تأیید شده @break
                                    @case('in_progress') در حال انجام @break
                                    @case('submitted') ارسال نهایی @break
                                    @case('evaluated') ارزیابی شده @break
                                    @case('rejected') رد شده @break
                                    @default {{ $project->status }}
                                @endswitch
                            </span>

                            <!-- Progress -->
                            <div class="flex-1">
                                <div class="flex justify-between text-xs mb-1">
                                    <span style="color: #464555;">پیشرفت</span>
                                    <span class="font-bold" style="color: #3525CD;">{{ $project->progress_percent }}٪</span>
                                </div>
                                <div class="mat-progress-bar">
                                    <div class="mat-progress-fill" style="width: {{ $project->progress_percent }}%;"></div>
                                </div>
                            </div>
                        </div>

                        <!-- Action Buttons -->
                        @if (in_array($project->status, ['approved', 'in_progress']))
                            <div class="flex flex-wrap gap-3">
                                <a href="{{ route('tasks.index', $project) }}" class="mat-btn-secondary">
                                    📋 وظایف
                                </a>
                                <a href="{{ route('reports.index', $project) }}" class="mat-btn-outline">
                                    📂 گزارش‌ها و فایل‌ها
                                </a>
                            </div>
                        @endif

                        <!-- Evaluation Result -->
                        @if ($project->evaluation)
                            <div class="mt-4 p-4 rounded-2xl" style="background-color: #ECFDF5;">
                                <p class="font-bold" style="color: #065F46;">
                                    ⭐ نمره نهایی: {{ $project->evaluation->score }} / ۲۰
                                </p>
                                @if ($project->evaluation->comments)
                                    <p class="text-sm mt-1" style="color: #059669;">{{ $project->evaluation->comments }}</p>
                                @endif
                            </div>
                        @endif
                    </div>
                @endif
            @endif
        </div>
    </div>
</x-app-layout>
