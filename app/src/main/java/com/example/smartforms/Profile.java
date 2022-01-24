package com.example.smartforms;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.Date;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
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
                        tv_fName.setText(user.getfName());
                        tv_lName.setText(user.getlName());
                        tv_name.setText(user.getfName() + " " + user.getlName());
                        tv_email.setText(user.getEmail());
                        myUri = user.getImage();
                        Picasso.get().load(myUri).placeholder(R.drawable.profilebg).into(profilePicture);
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
}