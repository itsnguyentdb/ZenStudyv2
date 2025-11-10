package com.example.quiz_clone.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz_clone.R;
import com.example.quiz_clone.models.QuizQuestion;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class SaveQuestionsAdapter extends RecyclerView.Adapter<SaveQuestionsAdapter.QuestionViewHolder> {

    private List<QuizQuestion> questions;
    private OnQuestionDeleteClickListener onQuestionDeleteClickListener;
    private ItemTouchHelper mItemTouchHelper;
    private boolean isRadioGroupProgrammaticChange = false;

    public interface OnQuestionDeleteClickListener {
        void onQuestionDeleteClick(QuizQuestion question);
    }

    public SaveQuestionsAdapter(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
        notifyDataSetChanged();
    }

    public void setOnQuestionDeleteClickListener(OnQuestionDeleteClickListener listener) {
        this.onQuestionDeleteClickListener = listener;
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.mItemTouchHelper = itemTouchHelper;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_edit, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuizQuestion question = questions.get(position);

        holder.editTextQuestion.setText(question.getQuestionText());
        holder.editOptionA.setText(question.getOptionA());
        holder.editOptionB.setText(question.getOptionB());
        holder.editOptionC.setText(question.getOptionC());
        holder.editOptionD.setText(question.getOptionD());
        holder.editTextExplanation.setText(question.getExplanation());

        // Set points - default to 1 if not set
        int points = question.getPoints() > 0 ? question.getPoints() : 1;
        holder.editTextPoints.setText(String.valueOf(points));

        // Update radio button enabled states based on option content
        updateRadioButtonStates(holder, question);

        // Set radio based on correctAnswer - use programmatic change to avoid triggering listener
        isRadioGroupProgrammaticChange = true;
        holder.radioGroupCorrect.clearCheck();
        switch (question.getCorrectAnswer() != null ? question.getCorrectAnswer() : "") {
            case "A":
                if (!holder.editOptionA.getText().toString().trim().isEmpty()) {
                    holder.radioGroupCorrect.check(R.id.radio_a);
                }
                break;
            case "B":
                if (!holder.editOptionB.getText().toString().trim().isEmpty()) {
                    holder.radioGroupCorrect.check(R.id.radio_b);
                }
                break;
            case "C":
                if (!holder.editOptionC.getText().toString().trim().isEmpty()) {
                    holder.radioGroupCorrect.check(R.id.radio_c);
                }
                break;
            case "D":
                if (!holder.editOptionD.getText().toString().trim().isEmpty()) {
                    holder.radioGroupCorrect.check(R.id.radio_d);
                }
                break;
        }
        isRadioGroupProgrammaticChange = false;

        // Update model on text changes
        setupTextWatchers(holder, question, position);

        // Update correctAnswer on radio change
        setupRadioGroupListener(holder, question, position);

        // Setup individual radio button click listeners for toggle behavior
        setupRadioButtonClickListeners(holder, question, position);

        holder.btnDelete.setOnClickListener(v -> {
            if (onQuestionDeleteClickListener != null) {
                onQuestionDeleteClickListener.onQuestionDeleteClick(question);
            }
        });

        // Enable drag on handle
        holder.ivDragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mItemTouchHelper.startDrag(holder);
            }
            return true;
        });
    }

    private void setupTextWatchers(QuestionViewHolder holder, QuizQuestion question, int position) {
        holder.editTextQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                question.setQuestionText(s.toString().trim());
            }
        });

        // Points TextWatcher
        holder.editTextPoints.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String pointsText = s.toString().trim();
                    if (!pointsText.isEmpty()) {
                        int points = Integer.parseInt(pointsText);
                        if (points > 0) {
                            question.setPoints(points);
                            holder.textInputLayoutPoints.setError(null);
                        } else {
                            holder.textInputLayoutPoints.setError("Points must be greater than 0");
                        }
                    } else {
                        // If empty, set to default 1
                        question.setPoints(1);
                        holder.editTextPoints.setText("1");
                    }
                } catch (NumberFormatException e) {
                    holder.textInputLayoutPoints.setError("Please enter a valid number");
                }
            }
        });

        holder.editOptionA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String optionText = s.toString().trim();
                question.setOptionA(optionText);
                updateRadioButtonState(holder.radioA, optionText);

                // If this was the correct answer and now it's empty, clear the correct answer
                if ("A".equals(question.getCorrectAnswer()) && optionText.isEmpty()) {
                    question.setCorrectAnswer("");
                    isRadioGroupProgrammaticChange = true;
                    holder.radioGroupCorrect.clearCheck();
                    isRadioGroupProgrammaticChange = false;
                }
            }
        });

        holder.editOptionB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String optionText = s.toString().trim();
                question.setOptionB(optionText);
                updateRadioButtonState(holder.radioB, optionText);

                if ("B".equals(question.getCorrectAnswer()) && optionText.isEmpty()) {
                    question.setCorrectAnswer("");
                    isRadioGroupProgrammaticChange = true;
                    holder.radioGroupCorrect.clearCheck();
                    isRadioGroupProgrammaticChange = false;
                }
            }
        });

        holder.editOptionC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String optionText = s.toString().trim();
                question.setOptionC(optionText);
                updateRadioButtonState(holder.radioC, optionText);

                if ("C".equals(question.getCorrectAnswer()) && optionText.isEmpty()) {
                    question.setCorrectAnswer("");
                    isRadioGroupProgrammaticChange = true;
                    holder.radioGroupCorrect.clearCheck();
                    isRadioGroupProgrammaticChange = false;
                }
            }
        });

        holder.editOptionD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String optionText = s.toString().trim();
                question.setOptionD(optionText);
                updateRadioButtonState(holder.radioD, optionText);

                if ("D".equals(question.getCorrectAnswer()) && optionText.isEmpty()) {
                    question.setCorrectAnswer("");
                    isRadioGroupProgrammaticChange = true;
                    holder.radioGroupCorrect.clearCheck();
                    isRadioGroupProgrammaticChange = false;
                }
            }
        });

        holder.editTextExplanation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                question.setExplanation(s.toString().trim());
            }
        });
    }

    private void setupRadioGroupListener(QuestionViewHolder holder, QuizQuestion question, int position) {
        holder.radioGroupCorrect.setOnCheckedChangeListener(null); // Clear previous listener first

        holder.radioGroupCorrect.setOnCheckedChangeListener((group, checkedId) -> {
            if (isRadioGroupProgrammaticChange) {
                return; // Ignore programmatic changes
            }

            String previousCorrectAnswer = question.getCorrectAnswer();

            if (checkedId == R.id.radio_a) {
                question.setCorrectAnswer("A");
            } else if (checkedId == R.id.radio_b) {
                question.setCorrectAnswer("B");
            } else if (checkedId == R.id.radio_c) {
                question.setCorrectAnswer("C");
            } else if (checkedId == R.id.radio_d) {
                question.setCorrectAnswer("D");
            } else {
                question.setCorrectAnswer("");
            }

            // Update radio button states without triggering notifyItemChanged during layout
            if (!question.getCorrectAnswer().equals(previousCorrectAnswer)) {
                // Use post to update UI after current layout pass
                holder.itemView.post(() -> {
                    updateRadioButtonStates(holder, question);
                });
            }
        });
    }

    private void setupRadioButtonClickListeners(QuestionViewHolder holder, QuizQuestion question, int position) {
        // Allow toggling off by clicking the same radio
        holder.radioA.setOnClickListener(v -> {
            if (holder.radioGroupCorrect.getCheckedRadioButtonId() == R.id.radio_a) {
                // Toggle off
                isRadioGroupProgrammaticChange = true;
                holder.radioGroupCorrect.clearCheck();
                question.setCorrectAnswer("");
                isRadioGroupProgrammaticChange = false;
            } else if (!holder.editOptionA.getText().toString().trim().isEmpty()) {
                // Select this option
                holder.radioGroupCorrect.check(R.id.radio_a);
            }
        });

        holder.radioB.setOnClickListener(v -> {
            if (holder.radioGroupCorrect.getCheckedRadioButtonId() == R.id.radio_b) {
                isRadioGroupProgrammaticChange = true;
                holder.radioGroupCorrect.clearCheck();
                question.setCorrectAnswer("");
                isRadioGroupProgrammaticChange = false;
            } else if (!holder.editOptionB.getText().toString().trim().isEmpty()) {
                holder.radioGroupCorrect.check(R.id.radio_b);
            }
        });

        holder.radioC.setOnClickListener(v -> {
            if (holder.radioGroupCorrect.getCheckedRadioButtonId() == R.id.radio_c) {
                isRadioGroupProgrammaticChange = true;
                holder.radioGroupCorrect.clearCheck();
                question.setCorrectAnswer("");
                isRadioGroupProgrammaticChange = false;
            } else if (!holder.editOptionC.getText().toString().trim().isEmpty()) {
                holder.radioGroupCorrect.check(R.id.radio_c);
            }
        });

        holder.radioD.setOnClickListener(v -> {
            if (holder.radioGroupCorrect.getCheckedRadioButtonId() == R.id.radio_d) {
                isRadioGroupProgrammaticChange = true;
                holder.radioGroupCorrect.clearCheck();
                question.setCorrectAnswer("");
                isRadioGroupProgrammaticChange = false;
            } else if (!holder.editOptionD.getText().toString().trim().isEmpty()) {
                holder.radioGroupCorrect.check(R.id.radio_d);
            }
        });
    }

    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }

    private void updateRadioButtonStates(QuestionViewHolder holder, QuizQuestion question) {
        updateRadioButtonState(holder.radioA, question.getOptionA());
        updateRadioButtonState(holder.radioB, question.getOptionB());
        updateRadioButtonState(holder.radioC, question.getOptionC());
        updateRadioButtonState(holder.radioD, question.getOptionD());
    }

    private void updateRadioButtonState(RadioButton radioButton, String optionText) {
        boolean hasText = optionText != null && !optionText.trim().isEmpty();
        radioButton.setEnabled(hasText);
        radioButton.setAlpha(hasText ? 1.0f : 0.5f);
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        public TextInputEditText editTextQuestion;
        public TextInputEditText editOptionA;
        public TextInputEditText editOptionB;
        public TextInputEditText editOptionC;
        public TextInputEditText editOptionD;
        public TextInputEditText editTextPoints;
        TextInputEditText editTextExplanation;
        com.google.android.material.imageview.ShapeableImageView ivDragHandle;
        com.google.android.material.button.MaterialButton btnDelete;
        RadioGroup radioGroupCorrect;
        RadioButton radioA;
        RadioButton radioB;
        RadioButton radioC;
        RadioButton radioD;

        // TextInputLayout references for validation
        public TextInputLayout textInputLayoutQuestion;
        public TextInputLayout textInputLayoutOptionA;
        public TextInputLayout textInputLayoutOptionB;
        public TextInputLayout textInputLayoutOptionC;
        public TextInputLayout textInputLayoutOptionD;
        public TextInputLayout textInputLayoutPoints;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            editTextQuestion = itemView.findViewById(R.id.edit_text_question);
            editOptionA = itemView.findViewById(R.id.edit_option_a);
            editOptionB = itemView.findViewById(R.id.edit_option_b);
            editOptionC = itemView.findViewById(R.id.edit_option_c);
            editOptionD = itemView.findViewById(R.id.edit_option_d);
            editTextPoints = itemView.findViewById(R.id.edit_text_points);
            editTextExplanation = itemView.findViewById(R.id.edit_text_explanation);
            ivDragHandle = itemView.findViewById(R.id.iv_drag_handle);
            btnDelete = itemView.findViewById(R.id.btn_delete_question);
            radioGroupCorrect = itemView.findViewById(R.id.radio_group_correct);
            radioA = itemView.findViewById(R.id.radio_a);
            radioB = itemView.findViewById(R.id.radio_b);
            radioC = itemView.findViewById(R.id.radio_c);
            radioD = itemView.findViewById(R.id.radio_d);

            // Initialize TextInputLayouts
            textInputLayoutQuestion = itemView.findViewById(R.id.text_input_layout_question);
            textInputLayoutOptionA = itemView.findViewById(R.id.text_input_layout_option_a);
            textInputLayoutOptionB = itemView.findViewById(R.id.text_input_layout_option_b);
            textInputLayoutOptionC = itemView.findViewById(R.id.text_input_layout_option_c);
            textInputLayoutOptionD = itemView.findViewById(R.id.text_input_layout_option_d);
            textInputLayoutPoints = itemView.findViewById(R.id.text_input_layout_points);
        }
    }
}