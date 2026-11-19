package com.example.graduationsystem.util;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds simple form dialogs (text fields + dropdowns) programmatically for the admin
 * CRUD screens (create/edit project &amp; user). Uses plain EditText/AutoCompleteTextView so
 * no Material internal style constants are referenced. RTL-friendly vertical layout.
 */
public final class FormDialog {

    /** A single field definition for the dialog. */
    public static class Field {
        public final String key;
        public final @Nullable List<String> options;
        public final EditText edit;

        Field(String key, List<String> options, EditText edit) {
            this.key = key;
            this.options = options;
            this.edit = edit;
        }
    }

    private final List<Field> fields = new ArrayList<>();
    private final LinearLayout container;

    private FormDialog(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        int padH = (int) (24 * density);
        int padV = (int) (8 * density);
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padH, padV, padH, 0);
    }

    public static FormDialog build(Context context) {
        return new FormDialog(context);
    }

    public View getRoot() {
        return container;
    }

    /** Adds a plain text / multiline / password field. Returns this for chaining. */
    public FormDialog text(String key, String label, @Nullable String initial, boolean multiline, boolean password) {
        Context ctx = container.getContext();
        EditText et = new EditText(ctx);
        et.setHint(label);
        et.setText(initial == null ? "" : initial);
        if (initial != null) et.setSelection(initial.length());
        if (multiline) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            et.setMinLines(2);
            et.setGravity(Gravity.TOP | Gravity.START);
        } else if (password) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        addRow(et);
        fields.add(new Field(key, null, et));
        return this;
    }

    /** Adds a dropdown field with fixed options. Returns this for chaining. */
    public FormDialog dropdown(String key, String label, @Nullable String selected, @NonNull List<String> options) {
        Context ctx = container.getContext();
        AutoCompleteTextView actv = new AutoCompleteTextView(ctx);
        actv.setHint(label);
        actv.setInputType(InputType.TYPE_NULL);
        actv.setThreshold(1);
        actv.setAdapter(new ArrayAdapter<>(ctx,
                android.R.layout.simple_list_item_1, new ArrayList<>(options)));
        if (selected != null && options.contains(selected)) {
            actv.setText(selected, false);
        }
        addRow(actv);
        fields.add(new Field(key, new ArrayList<>(options), actv));
        return this;
    }

    private void addRow(View view) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = (int) (6 * container.getContext().getResources().getDisplayMetrics().density);
        lp.setMargins(0, margin, 0, margin);
        view.setLayoutParams(lp);
        container.addView(view);
    }

    /** Value of a field by key (trimmed). Null if absent or empty. */
    @Nullable
    public String value(String key) {
        for (Field f : fields) {
            if (f.key.equals(key)) {
                String v = f.edit.getText().toString().trim();
                return v.isEmpty() ? null : v;
            }
        }
        return null;
    }
}
