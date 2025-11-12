package com.example.quiz_clone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz_clone.R;
import com.example.quiz_clone.models.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectLibraryAdapter extends RecyclerView.Adapter<SubjectLibraryAdapter.SubjectViewHolder> {
    public interface Listener {
        void onSubjectClick(Subject subject);
        void onSubjectEdit(Subject subject);
        void onSubjectDelete(Subject subject);
    }

    private final Listener listener;
    private List<Subject> subjects = new ArrayList<>();

    public SubjectLibraryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Subject> newSubjects) {
        subjects = newSubjects != null ? newSubjects : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        holder.bind(subjects.get(position));
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    class SubjectViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView description;
        private final ImageButton editBtn;
        private final ImageButton deleteBtn;

        SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_subject_name);
            description = itemView.findViewById(R.id.text_subject_description);
            editBtn = itemView.findViewById(R.id.button_edit_subject);
            deleteBtn = itemView.findViewById(R.id.button_delete_subject);
        }

        void bind(Subject subject) {
            name.setText(subject.getName());
            String desc = subject.getDescription();
            if (desc != null && !desc.isEmpty()) {
                description.setText(desc);
                description.setVisibility(View.VISIBLE);
            } else {
                description.setVisibility(View.GONE);
            }
            itemView.setOnClickListener(v -> listener.onSubjectClick(subject));
            editBtn.setOnClickListener(v -> listener.onSubjectEdit(subject));
            deleteBtn.setOnClickListener(v -> listener.onSubjectDelete(subject));
        }
    }
}

