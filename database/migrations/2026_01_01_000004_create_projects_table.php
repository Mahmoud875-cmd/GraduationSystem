<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('projects', function (Blueprint $table) {
            $table->id();

            $table->foreignId('group_id')
                  ->unique() // علاقة 1:1 بين الفريق والمشروع
                  ->constrained('groups')
                  ->cascadeOnDelete();

            $table->foreignId('professor_id')
                  ->nullable() // قد لا يُعيَّن مشرف فور إنشاء المشروع
                  ->constrained('users')
                  ->nullOnDelete();

            $table->string('title');
            $table->text('description')->nullable();

            // حالة المشروع في دورة حياته
            $table->enum('status', [
                'pending',      // بانتظار موافقة الأستاذ
                'approved',     // تم اعتماد الموضوع
                'rejected',     // تم رفض الموضوع
                'in_progress',  // قيد التنفيذ
                'submitted',    // تم التسليم النهائي
                'evaluated',    // تم التقييم
            ])->default('pending');

            $table->date('start_date')->nullable();
            $table->date('end_date')->nullable();

            // حقل محسوب، يُحدَّث تلقائيًا عبر Observer عند تغيّر حالة أي مهمة
            $table->unsignedTinyInteger('progress_percent')->default(0);

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('projects');
    }
};
