package com.example.smartforms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    ProgressDialog pd;
    private EditText edit_fName, edit_lName, edit_email, edit_password, edit_cpassword;
    private FirebaseAuth auth;
    private DatabaseReference dataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        edit_fName = (EditText) findViewById(R.id.fName);
        edit_lName = (EditText) findViewById(R.id.lName);
        edit_email = (EditText) findViewById(R.id.email);
        edit_password = (EditText) findViewById(R.id.password);
        edit_cpassword = (EditText) findViewById(R.id.cpassword);
        Button btn_register = (Button) findViewById(R.id.register);
        auth = FirebaseAuth.getInstance();
        dataRef = FirebaseDatabase.getInstance().getReference();
        pd = new ProgressDialog(this);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueRegister();
            }
        });
        TextView tv_signIn = (TextView) findViewById(R.id.signIn);
        tv_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueSignIn();
            }
        });
    }

    void continueRegister() {
        String txt_fName = edit_fName.getText().toString();
        String txt_lName = edit_lName.getText().toString();
        String txt_email = edit_email.getText().toString() + getString(R.string.domain);
        String txt_password = edit_password.getText().toString();
        String txt_cpassword = edit_cpassword.getText().toString();
        if (TextUtils.isEmpty(txt_fName) || TextUtils.isEmpty(txt_lName) || TextUtils.isEmpty(txt_email)
                || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(txt_cpassword)) {
            Toast.makeText(SignUp.this, "Empty Fields!!!", Toast.LENGTH_SHORT).show();
        } else if (txt_password.length() < 8) {
            Toast.makeText(SignUp.this, "Password too short!!!", Toast.LENGTH_SHORT).show();
        } else if (!txt_password.equals(txt_cpassword)) {
            Toast.makeText(SignUp.this, "Password not match!!!", Toast.LENGTH_SHORT).show();
        } else {
            registerUser(txt_fName, txt_lName, txt_email, txt_password);
        }
    }

    void continueSignIn() {
        Intent intent = new Intent(this, SignIn.class);
        startActivity(intent);
        finish();
    }

    void registerUser(String fName, String lName, String email, String password) {
        pd.setTitle("Registering User");
        pd.setMessage("Please wait!");
        pd.show();
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                pd.dismiss();
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", auth.getCurrentUser().getUid());
                map.put("fName", fName);
                map.put("lName", lName);
                map.put("email", email);
                map.put("image", "https://firebasestorage.googleapis.com/v0/b/smart-forms-4f86d.appspot.com/o/Profile%20Pictures%2FProfile.jpg?alt=media&token=df7a7d73-8d0e-45c2-a655-8133b9d7105e");
                dataRef.child("Users").child(auth.getCurrentUser().getUid()).child("Personal Details").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Profile Added!!!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUp.this, MainActivity.class));
                            finish();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}