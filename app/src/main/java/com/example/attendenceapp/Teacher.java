package com.example.attendenceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

public class Teacher extends AppCompatActivity {

    private static final int REQUEST_ADD_SUBJECT = 1;
    private TextView tvTeacherName, tvTeacherUSN;
    Button cr_sub, refresh;
    private FirebaseFirestore db;

    //
    private RecyclerView recyclerViewSubjects;
    private SubjectAdapter subjectAdapter;
    private List<String> subjectList;
    private String teacherEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvTeacherUSN = findViewById(R.id.tvTeacherUSN);
        cr_sub = findViewById(R.id.buttonAddSubject);
        refresh = findViewById(R.id.refresh);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        //
        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);
        recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));
        subjectList = new ArrayList<>();
        subjectAdapter = new SubjectAdapter(subjectList);
        recyclerViewSubjects.setAdapter(subjectAdapter);

        // Get the teacher email from intent
        teacherEmail = getIntent().getStringExtra("email");

        cr_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Teacher.this, add_subject.class);
                intent.putExtra("teacherEmail", teacherEmail);  // Pass the teacher's email
                startActivityForResult(intent, REQUEST_ADD_SUBJECT);
                fetchSubjects(teacherEmail);
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchSubjects(teacherEmail);
            }
        });

        // Check if the email is not null or empty
        if (teacherEmail != null && !teacherEmail.isEmpty()) {
            fetchSubjects(teacherEmail);  // Load subjects initially
            // Query Firestore for the document where the email matches
            db.collection("Teacher").whereEqualTo("email", teacherEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    // Now you have access to the document where the email matches
                                    String teacherName = document.getString("name");
                                    String teacherUSN = document.getId(); // The document ID is the USN

                                    // Display the name and USN
                                    tvTeacherName.setText(teacherName);
                                    tvTeacherUSN.setText(teacherUSN);
                                }
                            } else {
                                Toast.makeText(Teacher.this, "No teacher found with this email", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Teacher.this, "Error fetching teacher details", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(Teacher.this, "Error fetching data", Toast.LENGTH_SHORT).show());
        } else {
            // Show error if email is not provided
            Toast.makeText(Teacher.this, "Teacher email not provided", Toast.LENGTH_SHORT).show();
        }

    }


    private void fetchSubjects(String teacherEmail) {
        // Query to find the document by teacher email
        db.collection("Teacher")
                .whereEqualTo("email", teacherEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);

                        // Check if "subjects" field exists and is an array
                        if (document.contains("subjects")) {
                            List<String> subjects = (List<String>) document.get("subjects");
                            if (subjects != null) {
                                subjectList.clear();
                                subjectList.addAll(subjects);
                                subjectAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        Toast.makeText(Teacher.this, "Failed to fetch subjects", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(Teacher.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_SUBJECT && resultCode == RESULT_OK) {
            // Refresh subjects after returning from add_subject
            fetchSubjects(teacherEmail);
        }
    }



}
