package com.chzu.ngRobot_csj.chat;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chzu.ngRobot_csj.R;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.Holder> {
    private final List<ChatMessage> data = new ArrayList<>();

    public void submit(List<ChatMessage> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void add(ChatMessage msg) {
        data.add(msg);
        notifyItemInserted(data.size() - 1);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public List<ChatMessage> snapshot() {
        return new ArrayList<>(data);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChatMessage msg = data.get(position);
        holder.tvText.setText(msg.text);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) holder.bubble.getLayoutParams();
        if (msg.role == ChatMessage.ROLE_USER) {
            lp.gravity = Gravity.END;
            holder.bubble.setBackgroundResource(R.drawable.bg_chat_user);
            holder.tvRole.setText("我");
            holder.tvRole.setTextColor(Color.parseColor("#1565C0"));
        } else if (msg.role == ChatMessage.ROLE_BOT) {
            lp.gravity = Gravity.START;
            holder.bubble.setBackgroundResource(R.drawable.bg_chat_bot);
            holder.tvRole.setText("机器人");
            holder.tvRole.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            holder.bubble.setBackgroundResource(R.drawable.bg_chat_system);
            holder.tvRole.setText("系统");
            holder.tvRole.setTextColor(Color.parseColor("#6D4C41"));
        }
        holder.bubble.setLayoutParams(lp);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final LinearLayout bubble;
        final TextView tvRole;
        final TextView tvText;

        Holder(@NonNull View itemView) {
            super(itemView);
            bubble = itemView.findViewById(R.id.bubble);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvText = itemView.findViewById(R.id.tv_text);
        }
    }
}