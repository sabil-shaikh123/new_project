package com.example.attendenceapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

public class Teacher extends AppCompatActivity {

    private static final int REQUEST_ADD_SUBJECT = 1;
    private TextView tvTeacherName, tvTeacherUSN;
    Button cr_sub;
    private FirebaseFirestore db;

    //
    private RecyclerView recyclerViewSubjects;
    private SubjectAdapter subjectAdapter;
    private List<Map<String, String>> mappedSubjects; // List of Map to hold both name and code
    private List<String> subjectList;
    private List<String> subjects1;
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


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        subjectList = new ArrayList<>();
        subjects1 = new ArrayList<>();
        mappedSubjects = new ArrayList<>();

        // Ensure both lists are of the same size
        if (subjectList.size() == subjects1.size()) {
            for (int i = 0; i < subjectList.size(); i++) {
                Map<String, String> subjectMap = new HashMap<>();
                subjectMap.put("name", subjectList.get(i));  // Add subject name
                subjectMap.put("code", subjects1.get(i));   // Add subject code
                mappedSubjects.add(subjectMap);
            }
        } else {
            // Handle the case where the lists are not of the same size
            Log.e("MappingError", "Subject lists and codes list are of different sizes.");
        }

        //
        recyclerViewSubjects = findViewById(R.id.recyclerViewSubjects);
        recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));
        subjectList = new ArrayList<>();
        subjectAdapter = new SubjectAdapter(this,mappedSubjects);
        recyclerViewSubjects.setAdapter(subjectAdapter);

        // Get the teacher email from intent
        teacherEmail = getIntent().getStringExtra("email");

        cr_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Teacher.this, add_subject.class);
                intent.putExtra("teacherEmail", teacherEmail);  // Pass the teacher's email
                startActivity(intent);

            }
        });


        // Check if the email is not null or empty and set the text
        if (teacherEmail != null && !teacherEmail.isEmpty()) {
            fetchSubjectsInRealTime1(teacherEmail); // Set up real-time listener for subjects
            // Query Firestore for the document where the email matches
            db.collection("Teacher").document(teacherEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult(); // Get the DocumentSnapshot
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                // Now you have access to the document where the email matches
                                String teacherName = documentSnapshot.getString("name");
                                String teacherUSN = documentSnapshot.getString("usn"); // The document ID is the USN

                                // Display the name and USN
                                tvTeacherName.setText(teacherName);
                                tvTeacherUSN.setText(teacherUSN);
                            } else {
                                Toast.makeText(Teacher.this, "No teacher found with this email", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Teacher.this, "Error fetching teacher details", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(Teacher.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
        else {
            // Show error if email is not provided
            Toast.makeText(Teacher.this, "Teacher email not provided", Toast.LENGTH_SHORT).show();
        }


    }


    private void fetchSubjectsInRealTime1(String teacherEmail) {
        db.collection("Teacher")
                .document(teacherEmail)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(Teacher.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        if (documentSnapshot.contains("subjects")) {
                            subjects1 = (List<String>) documentSnapshot.get("subjects");
                            if (subjects1 != null) {
                                // Clear the existing lists before adding new subjects
                                subjectList.clear();
                                mappedSubjects.clear();  // Make sure mappedSubjects is also cleared

                                final int[] pendingFetches = {subjects1.size()};  // Counter to track pending fetches

                                for (String subjectId : subjects1) {
                                    db.collection("Subjects")
                                            .document(subjectId)
                                            .get()
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        String subjectName = document.getString("subject_name");
                                                        if (subjectName != null) {
                                                            subjectList.add(subjectName);

                                                            // Map the subject name with its corresponding code
                                                            Map<String, String> subjectMap = new HashMap<>();
                                                            subjectMap.put("name", subjectName);
                                                            subjectMap.put("code", subjectId);
                                                            mappedSubjects.add(subjectMap);
                                                        }
                                                    }
                                                } else {
                                                    Log.e("SubjectFetch", "Error getting subject: ", task.getException());
                                                }

                                                // Decrement counter and notify adapter when all fetches are done
                                                pendingFetches[0]--;
                                                if (pendingFetches[0] == 0) {
                                                    subjectAdapter.notifyDataSetChanged();  // Update RecyclerView
                                                }
                                            });
                                }
                            }
                        }
                    } else {
                        Toast.makeText(Teacher.this, "Document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
