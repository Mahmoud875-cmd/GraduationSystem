<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\View\View;

class DashboardController extends Controller
{
    /**
     * يعرض لوحة التحكم المناسبة حسب دور المستخدم المسجّل دخوله.
     * لا حاجة لثلاث Routes منفصلة الآن — كل شيء يمر من هنا مؤقتًا،
     * ويمكن تقسيمها لاحقًا إلى Controllers منفصلة (StudentDashboardController...)
     * عندما يكبر منطق كل لوحة.
     */
    public function index(Request $request): View
    {
        $user = $request->user();

        return match ($user->role) {
            'student' => view('dashboards.student', ['user' => $user]),
            'professor' => view('dashboards.professor', ['user' => $user]),
            'admin' => view('dashboards.admin', ['user' => $user]),
            default => abort(403, 'نوبت کاربر نامشخص'),
        };
    }
}
