<?php

use App\Http\Controllers\Api\AdminController;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\DashboardController;
use App\Http\Controllers\Api\EvaluationController;
use App\Http\Controllers\Api\GroupController;
use App\Http\Controllers\Api\ProjectController;
use App\Http\Controllers\Api\ReportController;
use App\Http\Controllers\Api\TaskController;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API Routes — طبقة JSON لتطبيق Android (Sanctum Token)
|--------------------------------------------------------------------------
| نفس قواعد تحكمات الويب الحالية، لكن بردود JSON.
*/

Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/me', [AuthController::class, 'me']);
    Route::get('/dashboard', [DashboardController::class, 'index']);

    // الطالب: إنشاء فريق + جلب قائمة الأساتذة
    Route::middleware('role:student')->group(function () {
        Route::post('/groups', [GroupController::class, 'store']);
    });
    Route::get('/professors', [ProjectController::class, 'professors']);

    // الأستاذ: مراجعة المواضيع
    Route::middleware('role:professor')->group(function () {
        Route::get('/professor/projects', [ProjectController::class, 'professorIndex']);
        Route::post('/projects/{project}/approve', [ProjectController::class, 'approve']);
        Route::post('/projects/{project}/reject', [ProjectController::class, 'reject']);
        Route::get('/projects/{project}/evaluate', [EvaluationController::class, 'show']);
        Route::post('/projects/{project}/evaluate', [EvaluationController::class, 'store']);
    });

    // الطالب: تسجيل موضوع
    Route::middleware('role:student')->group(function () {
        Route::post('/projects', [ProjectController::class, 'store']);
    });

    // مهام وتقارير: الوصول يتحقق داخل كل التحكم (أستاذ مشرف أو أعضاء الفريق)
    Route::get('/projects/{project}/tasks', [TaskController::class, 'index']);
    Route::post('/projects/{project}/tasks', [TaskController::class, 'store']);
    Route::patch('/tasks/{task}/status', [TaskController::class, 'updateStatus']);

    Route::get('/projects/{project}/reports', [ReportController::class, 'index']);
    Route::post('/projects/{project}/reports', [ReportController::class, 'store']);
    Route::get('/reports/{report}/download', [ReportController::class, 'download'])->name('api.reports.download');
    Route::patch('/reports/{report}/feedback', [ReportController::class, 'feedback']);

    // الأدمن
    Route::middleware('role:admin')->group(function () {
        Route::get('/admin/users', [AdminController::class, 'users']);
        Route::post('/admin/users', [AdminController::class, 'storeUser']);
        Route::patch('/admin/users/{user}', [AdminController::class, 'updateUser']);
        Route::delete('/admin/users/{user}', [AdminController::class, 'destroyUser']);

        Route::get('/admin/projects', [AdminController::class, 'projects']);
        Route::post('/admin/projects', [AdminController::class, 'storeProject']);
        Route::patch('/admin/projects/{project}', [AdminController::class, 'updateProject']);
        Route::patch('/admin/projects/{project}/extend', [AdminController::class, 'extendDeadline']);
        Route::delete('/admin/projects/{project}', [AdminController::class, 'destroyProject']);
    });
});
