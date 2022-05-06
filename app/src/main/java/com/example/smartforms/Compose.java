package com.example.smartforms;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Compose extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView imageView, Send;
    ArrayList<String> mails_list;
    MailsAdapter mailsAdapter;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference dataref;
    Query query;
    FirebaseRecyclerAdapter<Mail, MailsViewHolder> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<Mail> itemFirebaseRecyclerOptions;
    String ClickedItemKey;
    EditText input;
    RecyclerView rv;
    ArrayList<String> sendList;
    EditText subject, body;
    TextView from, deadline, sample;
    MailItem mailItem;

    private final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;

    static String Name = "", Email = "", Image = "", Deadline = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
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
        database = FirebaseDatabase.getInstance();
        dataref = database.getReference();
        subject = findViewById(R.id.subject);
        body = findViewById(R.id.body);
        sample = findViewById(R.id.sample);
        deadline = findViewById(R.id.deadline);
        from = findViewById(R.id.from);
        dataref.child("AllUsers").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Mail mail = snapshot.getValue(Mail.class);
                from.setText(mail.getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        deadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deadline.setText("");
                sample.setText("");
                selectTime();
                selectDate();
            }
        });
        Toolbar toolbar = findViewById(R.id.compose_toolbar);
        setSupportActionBar(toolbar);
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
        ImageView createform = findViewById(R.id.createform);
        createform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Compose.this, GoogleformActivity.class));
            }
        });
        Send = findViewById(R.id.send);
        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });
        recyclerView = findViewById(R.id.list);
        mails_list = new ArrayList<String>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        mailsAdapter = new MailsAdapter(mails_list);

        recyclerView.setAdapter(mailsAdapter);

        imageView = findViewById(R.id.add);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogBox();
            }
        });

        sendList = new ArrayList<String>();

    }

    private void sendMail() {

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        Date myDate = new Date();
        String date = dateFormat.format(myDate);

        long order = 999999999999L - Long.parseLong(date);

        mailItem = new MailItem(auth.getCurrentUser().getUid(), AESEncryptionMethod(subject.getText().toString()),
                AESEncryptionMethod(body.getText().toString()), date, order, sample.getText().toString());

        dataref.child("Users").child(auth.getCurrentUser().getUid()).child("Sent").push().setValue(mailItem);
        for (String uid : sendList) {
            dataref.child("Users").child(uid).child("Inbox").push().setValue(mailItem);
        }
        Toast.makeText(Compose.this, "Mail Sent Successfully", Toast.LENGTH_SHORT).show();
        onBackPressed();

    }

    private void showDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Compose.this);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.mails, null, false);
        builder.setView(view);
        rv = view.findViewById(R.id.rv1);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(linearLayoutManager);

        input = view.findViewById(R.id.input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = dataref.child("AllUsers").orderByChild("email").startAt(s.toString()).endAt(s.toString() + "\uf8ff");
                fetchRecord(query);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mail = input.getText().toString();
                if (!sendList.contains(ClickedItemKey)) {
                    sendList.add(ClickedItemKey);
                    mails_list.add(mail);
                }
                mailsAdapter.notifyDataSetChanged();
            }
        }).create();

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).create();

        builder.show();
    }

    private void fetchRecord(Query query) {

        itemFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Mail>()
                .setQuery(query, Mail.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Mail, MailsViewHolder>(itemFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull MailsViewHolder holder, int position, @NonNull Mail model) {
                String s = model.getEmail();
                holder.textView.setText(s);
                holder.setMailsClickListener(new MailsClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        ClickedItemKey = getSnapshots().getSnapshot(position).getKey();
                        input.setText(s);
                    }
                });
            }


            @NonNull
            @Override
            public MailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View view = layoutInflater.inflate(R.layout.mail, parent, false);
                MailsViewHolder mailsViewHolder = new MailsViewHolder(view);
                return mailsViewHolder;
            }
        };
        firebaseRecyclerAdapter.startListening();
        rv.setAdapter(firebaseRecyclerAdapter);
    }


    private void selectDate() {
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int date) {

                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(year, month, date, 0, 0);
                String dateText = DateFormat.format("yyyyMMdd", calendar1).toString();
                sample.setText(sample.getText() + dateText);
                String DateText = DateFormat.format("MMM d, yyyy", calendar1).toString();
                deadline.setText(deadline.getText() + DateText + " ");
            }
        }, YEAR, MONTH, DATE);

        datePickerDialog.show();

    }

    private void selectTime() {
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR);
        int MINUTE = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Log.d("Time", "" + hour);
                Log.d("Time", "" + minute);
                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(0, 0, 0, hour, minute);
                Date date = new Date();
                Log.d("Time", calendar1.getTime().toString());
                String timeText = DateFormat.format("HHmm", calendar1).toString();
                sample.setText(sample.getText() + timeText);
                String TimeText = DateFormat.format("hh:mm a", calendar1).toString();
                deadline.setText(deadline.getText() + TimeText);
                Log.d("Time", sample.getText().toString());
            }

        }, HOUR, MINUTE, true);

        timePickerDialog.show();

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