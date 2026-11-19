<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('evaluations', function (Blueprint $table) {
            $table->id();

            $table->foreignId('project_id')
                  ->unique() // تقييم نهائي واحد لكل مشروع
                  ->constrained('projects')
                  ->cascadeOnDelete();

            $table->foreignId('professor_id')
                  ->constrained('users')
                  ->cascadeOnDelete();

            $table->decimal('score', 4, 2); // مثال: 18.50 من 20
            $table->text('comments')->nullable();
            $table->timestamp('evaluated_at')->useCurrent();

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('evaluations');
    }
};
