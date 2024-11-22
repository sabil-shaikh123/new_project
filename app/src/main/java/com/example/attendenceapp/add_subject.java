package com.example.attendenceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class add_subject extends AppCompatActivity {

    private EditText editTextSubjectName, editTextSubjectCode;
    private Button buttonSaveSubject;
    private FirebaseFirestore db;
    private String teacherEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_subject);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get teacher's email from the intent
        teacherEmail = getIntent().getStringExtra("teacherEmail");

        // Initialize views
        editTextSubjectName = findViewById(R.id.editTextSubjectName);
        editTextSubjectCode = findViewById(R.id.editTextSubjectCode);
        buttonSaveSubject = findViewById(R.id.buttonSaveSubject);

        // Set onClickListener for save button
        buttonSaveSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSubjectToFirestore();
            }
        });
    }

    private void saveSubjectToFirestore() {
        // Get input data
        String subjectName = editTextSubjectName.getText().toString().trim();
        String subjectCode = editTextSubjectCode.getText().toString().trim();

        if (subjectName.isEmpty() || subjectCode.isEmpty() || teacherEmail == null) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a subject data map
        Map<String, Object> subject = new HashMap<>();
        subject.put("subject_name", subjectName);
        subject.put("teacher_email", teacherEmail);
        subject.put("students", new ArrayList<>());  // Initialize students list if needed

        // Add subject to Firestore in the "Subjects" collection using subjectCode as the document ID
        db.collection("Subjects")
                .document(subjectCode)
                .set(subject)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(add_subject.this, "Subject added successfully", Toast.LENGTH_SHORT).show();

                        // Now, add the subject name to the teacher's subject list
                        addSubjectToTeacher(subjectName);


                        finish();  // Close this activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(add_subject.this, "Error adding subject: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }




    private void addSubjectToTeacher(String subjectName) {
        // Query the teacher document where email matches the provided email
        db.collection("Teacher")
                .whereEqualTo("email", teacherEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document matching the email
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String documentId = documentSnapshot.getId();

                        // Update the subjects array in the found document
                        db.collection("Teacher").document(documentId)
                                .update("subjects", FieldValue.arrayUnion(subjectName))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(add_subject.this, "Subject added to teacher's list.", Toast.LENGTH_SHORT).show();
                                    // Inside add_subject.java after successfully adding the subject
                                    Intent resultIntent = new Intent();
                                    setResult(RESULT_OK, resultIntent);
                                    finish();

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(add_subject.this, "Error updating teacher's subjects: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(add_subject.this, "Teacher with given email not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(add_subject.this, "Error fetching teacher document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}