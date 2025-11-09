package com.example.quiz_clone.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz_clone.R;
import com.example.quiz_clone.dto.CalendarEvent;

import java.util.ArrayList;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<CalendarEvent> events;
    private OnEventClickListener eventClickListener;
    private boolean showDateHeader = false;

    public interface OnEventClickListener {
        void onEventClick(CalendarEvent event);

        void onEventLongClick(CalendarEvent event);
    }

    public EventsAdapter(List<CalendarEvent> events) {
        this.events = events != null ? events : new ArrayList<>();
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.eventClickListener = listener;
    }

    public void updateEvents(List<CalendarEvent> newEvents) {
        this.events = newEvents != null ? newEvents : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setShowDateHeader(boolean show) {
        this.showDateHeader = show;
        notifyDataSetChanged();
    }

    public void addEvent(CalendarEvent event) {
        if (events == null) {
            events = new ArrayList<>();
        }
        events.add(event);
        notifyItemInserted(events.size() - 1);
    }

    public void removeEvent(CalendarEvent event) {
        if (events != null) {
            int position = events.indexOf(event);
            if (position != -1) {
                events.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    public void updateEvent(CalendarEvent oldEvent, CalendarEvent newEvent) {
        if (events != null) {
            int position = events.indexOf(oldEvent);
            if (position != -1) {
                events.set(position, newEvent);
                notifyItemChanged(position);
            }
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CalendarEvent event = events.get(position);
        holder.bind(event, position);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public List<CalendarEvent> getEvents() {
        return events != null ? events : new ArrayList<>();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView eventTitle;
        private TextView eventTime;
        private TextView eventType;
        private TextView eventDescription;
        private TextView eventLocation;
        private ImageView eventIcon;
        private View colorIndicator;
        private CardView eventCard;
        private View dateHeader;
        private TextView dateHeaderText;

        EventViewHolder(View itemView) {
            super(itemView);

            // Initialize views
            eventTitle = itemView.findViewById(R.id.event_title);
            eventTime = itemView.findViewById(R.id.event_time);
            eventType = itemView.findViewById(R.id.event_type);
            eventDescription = itemView.findViewById(R.id.event_description);
            eventLocation = itemView.findViewById(R.id.event_location);
            eventIcon = itemView.findViewById(R.id.event_icon);
            colorIndicator = itemView.findViewById(R.id.color_indicator);
            eventCard = itemView.findViewById(R.id.event_card);
            dateHeader = itemView.findViewById(R.id.date_header);
            dateHeaderText = itemView.findViewById(R.id.date_header_text);

            // Click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && eventClickListener != null) {
                    eventClickListener.onEventClick(events.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && eventClickListener != null) {
                    eventClickListener.onEventLongClick(events.get(position));
                    return true;
                }
                return false;
            });
        }

        void bind(CalendarEvent event, int position) {
            // Bind basic event info
            eventTitle.setText(event.getTitle());
            eventTime.setText(event.getDuration());
            eventType.setText(event.getType().getDisplayName());

            // Set event icon
            eventIcon.setImageResource(event.getType().getIconRes());

            // Set color indicator
            colorIndicator.setBackgroundColor(event.getColor());

            // Bind optional fields (show/hide based on content)
            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                eventDescription.setText(event.getDescription());
                eventDescription.setVisibility(View.VISIBLE);
            } else {
                eventDescription.setVisibility(View.GONE);
            }

            if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                eventLocation.setText(event.getLocation());
                eventLocation.setVisibility(View.VISIBLE);
            } else {
                eventLocation.setVisibility(View.GONE);
            }

            // Apply card styling based on event type
            applyCardStyling(event);

            // Show date header if enabled and it's the first event or date changed
            if (showDateHeader) {
                boolean showHeader = shouldShowDateHeader(position, event);
                dateHeader.setVisibility(showHeader ? View.VISIBLE : View.GONE);
                if (showHeader) {
                    dateHeaderText.setText(event.getFormattedDate());
                }
            } else {
                dateHeader.setVisibility(View.GONE);
            }
        }

        private void applyCardStyling(CalendarEvent event) {
            // Set card background based on event type
            int cardColor = getCardColor(event.getType());
            eventCard.setCardBackgroundColor(cardColor);

            // Make text visible based on background brightness
            setTextColorsBasedOnBackground(cardColor);

            // Add elevation for important events
            if (event.getType() == CalendarEvent.EventType.MEETING ||
                    event.getType() == CalendarEvent.EventType.APPOINTMENT) {
                eventCard.setCardElevation(4f);
            } else {
                eventCard.setCardElevation(2f);
            }
        }

        private int getCardColor(CalendarEvent.EventType type) {
            // Return different background colors based on event type
            switch (type) {
                case MEETING:
                    return Color.parseColor("#E8F5E8"); // Light green
                case APPOINTMENT:
                    return Color.parseColor("#E3F2FD"); // Light blue
                case PERSONAL:
                    return Color.parseColor("#FFF3E0"); // Light orange
                case BIRTHDAY:
                    return Color.parseColor("#FCE4EC"); // Light pink
                case REMINDER:
                    return Color.parseColor("#F3E5F5"); // Light purple
                case WORK:
                    return Color.parseColor("#E0F2F1"); // Light teal
                case HEALTH:
                    return Color.parseColor("#FFEBEE"); // Light red
                default:
                    return Color.WHITE;
            }
        }

        private void setTextColorsBasedOnBackground(int backgroundColor) {
            // Simple brightness calculation
            double brightness = Color.red(backgroundColor) * 0.299 +
                    Color.green(backgroundColor) * 0.587 +
                    Color.blue(backgroundColor) * 0.114;

            int textColor = brightness > 186 ? Color.BLACK : Color.WHITE;
            int secondaryTextColor = brightness > 186 ?
                    Color.parseColor("#666666") : Color.parseColor("#CCCCCC");

            eventTitle.setTextColor(textColor);
            eventTime.setTextColor(secondaryTextColor);
            eventType.setTextColor(secondaryTextColor);
            eventDescription.setTextColor(secondaryTextColor);
            eventLocation.setTextColor(secondaryTextColor);
        }

        private boolean shouldShowDateHeader(int position, CalendarEvent currentEvent) {
            if (position == 0) {
                return true; // Always show header for first event
            }

            // Show header if this event is on a different day than previous event
            CalendarEvent previousEvent = events.get(position - 1);
            return !isSameDay(currentEvent.getTimeInMillis(), previousEvent.getTimeInMillis());
        }

        private boolean isSameDay(long time1, long time2) {
            java.util.Calendar cal1 = java.util.Calendar.getInstance();
            java.util.Calendar cal2 = java.util.Calendar.getInstance();
            cal1.setTimeInMillis(time1);
            cal2.setTimeInMillis(time2);
            return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                    cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH) &&
                    cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH);
        }
    }
}