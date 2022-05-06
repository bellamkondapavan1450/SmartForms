package com.example.smartforms;

import static android.graphics.Color.RED;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class MailItemAdapter extends RecyclerView.Adapter<MailItemAdapter.ViewHolder> {

    ArrayList<MailItem> mailItems;

    FirebaseDatabase firebaseDatabase;

    private final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;

    public MailItemAdapter(ArrayList<MailItem> mailItems) {
        this.mailItems = mailItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.mail_card, parent, false);
        firebaseDatabase = FirebaseDatabase.getInstance();
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
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MailItem item = mailItems.get(position);

        String Uid = item.getSenderUid();
        firebaseDatabase.getReference().child("Users").child(Uid).child("Details").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    try {
                        if (!user.getImage().equals(""))
                            Picasso.get().load(user.getImage()).placeholder(R.drawable.profilebg).into(holder.profile);
                        holder.sender.setText(AESDecryptionMethod(user.getfName()) + " " + AESDecryptionMethod(user.getlName()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.subject.setText(item.getSubject());
        holder.body.setText(item.getBody());
        holder.date.setText(item.getDate());
        if (item.getIsRead())
            holder.sender.setTypeface(null, Typeface.NORMAL);
        else
            holder.sender.setTypeface(null, Typeface.BOLD);

        if (item.getIsImportant())
            holder.star.setImageDrawable(ContextCompat.getDrawable(holder.context, R.drawable.ic_star));
        else
            holder.star.setImageDrawable(ContextCompat.getDrawable(holder.context, R.drawable.ic_star_outline));

        holder.star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getIsImportant()) {
                    item.setImportant(false);
                    holder.star.setImageResource(R.drawable.ic_star_outline);
                } else {
                    item.setImportant(true);
                    holder.star.setImageResource(R.drawable.ic_star);
                }
            }
        });

        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.profile.setImageResource(R.drawable.ic_star);
                holder.mail.setBackgroundColor(RED);
            }
        });

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!item.getIsRead())
                    holder.sender.setTypeface(null, Typeface.NORMAL);
                Toast.makeText(v.getContext(), "Mail is Clicked.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return mailItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final Context context;
        RelativeLayout relativeLayout;
        CircleImageView profile;
        TextView sender, subject, body, date;
        ImageView star;
        RelativeLayout mail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.mail_layout);
            profile = itemView.findViewById(R.id.profile);
            sender = itemView.findViewById(R.id.sender);
            subject = itemView.findViewById(R.id.subject);
            body = itemView.findViewById(R.id.body);
            date = itemView.findViewById(R.id.date);
            star = itemView.findViewById(R.id.star);
            mail = itemView.findViewById(R.id.mail);
            context = itemView.getContext();
        }
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