<x-app-layout>
    <div class="max-w-2xl mx-auto space-y-4">

        <a href="{{ route('reports.index', $project) }}" class="inline-flex items-center gap-1 text-sm text-primary font-medium hover:underline">
            → العودة إلى التقارير
        </a>

        <div class="mat-card">
            <h1 class="text-[22px] font-bold text-on-surface mb-1">رفع تقرير</h1>
            <p class="text-sm text-on-surface-variant mb-5">{{ $project->title }}</p>

            @if ($errors->any())
                <div class="mb-4 p-4 rounded-xl bg-red-50 text-error text-sm space-y-1">
                    @foreach ($errors->all() as $error)
                        <div>{{ $error }}</div>
                    @endforeach
                </div>
            @endif

            <form method="POST" action="{{ route('reports.store', $project) }}" enctype="multipart/form-data" class="space-y-4">
                @csrf

                <div>
                    <label for="task_id" class="block text-sm font-medium text-on-surface-variant mb-1.5">مرتبط بمهمة (اختياري)</label>
                    <select id="task_id" name="task_id" class="mat-input">
                        <option value="">— تقرير عام عن المشروع —</option>
                        @foreach ($tasks as $task)
                            <option value="{{ $task->id }}">{{ $task->title }}</option>
                        @endforeach
                    </select>
                </div>

                <div>
                    <label for="file" class="block text-sm font-medium text-on-surface-variant mb-1.5">الملف (PDF, Word, أو صورة — حتى 10 ميجابايت)</label>
                    <input id="file" name="file" type="file" required
                        class="mat-input file:ml-3 file:rounded-full file:border-0 file:bg-primary file:px-4 file:py-2 file:text-sm file:font-bold file:text-white" />
                </div>

                <button type="submit" class="mat-btn-primary w-full justify-center mt-2">
                    رفع التقرير
                </button>
            </form>
        </div>
    </div>
</x-app-layout>
