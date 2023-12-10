package com.example.whatsgram;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUp extends AppCompatActivity {


    EditText rg_username, rg_email, rg_password, rg_repassword;
    Button rg_signup, rg_signinbut;
    CircleImageView rg_profileImg;
    Uri imageURI;
    String imageuri;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Establishing The Account");
        progressDialog.setCancelable(false);
        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        rg_signinbut = findViewById(R.id.signinbut);
        rg_username = findViewById(R.id.rgusername);
        rg_email = findViewById(R.id.rgemail);
        rg_password = findViewById(R.id.rgpassword);
        rg_repassword = findViewById(R.id.rgrepassword);
        rg_signup = findViewById(R.id.signup);
        rg_profileImg = findViewById(R.id.profilerg);

        // SignIn Button redirects to Login page
        rg_signinbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        //SignUP Button
        rg_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = rg_username.getText().toString();
                String email = rg_email.getText().toString();
                String password = rg_password.getText().toString();
                String repassword = rg_repassword.getText().toString();
                String status = "Hey I'm Using This Application";

                // All conditions
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repassword)) {
                    progressDialog.dismiss();
                    Toast.makeText(SignUp.this, "Please Enter Valid Informations", Toast.LENGTH_SHORT).show();
                } else if (!email.matches(emailPattern)) {
                    progressDialog.dismiss();
                    rg_email.setError("Enter a Valid Email");
                } else if (password.length() < 6) {
                    progressDialog.dismiss();
                    rg_password.setError("Password must be 6 character or more");
                } else if (!password.equals(repassword)) {
                    progressDialog.dismiss();
                    rg_password.setError("Password Doesn't Matches with re-password");
                } else {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String id = task.getResult().getUser().getUid();
                                DatabaseReference databaseReference = database.getReference().child("user").child(id);
                                StorageReference storageReference = storage.getReference().child("Upload").child(id);

                                if (imageURI != null) {
                                    storageReference.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        imageuri = uri.toString();
                                                        Users users = new Users(id, username, email, password, imageuri, status);
                                                        databaseReference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    progressDialog.show();
                                                                    Intent intent = new Intent(SignUp.this, MainActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(SignUp.this, "Error in Creating the User", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                } else {
                                    String status = "Hey I'm Using This Application";
                                    imageuri = "https://firebasestorage.googleapis.com/v0/b/whatsgram-a21cf.appspot.com/o/man.png?alt=media&token=2798f26b-d55f-413b-bc96-b591b35081fa";
                                    Users users = new Users(id, username, email, password, imageuri, status);
                                    databaseReference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.show();
                                                Intent intent = new Intent(SignUp.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(SignUp.this, "Error in Creating the User", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(SignUp.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        // Profile Image through gallery
        rg_profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (data != null) {
                imageURI = data.getData();
                rg_profileImg.setImageURI(imageURI);
            }
        }
    }
}