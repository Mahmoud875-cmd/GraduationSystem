<?php

namespace App\Http\Controllers;

use App\Models\Project;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class EvaluationController extends Controller
{
    /**
     * نموذج التقييم النهائي (الأستاذ فقط، ومرة واحدة لكل مشروع)
     */
    public function create(Request $request, Project $project): View|RedirectResponse
    {
        abort_unless($project->professor_id === $request->user()->id, 403);

        if ($project->evaluation) {
            return redirect()->route('dashboard')
                ->with('error', 'این پروژه قبلا ارزیابی شده است');
        }

        return view('evaluations.create', compact('project'));
    }

    public function store(Request $request, Project $project): RedirectResponse
    {
        abort_unless($project->professor_id === $request->user()->id, 403);

        if ($project->evaluation) {
            return redirect()->route('dashboard')
                ->with('error', 'این پروژه قبلا ارزیابی شده است');
        }

        $validated = $request->validate([
            // درجة من 0 إلى 20 (عدّلها حسب نظام الدرجات في جامعتك)
            'score' => 'required|numeric|min:0|max:20',
            'comments' => 'nullable|string',
        ]);

        $project->evaluation()->create([
            'professor_id' => $request->user()->id,
            'score' => $validated['score'],
            'comments' => $validated['comments'] ?? null,
            'evaluated_at' => now(),
        ]);

        $project->update(['status' => 'evaluated']);

        return redirect()->route('dashboard')
            ->with('success', 'ارزیابی نهایی باموفقیت ذخیره شد');
    }
}
