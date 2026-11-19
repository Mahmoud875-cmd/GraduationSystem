<x-guest-layout>
    <h2 class="text-xl font-bold mb-6 text-center" style="color: #1A1C1C;">ایجاد حساب کاربری</h2>

    <form method="POST" action="{{ route('register') }}" class="space-y-4">
        @csrf

        <!-- Name -->
        <div>
            <label for="name" class="block text-sm font-medium mb-1" style="color: #464555;">نام و نام خانوادگی</label>
            <input id="name" type="text" name="name" value="{{ old('name') }}" required autofocus
                   autocomplete="name" class="mat-input" placeholder="نام کامل شما">
            <x-input-error :messages="$errors->get('name')" class="mt-1 text-xs text-red-600" />
        </div>

        <!-- Email Address -->
        <div>
            <label for="email" class="block text-sm font-medium mb-1" style="color: #464555;">ایمیل</label>
            <input id="email" type="email" name="email" value="{{ old('email') }}" required
                   autocomplete="username" class="mat-input" placeholder="example@email.com">
            <x-input-error :messages="$errors->get('email')" class="mt-1 text-xs text-red-600" />
        </div>

        <!-- Role -->
        <div>
            <label for="role" class="block text-sm font-medium mb-1" style="color: #464555;">نوع حساب</label>
            <select id="role" name="role" required class="mat-input">
                <option value="">-- انتخاب کنید --</option>
                <option value="student" @selected(old('role') == 'student')>دانشجو</option>
                <option value="professor" @selected(old('role') == 'professor')>استاد راهنما</option>
            </select>
            <x-input-error :messages="$errors->get('role')" class="mt-1 text-xs text-red-600" />
        </div>

        <!-- Password -->
        <div>
            <label for="password" class="block text-sm font-medium mb-1" style="color: #464555;">رمز عبور</label>
            <input id="password" type="password" name="password" required autocomplete="new-password"
                   class="mat-input" placeholder="••••••••">
            <x-input-error :messages="$errors->get('password')" class="mt-1 text-xs text-red-600" />
        </div>

        <!-- Confirm Password -->
        <div>
            <label for="password_confirmation" class="block text-sm font-medium mb-1" style="color: #464555;">تکرار رمز عبور</label>
            <input id="password_confirmation" type="password" name="password_confirmation" required
                   autocomplete="new-password" class="mat-input" placeholder="••••••••">
            <x-input-error :messages="$errors->get('password_confirmation')" class="mt-1 text-xs text-red-600" />
        </div>

        <button type="submit" class="mat-btn-primary w-full mt-2">
            ثبت‌نام
        </button>

        <p class="text-center text-sm" style="color: #464555;">
            قبلاً ثبت‌نام کرده‌اید؟
            <a href="{{ route('login') }}" class="font-semibold underline" style="color: #3525CD;">وارد شوید</a>
        </p>
    </form>
</x-guest-layout>
