package com.example.farmcongduc;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView meritCountElement;
    private ImageView woodenFish;
    private Button autoKnockBtn;
    private ImageView mallet;

    private int meritCount = 0;
    private MediaPlayer knockSound;
    private Handler autoKnockHandler = new Handler(Looper.getMainLooper());
    private Runnable autoKnockRunnable;
    private boolean isAutoKnocking = false;
    private Animation strikeAnimation;
    private boolean isKnocking = false; // Thêm biến cờ để tránh spam click

    public static final String PREFS_NAME = "CongDucPrefs";
    public static final String PREF_MERIT_COUNT_KEY = "meritCount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        meritCountElement = findViewById(R.id.tv_merit_counter);
        woodenFish = findViewById(R.id.iv_wooden_fish);
        autoKnockBtn = findViewById(R.id.btn_auto_knock);
        mallet = findViewById(R.id.iv_mallet);
        knockSound = MediaPlayer.create(this, R.raw.sound);
        strikeAnimation = AnimationUtils.loadAnimation(this, R.anim.strike_animation);

        loadMerit();

        woodenFish.setOnClickListener(v -> knock());

        autoKnockBtn.setOnClickListener(v -> toggleAutoKnock());
    }

    private void knock() {
        // Nếu đang trong một hành động gõ, không làm gì cả
        if (isKnocking) return;
        isKnocking = true;

        meritCount++;
        meritCountElement.setText("Công đức: " + meritCount);
        saveMerit();
        playSound();

        // CẬP NHẬT: Thêm hiệu ứng mở miệng
        woodenFish.setImageResource(R.drawable.fish_open); // Đổi sang ảnh miệng mở

        animateFish();
        animateMallet();
        updateAllWidgets();

        // Đặt lịch để đóng miệng và cho phép gõ lại sau 150ms
        autoKnockHandler.postDelayed(() -> {
            woodenFish.setImageResource(R.drawable.fish_closed); // Đổi về ảnh miệng đóng
            isKnocking = false; // Cho phép gõ lại
        }, 150);
    }

    private void toggleAutoKnock() {
        if (isAutoKnocking) {
            autoKnockHandler.removeCallbacks(autoKnockRunnable);
            isAutoKnocking = false;
            autoKnockBtn.setText("Tự Động Gõ");
        } else {
            isAutoKnocking = true;
            autoKnockBtn.setText("Dừng Tự Động");
            autoKnockRunnable = new Runnable() {
                @Override
                public void run() {
                    knock();
                    // Lên lịch cho lần gõ tiếp theo
                    autoKnockHandler.postDelayed(this, 300);
                }
            };
            autoKnockHandler.post(autoKnockRunnable);
        }
    }

    private void playSound() {
        if (knockSound != null) {
            if (knockSound.isPlaying()) {
                knockSound.seekTo(0);
            }
            knockSound.start();
        }
    }

    private void animateMallet() {
        mallet.startAnimation(strikeAnimation);
    }

    private void animateFish() {
        woodenFish.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(75)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    woodenFish.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(75)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                }).start();
    }


    private void loadMerit() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        meritCount = prefs.getInt(PREF_MERIT_COUNT_KEY, 0);
        meritCountElement.setText("Công đức: " + meritCount);
    }

    private void saveMerit() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(PREF_MERIT_COUNT_KEY, meritCount);
        editor.apply();
    }

    private void updateAllWidgets() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, CongDucWidgetProvider.class));
        if (appWidgetIds.length > 0) {
            new CongDucWidgetProvider().onUpdate(this, appWidgetManager, appWidgetIds);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (isAutoKnocking) {
            toggleAutoKnock();
        }
        saveMerit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMerit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (knockSound != null) {
            knockSound.release();
            knockSound = null;
        }
        autoKnockHandler.removeCallbacksAndMessages(null); // Xóa tất cả callbacks
    }
}