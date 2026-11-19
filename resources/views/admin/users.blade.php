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
                <h1 class="text-[22px] font-bold text-on-surface">إدارة المستخدمين</h1>
                <p class="text-sm text-on-surface-variant mt-1">إجمالي: {{ $users->count() }} مستخدم</p>
            </div>
            <button type="button" class="mat-btn-primary" onclick="openUserModal()">+ إضافة مستخدم</button>
        </div>

        @if ($errors->any())
            <div class="mat-card !bg-red-50 text-error text-sm space-y-1">
                @foreach ($errors->all() as $error)
                    <div>{{ $error }}</div>
                @endforeach
            </div>
        @endif

        <!-- Users grid -->
        <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
            @foreach ($users as $user)
                <div class="mat-card !p-4 flex flex-col gap-3">
                    <div class="flex items-center gap-3">
                        <div class="avatar-circle shrink-0" style="background:#E2E2E2;color:#464555;">
                            {{ mb_substr($user->name, 0, 1) }}
                        </div>
                        <div class="min-w-0 flex-1">
                            <p class="font-bold text-on-surface truncate">{{ $user->name }}</p>
                            <p class="text-xs text-on-surface-variant truncate">{{ $user->email }}</p>
                        </div>
                    </div>

                    <span @class([
                        'status-chip self-start',
                        'chip-inprogress' => $user->role === 'student',
                        'chip-pending' => $user->role === 'professor',
                        'chip-approved' => $user->role === 'admin',
                    ])>
                        {{ ['student' => 'طالب', 'professor' => 'أستاذ', 'admin' => 'مدير'][$user->role] ?? $user->role }}
                    </span>

                    <div class="flex items-center gap-2 pt-1 border-t border-outline-variant">
                        <button type="button" class="mat-btn-outline flex-1 !py-2 text-sm"
                            data-name="{{ $user->name }}" data-email="{{ $user->email }}" data-role="{{ $user->role }}" data-id="{{ $user->id }}"
                            onclick="openEditUserModal(this)">تعديل</button>
                        <form method="POST" action="{{ route('admin.users.destroy', $user) }}"
                            onsubmit="return confirm('هل أنت متأكد من حذف المستخدم؟')" class="flex-1">
                            @csrf
                            @method('DELETE')
                            <button type="submit" class="mat-btn-danger w-full !py-2 text-sm">حذف</button>
                        </form>
                    </div>
                </div>
            @endforeach
        </div>
    </div>

    <!-- Add user modal -->
    <div id="userModal" class="fixed inset-0 z-50 hidden items-center justify-center p-4 bg-black/50">
        <div class="mat-card w-full max-w-md !p-6">
            <h2 class="text-lg font-bold text-on-surface mb-4" id="userModalTitle">إضافة مستخدم</h2>
            <form method="POST" id="userModalForm" data-add-url="{{ route('admin.users.store') }}" data-edit-base="{{ url('/admin/users') }}" class="space-y-4">
                @csrf
                <input type="hidden" name="_method" id="userModalMethod" value="POST" />
                <div>
                    <label class="block text-sm font-medium text-on-surface-variant mb-1.5">الاسم</label>
                    <input type="text" name="name" id="uf_name" class="mat-input" required />
                </div>
                <div>
                    <label class="block text-sm font-medium text-on-surface-variant mb-1.5">البريد الإلكتروني</label>
                    <input type="email" name="email" id="uf_email" class="mat-input" required />
                </div>
                <div>
                    <label class="block text-sm font-medium text-on-surface-variant mb-1.5">الدور</label>
                    <select name="role" id="uf_role" class="mat-input" required>
                        <option value="student">طالب</option>
                        <option value="professor">أستاذ</option>
                        <option value="admin">مدير</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-on-surface-variant mb-1.5">كلمة المرور <span id="uf_pass_hint" class="text-xs text-outline">(8 أحرف على الأقل)</span></label>
                    <input type="password" name="password" id="uf_password" class="mat-input" minlength="8" />
                </div>
                <div class="flex gap-2 pt-2">
                    <button type="button" class="mat-btn-secondary flex-1" onclick="closeUserModal()">إلغاء</button>
                    <button type="submit" class="mat-btn-primary flex-1">حفظ</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function addUrlValue() { return document.getElementById('userModalForm').dataset.addUrl; }
        function editUrl(id) { return document.getElementById('userModalForm').dataset.editBase + '/' + id; }

        function openUserModal() {
            document.getElementById('userModalTitle').textContent = 'إضافة مستخدم';
            document.getElementById('userModalForm').action = addUrlValue();
            document.getElementById('userModalMethod').value = 'POST';
            document.getElementById('uf_name').value = '';
            document.getElementById('uf_email').value = '';
            document.getElementById('uf_role').value = 'student';
            document.getElementById('uf_password').value = '';
            document.getElementById('uf_password').required = true;
            document.getElementById('uf_pass_hint').textContent = '(8 أحرف على الأقل)';
            showUserModal();
        }

        function openEditUserModal(btn) {
            const d = btn.dataset;
            document.getElementById('userModalTitle').textContent = 'تعديل مستخدم';
            document.getElementById('userModalForm').action = editUrl(d.id);
            document.getElementById('userModalMethod').value = 'PATCH';
            document.getElementById('uf_name').value = d.name;
            document.getElementById('uf_email').value = d.email;
            document.getElementById('uf_role').value = d.role;
            document.getElementById('uf_password').value = '';
            document.getElementById('uf_password').required = false;
            document.getElementById('uf_pass_hint').textContent = '(اتركه فارغًا للإبقاء على الحالي)';
            showUserModal();
        }

        function showUserModal() {
            const m = document.getElementById('userModal');
            m.classList.remove('hidden');
            m.classList.add('flex');
        }
        function closeUserModal() {
            const m = document.getElementById('userModal');
            m.classList.add('hidden');
            m.classList.remove('flex');
        }
        document.getElementById('userModal').addEventListener('click', function (e) {
            if (e.target === this) closeUserModal();
        });
    </script>
</x-app-layout>
