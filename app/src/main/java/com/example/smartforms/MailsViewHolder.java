package com.example.smartforms;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MailsViewHolder extends RecyclerView.ViewHolder {

    MailsClickListener mailsClickListener;

    TextView textView;

    MailsViewHolder(View view) {
        super(view);

        textView = view.findViewById(R.id.mail);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailsClickListener.onClick(v, getAbsoluteAdapterPosition());
            }
        });

    }

    public void setMailsClickListener(MailsClickListener mailsClickListener) {
        this.mailsClickListener = mailsClickListener;
    }
}