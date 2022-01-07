package com.example.quizadminapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<String> catList;
    public CategoryAdapter(List<String> catList) {
        this.catList = catList;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i ) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cat_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder ViewHolder, int pos) {
        String title = catList.get(pos);

        ViewHolder.setData(title);
    }

    @Override
    public int getItemCount() {
        return catList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView catName;
        private ImageView deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            catName =  itemView.findViewById(R.id.catName);
            deleteButton = itemView.findViewById(R.id.catDelB);
        }

        private void setData(String title) {
            catName.setText(title);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }

    }
}