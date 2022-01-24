package com.example.smartforms;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class MailItemViewHolder extends RecyclerView.ViewHolder {

    final Context context;
    MailItemClickListener mailItemClickListener;
    RelativeLayout relativeLayout;
    CircleImageView profile;
    TextView sender, subject, body, date;
    ImageView star;
    RelativeLayout mail;

    MailItemViewHolder(View view) {
        super(view);

        relativeLayout = view.findViewById(R.id.mail_layout);
        profile = view.findViewById(R.id.profile);
        sender = view.findViewById(R.id.sender);
        subject = view.findViewById(R.id.subject);
        body = view.findViewById(R.id.body);
        date = view.findViewById(R.id.date);
        star = view.findViewById(R.id.star);
        mail = view.findViewById(R.id.mail);
        context = view.getContext();
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailItemClickListener.onMailItemClick(v, getAbsoluteAdapterPosition());
            }
        });
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailItemClickListener.onStarClick(v, getAbsoluteAdapterPosition());
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailItemClickListener.onProfileClick(v, getAbsoluteAdapterPosition());
            }
        });
    }

    public void setMailItemClickListener(MailItemClickListener mailItemClickListener) {
        this.mailItemClickListener = mailItemClickListener;
    }
}