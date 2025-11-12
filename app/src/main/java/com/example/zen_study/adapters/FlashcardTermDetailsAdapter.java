package com.example.zen_study.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.models.FlashcardTerm;

import java.util.List;

public class FlashcardTermDetailsAdapter extends RecyclerView.Adapter<FlashcardTermDetailsAdapter.ViewHolder> {

    private List<FlashcardTerm> terms;

    public FlashcardTermDetailsAdapter(List<FlashcardTerm> terms) {
        this.terms = terms;
    }

    public void setTerms(List<FlashcardTerm> terms) {
        this.terms = terms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_details_flashcard_term, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FlashcardTerm term = terms.get(position);
        holder.tvTerm.setText(term.getTerm());
        holder.tvDefinition.setText(term.getDefinition());
    }

    @Override
    public int getItemCount() {
        return terms != null ? terms.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTerm;
        TextView tvDefinition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTerm = itemView.findViewById(R.id.tvTerm);
            tvDefinition = itemView.findViewById(R.id.tvDefinition);
        }
    }
}
