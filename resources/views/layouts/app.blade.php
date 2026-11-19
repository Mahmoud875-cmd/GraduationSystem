<!DOCTYPE html>
<html lang="fa" dir="rtl">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="csrf-token" content="{{ csrf_token() }}">
        <title>{{ config('app.name', 'سامانه مدیریت پروژه‌های پایانی') }}</title>
        <link href="https://cdn.jsdelivr.net/gh/rastikerdar/vazirmatn@v33.003/Vazirmatn-font-face.css" rel="stylesheet" />
        @vite(['resources/css/app.css', 'resources/js/app.js'])
    </head>
    <body style="background-color: #F9F9F9; color: #1A1C1C;">

        <!-- Mobile header -->
        <header class="lg:hidden fixed top-0 right-0 left-0 z-50 bg-white shadow-sm px-4 py-3 flex items-center justify-between">
            <h1 class="text-lg font-bold" style="color: #3525CD;">سامانه پروژه‌های پایانی</h1>
            <button onclick="document.getElementById('sidebar').classList.toggle('-translate-x-full')"
                    class="p-2 rounded-xl" style="background-color: #E2DFFF;">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" style="color: #3525CD;">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
                </svg>
            </button>
        </header>

        <!-- Sidebar (desktop) / Overlay (mobile) -->
        <aside id="sidebar"
               class="fixed top-0 right-0 h-screen w-64 bg-white shadow-lg z-40 p-6 flex flex-col
                      transform transition-transform lg:translate-x-0 -translate-x-full"
               style="right: 0;">

            <!-- Logo -->
            <div class="flex items-center gap-3 mb-8">
                <div class="w-10 h-10 rounded-xl flex items-center justify-center" style="background-color: #3525CD;">
                    <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
                    </svg>
                </div>
                <span class="font-bold text-sm">پروژه‌های پایانی</span>
            </div>

            <!-- User info -->
            <div class="flex items-center gap-3 mb-6 p-3 rounded-2xl" style="background-color: #F3F3F4;">
                <div class="avatar-circle">
                    {{ mb_substr(Auth::user()->name, 0, 1) }}
                </div>
                <div>
                    <p class="text-sm font-semibold">{{ Auth::user()->name }}</p>
                    <p class="text-xs" style="color: #464555;">{{ Auth::user()->role }}</p>
                </div>
            </div>

            <!-- Navigation Links -->
            <nav class="flex-1 space-y-1">
                <a href="{{ route('dashboard') }}"
                   class="nav-link-item {{ request()->routeIs('dashboard') ? 'active' : '' }}">
                    🏠 داشبورد
                </a>

                @if(Auth::user()->role === 'student')
                    <a href="{{ route('groups.create') }}" class="nav-link-item">👥 تیم من</a>
                    <a href="{{ route('projects.create') }}" class="nav-link-item">📝 ثبت موضوع</a>
                @elseif(Auth::user()->role === 'professor')
                    <a href="{{ route('projects.professor-index') }}" class="nav-link-item">📋 مرور مباحث</a>
                @elseif(Auth::user()->role === 'admin')
                    <a href="{{ route('admin.users') }}" class="nav-link-item">👤 مدیریت کاربران</a>
                    <a href="{{ route('admin.projects') }}" class="nav-link-item">📁 همه پروژه‌ها</a>
                @endif

                <a href="{{ route('profile.edit') }}" class="nav-link-item">⚙️ پروفایل</a>

                <!-- Logout -->
                <form method="POST" action="{{ route('logout') }}" class="mt-4">
                    @csrf
                    <button type="submit"
                            class="mat-btn-danger w-full text-sm">
                        خروج
                    </button>
                </form>
            </nav>
        </aside>

        <!-- Main Content Area -->
        <main class="lg:mr-64 pt-16 lg:pt-6 px-4 lg:px-8 pb-8 min-h-screen">
            @isset($header)
                <div class="mb-6">
                    {{ $header }}
                </div>
            @endisset

            {{ $slot }}
        </main>

        <!-- Close sidebar on mobile when clicking outside -->
        <div id="overlay" class="fixed inset-0 bg-black bg-opacity-50 z-30 hidden lg:hidden"
             onclick="document.getElementById('sidebar').classList.add('-translate-x-full')"></div>

        <script>
        // Toggle overlay visibility with sidebar
        const sidebar = document.getElementById('sidebar');
        const overlay = document.getElementById('overlay');
        const observer = new MutationObserver(() => {
            overlay.classList.toggle('hidden', sidebar.classList.contains('-translate-x-full'));
        });
        observer.observe(sidebar, { attributes: true, attributeFilter: ['class'] });
        </script>
    </body>
</html>
