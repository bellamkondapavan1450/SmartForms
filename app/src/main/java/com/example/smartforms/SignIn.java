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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignIn extends AppCompatActivity {

    ProgressDialog pd;
    private EditText edit_email, edit_password;
    private Button btn_login;
    private TextView tv_signUp;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edit_email = (EditText) findViewById(R.id.email);
        edit_password = (EditText) findViewById(R.id.password);
        auth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);

        btn_login = (Button) findViewById(R.id.login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_email = edit_email.getText().toString() + getString(R.string.domain);
                String txt_password = edit_password.getText().toString();

                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(SignIn.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
                } else {
                    continueLogin(txt_email, txt_password);
                }
            }
        });
        tv_signUp = (TextView) findViewById(R.id.signUp);
        tv_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueSignUp();
            }
        });
    }

    void continueLogin(String email, String password) {
        pd.setTitle("Logging User");
        pd.setMessage("Please wait!");
        pd.show();
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                pd.dismiss();
                Toast.makeText(SignIn.this, "User LoggedIn Successfully!!!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignIn.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(SignIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void continueSignUp() {
        Intent intent = new Intent(SignIn.this, SignUp.class);
        startActivity(intent);
        finish();
    }
}