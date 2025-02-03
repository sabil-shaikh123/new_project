package com.example.attendenceapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>{
    private List<SubjectDetails> subjectDetailsList;

    public SubjectsAdapter(List<SubjectDetails> subjectDetailsList) {
        this.subjectDetailsList = subjectDetailsList;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject1, parent, false);
        return new SubjectsAdapter.SubjectViewHolder(view); // Correct class name
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        SubjectDetails subject = subjectDetailsList.get(position);
        holder.tvSubjectName.setText(subject.getSubjectName());
        holder.tvTotalClasses.setText("A:"+String.valueOf(subject.getAttendedClasses()));
        holder.tvAttendedClasses.setText("T:"+String.valueOf(subject.getTotalClasses()));
        holder.tvAttendancePercentage.setText(String.format(Locale.getDefault(), "%.2f%%", subject.getAttendancePercentage()));
    }

    @Override
    public int getItemCount() {
        return subjectDetailsList.size();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvTotalClasses, tvAttendedClasses, tvAttendancePercentage;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvTotalClasses = itemView.findViewById(R.id.tvTotalClasses);
            tvAttendedClasses = itemView.findViewById(R.id.tvAttendedClasses);
            tvAttendancePercentage = itemView.findViewById(R.id.tvAttendancePercentage);
        }
    }
}


