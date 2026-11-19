<?php

namespace App\Http\Controllers\Api;

use Illuminate\Http\JsonResponse;

/**
 * مساعد استجابة JSON موحد لكل نقاط API الخاصة بتطبيق Android.
 * success() => { "data": ... }
 * failure() => { "message": ..., "error_code": ..., "errors": {...} }
 */
trait ApiResponse
{
    protected function success(mixed $data = null, string $message = '', int $status = 200): JsonResponse
    {
        $payload = ['data' => $data];

        if ($message !== '') {
            $payload['message'] = $message;
        }

        return new JsonResponse($payload, $status);
    }

    protected function failure(
        string $message,
        string $errorCode = 'ERROR',
        int $status = 422,
        array $errors = [],
    ): JsonResponse {
        return new JsonResponse([
            'message' => $message,
            'error_code' => $errorCode,
            'errors' => $errors,
        ], $status);
    }
}
