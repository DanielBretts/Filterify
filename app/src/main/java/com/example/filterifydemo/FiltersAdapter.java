package com.example.filterifydemo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FiltersAdapter extends RecyclerView.Adapter<FiltersAdapter.StringViewHolder> {
    private List<String> stringList;
    public static List<String> selected = new ArrayList<>();


    public FiltersAdapter(List<String> stringList) {
        this.stringList = stringList;
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_checkbox, parent, false);
        return new StringViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StringViewHolder holder, int position) {
        holder.bind(stringList.get(position));
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }


    static class StringViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;

        public StringViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
        }

        public void bind(String string) {
            checkBox.setText(string);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        selected.add(string);
                    }else{
                        selected.remove(string);
                    }
                    Log.d("RV",selected.toString());
                }
            });
        }
    }
}
