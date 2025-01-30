package com.example.attendenceapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

import java.util.List;

public class Student extends AppCompatActivity {
    private TextView tvStudentName, tvStudentUSN;
    private FirebaseFirestore db;
    private FirebaseAuth auth; // Declare FirebaseAuth instance
    private String studentEmail;
    private BarcodeView barcodeView;
    @SuppressLint("MissingInflatedId")
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
        barcodeView = findViewById(R.id.barcode_scanner);
        Button btnScan = findViewById(R.id.btnScan);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request Camera Permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            // Initialize Scanner if Permission is Already Granted
            initializeScanner();
        }

        // Start scanning when the button is clicked
        // Set button click listener
        // Set button click listener
        btnScan.setOnClickListener(v -> {
            if (barcodeView.getVisibility() == BarcodeView.GONE) {
                // Check for Camera Permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    // Make the button invisible
                    btnScan.setVisibility(Button.GONE);

                    // Show the barcode scanner
                    barcodeView.setVisibility(BarcodeView.VISIBLE);
                    barcodeView.decodeContinuous(new BarcodeCallback() {
                        @Override
                        public void barcodeResult(BarcodeResult result) {
                            String scannedQrCode = result.getText();
                            Toast.makeText(Student.this, "Scanned: " + scannedQrCode, Toast.LENGTH_SHORT).show();

                            // Student email (assume this is passed via intent)
                            String studentEmail = getIntent().getStringExtra("email");


                            // Check if both scanned QR code and student email are valid
                            if (scannedQrCode != null && !scannedQrCode.isEmpty() && studentEmail != null && !studentEmail.isEmpty()) {
                                // First, check if the QR code matches the 'qr_code_s' field
                                db.collection("Subjects")
                                        .whereEqualTo("qr_code_S", scannedQrCode)
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                // Found a match for 'qr_code_s'
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    String subjectId = document.getId();

                                                    // Add the student email to the 'students' array field
                                                    db.collection("Subjects").document(subjectId)
                                                            .update("students", FieldValue.arrayUnion(studentEmail))
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(Student.this, "You have been added to the subject successfully!", Toast.LENGTH_SHORT).show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(Student.this, "Failed to add student to subject: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            } else {
                                                // If no match for 'qr_code_s', check 'qr_code_A'
                                                db.collection("Subjects")
                                                        .whereEqualTo("qr_code_A", scannedQrCode)
                                                        .get()
                                                        .addOnCompleteListener(adminTask -> {
                                                            if (adminTask.isSuccessful() && !adminTask.getResult().isEmpty()) {
                                                                // Found a match for 'qr_code_A'
                                                                Toast.makeText(Student.this, "QR Code belongs to admin for attendance marking.", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                // No match for either 'qr_code_s' or 'qr_code_A'
                                                                Toast.makeText(Student.this, "No matching QR code found.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(Student.this, "Error checking QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(Student.this, "Error checking QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                // Invalid QR code or student email
                                Toast.makeText(Student.this, "Invalid QR code or student email.", Toast.LENGTH_SHORT).show();
                            }

                            // Hide the scanner and make the button visible after task completion
                            barcodeView.setVisibility(BarcodeView.GONE);
                            barcodeView.pause();
                            btnScan.setVisibility(Button.VISIBLE); // Make the button visible again
                        }

                        @Override
                        public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
                            // Optional: Highlight QR code points
                        }
                    });

                    // Resume scanning
                    barcodeView.resume();
                }
            } else {
                // If already visible, hide the barcode scanner
                barcodeView.setVisibility(BarcodeView.GONE);
                barcodeView.pause();
                btnScan.setVisibility(Button.VISIBLE); // Ensure button is visible
            }
        });

        // Resume scanning when the activity is resumed
        barcodeView.resume();

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
            db.collection("Student").document(studentEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult(); // Get the DocumentSnapshot
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                // Now you have access to the document where the email matches
                                String studentName = documentSnapshot.getString("name");
                                String studentUSN = documentSnapshot.getString("usn"); // The document ID is the USN

                                // Display the name and USN
                                tvStudentName.setText(studentName);
                                tvStudentUSN.setText(studentUSN);
                            } else {
                                Toast.makeText(Student.this, "No student found with this email", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Student.this, "Error fetching student details", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(Student.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            // Show error if email is not provided
            Toast.makeText(Student.this, "Student email not provided", Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }
    // Handle Permission Request Result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                initializeScanner();
            } else {
                // Permission Denied
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Initialize BarcodeView
    private void initializeScanner() {
        barcodeView = findViewById(R.id.barcode_scanner);
        // Add your barcode scanning logic here
        Toast.makeText(this, "Camera permission granted. Ready to scan.", Toast.LENGTH_SHORT).show();
    }
}