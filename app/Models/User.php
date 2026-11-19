<?php

namespace App\Models;

// use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    /** @use HasFactory<\Database\Factories\UserFactory> */
    use HasApiTokens, HasFactory, Notifiable;

    protected $fillable = [
        'name',
        'email',
        'password',
        'role', // student | professor | admin
    ];

    protected $hidden = [
        'password',
        'remember_token',
    ];

    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
        ];
    }

    /*
    |--------------------------------------------------------------------
    | Helper methods لمعرفة نوع المستخدم بسهولة
    |--------------------------------------------------------------------
    */
    public function isStudent(): bool
    {
        return $this->role === 'student';
    }

    public function isProfessor(): bool
    {
        return $this->role === 'professor';
    }

    public function isAdmin(): bool
    {
        return $this->role === 'admin';
    }

    /*
    |--------------------------------------------------------------------
    | العلاقات
    |--------------------------------------------------------------------
    */

    // الفرق التي أنشأها هذا المستخدم (كطالب عادة)
    public function createdGroups(): HasMany
    {
        return $this->hasMany(Group::class, 'created_by');
    }

    // الفرق التي هو عضو فيها (Many-to-Many عبر group_members)
    public function groups(): BelongsToMany
    {
        return $this->belongsToMany(Group::class, 'group_members');
    }

    // المشاريع التي يشرف عليها (كأستاذ)
    public function supervisedProjects(): HasMany
    {
        return $this->hasMany(Project::class, 'professor_id');
    }

    // المهام المسندة إليه (كطالب)
    public function assignedTasks(): HasMany
    {
        return $this->hasMany(Task::class, 'assigned_to');
    }

    // التقييمات التي قام بكتابتها (كأستاذ)
    public function evaluationsGiven(): HasMany
    {
        return $this->hasMany(Evaluation::class, 'professor_id');
    }
}
