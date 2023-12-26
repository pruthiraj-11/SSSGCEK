package com.example.sspgcek.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sspgcek.Models.ChatsModel;
import com.example.sspgcek.R;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter {

    ArrayList<ChatsModel> list=new ArrayList<>();
    Context context;

    public ChatAdapter(ArrayList<ChatsModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case 0:
                view= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_msg_item,parent,false);
                return new UserViewHolder(view);
            case 1:
                view=LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_msg_item,parent,false);
                return new BotViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatsModel chatsModel=list.get(position);
        switch (chatsModel.getMSG_TYPE()){
            case "user":
                ((UserViewHolder)holder).textView.setText(chatsModel.getMsg());
                ((UserViewHolder)holder).textView2.setText(chatsModel.getSentTime());
                break;
            case "bot":
                ((BotViewHolder)holder).textView1.setText(chatsModel.getMsg());
                ((BotViewHolder)holder).textView2.setText(chatsModel.getSentTime());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        TextView textView,textView2;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textView=itemView.findViewById(R.id.userinput);
            textView2=itemView.findViewById(R.id.sendtime);
        }
    }

    public static class BotViewHolder extends RecyclerView.ViewHolder{
        TextView textView1,textView2;
        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
            textView1=itemView.findViewById(R.id.botresponse);
            textView2=itemView.findViewById(R.id.bottime);
        }
    }
}