package com.example.zen_study.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.models.FlashcardTerm;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SaveFlashcardAdapter extends ListAdapter<FlashcardTerm, SaveFlashcardAdapter.ViewHolder> {
    private ItemTouchHelper itemTouchHelper;
    private List<FlashcardTerm> mutableList = new ArrayList<>();

    public SaveFlashcardAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_save_flashcard_term, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FlashcardTerm term = getItem(position);
        holder.bind(term, position);
    }

    @Override
    public void submitList(@Nullable List<FlashcardTerm> list) {
        mutableList.clear();
        if (list != null) {
            mutableList.addAll(list);
        }
        super.submitList(list != null ? new ArrayList<>(list) : null);
    }

    public void setItemTouchHelper(ItemTouchHelper helper) {
        this.itemTouchHelper = helper;
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0 ||
                fromPosition >= mutableList.size() || toPosition >= mutableList.size() ||
                fromPosition == toPosition) {
            return;
        }

        try {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mutableList, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mutableList, i, i - 1);
                }
            }

            submitList(new ArrayList<>(mutableList));
            updatePositions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePositions() {
        for (int i = 0; i < getItemCount(); i++) {
            getItem(i).setPosition(i);
        }
    }

    public void deleteItem(int position) {
        if (position >= 0 && position < mutableList.size()) {
            mutableList.remove(position);
            submitList(new ArrayList<>(mutableList));
            updatePositions();
        }
    }

    // Add this method to get the current terms
    public List<FlashcardTerm> getCurrentTerms() {
        return new ArrayList<>(mutableList);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextInputEditText etTerm, etDefinition;
        private MaterialButton btnDelete;
        private ShapeableImageView ivDragHandle;
        private TextWatcher termWatcher, definitionWatcher;

        ViewHolder(View itemView) {
            super(itemView);
            etTerm = itemView.findViewById(R.id.edit_text_term);
            etDefinition = itemView.findViewById(R.id.edit_text_definition);
            btnDelete = itemView.findViewById(R.id.btn_delete_term);
            ivDragHandle = itemView.findViewById(R.id.iv_drag_handle);
        }

        void bind(FlashcardTerm term, int position) {
            // Remove existing text watchers to avoid duplicates
            if (termWatcher != null) {
                etTerm.removeTextChangedListener(termWatcher);
            }
            if (definitionWatcher != null) {
                etDefinition.removeTextChangedListener(definitionWatcher);
            }

            etTerm.setText(term.getTerm());
            etDefinition.setText(term.getDefinition());

            // Create new text watchers
            termWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    term.setTerm(s.toString());
                }
            };

            definitionWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    term.setDefinition(s.toString());
                }
            };

            etTerm.addTextChangedListener(termWatcher);
            etDefinition.addTextChangedListener(definitionWatcher);

            btnDelete.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    deleteItem(adapterPosition);
                }
            });

            ivDragHandle.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (itemTouchHelper != null) {
                        itemTouchHelper.startDrag(this);
                    }
                }
                return false;
            });
        }
    }

    private static final DiffUtil.ItemCallback<FlashcardTerm> DIFF_CALLBACK = new DiffUtil.ItemCallback<FlashcardTerm>() {
        @Override
        public boolean areItemsTheSame(@NonNull FlashcardTerm oldItem, @NonNull FlashcardTerm newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FlashcardTerm oldItem, @NonNull FlashcardTerm newItem) {
            return oldItem.equals(newItem);
        }
    };
}
