<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\Rules;

class AuthController extends Controller
{
    use ApiResponse;

    /**
     * POST /api/register — تسجيل طالب أو أستاذ فقط (الدور admin ممنوع دائمًا)
     */
    public function register(Request $request): JsonResponse
    {
        $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|string|lowercase|email|max:255|unique:'.User::class,
            'password' => ['required', 'confirmed', Rules\Password::defaults()],
            'role' => ['required', 'in:student,professor'],
        ]);

        $user = User::create([
            'name' => $request->name,
            'email' => $request->email,
            'password' => Hash::make($request->password),
            'role' => $request->role,
        ]);

        $token = $user->createToken('android-app')->plainTextToken;

        return $this->success([
            'token' => $token,
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'email' => $user->email,
                'role' => $user->role,
            ],
        ], status: 201);
    }

    /**
     * POST /api/login — إرجاع رمز Sanctum مع بيانات المستخدم
     */
    public function login(Request $request): JsonResponse
    {
        $credentials = $request->validate([
            'email' => 'required|email',
            'password' => 'required|string',
        ]);

        $user = User::where('email', $credentials['email'])->first();

        if (! $user || ! Hash::check($credentials['password'], $user->password)) {
            return $this->failure(
                message: 'ایمیل یا رمز عبور نادرست است.',
                errorCode: 'INVALID_CREDENTIALS',
                status: 422,
            );
        }

        // رمز واحد لكل مستخدم في مرحلة التطوير — إلغاء الرموز القديمة
        $user->tokens()->delete();

        $token = $user->createToken('android-app')->plainTextToken;

        return $this->success([
            'token' => $token,
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'email' => $user->email,
                'role' => $user->role,
            ],
        ]);
    }

    /**
     * POST /api/logout — حذف الرمز الحالي فقط
     */
    public function logout(Request $request): JsonResponse
    {
        $request->user()->currentAccessToken()->delete();

        return $this->success(['message' => 'خروج انجام شد.']);
    }

    /**
     * GET /api/me — بيانات المستخدم الحالي
     */
    public function me(Request $request): JsonResponse
    {
        return $this->success([
            'id' => $request->user()->id,
            'name' => $request->user()->name,
            'email' => $request->user()->email,
            'role' => $request->user()->role,
        ]);
    }
}
