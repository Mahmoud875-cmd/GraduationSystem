<x-app-layout>
    <div class="space-y-4">

        @if (session('success'))
            <div class="mat-card !bg-emerald-50 border border-emerald-600" style="color:#065F46">{{ session('success') }}</div>
        @endif
        @if (session('error'))
            <div class="mat-card !bg-red-50 text-error">{{ session('error') }}</div>
        @endif

        <!-- Header -->
        <div class="mat-card flex items-center justify-between gap-3 flex-wrap">
            <div>
                <h1 class="text-[22px] font-bold text-on-surface">إدارة المشاريع</h1>
                <p class="text-sm text-on-surface-variant mt-1">إجمالي: {{ $projects->count() }} مشروع</p>
            </div>
            <button type="button" class="mat-btn-primary" onclick="openProjectModal()">+ إضافة مشروع</button>
        </div>

        @if ($errors->any())
            <div class="mat-card !bg-red-50 text-error text-sm space-y-1">
                @foreach ($errors->all() as $error)
                    <div>{{ $error }}</div>
                @endforeach
            </div>
        @endif

        <!-- Projects list -->
        @forelse ($projects as $project)
            <div class="mat-card">
                <div class="flex items-start justify-between gap-4 flex-wrap">
                    <div class="flex-1 min-w-[240px]">
                        <div class="flex items-center gap-3 flex-wrap">
                            <h3 class="font-bold text-lg text-on-surface">{{ $project->title }}</h3>
                            <span @class([
                                'status-chip',
                                'chip-pending' => $project->status === 'pending',
                                'chip-approved' => in_array($project->status, ['approved', 'evaluated']),
                                'chip-inprogress' => $project->status === 'in_progress',
                                'chip-submitted' => $project->status === 'submitted',
                                'chip-rejected' => $project->status === 'rejected',
                            ])>
                                {{ ['pending' => 'بانتظار الموافقة', 'approved' => 'معتمد', 'in_progress' => 'قيد التنفيذ', 'submitted' => 'مُقدَّم', 'evaluated' => 'مُقيَّم', 'rejected' => 'مرفوض'][$project->status] ?? $project->status }}
                            </span>
                        </div>
                        <p class="text-xs text-on-surface-variant mt-2">
                            👥 {{ $project->group->name ?? '—' }} · 🧑‍🏫 المشرف: {{ $project->professor->name ?? '—' }}
                        </p>
                        <p class="text-xs text-on-surface-variant mt-1">
                            📅 {{ $project->start_date?->format('Y-m-d') ?? '—' }} → {{ $project->end_date?->format('Y-m-d') ?? '—' }}
                            · ⭐ الدرجة: {{ $project->evaluation->score ?? '—' }}
                        </p>
                        <div class="mt-3 max-w-sm">
                            <div class="flex justify-between text-xs text-on-surface-variant mb-1">
                                <span>التقدم</span><span>{{ $project->progress_percent }}%</span>
                            </div>
                            <div class="mat-progress-bar"><div class="mat-progress-fill" style="width: {{ $project->progress_percent }}%;"></div></div>
                        </div>
                    </div>

                    <div class="flex flex-col gap-2 shrink-0 w-40">
                        <button type="button" class="mat-btn-outline !py-2 text-sm"
                            data-id="{{ $project->id }}" data-title="{{ $project->title }}" data-description="{{ $project->description }}"
                            data-status="{{ $project->status }}" data-professor="{{ $project->professor_id }}"
                            data-start="{{ $project->start_date?->format('Y-m-d') }}" data-end="{{ $project->end_date?->format('Y-m-d') }}"
                            onclick="openEditProjectModal(this)">تعديل</button>
                        <button type="button" class="mat-btn-secondary !py-2 text-sm"
                            data-id="{{ $project->id }}" data-end="{{ $project->end_date?->format('Y-m-d') }}"
                            data-start="{{ $project->start_date?->format('Y-m-d') }}"
                            onclick="openExtendModal(this)">تمديد الموعد</button>
                        <form method="POST" action="{{ route('admin.projects.destroy', $project) }}"
                            onsubmit="return confirm('هل أنت متأكد من حذف المشروع؟')">
                            @csrf
                            @method('DELETE')
                            <button type="submit" class="mat-btn-danger w-full !py-2 text-sm">حذف</button>
                        </form>
                    </div>
                </div>
            </div>
        @empty
            <div class="mat-card text-center py-12">
                <p class="material-symbols-outlined text-5xl text-outline mb-2">folder_open</p>
                <p class="text-on-surface-variant">لا توجد مشاريع بعد.</p>
            </div>
        @endforelse
    </div>

    <!-- Add/Edit project modal -->
    <div id="projectModal" class="fixed inset-0 z-50 hidden items-center justify-center p-4 bg-black/50">
        <div class="mat-card w-full max-w-md !p-6 max-h-[90vh] overflow-y-auto">
            <h2 class="text-lg font-bold text-on-surface mb-4" id="projectModalTitle">إضافة مشروع</h2>
            <form method="POST" id="projectModalForm" data-add-url="{{ route('admin.projects.store') }}" data-edit-base="{{ url('/admin/projects') }}" class="space-y-4">
                @csrf
                <input type="hidden" name="_method" id="pf_method" value="POST" />
                <div>
                    <label class="block text-sm font-medium text-on-surface-variant mb-1.5">عنوان المشروع</label>
                    <input type="text" name="title" id="pf_title" class="mat-input" required />
                </div>
                <div>
                    <label class="block text-sm font-medium text-on-surface-variant mb-1.5">الوصف</label>
                    <textarea name="description" id="pf_desc" rows="3" class="mat-input resize-none"></textarea>
                </div>
                <div>
                    <label class="block text-sm font-medium text-on-surface-variant mb-1.5">الحالة</label>
                    <select name="status" id="pf_status" class="mat-input" required>
                        <option value="pending">بانتظار الموافقة</option>
                        <option value="approved">معتمد</option>
                        <option value="in_progress">قيد التنفيذ</option>
                        <option value="submitted">مُقدَّم</option>
                        <option value="evaluated">مُقيَّم</option>
                        <option value="rejected">مرفوض</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-on-surface-variant mb-1.5">الأستاذ المشرف</label>
                    <select name="professor_id" id="pf_prof" class="mat-input">
                        <option value="">— بدون —</option>
                        @foreach ($professors as $prof)
                            <option value="{{ $prof->id }}">{{ $prof->name }}</option>
                        @endforeach
                    </select>
                </div>
                @if ($projects->isEmpty())
                    <div>
                        <label class="block text-sm font-medium text-on-surface-variant mb-1.5">الفريق (اختياري)</label>
                        <select name="group_id" class="mat-input">
                            <option value="">— إنشاء فريق تلقائيًا —</option>
                            @foreach ($groups as $group)
                                <option value="{{ $group->id }}">{{ $group->name }}</option>
                            @endforeach
                        </select>
                    </div>
                @endif
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-sm font-medium text-on-surface-variant mb-1.5">تاريخ البداية</label>
                        <input type="date" name="start_date" id="pf_start" class="mat-input" />
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-on-surface-variant mb-1.5">تاريخ النهاية</label>
                        <input type="date" name="end_date" id="pf_end" class="mat-input" />
                    </div>
                </div>
                <div class="flex gap-2 pt-2">
                    <button type="button" class="mat-btn-secondary flex-1" onclick="closeProjectModal()">إلغاء</button>
                    <button type="submit" class="mat-btn-primary flex-1">حفظ</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Extend deadline modal -->
    <div id="extendModal" class="fixed inset-0 z-50 hidden items-center justify-center p-4 bg-black/50">
        <div class="mat-card w-full max-w-md !p-6">
            <h2 class="text-lg font-bold text-on-surface mb-4">تمديد الموعد النهائي</h2>
            <form method="POST" id="extendModalForm" data-edit-base="{{ url('/admin/projects') }}" class="space-y-4">
                @csrf
                <input type="hidden" name="_method" value="PATCH" />
                <div>
                    <label class="block text-sm font-medium text-on-surface-variant mb-1.5">التاريخ الجديد للنهاية</label>
                    <input type="date" name="end_date" id="ef_end" class="mat-input" required />
                </div>
                <div class="flex gap-2 pt-2">
                    <button type="button" class="mat-btn-secondary flex-1" onclick="closeExtendModal()">إلغاء</button>
                    <button type="submit" class="mat-btn-primary flex-1">تمديد</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function pm() { return document.getElementById('projectModalForm'); }
        function openProjectModal() {
            document.getElementById('projectModalTitle').textContent = 'إضافة مشروع';
            pm().action = pm().dataset.addUrl;
            document.getElementById('pf_method').value = 'POST';
            ['pf_title','pf_desc','pf_start','pf_end'].forEach(function (i) { document.getElementById(i).value = ''; });
            document.getElementById('pf_status').value = 'pending';
            document.getElementById('pf_prof').value = '';
            toggleModal('projectModal', true);
        }
        function openEditProjectModal(btn) {
            const d = btn.dataset;
            document.getElementById('projectModalTitle').textContent = 'تعديل مشروع';
            pm().action = pm().dataset.editBase + '/' + d.id;
            document.getElementById('pf_method').value = 'PATCH';
            document.getElementById('pf_title').value = d.title || '';
            document.getElementById('pf_desc').value = d.description || '';
            document.getElementById('pf_status').value = d.status || 'pending';
            document.getElementById('pf_prof').value = d.professor || '';
            document.getElementById('pf_start').value = d.start || '';
            document.getElementById('pf_end').value = d.end || '';
            toggleModal('projectModal', true);
        }
        function em() { return document.getElementById('extendModalForm'); }
        function openExtendModal(btn) {
            const d = btn.dataset;
            em().action = em().dataset.editBase + '/' + d.id + '/extend';
            document.getElementById('ef_end').value = d.end || d.start || '';
            toggleModal('extendModal', true);
        }
        function closeProjectModal() { toggleModal('projectModal', false); }
        function closeExtendModal() { toggleModal('extendModal', false); }
        function toggleModal(id, show) {
            const m = document.getElementById(id);
            m.classList.toggle('hidden', !show);
            m.classList.toggle('flex', show);
        }
        ['projectModal','extendModal'].forEach(function (id) {
            document.getElementById(id).addEventListener('click', function (e) { if (e.target === this) toggleModal(id, false); });
        });
    </script>
</x-app-layout>
