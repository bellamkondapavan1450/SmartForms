package com.example.smartforms;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    public static String FNAME = "fName";
    public static String LNAME = "lName";
    public static String IMAGE = "Image";

    ProgressDialog pd;

    private CircleImageView profilePicture;
    private TextView tv_name;
    private TextView tv_fName;
    private TextView tv_lName;
    private TextView tv_email;
    private TextView tasks, missed;

    private FirebaseAuth auth;
    private DatabaseReference dataRef;
    private String myUri = "";


    private final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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

        auth = FirebaseAuth.getInstance();
        dataRef = FirebaseDatabase.getInstance().getReference().child("Users");
        pd = new ProgressDialog(this);

        tv_fName = (TextView) findViewById(R.id.fName);
        tv_lName = (TextView) findViewById(R.id.lName);
        tv_name = (TextView) findViewById(R.id.name);
        tv_email = (TextView) findViewById(R.id.email);
        profilePicture = findViewById(R.id.profile);
        tasks = findViewById(R.id.tasks);
        missed = findViewById(R.id.missed);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date myDate = new Date();
        String date = dateFormat.format(myDate);

        dataRef.child(auth.getCurrentUser().getUid()).child("Tasks").orderByChild("deadline")
                .startAt(date).endAt(date+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasks.setText(snapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dataRef.child(auth.getCurrentUser().getUid()).child("Missed").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                missed.setText(snapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ImageView editProfile = (ImageView) findViewById(R.id.edit);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, EditProfile.class);
                intent.putExtra(IMAGE, myUri);
                intent.putExtra(FNAME, tv_fName.getText());
                intent.putExtra(LNAME, tv_lName.getText());
                startActivity(intent);
            }
        });

        ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        pd.setMessage("Please wait!");
        pd.show();
        getUserInfo();
    }

    private void getUserInfo() {
        DatabaseReference ref = dataRef.child(auth.getCurrentUser().getUid()).child("Details");
        if (ref != null) {
            ref.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        try {
                            tv_fName.setText(AESDecryptionMethod(user.getfName()));
                            tv_lName.setText(AESDecryptionMethod(user.getlName()));
                            tv_name.setText(AESDecryptionMethod(user.getfName()) + " " + AESDecryptionMethod(user.getlName()));
                            tv_email.setText(AESDecryptionMethod(user.getEmail()));
                            myUri = user.getImage();
                            Picasso.get().load(myUri).placeholder(R.drawable.profilebg).into(profilePicture);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        pd.dismiss();
                    } else {
                        pd.dismiss();
                        Toast.makeText(Profile.this, "Error, Try Again!!!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Profile.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            getUserInfo();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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