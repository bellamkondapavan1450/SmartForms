package com.example.smartforms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    TextView getStarted;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        Animation bottomanim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabs);
        getStarted = findViewById(R.id.getStarted);
        linearLayout = findViewById(R.id.linear);
        viewPager.setAnimation(topAnim);
        linearLayout.setAnimation(bottomanim);

        final List<SplashScreenItem> mList = new ArrayList<>();
        mList.add(new SplashScreenItem("Messages", "Send mails and get notified.", R.drawable.messages));
        mList.add(new SplashScreenItem("Create forms", "Create and share forms instantly with no worries.", R.drawable.createform));
        mList.add(new SplashScreenItem("Reminders", "You won’t regret this time for forgetting something ,because we remind you.", R.drawable.remainder));

        SplashScreenViewPagerAdapter splashScreenViewPagerAdapter = new SplashScreenViewPagerAdapter(this, mList);
        viewPager.setAdapter(splashScreenViewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                savePrefsData();
                startActivity(new Intent(StartActivity.this, LoginRegister.class));
                finish();
            }
        });

    }

//    private boolean restorePrefData() {
//
//
//        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
//        Boolean isIntroActivityOpnendBefore = pref.getBoolean("isIntroOpnend", false);
//        return isIntroActivityOpnendBefore;
//
//    }
//
//    private void savePrefsData() {
//
//        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = pref.edit();
//        editor.putBoolean("isIntroOpnend", true);
//        editor.commit();
//
//    }


    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(StartActivity.this, MainActivity.class));
            finish();
        }
    }

}