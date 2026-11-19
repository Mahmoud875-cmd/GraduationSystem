<x-app-layout>
    <x-slot name="header">
        <h2 class="font-semibold text-xl text-gray-800 leading-tight">
            تسجيل موضوع المشروع
        </h2>
    </x-slot>

    <div class="py-12">
        <div class="max-w-2xl mx-auto sm:px-6 lg:px-8">
            <div class="bg-white overflow-hidden shadow-sm sm:rounded-lg p-6">

                @if ($errors->any())
                    <div class="mb-4 p-4 bg-red-50 text-red-700 rounded-md">
                        <ul class="list-disc list-inside">
                            @foreach ($errors->all() as $error)
                                <li>{{ $error }}</li>
                            @endforeach
                        </ul>
                    </div>
                @endif

                <form method="POST" action="{{ route('projects.store') }}">
                    @csrf

                    <div>
                        <x-input-label for="title" :value="'عنوان المشروع'" />
                        <x-text-input id="title" name="title" class="block mt-1 w-full" :value="old('title')" required autofocus />
                    </div>

                    <div class="mt-4">
                        <x-input-label for="description" :value="'وصف مختصر للمشروع'" />
                        <textarea id="description" name="description" rows="4"
                            class="block mt-1 w-full border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 rounded-md shadow-sm">{{ old('description') }}</textarea>
                    </div>

                    <div class="mt-4">
                        <x-input-label for="professor_id" :value="'الأستاذ المشرف المطلوب'" />
                        <select id="professor_id" name="professor_id"
                            class="block mt-1 w-full border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 rounded-md shadow-sm"
                            required>
                            <option value="">-- اختر أستاذًا --</option>
                            @foreach ($professors as $professor)
                                <option value="{{ $professor->id }}" @selected(old('professor_id') == $professor->id)>
                                    {{ $professor->name }}
                                </option>
                            @endforeach
                        </select>
                    </div>

                    <div class="flex items-center justify-end mt-6">
                        <x-primary-button>
                            إرسال الموضوع
                        </x-primary-button>
                    </div>
                </form>

            </div>
        </div>
    </div>
</x-app-layout>
