<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasOne;

class Group extends Model
{
    use HasFactory;

    protected $fillable = [
        'name',
        'created_by',
    ];

    /*
    |--------------------------------------------------------------------
    | العلاقات
    |--------------------------------------------------------------------
    */

    // الطالب الذي أنشأ الفريق
    public function creator(): BelongsTo
    {
        return $this->belongsTo(User::class, 'created_by');
    }

    // أعضاء الفريق (Many-to-Many عبر group_members)
    public function members(): BelongsToMany
    {
        return $this->belongsToMany(User::class, 'group_members');
    }

    // المشروع الخاص بهذا الفريق (علاقة 1:1)
    public function project(): HasOne
    {
        return $this->hasOne(Project::class);
    }
}
