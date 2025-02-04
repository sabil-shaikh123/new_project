package com.example.attendenceapp;
//subject adapter in the teacher activity to make the subject onclick and the move to specific subject

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {



    private Context context; // To start the new intent

    // Constructor
    private List<Map<String, String>> subjectList; // Update the type of subjectList

    public SubjectAdapter(Context context, List<Map<String, String>> subjectList) {
        this.context = context;
        this.subjectList = subjectList;
    }


    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Map<String, String> subject = subjectList.get(position); // Get the subject map
        String subjectName = subject.get("name"); // Get the subject name
        String subjectCode = subject.get("code"); // Get the subject code

        holder.textViewSubjectName.setText(subjectName);

        // Set OnClickListener for the item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SubjectDetailsActivity.class);
            intent.putExtra("subjectName", subjectName); // Pass the subject name to the new activity
            intent.putExtra("subjectCode", subjectCode); // Pass the subject code to the new activity
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSubjectName;

        SubjectViewHolder(View itemView) {
            super(itemView);
            textViewSubjectName = itemView.findViewById(R.id.textViewSubjectName);
        }
    }
}
