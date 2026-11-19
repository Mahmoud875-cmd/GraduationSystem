<?php

use App\Http\Controllers\AdminController;
use App\Http\Controllers\DashboardController;
use App\Http\Controllers\EvaluationController;
use App\Http\Controllers\GroupController;
use App\Http\Controllers\ProfileController;
use App\Http\Controllers\ProjectController;
use App\Http\Controllers\ReportController;
use App\Http\Controllers\TaskController;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return view('welcome');
});

Route::get('/dashboard', [DashboardController::class, 'index'])
    ->middleware(['auth', 'verified'])
    ->name('dashboard');

Route::middleware('auth')->group(function () {
    Route::get('/profile', [ProfileController::class, 'edit'])->name('profile.edit');
    Route::patch('/profile', [ProfileController::class, 'update'])->name('profile.update');
    Route::delete('/profile', [ProfileController::class, 'destroy'])->name('profile.destroy');
});

Route::middleware(['auth', 'role:student'])->group(function () {
    Route::get('/groups/create', [GroupController::class, 'create'])->name('groups.create');
    Route::post('/groups', [GroupController::class, 'store'])->name('groups.store');

    Route::get('/projects/create', [ProjectController::class, 'create'])->name('projects.create');
    Route::post('/projects', [ProjectController::class, 'store'])->name('projects.store');
});

Route::middleware(['auth', 'role:professor'])->group(function () {
    Route::get('/professor/projects', [ProjectController::class, 'professorIndex'])->name('projects.professor-index');
    Route::post('/projects/{project}/approve', [ProjectController::class, 'approve'])->name('projects.approve');
    Route::post('/projects/{project}/reject', [ProjectController::class, 'reject'])->name('projects.reject');
});

Route::middleware('auth')->group(function () {
    Route::get('/projects/{project}/tasks', [TaskController::class, 'index'])->name('tasks.index');
    Route::get('/projects/{project}/tasks/create', [TaskController::class, 'create'])->name('tasks.create');
    Route::post('/projects/{project}/tasks', [TaskController::class, 'store'])->name('tasks.store');
    Route::patch('/tasks/{task}/status', [TaskController::class, 'updateStatus'])->name('tasks.update-status');
});

Route::middleware('auth')->group(function () {
    Route::get('/projects/{project}/reports', [ReportController::class, 'index'])->name('reports.index');
    Route::get('/projects/{project}/reports/create', [ReportController::class, 'create'])->name('reports.create');
    Route::post('/projects/{project}/reports', [ReportController::class, 'store'])->name('reports.store');
    Route::get('/reports/{report}/download', [ReportController::class, 'download'])->name('reports.download');
    Route::patch('/reports/{report}/feedback', [ReportController::class, 'feedback'])->name('reports.feedback');
});

Route::middleware(['auth', 'role:professor'])->group(function () {
    Route::get('/projects/{project}/evaluate', [EvaluationController::class, 'create'])->name('evaluations.create');
    Route::post('/projects/{project}/evaluate', [EvaluationController::class, 'store'])->name('evaluations.store');
});

Route::middleware(['auth', 'role:admin'])->group(function () {
    Route::get('/admin/users', [AdminController::class, 'users'])->name('admin.users');
    Route::post('/admin/users', [AdminController::class, 'storeUser'])->name('admin.users.store');
    Route::patch('/admin/users/{user}', [AdminController::class, 'updateUser'])->name('admin.users.update');
    Route::delete('/admin/users/{user}', [AdminController::class, 'destroyUser'])->name('admin.users.destroy');

    Route::get('/admin/projects', [AdminController::class, 'projects'])->name('admin.projects');
    Route::post('/admin/projects', [AdminController::class, 'storeProject'])->name('admin.projects.store');
    Route::patch('/admin/projects/{project}', [AdminController::class, 'updateProject'])->name('admin.projects.update');
    Route::patch('/admin/projects/{project}/extend', [AdminController::class, 'extendDeadline'])->name('admin.projects.extend');
    Route::delete('/admin/projects/{project}', [AdminController::class, 'destroyProject'])->name('admin.projects.destroy');
});

require __DIR__.'/auth.php';