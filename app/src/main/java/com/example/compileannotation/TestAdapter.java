package com.example.compileannotation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.annotation.DBindView;
import com.example.annotation.DClick;
import com.example.annotation.DLongClick;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hongda on 2019-09-10.
 */
public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestHolder> {

    private Context context;
    private List<String> strings;
    private LayoutInflater inflater;

    public TestAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        strings = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            strings.add("吃几个汉堡包 = " + i);
        }

    }

    @NonNull
    @Override
    public TestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item, parent, false);
        return new TestHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestHolder holder, int position) {
        holder.textView.setText(strings.get(position));
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return strings.size();
    }

    class TestHolder extends RecyclerView.ViewHolder {

        @DBindView(R.id.tv_item_test)
        TextView textView;

        public TestHolder(@NonNull View itemView) {
            super(itemView);
            DInject.inject(this, itemView);
        }

        @DClick(R.id.tv_item_test)
        public void onItemClick() {
            Toast.makeText(context, "onItemClick 点击了ietm = " + itemView.getTag(), Toast.LENGTH_SHORT).show();
        }

        @DLongClick(R.id.tv_item_test)
        public void onItemLongClick() {
            Toast.makeText(context, "onItemLongClick 点击了ietm = " + itemView.getTag(), Toast.LENGTH_SHORT).show();
        }

    }

}
