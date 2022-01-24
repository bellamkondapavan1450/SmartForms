package com.example.smartforms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterFragment extends Fragment {

    ExtendedFloatingActionButton register;
    ProgressDialog pd;
    private EditText edit_fName, edit_lName, edit_email, edit_password;
    private FirebaseAuth auth;
    private DatabaseReference dataRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        edit_fName = view.findViewById(R.id.fName);
        edit_lName = view.findViewById(R.id.lName);
        edit_email = view.findViewById(R.id.email);
        edit_password = view.findViewById(R.id.password);
        auth = FirebaseAuth.getInstance();
        dataRef = FirebaseDatabase.getInstance().getReference();
        pd = new ProgressDialog(getContext());
        register = view.findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueRegister();
            }
        });
        return view;
    }

    void continueRegister() {
        String txt_fName = edit_fName.getText().toString();
        String txt_lName = edit_lName.getText().toString();
        String txt_email = edit_email.getText().toString();
        String txt_password = edit_password.getText().toString();
        if (TextUtils.isEmpty(txt_fName) || TextUtils.isEmpty(txt_lName)
                || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
            Toast.makeText(getContext(), "Empty Fields!!!", Toast.LENGTH_SHORT).show();
        } else if (txt_password.length() < 8) {
            Toast.makeText(getContext(), "Password too short!!!", Toast.LENGTH_SHORT).show();
        } else {
            registerUser(txt_fName, txt_lName, txt_email, txt_password);
        }
    }

    void registerUser(String fName, String lName, String email, String password) {
        pd.setTitle("Registering User");
        pd.setMessage("Please wait!");
        pd.show();
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                pd.dismiss();
                HashMap<String, Object> user = new HashMap<>();
                user.put("email", email);
                dataRef.child("AllUsers").child(auth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "User Added!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                HashMap<String, Object> map = new HashMap<>();
                map.put("fName", fName);
                map.put("lName", lName);
                map.put("email", email);
                map.put("image", "https://firebasestorage.googleapis.com/v0/b/smart-forms-4f86d.appspot.com/o/Profile%20Pictures%2Fprofilebg.png?alt=media&token=7d005ab1-517d-46db-addb-a21f90301e77");
                dataRef.child("Users").child(auth.getCurrentUser().getUid()).child("Details").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Profile Added!!!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getContext(), MainActivity.class));
                            getActivity().finish();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}