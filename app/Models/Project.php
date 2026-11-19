<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\HasOne;

class Project extends Model
{
    use HasFactory;

    protected $fillable = [
        'group_id',
        'professor_id',
        'title',
        'description',
        'status',
        'start_date',
        'end_date',
        'progress_percent',
    ];

    protected function casts(): array
    {
        return [
            'start_date' => 'date',
            'end_date' => 'date',
        ];
    }

    /*
    |--------------------------------------------------------------------
    | العلاقات
    |--------------------------------------------------------------------
    */

    public function group(): BelongsTo
    {
        return $this->belongsTo(Group::class);
    }

    public function professor(): BelongsTo
    {
        return $this->belongsTo(User::class, 'professor_id');
    }

    public function tasks(): HasMany
    {
        return $this->hasMany(Task::class);
    }

    public function reports(): HasMany
    {
        return $this->hasMany(Report::class);
    }

    public function evaluation(): HasOne
    {
        return $this->hasOne(Evaluation::class);
    }

    /*
    |--------------------------------------------------------------------
    | Helper: إعادة حساب نسبة التقدم تلقائيًا بناءً على حالة المهام
    |--------------------------------------------------------------------
    | استدعِ هذه الدالة بعد أي تحديث لحالة مهمة (مثلاً داخل TaskController)
    | لتحديث progress_percent تلقائيًا دون حسابه يدويًا في كل مكان.
    */
    public function recalculateProgress(): void
    {
        // A finalized (evaluated) project is always considered 100% complete.
        if ($this->status === 'evaluated') {
            $this->update(['progress_percent' => 100]);

            return;
        }

        $total = $this->tasks()->count();

        if ($total === 0) {
            $this->update(['progress_percent' => 0]);

            return;
        }

        $done = $this->tasks()->where('status', 'done')->count();

        $this->update([
            'progress_percent' => (int) round(($done / $total) * 100),
        ]);
    }
}
