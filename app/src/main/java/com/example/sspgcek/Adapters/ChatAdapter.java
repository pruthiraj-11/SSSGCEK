package com.example.sspgcek.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.sspgcek.Models.ChatsModel;
import com.example.sspgcek.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

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
            case 1:
                view= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_msg_item,parent,false);
                return new UserViewHolder(view);
            case 2:
                view=LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_msg_item,parent,false);
                return new BotViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if(list.get(position).getMSG_TYPE().equals("user")) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatsModel chatsModel=list.get(position);
        if(holder.getClass()==UserViewHolder.class){
            ((UserViewHolder)holder).senderMsg.setText(chatsModel.getMsg());
            ((UserViewHolder)holder).senderTime.setText(chatsModel.getSentTime());
        }
        else{
            ((BotViewHolder)holder).lottieAnimationView.setVisibility(View.INVISIBLE);
            ((BotViewHolder)holder).constraintLayout.setVisibility(View.VISIBLE);
            ((BotViewHolder)holder).textView1.setText(chatsModel.getMsg());
            ((BotViewHolder)holder).textView2.setText(chatsModel.getSentTime());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        TextView senderMsg,senderTime;
        RelativeLayout relativeLayout;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
//            itemView.setLongClickable(true);
            senderMsg=itemView.findViewById(R.id.userinput);
            senderTime=itemView.findViewById(R.id.sendtime);
            relativeLayout=itemView.findViewById(R.id.cardView2);
            relativeLayout.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//            menu.setHeaderTitle("Select any one");
            menu.add(getAdapterPosition(),102,0,"Delete");
        }
    }

    public static class BotViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        TextView textView1,textView2;
        ConstraintLayout constraintLayout;
        private LottieAnimationView lottieAnimationView;
        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
//            itemView.setLongClickable(true);
            textView1=itemView.findViewById(R.id.botresponse);
            textView2=itemView.findViewById(R.id.bottime);
            lottieAnimationView=itemView.findViewById(R.id.animation_view);
            constraintLayout=itemView.findViewById(R.id.main_layout);
            constraintLayout.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//            menu.setHeaderTitle("Select any one");
            menu.add(getAdapterPosition(),101,0,"Share");
            menu.add(getAdapterPosition(),102,1,"Delete");
        }
    }

    public void removeItem(int position) {
        String msgid= list.get(position).getMsgid();
        list.remove(position);
        notifyItemRemoved(position);
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        String id= Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        database.getReference().child("Users").child(id).child("Chats").child(msgid).setValue(null);
    }

    public void shareText(int position) {
        String sharebody = list.get(position).getMsg();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.putExtra(Intent.EXTRA_TEXT, sharebody);
        context.startActivity(Intent.createChooser(intent, "Share text via").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}