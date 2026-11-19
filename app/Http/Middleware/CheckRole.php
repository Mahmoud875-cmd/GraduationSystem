<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class CheckRole
{
    /**
     * Handle an incoming request.
     *
     * الاستخدام في الـRoutes:
     *   Route::get('/admin', ...)->middleware('role:admin');
     *   Route::get('/professor', ...)->middleware('role:professor,admin');
     *
     * @param  \Closure(\Illuminate\Http\Request): (\Symfony\Component\HttpFoundation\Response)  $next
     */
    public function handle(Request $request, Closure $next, string ...$roles): Response
    {
        if (! $request->user() || ! in_array($request->user()->role, $roles, true)) {
            abort(403, 'ليست لديك صلاحية الوصول إلى هذه الصفحة.');
        }

        return $next($request);
    }
}
