package com.example.smartforms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MailsAdapter extends RecyclerView.Adapter<MailsAdapter.ViewHolder> {

    ArrayList<String> mails;

    public MailsAdapter(ArrayList<String> mails) {
        this.mails = mails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.mail, parent, false);
        return new MailsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String mail = mails.get(position);
        if (!mail.equals(""))
            holder.mail.setText(mail);

    }

    @Override
    public int getItemCount() {
        return mails.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mail = itemView.findViewById(R.id.mail);
        }
    }
}
