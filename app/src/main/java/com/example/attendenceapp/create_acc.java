package com.example.attendenceapp;

import static android.app.ProgressDialog.show;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class create_acc extends AppCompatActivity {
    EditText txtname, txtusn, txtemail, txtpass, txtcompass;
    Button btnreg;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    CheckBox checkBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_acc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();  // Initialize FirebaseAuth
        firestore = FirebaseFirestore.getInstance();  // Initialize Firestore

        txtname = findViewById(R.id.txtname);
        txtusn = findViewById(R.id.txtusn);
        txtemail = findViewById(R.id.txtemail);
        txtpass = findViewById(R.id.txtpass);
        txtcompass = findViewById(R.id.txtcompass);
        btnreg = findViewById(R.id.btnreg);
        checkBox = findViewById(R.id.checkBox);

        btnreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = txtname.getText().toString();
                String usn = txtusn.getText().toString();
                String email = txtemail.getText().toString();
                String pass = txtpass.getText().toString();
                String compass = txtcompass.getText().toString();

                // Check if all fields are filled and valid
                if (validateFields(name, usn, email, pass, compass)) {
                    // Check if email already exists
                    checkIfEmailExistsInFirestore(email,pass,name,usn);
                     //sendEmailVerification(email,pass,name,usn);
                }
            }
        });
    }

    private boolean validateFields(String name, String usn, String email, String pass, String compass) {
        if (TextUtils.isEmpty(name)) {
            txtname.setError("Enter the full name");
            txtname.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(usn)) {
            txtusn.setError("Enter the USN");
            txtusn.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(email)) {
            txtemail.setError("Enter the email");
            txtemail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtemail.setError("Enter a valid email");
            txtemail.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(pass)) {
            txtpass.setError("Enter the password");
            txtpass.requestFocus();
            return false;
        } else if (pass.length() < 6) {
            txtpass.setError("Password must be at least 6 characters long");
            txtpass.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(compass)) {
            txtcompass.setError("Confirm your password");
            txtcompass.requestFocus();
            return false;
        } else if (!pass.equals(compass)) {
            txtcompass.setError("Passwords do not match");
            txtcompass.requestFocus();
            return false;
        }
        return true;
    }

    private void sendEmailVerification(String email, String pass, String name, String usn) {
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(create_acc.this, "Verification email sent. Please verify and log in.", Toast.LENGTH_LONG).show();
                                                    auth.signOut();

                                                    // Store additional user details in Firestore
                                                    storeUserDetails(name, usn, email);

                                                    // After sending verification email, return to login
                                                    Intent intent = new Intent(create_acc.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(create_acc.this, "Failed to send verification email.", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(create_acc.this, "Registration failed. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void storeUserDetails(String name, String usn, String email) {
        // Create a map to store user details
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("usn", usn);  // Add USN as a field in the document

        user.put("verified", false);  // Mark as false until they verify their email

        String collectionPath = checkBox.isChecked() ? "Teacher" : "Student";

        // Add a subjects array field for teachers
        if ("Teacher".equals(collectionPath)) {
            List<String> subjects = new ArrayList<>();
            user.put("subjects", subjects);
        }

        // Store the user details in Firestore with the unique document ID
        firestore.collection(collectionPath).document(email)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(create_acc.this, "User details stored successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(create_acc.this, "Failed to store user details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerAndStoreUser(String email, String pass, String name, String usn) {
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(create_acc.this, "Verification email sent. Please verify and log in.", Toast.LENGTH_LONG).show();
                                            auth.signOut();
                                            storeUserDetails(name, usn, email);

                                            // Redirect to login
                                            Intent intent = new Intent(create_acc.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(create_acc.this, "Failed to send verification email.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        String error = ((FirebaseAuthException) task.getException()).getErrorCode();
                        Toast.makeText(create_acc.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkIfEmailExistsInFirestore(String email, String pass, String name, String usn) {
        firestore.collection("Student")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(create_acc.this, "Email exists in the Student database.", Toast.LENGTH_LONG).show();
                    } else {
                        firestore.collection("Teacher")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnCompleteListener(teacherTask -> {
                                    if (teacherTask.isSuccessful() && !teacherTask.getResult().isEmpty()) {
                                        Toast.makeText(create_acc.this, "Email exists in the Teacher database.", Toast.LENGTH_LONG).show();
                                    } else {
                                        // If email doesn't exist, proceed with verification and storage
                                        registerAndStoreUser(email, pass, name, usn);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(create_acc.this, "Error checking Teacher database: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(create_acc.this, "Error checking Student database: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


}
