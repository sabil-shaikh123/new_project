package com.example.attendenceapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Student extends AppCompatActivity {
    private TextView tvStudentName, tvStudentUSN;
    private FirebaseFirestore db;
    private FirebaseAuth auth; // Declare FirebaseAuth instance
    private String studentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize views
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentUSN = findViewById(R.id.tvStudentUSN);


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the student email from intent
        String studentEmail = getIntent().getStringExtra("email");

        // Check if the email is not null or empty
        if (studentEmail != null && !studentEmail.isEmpty()) {
            // Query Firestore for the document where the email matches
            db.collection("Student").whereEqualTo("email", studentEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    // Now you have access to the document where the email matches
                                    String studentName = document.getString("name");
                                    String studentUSN = document.getId(); // The document ID is the USN

                                    // Display the name and USN
                                    tvStudentName.setText(studentName);
                                    tvStudentUSN.setText(studentUSN);
                                }
                            } else {
                                Toast.makeText(Student.this, "No student found with this email", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Student.this, "Error fetching student details", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(Student.this, "Error fetching data", Toast.LENGTH_SHORT).show());
        } else {
            // Show error if email is not provided
            Toast.makeText(Student.this, "Student email not provided", Toast.LENGTH_SHORT).show();
        }
    }
}