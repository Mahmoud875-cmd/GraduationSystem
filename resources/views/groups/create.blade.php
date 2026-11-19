<x-app-layout>
    <x-slot name="header">
        <h2 class="font-semibold text-xl text-gray-800 leading-tight">
            إنشاء فريق جديد
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

                <form method="POST" action="{{ route('groups.store') }}">
                    @csrf

                    <div>
                        <x-input-label for="name" :value="'اسم الفريق'" />
                        <x-text-input id="name" name="name" class="block mt-1 w-full" :value="old('name')" required autofocus />
                    </div>

                    <div class="mt-4">
                        <x-input-label for="member_emails" :value="'بريد أعضاء الفريق (اختياري)'" />
                        <textarea id="member_emails" name="member_emails" rows="3"
                            class="block mt-1 w-full border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 rounded-md shadow-sm"
                            placeholder="افصل بين كل بريد وآخر بفاصلة، مثال: ahmed@example.com, sara@example.com">{{ old('member_emails') }}</textarea>
                        <p class="text-sm text-gray-500 mt-1">
                            يجب أن يكون زملاؤك مسجّلين مسبقًا في النظام كطلاب.
                        </p>
                    </div>

                    <div class="flex items-center justify-end mt-6">
                        <x-primary-button>
                            إنشاء الفريق
                        </x-primary-button>
                    </div>
                </form>

            </div>
        </div>
    </div>
</x-app-layout>
