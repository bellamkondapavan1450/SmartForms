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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MailItemAdapter extends RecyclerView.Adapter<MailItemAdapter.ViewHolder> {

    ArrayList<MailItem> mailItems;

    public MailItemAdapter(ArrayList<MailItem> mailItems) {
        this.mailItems = mailItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.mail_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MailItem item = mailItems.get(position);
        if (!item.getImgUri().equals(""))
            Picasso.get().load(item.getImgUri()).placeholder(R.drawable.profilebg).into(holder.profile);
        holder.sender.setText(item.getSender());
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
}