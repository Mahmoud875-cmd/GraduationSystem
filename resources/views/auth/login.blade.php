<x-guest-layout>
    <!-- Session Status -->
    <x-auth-session-status class="mb-4" :status="session('status')" />

    <h2 class="text-xl font-bold mb-6 text-center" style="color: #1A1C1C;">ورود به سامانه</h2>

    <form method="POST" action="{{ route('login') }}" class="space-y-5">
        @csrf

        <!-- Email Address -->
        <div>
            <label for="email" class="block text-sm font-medium mb-1" style="color: #464555;">ایمیل</label>
            <input id="email" type="email" name="email" value="{{ old('email') }}" required autofocus
                   autocomplete="username" class="mat-input" placeholder="example@email.com">
            <x-input-error :messages="$errors->get('email')" class="mt-1 text-xs text-red-600" />
        </div>

        <!-- Password -->
        <div>
            <label for="password" class="block text-sm font-medium mb-1" style="color: #464555;">رمز عبور</label>
            <input id="password" type="password" name="password" required autocomplete="current-password"
                   class="mat-input" placeholder="••••••••">
            <x-input-error :messages="$errors->get('password')" class="mt-1 text-xs text-red-600" />
        </div>

        <!-- Remember Me -->
        <label class="flex items-center gap-2 cursor-pointer">
            <input type="checkbox" name="remember"
                   class="rounded border-gray-300 text-primary shadow-sm focus:ring-primary">
            <span class="text-sm" style="color: #464555;">مرا به خاطر بسپار</span>
        </label>

        <div class="flex items-center justify-between">
            @if (Route::has('password.request'))
                <a href="{{ route('password.request') }}" class="text-sm underline" style="color: #3525CD;">
                    رمز عبور را فراموش کرده‌اید؟
                </a>
            @endif

            <button type="submit" class="mat-btn-primary">
                ورود
            </button>
        </div>

        <p class="text-center text-sm mt-4" style="color: #464555;">
            حساب کاربری ندارید؟
            <a href="{{ route('register') }}" class="font-semibold underline" style="color: #3525CD;">ثبت‌نام کنید</a>
        </p>
    </form>
</x-guest-layout>
