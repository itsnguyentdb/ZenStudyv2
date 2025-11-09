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
import com.example.quiz_clone.models.FlashcardDeck;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class FlashcardLibraryAdapter extends ListAdapter<FlashcardDeck, FlashcardLibraryAdapter.FlashcardDeckViewHolder> {
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public FlashcardLibraryAdapter() {
        super(DIFF_CALLBACK);
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
                    oldItem.getLastUpdatedTime().equals(newItem.getLastUpdatedTime());
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

        if (currentDeck.getLastUpdatedTime() != null) {
            String formattedDate = dateFormat.format(currentDeck.getLastUpdatedTime());
            holder.textViewLastUpdated.setText("Last updated: " + formattedDate);
        }
    }

    public FlashcardDeck getDeckAt(int position) {
        return getItem(position);
    }

    class FlashcardDeckViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDescription;
        private TextView textViewLastUpdated;

        public FlashcardDeckViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.tvDeckTitle);
            textViewDescription = itemView.findViewById(R.id.tvDeckDescription);
            textViewLastUpdated = itemView.findViewById(R.id.tvDeckUpdated);

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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
