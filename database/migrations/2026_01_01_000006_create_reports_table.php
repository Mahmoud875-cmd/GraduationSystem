<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('reports', function (Blueprint $table) {
            $table->id();

            $table->foreignId('project_id')
                  ->constrained('projects')
                  ->cascadeOnDelete();

            $table->foreignId('task_id')
                  ->nullable() // بعض التقارير عامة وغير مرتبطة بمهمة محددة
                  ->constrained('tasks')
                  ->nullOnDelete();

            $table->string('file_path');
            $table->text('feedback')->nullable(); // ملاحظة الأستاذ على هذا التقرير

            $table->timestamp('submitted_at')->useCurrent();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('reports');
    }
};
