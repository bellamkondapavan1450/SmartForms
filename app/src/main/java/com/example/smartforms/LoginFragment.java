package com.example.smartforms;

import static android.graphics.Color.RED;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class LoginFragment extends Fragment {

    ExtendedFloatingActionButton login;
    ProgressDialog pd;
    TextView forgetPassword;
    private TextInputLayout email_layout, password_layout;
    private TextInputEditText edit_email, edit_password;
    private FirebaseAuth auth;

    @SuppressLint("CutPasteId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        email_layout = view.findViewById(R.id.email_layout);
        password_layout = view.findViewById(R.id.password_layout);
        password_layout.setHelperText("");
        edit_email = view.findViewById(R.id.email);
        edit_password = view.findViewById(R.id.password);
        edit_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().matches("[abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ@#]+") && !s.toString().equals("")) {
                    password_layout.setHelperText("*Use only A-Z, a-z, 0-9, @, #");
                    password_layout.setHelperTextColor(ColorStateList.valueOf(RED));
                } else {
                    password_layout.setHelperText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        auth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(getContext());
        login = view.findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email = edit_email.getText().toString();
                String txt_password = edit_password.getText().toString();

                if (TextUtils.isEmpty(txt_email)) {
                    edit_email.setError("Empty Email!!!");
                    Toast.makeText(getContext(), "Empty Email", Toast.LENGTH_SHORT).show();
                } else {
                    continueLogin(txt_email, txt_password);
                }
            }
        });
        forgetPassword = view.findViewById(R.id.forget);
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetPassword();
            }
        });
        return view;
    }

    private void continueLogin(String email, String password) {
        pd.setTitle("Logging User");
        pd.setMessage("Please wait!");
        pd.show();
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                pd.dismiss();
                checkEmailVerification();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialogBox() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Email Not Verified!!!");
        builder.setMessage("Please verify your Email & Login again.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                auth.signOut();
                edit_password.setText("");
            }
        }).create();
        builder.show();
    }

    void checkEmailVerification() {
        FirebaseUser currentuser = auth.getCurrentUser();
        boolean result= currentuser.isEmailVerified();

        if(result){
            Toast.makeText(getContext(), "User LoggedIn Successfully!!!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getContext(), MainActivity.class));
            getActivity().finish();
        }
        else{
            showDialogBox();
            Toast.makeText(getContext(), "Please verify your Email. Try again!!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void ResetPassword() {
        EditText reset_mail = new EditText(getContext());
        AlertDialog.Builder passwordResetDailog = new AlertDialog.Builder(getContext());
        passwordResetDailog.setTitle("Reset Password");
        passwordResetDailog.setMessage("Enter you Email To Receive Reset Link");
        passwordResetDailog.setView(reset_mail);
        passwordResetDailog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mail = reset_mail.getText().toString().trim();
                auth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Reset Link Sent To Your Email", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getContext(), "Reset Link Not Sent To Your Email" + e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });

            }
        });
        passwordResetDailog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        passwordResetDailog.create().show();
    }

}
