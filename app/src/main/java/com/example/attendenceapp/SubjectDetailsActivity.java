package com.example.attendenceapp;
//this class is for the perticular subject in the teacher
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.WriterException;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

interface PresentStudentsCallback {
    void onPresentStudentsFetched(List<String> presentStudents);
}

public class SubjectDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String subjectId;
    private ImageView imageViewQRCode;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subject_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView textViewSubjectTitle = findViewById(R.id.textViewSubjectTitle);
        Button buttonGenerateQRCode = findViewById(R.id.buttonGenerateQRCode);
        imageViewQRCode = findViewById(R.id.imageViewQRCode);
        Button buttonCloseQRCode = findViewById(R.id.buttonCloseQRCode);
        Button take_attend = findViewById(R.id.Take_attend);
        Button end_attend= findViewById(R.id.End_attend);
        db = FirebaseFirestore.getInstance();

        // Get the subject name and ID from the intent
        String subjectName = getIntent().getStringExtra("subjectName");
        subjectId = getIntent().getStringExtra("subjectCode");
        Toast.makeText(SubjectDetailsActivity.this, "subject id is "+subjectId.toString(), Toast.LENGTH_SHORT).show();
        // Set the subject name as the title
        if (subjectName != null) {
            textViewSubjectTitle.setText(subjectName);
        }

        // Set click listener for QR code generation
        buttonGenerateQRCode.setOnClickListener(v -> {
            generateAndSaveQRCode(false);
            take_attend.setVisibility(View.GONE);
            buttonCloseQRCode.setVisibility(View.VISIBLE);
            buttonGenerateQRCode.setVisibility(View.GONE);
        });

        take_attend.setOnClickListener(v ->{
            createAttendanceField();
            generateAndSaveQRCode(true);
            buttonGenerateQRCode.setVisibility(View.GONE);
            take_attend.setVisibility(View.GONE);
            end_attend.setVisibility(View.VISIBLE);
        });
        end_attend.setOnClickListener(v -> {
            buttonGenerateQRCode.setVisibility(View.VISIBLE);
            imageViewQRCode.setVisibility(View.GONE);
            take_attend.setVisibility(View.VISIBLE);
            end_attend.setVisibility(View.GONE);

            LocalDate currentDate1 = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String currentDate = currentDate1.format(formatter);


            db.collection("Subjects").document(subjectId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> enrolledStudents = (List<String>) documentSnapshot.get("students");
                            if (enrolledStudents == null) {
                                enrolledStudents = new ArrayList<>();
                            }

                            // Fetch present students asynchronously
                            List<String> finalEnrolledStudents1 = enrolledStudents;
                            getPresentStudents(subjectId, currentDate, presentStudents -> {
                                List<String> absentStudents = new ArrayList<>();
                                for (String student : finalEnrolledStudents1) {
                                    if (!presentStudents.contains(student)) {
                                        absentStudents.add(student);
                                    }
                                }

                                // Get or initialize attendance data
                                Map<String, Object> attendanceMap = (Map<String, Object>) documentSnapshot.get("attendance");
                                if (attendanceMap == null) {
                                    attendanceMap = new HashMap<>();
                                }

                                Map<String, Object> currentDateAttendance = (Map<String, Object>) attendanceMap.get(currentDate);
                                if (currentDateAttendance == null) {
                                    currentDateAttendance = new HashMap<>();
                                }

                                // Update the absent list
                                List<String> existingAbsent = (List<String>) currentDateAttendance.get("absent");
                                if (existingAbsent == null) {
                                    existingAbsent = new ArrayList<>();
                                }
                                existingAbsent.addAll(absentStudents);
                                currentDateAttendance.put("absent", existingAbsent);
                                attendanceMap.put(currentDate, currentDateAttendance);

                                // Update Firestore
                                db.collection("Subjects").document(subjectId)
                                        .update("attendance", attendanceMap)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(SubjectDetailsActivity.this, "Absent list updated successfully!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(SubjectDetailsActivity.this, "Failed to update absent list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            });
                        } else {
                            Toast.makeText(SubjectDetailsActivity.this, "Subject document not found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SubjectDetailsActivity.this, "Failed to retrieve subject data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // When Close QR Code is clicked
        buttonCloseQRCode.setOnClickListener(v -> {
            take_attend.setVisibility(View.VISIBLE);
            imageViewQRCode.setVisibility(View.GONE);  // Hide the QR code
            buttonCloseQRCode.setVisibility(View.GONE);  // Hide the Close button
            buttonGenerateQRCode.setVisibility(View.VISIBLE);  // Show the Generate button again
        });
    }
    public void getPresentStudents(String subjectId, String currentDate, PresentStudentsCallback callback) {
        db.collection("Subjects").document(subjectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> presentStudents = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        Map<String, Object> attendanceMap = (Map<String, Object>) documentSnapshot.get("attendance");
                        if (attendanceMap != null) {
                            Map<String, Object> dateAttendance = (Map<String, Object>) attendanceMap.get(currentDate);
                            if (dateAttendance != null) {
                                List<String> fetchedPresentStudents = (List<String>) dateAttendance.get("present");
                                if (fetchedPresentStudents != null) {
                                    presentStudents.addAll(fetchedPresentStudents);
                                }
                            }
                        }
                    }
                    callback.onPresentStudentsFetched(presentStudents);  // Call the callback with fetched data
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch present students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onPresentStudentsFetched(new ArrayList<>());  // Return empty list on failure
                });
    }


    private void generateAndSaveQRCode(boolean type) {
        if (subjectId == null) {
            Toast.makeText(this, "Subject ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique QR code
        String uniqueQRCode = UUID.randomUUID().toString(); // Random session ID
        String qrCodeData = subjectId + "_" + uniqueQRCode;

        // Generate the QR code bitmap
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 200, 200);
            Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);

            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            // Display the QR code in the ImageView
            imageViewQRCode.setImageBitmap(bitmap);
            imageViewQRCode.setVisibility(ImageView.VISIBLE);

            // Save the QR code session to Firestore
            saveQRCodeToFirestore(qrCodeData,type);

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQRCodeToFirestore(String uniqueQRCode, boolean isForA) {
        String fieldToUpdate = isForA ? "qr_code_A" : "qr_code_S";

        db.collection("Subjects").document(subjectId)
                .update(fieldToUpdate, uniqueQRCode)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "QR Code saved to Firestore in " + fieldToUpdate + ".", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void createAttendanceField() {
        if (subjectId == null) {
            Toast.makeText(this, "Subject ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current date in YYYY-MM-DD format
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Create the attendance map structure
        Map<String, Object> attendanceMap = new HashMap<>();

        // Present and absent maps
        Map<String, ArrayList<String>> presentAbsentMap = new HashMap<>();
        presentAbsentMap.put("present", new ArrayList<>()); // Empty list for present students
        presentAbsentMap.put("absent", new ArrayList<>());  // Empty list for absent students

        // Add to the main attendance map with the current date as the key
        attendanceMap.put(currentDate, presentAbsentMap);

        // Update Firestore with the attendance map
        db.collection("Subjects").document(subjectId)
                .update("attendance." + currentDate, presentAbsentMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Attendance field created successfully.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create attendance field: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}