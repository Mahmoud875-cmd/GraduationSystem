<x-app-layout>
    <div class="max-w-2xl mx-auto space-y-4">

        <!-- Project + progress -->
        <div class="mat-card">
            <h1 class="text-[22px] font-bold text-on-surface mb-1">التقييم النهائي</h1>
            <p class="text-sm text-on-surface-variant mb-4">{{ $project->title }}</p>
            <div>
                <div class="flex justify-between text-xs text-on-surface-variant mb-1">
                    <span class="font-medium">نسبة تقدم المهام</span>
                    <span>{{ $project->progress_percent }}%</span>
                </div>
                <div class="mat-progress-bar">
                    <div class="mat-progress-fill" style="width: {{ $project->progress_percent }}%;"></div>
                </div>
            </div>
        </div>

        <!-- Evaluation form -->
        <div class="mat-card">
            @if ($errors->any())
                <div class="mb-4 p-4 rounded-xl bg-red-50 text-error text-sm space-y-1">
                    @foreach ($errors->all() as $error)
                        <div>{{ $error }}</div>
                    @endforeach
                </div>
            @endif

            <form method="POST" action="{{ route('evaluations.store', $project) }}" class="space-y-4">
                @csrf

                <div>
                    <label for="score" class="block text-sm font-medium text-on-surface-variant mb-1.5">الدرجة النهائية (من 20)</label>
                    <input id="score" name="score" type="number" step="0.5" min="0" max="20"
                        class="mat-input" value="{{ old('score') }}" required autofocus />
                </div>

                <div>
                    <label for="comments" class="block text-sm font-medium text-on-surface-variant mb-1.5">ملاحظات ختامية</label>
                    <textarea id="comments" name="comments" rows="4" class="mat-input resize-none">{{ old('comments') }}</textarea>
                </div>

                <button type="submit" class="mat-btn-primary w-full justify-center mt-2" style="background:#059669;">
                    حفظ التقييم النهائي
                </button>
            </form>
        </div>
    </div>
</x-app-layout>
