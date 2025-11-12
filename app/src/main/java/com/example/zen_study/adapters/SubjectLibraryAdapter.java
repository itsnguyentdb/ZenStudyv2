package com.example.zen_study.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.models.Subject;

public class SubjectLibraryAdapter extends ListAdapter<Subject, SubjectLibraryAdapter.ViewHolder> {

    public interface SubjectActionListener {
        void onEdit(Subject subject);
        void onDelete(Subject subject);
    }

    private final SubjectActionListener listener;

    public SubjectLibraryAdapter(@NonNull java.util.List<Subject> initial, SubjectActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        submitList(initial);
    }

    private static final DiffUtil.ItemCallback<Subject> DIFF_CALLBACK = new DiffUtil.ItemCallback<Subject>() {
        @Override
        public boolean areItemsTheSame(@NonNull Subject oldItem, @NonNull Subject newItem) {
            Long oid = oldItem.getId();
            Long nid = newItem.getId();
            return oid != null && oid.equals(nid);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Subject oldItem, @NonNull Subject newItem) {
            String on = oldItem.getName() == null ? "" : oldItem.getName();
            String nn = newItem.getName() == null ? "" : newItem.getName();
            return on.equals(nn);
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageButton edit;
        ImageButton delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_subject_name);
            edit = itemView.findViewById(R.id.button_edit_subject);
            delete = itemView.findViewById(R.id.button_delete_subject);
        }

        void bind(Subject subject, SubjectActionListener listener) {
            name.setText(subject.getName());
            edit.setOnClickListener(v -> listener.onEdit(subject));
            delete.setOnClickListener(v -> listener.onDelete(subject));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_subject, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }
}
