package com.example.smartforms;

import static com.example.smartforms.AppNotif.CHANNEL_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static String VAL1="val1";
    ProgressDialog pd;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference dataRef;
    private FirebaseAuth auth;
    private DrawerLayout drawer;
    private CircleImageView toolbarProfilePic;
    private TextView itemName;
    private TextView navName;
    private TextView navEmail;
    private CircleImageView navProfilePic;
    EditText search;
    User user;

    NotificationManagerCompat notificationManager;

    private final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        pd = new ProgressDialog(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        search = findViewById(R.id.search);

        itemName = (TextView) findViewById(R.id.itemname);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View navHeader = navigationView.getHeaderView(0);
        navName = (TextView) navHeader.findViewById(R.id.name);
        navEmail = (TextView) navHeader.findViewById(R.id.email);
        navProfilePic = (CircleImageView) navHeader.findViewById(R.id.profile);

        notificationManager = NotificationManagerCompat.from(this);

        TextView viewProfile = (TextView) navHeader.findViewById(R.id.viewprofile);
        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Profile.class));
            }
        });

        Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueLogout();
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            itemName.setText("INBOX");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new InboxFragment()).commit();
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putString(VAL1, s.toString());
                    Fragment fragment = new InboxFragment();
                    fragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.fragment_container, fragment);
                    fragmentTransaction.commit();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            navigationView.setCheckedItem(R.id.inbox);
        }
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dataRef = firebaseDatabase.getReference().child("Users").child(auth.getCurrentUser().getUid());

        toolbarProfilePic = (CircleImageView) findViewById(R.id.profile);
        toolbarProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Profile.class));
            }
        });


        dataRef.child("Inbox").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataRef.child("Tasks").removeValue();
                dataRef.child("Missed").removeValue();
                for (DataSnapshot data: snapshot.getChildren()) {
                    MailItem mailItem = data.getValue(MailItem.class);
                    if(!mailItem.getIsRead()) {
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
                        Date myDate = new Date();
                        String date = dateFormat.format(myDate);
                        if ((999999999999L - Long.parseLong(mailItem.getDeadline())) < (999999999999L - Long.parseLong(date)))
                            dataRef.child("Tasks").push().setValue(mailItem);
                        else
                            dataRef.child("Missed").push().setValue(mailItem);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dataRef.child("Tasks").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
                Date myDate = new Date();
                String date = dateFormat.format(myDate);

                for (DataSnapshot data: snapshot.getChildren()) {
                    MailItem mailItem = data.getValue(MailItem.class);
                    long dif = Long.parseLong(mailItem.getDeadline()) - Long.parseLong(date);
                    if(dif >= 9000 && dif <= 10000)
                        sendNotif(mailItem, "Remainder! (1 day Left)");
                    else if(dif >= 19000 && dif <= 20000)
                        sendNotif(mailItem, "Remainder! (2 days Left)");
                    else if(dif >= 29000 && dif <= 30000)
                        sendNotif(mailItem, "Remainder! (3 days Left)");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dataRef.child("Inbox").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MailItem mailItem = snapshot.getValue(MailItem.class);
                if(!mailItem.getIsRead())
                    sendNotif(mailItem, "New Message!");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void sendNotif(MailItem mailItem, String str) {

        String Uid = mailItem.getSenderUid();
        firebaseDatabase.getReference().child("Users").child(Uid).child("Details").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {

                    RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.notification_collapsed);
                    collapsedView.setTextViewText(R.id.text_view_collapsed_1, str);

                    RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.notification_expanded);

                    try {
                        String name = (AESDecryptionMethod(user.getfName()) + " " + AESDecryptionMethod(user.getlName()));
                        expandedView.setTextViewText(R.id.text_view_expanded_1, name);
                        expandedView.setTextViewText(R.id.text_view_expanded_2, AESDecryptionMethod(mailItem.getSubject()));
                        expandedView.setTextViewText(R.id.text_view_expanded_3, AESDecryptionMethod(mailItem.getBody()));
                        String date = mailItem.getDate().substring(0, 4);
                        switch (mailItem.getDate().substring(4, 6)) {
                            case "01":
                                date = "Jan " + date;
                                break;
                            case "02":
                                date = "Feb " + date;
                                break;
                            case "03":
                                date = "Mar " + date;
                                break;
                            case "04":
                                date = "Apr " + date;
                                break;
                            case "05":
                                date = "May " + date;
                                break;
                            case "06":
                                date = "Jun " + date;
                                break;
                            case "07":
                                date = "Jul " + date;
                                break;
                            case "08":
                                date = "Aug " + date;
                                break;
                            case "09":
                                date = "Sep " + date;
                                break;
                            case "10":
                                date = "Oct " + date;
                                break;
                            case "11":
                                date = "Nov " + date;
                                break;
                            case "12":
                                date = "Dec " + date;
                                break;

                        }
                        date = mailItem.getDate().substring(6, 8) + " " + date;

                        String time = mailItem.getDate().substring(10, 12);
                        int hrs = Integer.parseInt(mailItem.getDate().substring(8, 10));
                        if (hrs > 12)
                            time = (hrs - 12) + ":" + time + " PM";
                        else
                            time = (hrs) + ":" + time + " AM";

                        expandedView.setTextViewText(R.id.date, time + "\n" + date);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_email)
                            .setCustomContentView(collapsedView)
                            .setCustomBigContentView(expandedView)
                            .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .build();

                    notificationManager.notify(1, notification);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @SuppressLint({"SetTextI18n", "NonConstantResourceId"})
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Bundle bundle = new Bundle();

        switch (item.getItemId()) {
            case R.id.inbox:
                itemName.setText("INBOX");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new InboxFragment()).commit();
                searchActivity(new InboxFragment());
                break;
            case R.id.sent:
                itemName.setText("SENT");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SentFragment()).commit();
                searchActivity(new SentFragment());
                break;
            case R.id.starred:
                itemName.setText("STARRED");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new StarredFragment()).commit();
                searchActivity(new StarredFragment());
                break;
            case R.id.tasks:
                itemName.setText("TASKS");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new TasksFragment()).commit();
                searchActivity(new TasksFragment());
                break;
            case R.id.missed:
                itemName.setText("MISSED");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MissedFragment()).commit();
                searchActivity(new MissedFragment());
                break;
            case R.id.trash:
                itemName.setText("TRASH");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new TrashFragment()).commit();
                searchActivity(new TrashFragment());
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void searchActivity(Fragment fragment) {

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putString(VAL1, s.toString());
                fragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        pd.setMessage("Please wait!");
        pd.show();
        setProfile();
    }

    void setProfile() {
        DatabaseReference ref = dataRef.child("Details");
        if (ref != null) {
            ref.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    user = snapshot.getValue(User.class);
                    if (user != null) {
                        try {
                            Picasso.get().load(user.getImage()).placeholder(R.drawable.profilebg).into(toolbarProfilePic);
                            Picasso.get().load(user.getImage()).placeholder(R.drawable.profilebg).into(navProfilePic);
                            navName.setText(AESDecryptionMethod(user.getfName()) + " " + AESDecryptionMethod(user.getlName()));
                            navEmail.setText(AESDecryptionMethod(user.getEmail()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        pd.dismiss();
                    } else {
                        pd.dismiss();
                        Toast.makeText(MainActivity.this, "Error, Try Again!!!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            setProfile();
        }
    }

    void continueLogout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(MainActivity.this, "User LoggedOut Successfully!!!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
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