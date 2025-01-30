package com.example.eventapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.example.eventapp.model.Event;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private final Context context;
    private final List<Event> events;
    private final OnEventClickListener listener;
    private final SimpleDateFormat dateFormat;
    private int lastPosition = -1;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(Context context, List<Event> events, OnEventClickListener listener) {
        this.context = context;
        this.events = events;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        // 设置基本信息
        holder.titleText.setText(event.getTitle());
        holder.descriptionText.setText(event.getDescription());
        holder.locationText.setText(event.getLocation());
        holder.timeText.setText(String.format("%s - %s",
                dateFormat.format(new Date(event.getStartTime())),
                dateFormat.format(new Date(event.getEndTime()))));
        holder.participantsText.setText(String.format("%d/%d",
                event.getCurrentParticipants(),
                event.getMaxParticipants()));

        // 设置状态标签
        updateEventStatus(holder, event);

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });

        // 添加动画效果
        setAnimation(holder.itemView, position);
    }

    private void updateEventStatus(EventViewHolder holder, Event event) {
        long currentTime = System.currentTimeMillis();
        boolean isFull = event.getCurrentParticipants() >= event.getMaxParticipants();

        if (currentTime < event.getStartTime()) {
            // Not started
            if (isFull) {
                holder.statusLabel.setText(context.getString(R.string.event_full));
                holder.statusLabel.setBackgroundColor(ContextCompat.getColor(context, R.color.warning));
                holder.statusIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            } else {
                holder.statusLabel.setText(context.getString(R.string.event_not_started));
                holder.statusLabel.setBackgroundColor(ContextCompat.getColor(context, R.color.primary));
                holder.statusIcon.setImageResource(android.R.drawable.ic_menu_today);
            }
        } else if (currentTime <= event.getEndTime()) {
            // Ongoing
            holder.statusLabel.setText(context.getString(R.string.event_ongoing));
            holder.statusLabel.setBackgroundColor(ContextCompat.getColor(context, R.color.success));
            holder.statusIcon.setImageResource(android.R.drawable.ic_menu_send);
        } else {
            // Ended
            holder.statusLabel.setText(context.getString(R.string.event_ended));
            holder.statusLabel.setBackgroundColor(ContextCompat.getColor(context, R.color.text_tertiary));
            holder.statusIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            viewToAnimate.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull EventViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public void updateEvents(List<Event> newEvents) {
        this.events.clear();
        this.events.addAll(newEvents);
        notifyDataSetChanged();
        lastPosition = -1; // 重置动画位置
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView locationText;
        TextView timeText;
        TextView participantsText;
        TextView statusLabel;
        ImageView statusIcon;
        MaterialButton actionButton;

        EventViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            locationText = itemView.findViewById(R.id.locationText);
            timeText = itemView.findViewById(R.id.timeText);
            participantsText = itemView.findViewById(R.id.participantsText);
            statusLabel = itemView.findViewById(R.id.statusLabel);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }
}