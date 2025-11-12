package com.example.zen_study.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.models.Quiz;
import com.google.android.material.button.MaterialButton;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Setter;

public class QuizLibraryAdapter extends RecyclerView.Adapter<QuizLibraryAdapter.QuizViewHolder> {
    private List<Quiz> quizzes;
    @Setter
    private Map<Long, Integer> questionCounts = new HashMap<>();
    @Setter
    private Map<Long, Integer> attemptCounts = new HashMap<>();
    @Setter
    private Map<Long, Integer> averageScores = new HashMap<>();
    @Setter
    private Map<Long, Date> lastAttemptDates = new HashMap<>();
    // Setter methods for click listeners
    @Setter
    private OnQuizClickListener onQuizClickListener;
    @Setter
    private OnTakeQuizClickListener onTakeQuizClickListener;
    @Setter
    private OnViewResultsClickListener onViewResultsClickListener;
    @Setter
    private OnQuizMenuClickListener onQuizMenuClickListener;

    public QuizLibraryAdapter(List<Quiz> quizzes) {
        this.quizzes = quizzes;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_card, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizzes.get(position);
        holder.bind(quiz, questionCounts, attemptCounts, averageScores, lastAttemptDates);

        // Set click listeners using quizWithStats.getQuiz()
        holder.itemView.setOnClickListener(v -> {
            if (onQuizClickListener != null) {
                onQuizClickListener.onQuizClick(quiz);
            }
        });

        holder.buttonTakeQuiz.setOnClickListener(v -> {
            if (onTakeQuizClickListener != null) {
                onTakeQuizClickListener.onTakeQuizClick(quiz);
            }
        });

        holder.buttonViewResults.setOnClickListener(v -> {
            if (onViewResultsClickListener != null) {
                onViewResultsClickListener.onViewResultsClick(quiz);
            }
        });

        holder.iconQuizMenu.setOnClickListener(v -> {
            if (onQuizMenuClickListener != null) {
                onQuizMenuClickListener.onQuizMenuClick(quiz, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    public void setQuizzes(List<Quiz> quizzes) {
        this.quizzes = quizzes;
        notifyDataSetChanged();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        private TextView textQuizTitle;
        private TextView textQuizDescription;
        private TextView textQuestionsCount;
        private TextView textAttemptsCount;
        private TextView textAverageScore;
        private TextView textLastAttempt;
        private MaterialButton buttonTakeQuiz;
        private MaterialButton buttonViewResults;
        private ImageView iconQuizMenu;
        private View layoutLastAttempt;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            textQuizTitle = itemView.findViewById(R.id.text_quiz_title);
            textQuizDescription = itemView.findViewById(R.id.text_quiz_description);
            textQuestionsCount = itemView.findViewById(R.id.text_questions_count);
            textAttemptsCount = itemView.findViewById(R.id.text_attempts_count);
            textAverageScore = itemView.findViewById(R.id.text_average_score);
            textLastAttempt = itemView.findViewById(R.id.text_last_attempt);
            buttonTakeQuiz = itemView.findViewById(R.id.button_take_quiz);
            buttonViewResults = itemView.findViewById(R.id.button_view_results);
            iconQuizMenu = itemView.findViewById(R.id.icon_quiz_menu);
            layoutLastAttempt = itemView.findViewById(R.id.layout_last_attempt);
        }

        public void bind(Quiz quiz,
                         Map<Long, Integer> questionCounts,
                         Map<Long, Integer> attemptCounts,
                         Map<Long, Integer> averageScores,
                         Map<Long, Date> lastAttemptDates) {

            textQuizTitle.setText(quiz.getTitle());

            if (quiz.getDescription() != null && !quiz.getDescription().isEmpty()) {
                textQuizDescription.setText(quiz.getDescription());
                textQuizDescription.setVisibility(View.VISIBLE);
            } else {
                textQuizDescription.setVisibility(View.GONE);
            }

            // Get statistics for this specific quiz
            Long quizId = quiz.getId();

            // Question count
            Integer questionCount = questionCounts.get(quizId);
            textQuestionsCount.setText(questionCount != null ? String.valueOf(questionCount) : "0");

            // Attempt count
            Integer attemptCount = attemptCounts.get(quizId);
            textAttemptsCount.setText(attemptCount != null ? String.valueOf(attemptCount) : "0");

            // Average score
            Integer averageScore = averageScores.get(quizId);
            textAverageScore.setText(averageScore != null ? String.format(Locale.getDefault(), "%d%%", averageScore) : "0%");

            // Last attempt
            Date lastAttemptDate = lastAttemptDates.get(quizId);
            if (lastAttemptDate != null) {
                layoutLastAttempt.setVisibility(View.VISIBLE);
                String timeAgo = getTimeAgo(lastAttemptDate);
                textLastAttempt.setText(String.format("Last attempt: %s", timeAgo));
            } else {
                layoutLastAttempt.setVisibility(View.GONE);
            }
        }

        private String getTimeAgo(Date date) {
            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long weeks = days / 7;
            long months = days / 30;
            long years = days / 365;

            if (years > 0) {
                return years == 1 ? "1 year ago" : years + " years ago";
            } else if (months > 0) {
                return months == 1 ? "1 month ago" : months + " months ago";
            } else if (weeks > 0) {
                return weeks == 1 ? "1 week ago" : weeks + " weeks ago";
            } else if (days > 0) {
                return days == 1 ? "1 day ago" : days + " days ago";
            } else if (hours > 0) {
                return hours == 1 ? "1 hour ago" : hours + " hours ago";
            } else if (minutes > 0) {
                return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
            } else {
                return "Just now";
            }
        }
    }

    // Click listener interfaces
    public interface OnQuizClickListener {
        void onQuizClick(Quiz quiz);
    }

    public interface OnTakeQuizClickListener {
        void onTakeQuizClick(Quiz quiz);
    }

    public interface OnViewResultsClickListener {
        void onViewResultsClick(Quiz quiz);
    }

    public interface OnQuizMenuClickListener {
        void onQuizMenuClick(Quiz quiz, View anchorView);
    }
}
