package com.example.zen_study.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.models.FlashcardDeck;
import com.example.zen_study.models.Subject;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FlashcardLibraryAdapter extends ListAdapter<FlashcardDeck, FlashcardLibraryAdapter.FlashcardDeckViewHolder> {
    private OnItemClickListener listener;
    private OnOptionsClickListener optionsClickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private Map<Long, Subject> subjectMap = new HashMap<>();

    public FlashcardLibraryAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setSubjects(List<Subject> subjects) {
        subjectMap.clear();
        if (subjects != null) {
            for (Subject subject : subjects) {
                subjectMap.put(subject.getId(), subject);
            }
        }
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<FlashcardDeck> DIFF_CALLBACK = new DiffUtil.ItemCallback<FlashcardDeck>() {
        @Override
        public boolean areItemsTheSame(@NonNull FlashcardDeck oldItem, @NonNull FlashcardDeck newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FlashcardDeck oldItem, @NonNull FlashcardDeck newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getLastUpdatedAt().equals(newItem.getLastUpdatedAt()) &&
                    oldItem.getSubjectId().equals(newItem.getSubjectId());
        }
    };

    @NonNull
    @Override
    public FlashcardDeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard_deck, parent, false);
        return new FlashcardDeckViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardDeckViewHolder holder, int position) {
        FlashcardDeck currentDeck = getItem(position);
        holder.textViewTitle.setText(currentDeck.getTitle());
        holder.textViewDescription.setText(currentDeck.getDescription());

        // Set subject
        String subjectName = "Other";
        if (currentDeck.getSubjectId() != null && subjectMap.containsKey(currentDeck.getSubjectId())) {
            subjectName = subjectMap.get(currentDeck.getSubjectId()).getName();
        }
        holder.chipSubject.setText(subjectName);

        // Set different colors for different subjects
        setChipColor(holder.chipSubject, subjectName);

        if (currentDeck.getLastUpdatedAt() != null) {
            String formattedDate = dateFormat.format(currentDeck.getLastUpdatedAt());
            holder.textViewLastUpdated.setText("Last updated: " + formattedDate);
        }

        // Set card count
        holder.textViewCardCount.setText(currentDeck.getCardCount() + " cards");

        // Set options click listener
        holder.imageViewOptions.setOnClickListener(v -> {
            if (optionsClickListener != null && position != RecyclerView.NO_POSITION) {
                optionsClickListener.onOptionsClick(getItem(position), v);
            }
        });
    }

    private void setChipColor(Chip chip, String colorCode) {
        int textColor = Color.WHITE;
        try {
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(colorCode)));
        } catch (Exception e) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#607D8B")));
        }
        chip.setTextColor(textColor);
    }

    public FlashcardDeck getDeckAt(int position) {
        return getItem(position);
    }

    class FlashcardDeckViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDescription;
        private TextView textViewLastUpdated;
        private TextView textViewCardCount;
        private ImageView imageViewOptions;
        private Chip chipSubject;

        public FlashcardDeckViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.tvDeckTitle);
            textViewDescription = itemView.findViewById(R.id.tvDeckDescription);
            textViewLastUpdated = itemView.findViewById(R.id.tvDeckUpdated);
            textViewCardCount = itemView.findViewById(R.id.tvCardCount);
            imageViewOptions = itemView.findViewById(R.id.ivOptions);
            chipSubject = itemView.findViewById(R.id.chipSubject);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(FlashcardDeck deck);
    }

    public interface OnOptionsClickListener {
        void onOptionsClick(FlashcardDeck deck, View view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnOptionsClickListener(OnOptionsClickListener listener) {
        this.optionsClickListener = listener;
    }
}