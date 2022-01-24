package com.example.smartforms;

import android.annotation.SuppressLint;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewMail extends AppCompatActivity {


    public static String KEY = "clickedItemKey";
    public static String ITEM = "itemKey";

    ImageView star, back, unread, delete;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    CircleImageView profile;
    TextView subject, sender, email, date, body, deadline;

    MailItem mailItem;

    String ClickedItemKey;
    String ItemKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_mail);
        firebaseDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        star = findViewById(R.id.star);
        back = findViewById(R.id.back);
        unread = findViewById(R.id.unread);
        delete = findViewById(R.id.delete);
        profile = findViewById(R.id.profile);
        subject = findViewById(R.id.subject);
        sender = findViewById(R.id.sender);
        email = findViewById(R.id.email);
        date = findViewById(R.id.date);
        body = findViewById(R.id.body);
        deadline = findViewById(R.id.deadline);


        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mailItem.getIsImportant()) {
                    databaseReference.child(ClickedItemKey).child("isImportant").setValue(false);
                    firebaseDatabase.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("Starred").child(ClickedItemKey).removeValue();
                    star.setImageResource(R.drawable.ic_star_outline);
                } else {
                    databaseReference.child(ClickedItemKey).child("isImportant").setValue(true);
                    firebaseDatabase.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("Starred").child(ClickedItemKey).setValue(mailItem);
                    star.setImageResource(R.drawable.ic_star);
                }
            }
        });

        unread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child(ClickedItemKey).child("isRead").setValue(false);
                DatabaseReference dataref = firebaseDatabase.getReference().child("Users").child(auth.getCurrentUser().getUid());
                dataref.child("Inbox").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(ClickedItemKey))
                            dataref.child("Inbox").child(ClickedItemKey).child("isRead").setValue(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                dataref.child("Sent").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(ClickedItemKey))
                            dataref.child("Sent").child(ClickedItemKey).child("isRead").setValue(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                dataref.child("Starred").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(ClickedItemKey))
                            dataref.child("Starred").child(ClickedItemKey).child("isRead").setValue(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                onBackPressed();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ItemKey.equals("Trash"))
                    databaseReference.child(ClickedItemKey).removeValue();
                else {
                    deleteItem(ClickedItemKey, "Inbox");
                    deleteItem(ClickedItemKey, "Sent");
                    deleteItem(ClickedItemKey, "Starred");
                    deleteItem(ClickedItemKey, "Tasks");
                    deleteItem(ClickedItemKey, "Missed");
                    firebaseDatabase.getReference().child("Users")
                            .child(auth.getCurrentUser().getUid()).child("Trash")
                            .push().setValue(mailItem);
                }
                onBackPressed();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void deleteItem(String clickedItemKey, String label) {
        DatabaseReference dataRef = firebaseDatabase.getReference().child("Users").child(auth.getCurrentUser().getUid()).child(label);
        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(clickedItemKey))
                    dataRef.child(clickedItemKey).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        ClickedItemKey = intent.getStringExtra(KEY);
        ItemKey = intent.getStringExtra(ITEM);

        if (ItemKey.equals("Tasks") || ItemKey.equals("Missed")) {
            star.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        } else if (ItemKey.equals("Trash"))
            star.setVisibility(View.GONE);

        databaseReference = firebaseDatabase.getReference().child("Users").child(auth.getCurrentUser().getUid()).child(ItemKey);

        databaseReference.child(ClickedItemKey).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mailItem = snapshot.getValue(MailItem.class);
                if (mailItem != null) {
                    if (!mailItem.getImgUri().equals(""))
                        Picasso.get().load(mailItem.getImgUri()).placeholder(R.drawable.profilebg).into(profile);
                    subject.setText(mailItem.getSubject());
                    sender.setText(mailItem.getSender());
                    email.setText(mailItem.getEmail());
                    String d = mailItem.getDate().substring(0, 4);
                    switch (mailItem.getDate().substring(4, 6)) {
                        case "01":
                            d = "Jan " + d;
                            break;
                        case "02":
                            d = "Feb " + d;
                            break;
                        case "03":
                            d = "Mar " + d;
                            break;
                        case "04":
                            d = "Apr " + d;
                            break;
                        case "05":
                            d = "May " + d;
                            break;
                        case "06":
                            d = "Jun " + d;
                            break;
                        case "07":
                            d = "Jul " + d;
                            break;
                        case "08":
                            d = "Aug " + d;
                            break;
                        case "09":
                            d = "Sep " + d;
                            break;
                        case "10":
                            d = "Oct " + d;
                            break;
                        case "11":
                            d = "Nov " + d;
                            break;
                        case "12":
                            d = "Dec " + d;
                            break;

                    }
                    d = mailItem.getDate().substring(6, 8) + " " + d;

                    String time = mailItem.getDate().substring(10, 12);
                    int hrs = Integer.parseInt(mailItem.getDate().substring(8, 10));
                    if (hrs > 12)
                        time = (hrs - 12) + ":" + time + " PM";
                    else
                        time = (hrs) + ":" + time + " AM";

                    date.setText(time + "\n" + d);
                    body.setText(mailItem.getBody());

                    String d1 = mailItem.getDeadline().substring(0, 4);
                    switch (mailItem.getDeadline().substring(4, 6)) {
                        case "01":
                            d1 = "Jan " + d1;
                            break;
                        case "02":
                            d1 = "Feb " + d1;
                            break;
                        case "03":
                            d1 = "Mar " + d1;
                            break;
                        case "04":
                            d1 = "Apr " + d1;
                            break;
                        case "05":
                            d1 = "May " + d1;
                            break;
                        case "06":
                            d1 = "Jun " + d1;
                            break;
                        case "07":
                            d1 = "Jul " + d1;
                            break;
                        case "08":
                            d1 = "Aug " + d1;
                            break;
                        case "09":
                            d1 = "Sep " + d1;
                            break;
                        case "10":
                            d1 = "Oct " + d1;
                            break;
                        case "11":
                            d1 = "Nov " + d1;
                            break;
                        case "12":
                            d1 = "Dec " + d1;
                            break;

                    }
                    d1 = mailItem.getDeadline().substring(6, 8) + " " + d1;

                    String time1 = mailItem.getDeadline().substring(10, 12);
                    int hrs1 = Integer.parseInt(mailItem.getDeadline().substring(8, 10));
                    if (hrs1 > 12)
                        time1 = (hrs1 - 12) + ":" + time1 + " PM";
                    else
                        time1 = (hrs1) + ":" + time1 + " AM";

                    deadline.setText("DeadLine: " + d1 + " " + time1);


                    body.setText(mailItem.getBody());

                    if (mailItem.getIsImportant())
                        star.setImageResource(R.drawable.ic_star);
                    else
                        star.setImageResource(R.drawable.ic_star_outline);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewMail.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}