<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Report extends Model
{
    use HasFactory;

    protected $fillable = [
        'project_id',
        'task_id',
        'file_path',
        'feedback',
        'submitted_at',
    ];

    protected function casts(): array
    {
        return [
            'submitted_at' => 'datetime',
        ];
    }

    /*
    |--------------------------------------------------------------------
    | العلاقات
    |--------------------------------------------------------------------
    */

    public function project(): BelongsTo
    {
        return $this->belongsTo(Project::class);
    }

    // قد لا يكون التقرير مرتبطًا بمهمة محددة (task_id قابل للـNull)
    public function task(): BelongsTo
    {
        return $this->belongsTo(Task::class);
    }
}
