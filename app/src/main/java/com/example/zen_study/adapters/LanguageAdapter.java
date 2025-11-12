package com.example.zen_study.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;

import java.util.List;
import java.util.Map;

// LanguageAdapter.java
public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {
    private List<Map.Entry<String, String>> languages;
    private String selectedLanguage;
    private OnLanguageSelectedListener listener;

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(String languageCode);
    }

    public LanguageAdapter(List<Map.Entry<String, String>> languages, String selectedLanguage) {
        this.languages = languages;
        this.selectedLanguage = selectedLanguage;
    }

    public void setOnLanguageSelectedListener(OnLanguageSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_language, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, String> entry = languages.get(position);
        holder.bind(entry.getKey(), entry.getValue(), entry.getKey().equals(selectedLanguage));
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView languageName;
        private RadioButton radioButton;

        ViewHolder(View itemView) {
            super(itemView);
            languageName = itemView.findViewById(R.id.languageName);
            radioButton = itemView.findViewById(R.id.radioButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    String languageCode = languages.get(position).getKey();
                    listener.onLanguageSelected(languageCode);
                }
            });
        }

        void bind(String languageCode, String languageName, boolean isSelected) {
            this.languageName.setText(languageName);
            this.radioButton.setChecked(isSelected);
        }
    }
}