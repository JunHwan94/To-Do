package com.polarbearr.todo.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.polarbearr.todo.R;

import java.util.ArrayList;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder>{
    static Context context;
    List<TodoItem> items = new ArrayList<>();

    OnItemClickListener listener;

    private static final String TITLE = "title";
    private static final String CONTENT = "content";

    public static interface OnItemClickListener{
        public void onItemClick(ViewHolder holder, View view, int position);
    }

    public TodoAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.todo_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoItem item = items.get(position);
        holder.setItem(item);

        holder.setOnItemClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(TodoItem item){
        items.add(item);
    }

    public void addItems(List<TodoItem> items){
        this.items = items;
    }

    public TodoItem getItem(int position){
        return items.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle;
        TextView tvContent;
        TextView tvDate;

        OnItemClickListener listener;

        public ViewHolder(View itemView){
            super(itemView);

            tvTitle = itemView.findViewById(R.id.title);
            tvContent = itemView.findViewById(R.id.content);
            tvDate = itemView.findViewById(R.id.date);

            itemView.setOnClickListener(view -> {
                    int position = getAdapterPosition();

                    if(listener != null){
                        listener.onItemClick(ViewHolder.this, view, position);
                    }
                }
            );

            DisplayMetrics metrics = getMetrics(context);
            setViewSize(metrics, tvTitle, TITLE);
            setViewSize(metrics, tvContent, CONTENT);
        }

        public void setItem(TodoItem item){
            tvTitle.setText(item.getTitle());
            tvContent.setText(item.getContent());
            tvDate.setText(item.getDate());
        }

        public void setOnItemClickListener(OnItemClickListener listener){
            this.listener = listener;
        }

        public static DisplayMetrics getMetrics(Context context){
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);
            return metrics;
        }

        public static void setViewSize(DisplayMetrics metrics, View view, String type){
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)view.getLayoutParams();
            switch(type){
                case TITLE:
                    params.width = metrics.widthPixels * 9 / 10;
                    break;
                case CONTENT:
                    params.width = metrics.widthPixels * 2 / 5;
                    break;
            }

            view.setLayoutParams(params);
        }
    }
}

