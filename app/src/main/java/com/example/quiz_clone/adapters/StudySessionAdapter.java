package com.example.quiz_clone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz_clone.R;
import com.example.quiz_clone.models.StudySession;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class StudySessionAdapter extends ListAdapter<StudySession, StudySessionAdapter.SessionViewHolder> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public StudySessionAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<StudySession> DIFF_CALLBACK = new DiffUtil.ItemCallback<StudySession>() {
        @Override
        public boolean areItemsTheSame(@NonNull StudySession oldItem, @NonNull StudySession newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull StudySession oldItem, @NonNull StudySession newItem) {
            return oldItem.getStartTime().equals(newItem.getStartTime()) &&
                    oldItem.getDuration() == newItem.getDuration() &&
                    oldItem.getMode().equals(newItem.getMode());
        }
    };

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        StudySession session = getItem(position);
        holder.bind(session);
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDate;
        private final TextView textDuration;
        private final Chip chipMode;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.text_session_date);
            textDuration = itemView.findViewById(R.id.text_session_duration);
            chipMode = itemView.findViewById(R.id.chip_session_mode);
        }

        public void bind(StudySession session) {
            textDate.setText(formatDate(session.getStartTime()));
            textDuration.setText(formatDuration(session.getDuration()));
            setupModeChip(session.getMode());
        }

        private String formatDate(java.util.Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            return sdf.format(date);
        }

        private String formatDuration(long minutes) {
            if (minutes < 60) {
                return minutes + " minutes";
            } else {
                long hours = minutes / 60;
                long remainingMinutes = minutes % 60;
                if (remainingMinutes > 0) {
                    return hours + "h " + remainingMinutes + "m";
                } else {
                    return hours + " hour" + (hours > 1 ? "s" : "");
                }
            }
        }

        private void setupModeChip(StudySession.StudySessionMode mode) {
            switch (mode) {
                case NORMAL:
                    chipMode.setText("Normal");
                    chipMode.setChipBackgroundColorResource(R.color.mode_normal);
                    break;
                case POMODORO:
                    chipMode.setText("Pomodoro");
                    chipMode.setChipBackgroundColorResource(R.color.mode_pomodoro);
                    break;
                default:
                    chipMode.setText("Normal");
                    chipMode.setChipBackgroundColorResource(R.color.mode_normal);
            }
        }
    }
}