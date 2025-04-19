package com.example.myapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Model.Message;
import com.example.myapplication.R;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;

    public MessageAdapter() {
        this.messageList = new ArrayList<>();
    }

    public void addMessage(Message message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    public void removeTypingMessage() {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            if (messageList.get(i).getType() == Message.TYPE_TYPING) {
                messageList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getType();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == Message.TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_user, parent, false);
        } else if (viewType == Message.TYPE_BOT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_bot, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_typing, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.getType() != Message.TYPE_TYPING) {
            holder.tvMessage.setText(message.getContent());
        } else {
            // Áp dụng animation cho tin nhắn "Chatbot đang trả lời..."
            Animation rockAnimation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.rock);
            Animation blinkAnimation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.blink);
            holder.tvMessage.startAnimation(rockAnimation);
            holder.tvMessage.startAnimation(blinkAnimation);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
        }
    }
}