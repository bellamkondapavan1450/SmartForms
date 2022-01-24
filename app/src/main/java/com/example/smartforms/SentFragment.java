package com.example.smartforms;

import static com.example.smartforms.ViewMail.ITEM;
import static com.example.smartforms.ViewMail.KEY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SentFragment extends Fragment {

    RecyclerView recyclerView;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    Query query;
    FirebaseRecyclerAdapter<MailItem, MailItemViewHolder> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<MailItem> itemFirebaseRecyclerOptions;
    String ClickedItemKey;
    ImageView imageView;
    View view1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sent, container, false);
        view1 = view.findViewById(R.id.view);
        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view1.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
            }
        });
        imageView = view.findViewById(R.id.profilepic);
        recyclerView = view.findViewById(R.id.rv1);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        firebaseDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("Sent");

        Bundle bundle = getArguments();
        String s = "";
        if(bundle != null) {
            s = bundle.getString(MainActivity.VAL1);
            query = databaseReference.orderByChild("sender").startAt(s).endAt(s+"\uf8ff");
        }
        else
            query = databaseReference.orderByChild("order");

        fetchRecord(query);


        ExtendedFloatingActionButton compose = view.findViewById(R.id.compose);
        compose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Compose.class));
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    compose.shrink();

                } else {
                    compose.extend();

                }
            }
        });
        return view;
    }

    private void fetchRecord(Query query) {
        itemFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<MailItem>()
                .setQuery(query, MailItem.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<MailItem, MailItemViewHolder>(itemFirebaseRecyclerOptions) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull MailItemViewHolder holder, int position, @NonNull MailItem model) {

                if(!model.getImgUri().equals(""))
                    Picasso.get().load(model.getImgUri()).placeholder(R.drawable.profilebg).into(holder.profile);
                holder.sender.setText(model.getSender());
                holder.subject.setText(model.getSubject());
                holder.body.setText(model.getBody());
                String date = model.getDate().substring(0,4);
                switch(model.getDate().substring(4,6)) {
                    case "01" :
                        date = "Jan " + date;
                        break;
                    case "02" :
                        date = "Feb " + date;
                        break;
                    case "03" :
                        date = "Mar " + date;
                        break;
                    case "04" :
                        date = "Apr " + date;
                        break;
                    case "05" :
                        date = "May " + date;
                        break;
                    case "06" :
                        date = "Jun " + date;
                        break;
                    case "07" :
                        date = "Jul " + date;
                        break;
                    case "08" :
                        date = "Aug " + date;
                        break;
                    case "09" :
                        date = "Sep " + date;
                        break;
                    case "10" :
                        date = "Oct " + date;
                        break;
                    case "11" :
                        date = "Nov " + date;
                        break;
                    case "12" :
                        date = "Dec " + date;
                        break;

                }
                date = model.getDate().substring(6, 8) + " " + date;

                String time = model.getDate().substring(10, 12);
                int hrs = Integer.parseInt(model.getDate().substring(8, 10));
                if(hrs > 12)
                    time = (hrs-12) +":"+ time + " PM";
                else
                    time = (hrs) +":"+ time + " AM";

                holder.date.setText(time+"\n"+date);

                if(model.getIsRead()) {
                    holder.sender.setTypeface(null, Typeface.NORMAL);
                    holder.subject.setTypeface(null, Typeface.NORMAL);
                    holder.sender.setTextColor(ContextCompat.getColor(getContext(), R.color.subject));
                    holder.subject.setTextColor(ContextCompat.getColor(getContext(), R.color.message));
                }
                else {
                    holder.sender.setTypeface(null, Typeface.BOLD);
                    holder.subject.setTypeface(null, Typeface.BOLD);
                    holder.subject.setTextColor(ContextCompat.getColor(getContext(), R.color.sender));
                    holder.subject.setTextColor(ContextCompat.getColor(getContext(), R.color.subject));
                }

                if(model.getIsImportant())
                    holder.star.setImageResource(R.drawable.ic_star);
                else
                    holder.star.setImageResource(R.drawable.ic_star_outline);

                holder.setMailItemClickListener(new MailItemClickListener() {
                    @Override
                    public void onMailItemClick(View view, int position) {
                        ClickedItemKey = getSnapshots().getSnapshot(position).getKey();
                        if(!model.getIsRead()) {
                            holder.sender.setTypeface(null, Typeface.NORMAL);
                            holder.subject.setTypeface(null, Typeface.NORMAL);
                            holder.sender.setTextColor(ContextCompat.getColor(getContext(), R.color.subject));
                            holder.subject.setTextColor(ContextCompat.getColor(getContext(), R.color.message));
                            databaseReference.child(ClickedItemKey).child("isRead").setValue(true);
                            DatabaseReference dataref = firebaseDatabase.getReference().child("Users").child(auth.getCurrentUser().getUid());
                            dataref.child("Starred").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(ClickedItemKey))
                                        dataref.child("Starred").child(ClickedItemKey).child("isRead").setValue(true);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        Intent intent = new Intent(view.getContext(), ViewMail.class);
                        intent.putExtra(KEY, ClickedItemKey);
                        intent.putExtra(ITEM, "Sent");
                        holder.context.startActivity(intent);
                        Toast.makeText(view.getContext(), ClickedItemKey, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStarClick(View view, int position) {
                        ClickedItemKey = getSnapshots().getSnapshot(position).getKey();
                        if(model.getIsImportant()) {
                            model.setImportant(false);
                            databaseReference.child(ClickedItemKey).child("isImportant").setValue(false);
                            firebaseDatabase.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("Starred").child(ClickedItemKey).removeValue();
                            holder.star.setImageResource(R.drawable.ic_star_outline);
                        }
                        else {
                            model.setImportant(true);
                            databaseReference.child(ClickedItemKey).child("isImportant").setValue(true);
                            firebaseDatabase.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("Starred").child(ClickedItemKey).setValue(model);
                            holder.star.setImageResource(R.drawable.ic_star);
                        }
                    }

                    @Override
                    public void onProfileClick(View view, int position) {
                        view1.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                    }
                });

            }

            @NonNull
            @Override
            public MailItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View view = layoutInflater.inflate(R.layout.mail_card, parent, false);
                MailItemViewHolder mailItemViewHolder = new MailItemViewHolder(view);
                return mailItemViewHolder;
            }
        };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

}
