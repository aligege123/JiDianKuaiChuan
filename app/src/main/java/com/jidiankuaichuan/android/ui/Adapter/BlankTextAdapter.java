package com.jidiankuaichuan.android.ui.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jidiankuaichuan.android.R;

import java.util.ArrayList;
import java.util.List;


/**
 * 暂时没用了
 */
public class BlankTextAdapter extends RecyclerView.Adapter<BlankTextAdapter.ViewHolder>{

    private Context context;

    private List<String> textList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView waitText;

        public ViewHolder(View view) {
            super(view);
            waitText = (TextView) view.findViewById(R.id.text_wait);
        }
    }

    public BlankTextAdapter(Context context) {
        this.context = context;
        textList = new ArrayList<>();
        textList.add("等待对方加入...");
    }

    @NonNull
    @Override
    public BlankTextAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blank_text_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String str = textList.get(position);
        holder.waitText.setText(str);
    }

    @Override
    public int getItemCount() {
        return textList.size();
    }
}
