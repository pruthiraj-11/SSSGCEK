package com.example.sspgcek.Util;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.sspgcek.Models.ChatsModel;

import java.util.ArrayList;

public class RecyclerDiffUtilCallback extends DiffUtil.Callback {
    private final ArrayList<ChatsModel> oldList;
    private final ArrayList<ChatsModel> newList;

    public RecyclerDiffUtilCallback(ArrayList<ChatsModel> oldList, ArrayList<ChatsModel> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldItemPosition==newItemPosition;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition)==newList.get(newItemPosition);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
