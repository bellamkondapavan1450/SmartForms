package com.example.smartforms;

import static android.graphics.Color.DKGRAY;
import static android.graphics.Color.RED;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class RegisterFragment extends Fragment {

    ExtendedFloatingActionButton register;
    ProgressDialog pd;
    private TextInputLayout email_layout, password_layout;
    private EditText edit_fName, edit_lName, edit_email, edit_password;
    private FirebaseAuth auth;
    private DatabaseReference dataRef;

    private final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        edit_fName = view.findViewById(R.id.fName);
        edit_lName = view.findViewById(R.id.lName);
        edit_email = view.findViewById(R.id.email);
        password_layout = view.findViewById(R.id.password_layout);
        password_layout.setHelperTextColor(ColorStateList.valueOf(DKGRAY));
        edit_password = view.findViewById(R.id.password);
        edit_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().matches("[abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ@#]+") && !s.toString().equals("")) {
                    password_layout.setHelperTextColor(ColorStateList.valueOf(RED));
                } else {
                    password_layout.setHelperTextColor(ColorStateList.valueOf(DKGRAY));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        auth = FirebaseAuth.getInstance();
        dataRef = FirebaseDatabase.getInstance().getReference();
        pd = new ProgressDialog(getContext());
        register = view.findViewById(R.id.register);
        try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
            secretKeySpec = new SecretKeySpec(encryptionKey, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                sendVerificationEmail();
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
                map.put("fName", AESEncryptionMethod(fName));
                map.put("lName", AESEncryptionMethod(lName));
                map.put("email", AESEncryptionMethod(email));
                map.put("image", "https://firebasestorage.googleapis.com/v0/b/smart-forms-4f86d.appspot.com/o/Profile%20Pictures%2Fprofilebg.png?alt=media&token=7d005ab1-517d-46db-addb-a21f90301e77");
                dataRef.child("Users").child(auth.getCurrentUser().getUid()).child("Details").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Please Login!!!", Toast.LENGTH_SHORT).show();
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

    private void sendVerificationEmail() {
        FirebaseUser currentuser = auth.getCurrentUser();

        if (currentuser != null) {

            currentuser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "verification Email sent", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        int position = 0;  // position of the tab you want
                        showDialogBox();
                    } else {
                        Toast.makeText(getContext(), "Can't send email verify the mail provided", Toast.LENGTH_SHORT).show();
                        currentuser.delete();
                    }
                }
            });
        }
    }

    private void showDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Verification Email Sent!!!");
        builder.setMessage("Please verify your Email & Login again.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                edit_fName.setText("");
                edit_lName.setText("");
                edit_email.setText("");
                edit_password.setText("");
                ((LoginRegister)getActivity()).onResume();
            }
        }).create();
        builder.show();
    }

    private String AESEncryptionMethod(String string) {

        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String returnString = null;

        returnString = new String(encryptedByte, StandardCharsets.ISO_8859_1);
        return returnString;
    }

    private String AESDecryptionMethod(String string) throws UnsupportedEncodingException {
        byte[] EncryptedByte = string.getBytes(StandardCharsets.ISO_8859_1);
        String decryptedString = string;

        byte[] decryption;

        try {
            decipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            decryption = decipher.doFinal(EncryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;
    }

}