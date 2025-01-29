package com.example.attendenceapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.WriterException;


import java.util.UUID;

public class SubjectDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String subjectId;
    private ImageView imageViewQRCode;
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
        db = FirebaseFirestore.getInstance();

        // Get the subject name and ID from the intent
        String subjectName = getIntent().getStringExtra("subjectName");
        subjectId = getIntent().getStringExtra("subjectCode");

        // Set the subject name as the title
        if (subjectName != null) {
            textViewSubjectTitle.setText(subjectName);
        }

        // Set click listener for QR code generation
        buttonGenerateQRCode.setOnClickListener(v -> {
            generateAndSaveQRCode();
            buttonCloseQRCode.setVisibility(View.VISIBLE);
            buttonGenerateQRCode.setVisibility(View.GONE);
        });


        // When Close QR Code is clicked
        buttonCloseQRCode.setOnClickListener(v -> {
            imageViewQRCode.setVisibility(View.GONE);  // Hide the QR code
            buttonCloseQRCode.setVisibility(View.GONE);  // Hide the Close button
            buttonGenerateQRCode.setVisibility(View.VISIBLE);  // Show the Generate button again
        });
    }

    private void generateAndSaveQRCode() {
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
            saveQRCodeToFirestore(uniqueQRCode);

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQRCodeToFirestore(String uniqueQRCode) {
        db.collection("Subjects").document(subjectId)
                .update("qr_code", uniqueQRCode)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "QR Code saved to Firestore.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}