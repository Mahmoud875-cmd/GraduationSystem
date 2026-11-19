<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Evaluation extends Model
{
    use HasFactory;

    protected $fillable = [
        'project_id',
        'professor_id',
        'score',
        'comments',
        'evaluated_at',
    ];

    protected function casts(): array
    {
        return [
            'score' => 'decimal:2',
            'evaluated_at' => 'datetime',
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

    public function professor(): BelongsTo
    {
        return $this->belongsTo(User::class, 'professor_id');
    }
}
