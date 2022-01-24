package com.example.smartforms;

import android.view.View;

public interface MailItemClickListener {
    void onMailItemClick(View view, int position);

    void onStarClick(View view, int position);

    void onProfileClick(View view, int position);
}
