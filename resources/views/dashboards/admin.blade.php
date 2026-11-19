<x-app-layout>
    <x-slot name="header">
        <h2 class="text-2xl font-bold" style="color: #1A1C1C;">داشبورد مدیر سیستم</h2>
    </x-slot>

    <div class="space-y-6 max-w-4xl mx-auto">

        <!-- Welcome -->
        <div class="mat-card">
            <p class="text-lg font-semibold mb-6">خوش آمدید {{ $user->name }} 👋</p>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <a href="{{ route('admin.users') }}" class="mat-card flex items-center gap-4 no-underline hover:shadow-md transition-shadow">
                    <div class="stat-icon" style="background-color: #3525CD;">👤</div>
                    <div>
                        <p class="font-bold text-sm" style="color: #1A1C1C;">مدیریت کاربران</p>
                        <p class="text-xs" style="color: #464555;">مشاهده، ویرایش، افزودن و حذف کاربران</p>
                    </div>
                </a>

                <a href="{{ route('admin.projects') }}" class="mat-card flex items-center gap-4 no-underline hover:shadow-md transition-shadow">
                    <div class="stat-icon" style="background-color: #059669;">📁</div>
                    <div>
                        <p class="font-bold text-sm" style="color: #1A1C1C;">همه پروژه‌ها</p>
                        <p class="text-xs" style="color: #464555;">مشاهده، ویرایش، تمدید مهلت و حذف</p>
                    </div>
                </a>
            </div>
        </div>
    </div>
</x-app-layout>
