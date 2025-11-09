package com.example.quiz_clone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz_clone.R;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
public class BottomNavigationSubMenuAdapter extends RecyclerView.Adapter<BottomNavigationSubMenuAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.submenu_icon);
            title = itemView.findViewById(R.id.submenu_title);

            itemView.setOnClickListener(v -> {
                var position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onSubMenuItemClickListener != null) {
                    onSubMenuItemClickListener.onSubMenuItemClick(subMenuItems.get(position));
                }
            });
        }

        void bind(SubMenuItem item) {
            if (item.getIconRes() != 0) {
                icon.setImageResource(item.getIconRes());
                icon.setVisibility(View.VISIBLE);
            } else {
                icon.setVisibility(View.GONE);
            }
            title.setText(item.getTitle());
        }
    }

    @Data
    @AllArgsConstructor
    public static class SubMenuItem {
        private String title;
        private int iconRes;
        private Class<? extends Fragment> fragmentClass;
    }

    public interface OnSubMenuItemClickListener {
        void onSubMenuItemClick(SubMenuItem item);
    }

    private List<SubMenuItem> subMenuItems;
    private OnSubMenuItemClickListener onSubMenuItemClickListener;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_bottom_nav_submenu_grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        var item = subMenuItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return subMenuItems.size();
    }
}
