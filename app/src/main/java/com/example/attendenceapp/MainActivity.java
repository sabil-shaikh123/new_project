package com.example.attendenceapp;

import static android.service.autofill.Validators.and;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    EditText txtemail, txtpass;
    Button login , create_acc;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //to make a instance of firebase
        auth = FirebaseAuth.getInstance();
        txtemail = findViewById(R.id.txtemail);
        txtpass = findViewById(R.id.txtpass);
        login = findViewById(R.id.btnlog);
        create_acc = findViewById(R.id.createacc);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtemail.getText().toString();
                String pass = txtpass.getText().toString();

                // Check if email and password fields are not empty and valid
                if (validateFields(email, pass) ) {

                    checkUserRole(email); // Perform the login
                }
            }
        });
        create_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, create_acc.class);
                startActivity(intent);
            }
        });

    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int my_item = item.getItemId();

        if (my_item == R.id.appinfo) {


        }
        if (my_item == R.id.forpass) {


        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //to check weather the entered altributes are valid or not
    private boolean validateFields(String email, String pass) {
        if (TextUtils.isEmpty(email)) {
            txtemail.setError("Enter your email");
            txtemail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtemail.setError("Enter a valid email");
            txtemail.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(pass)) {
            txtpass.setError("Enter your password");
            txtpass.requestFocus();
            return false;
        }
        return true;

    }

    private void checkUserRole(String email) {
        String pass = txtpass.getText().toString();

        // First, authenticate the user with Firebase Authentication
        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // Email is verified, proceed to check role
                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                // Check in "Teacher" collection
                                db.collection("Teacher").document(email)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful() && task1.getResult().exists()) {
                                                // User is a teacher, navigate to Teacher page
                                                String teacherEmail = txtemail.getText().toString();
                                                Toast.makeText(MainActivity.this, "Login as Teacher", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(MainActivity.this, Teacher.class); // Replace with your Teacher activity
                                                intent.putExtra("email", teacherEmail);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // If not in Teacher collection, check in Student collection
                                                db.collection("Student").document(email)
                                                        .get()
                                                        .addOnCompleteListener(task2 -> {
                                                            if (task2.isSuccessful() && task2.getResult().exists()) {
                                                                // User is a student, navigate to Student page
                                                                String studentEmail = txtemail.getText().toString();
                                                                Toast.makeText(MainActivity.this, "Login as Student", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(MainActivity.this, Student.class); // Replace with your Student activity
                                                                intent.putExtra("email", studentEmail);
                                                                startActivity(intent);
                                                                finish();
                                                            } else {
                                                                // Email not found in either collection
                                                                Toast.makeText(MainActivity.this, "Error: User not found in records", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        });
                            } else if (user != null && !user.isEmailVerified()) {
                                // Email is not verified
                                Toast.makeText(MainActivity.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                auth.signOut(); // Log out the unverified user
                            } else {
                                // User object is null (shouldn't happen after successful authentication)
                                Toast.makeText(MainActivity.this, "Unexpected error. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Authentication failed (e.g., wrong password)
                            Toast.makeText(MainActivity.this, "Error: Incorrect email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}