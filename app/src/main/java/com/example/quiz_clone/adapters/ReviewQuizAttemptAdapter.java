package com.example.quiz_clone.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz_clone.R;
import com.example.quiz_clone.models.QuizAttemptAnswer;
import com.example.quiz_clone.models.QuizQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReviewQuizAttemptAdapter extends RecyclerView.Adapter<ReviewQuizAttemptAdapter.ReviewViewHolder> {
    private List<QuizQuestion> questions = new ArrayList<>();
    private List<QuizAttemptAnswer> answers = new ArrayList<>();

    public void setData(List<QuizQuestion> questions, List<QuizAttemptAnswer> answers) {
        this.questions = questions;
        this.answers = answers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question_review_card, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        QuizQuestion question = questions.get(position);
        QuizAttemptAnswer userAnswer = findUserAnswer(question.getId());
        holder.bind(question, userAnswer, position + 1);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    private QuizAttemptAnswer findUserAnswer(Long questionId) {
        for (QuizAttemptAnswer answer : answers) {
            if (answer.getQuestionId().equals(questionId)) {
                return answer;
            }
        }
        return null;
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewQuestion, textViewOptionA, textViewOptionB;
        private TextView textViewOptionC, textViewOptionD, textViewExplanation;
        private TextView textViewQuestionNumber;
        private View optionLayoutA, optionLayoutB, optionLayoutC, optionLayoutD;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuestion = itemView.findViewById(R.id.text_view_question);
            textViewOptionA = itemView.findViewById(R.id.text_view_option_a);
            textViewOptionB = itemView.findViewById(R.id.text_view_option_b);
            textViewOptionC = itemView.findViewById(R.id.text_view_option_c);
            textViewOptionD = itemView.findViewById(R.id.text_view_option_d);
            textViewExplanation = itemView.findViewById(R.id.text_view_explanation);
            textViewQuestionNumber = itemView.findViewById(R.id.text_view_question_number);

            optionLayoutA = itemView.findViewById(R.id.option_layout_a);
            optionLayoutB = itemView.findViewById(R.id.option_layout_b);
            optionLayoutC = itemView.findViewById(R.id.option_layout_c);
            optionLayoutD = itemView.findViewById(R.id.option_layout_d);
        }

        void bind(QuizQuestion question, QuizAttemptAnswer userAnswer, int questionNumber) {
            // Set question number
            textViewQuestionNumber.setText(String.format(Locale.getDefault(), "Q%d", questionNumber));

            textViewQuestion.setText(question.getQuestionText());

            // Set options
            textViewOptionA.setText(question.getOptionA());
            textViewOptionB.setText(question.getOptionB());
            textViewOptionC.setText(question.getOptionC() != null ? question.getOptionC() : "");
            textViewOptionD.setText(question.getOptionD() != null ? question.getOptionD() : "");

            // Show/hide options
            optionLayoutC.setVisibility(question.getOptionC() != null && !question.getOptionC().isEmpty() ? View.VISIBLE : View.GONE);
            optionLayoutD.setVisibility(question.getOptionD() != null && !question.getOptionD().isEmpty() ? View.VISIBLE : View.GONE);

            // Highlight answers
            highlightAnswer(optionLayoutA, "A", question, userAnswer);
            highlightAnswer(optionLayoutB, "B", question, userAnswer);
            highlightAnswer(optionLayoutC, "C", question, userAnswer);
            highlightAnswer(optionLayoutD, "D", question, userAnswer);

            // Show explanation
            if (question.getExplanation() != null && !question.getExplanation().isEmpty()) {
                textViewExplanation.setText(question.getExplanation());
                textViewExplanation.setVisibility(View.VISIBLE);
            } else {
                textViewExplanation.setVisibility(View.GONE);
            }
        }

        private void highlightAnswer(View optionLayout, String option, QuizQuestion question, QuizAttemptAnswer userAnswer) {
            // Reset background and text
            optionLayout.setBackgroundColor(Color.TRANSPARENT);

            TextView optionText = null;
            if (optionLayout instanceof ViewGroup) {
                ViewGroup layout = (ViewGroup) optionLayout;
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    if (child instanceof TextView) {
                        optionText = (TextView) child;
                        break;
                    }
                }
            }

            if (optionText != null) {
                // Remove any previous indicators
                String originalText = optionText.getText().toString()
                        .replace(" ✓", "")
                        .replace(" ✗", "");
                optionText.setText(originalText);
            }

            String correctAnswer = question.getCorrectAnswer();
            String userSelected = userAnswer != null ? userAnswer.getUserAnswer() : null;

            if (option.equals(correctAnswer)) {
                // Correct answer - green background
                optionLayout.setBackgroundColor(Color.parseColor("#E8F5E8"));
                if (optionText != null) {
                    optionText.setText(optionText.getText() + " ✓");
                }
            } else if (option.equals(userSelected) && !option.equals(correctAnswer)) {
                // Wrong user answer - red background
                optionLayout.setBackgroundColor(Color.parseColor("#FFEBEE"));
                if (optionText != null) {
                    optionText.setText(optionText.getText() + " ✗");
                }
            }
        }
    }
}