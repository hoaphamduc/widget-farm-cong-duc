package com.example.farmcongduc;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout mainContainer;
    private TextView meritCountElement;
    private ImageView woodenFish;
    private Button autoKnockBtn;
    private ImageView mallet;

    private int meritCount = 0;
    private final Handler autoKnockHandler = new Handler(Looper.getMainLooper());
    private Runnable autoKnockRunnable;
    private boolean isAutoKnocking = false;
    private Animation strikeAnimation;
    private boolean isKnocking = false;

    private SoundPool soundPool;
    private int knockSoundId;
    private boolean isSoundLoaded = false;

    private final List<TextView> plusOnePool = new ArrayList<>();
    private int poolIndex = 0;
    private static final int POOL_SIZE = 15;

    public static final String PREFS_NAME = "CongDucPrefs";
    public static final String PREF_MERIT_COUNT_KEY = "meritCount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContainer = findViewById(R.id.main_container);
        meritCountElement = findViewById(R.id.tv_merit_counter);
        woodenFish = findViewById(R.id.iv_wooden_fish);
        autoKnockBtn = findViewById(R.id.btn_auto_knock);
        mallet = findViewById(R.id.iv_mallet);
        strikeAnimation = AnimationUtils.loadAnimation(this, R.anim.strike_animation);

        initSoundPool();
        initPlusOnePool();

        loadMerit();
        woodenFish.setOnClickListener(v -> knock());
        autoKnockBtn.setOnClickListener(v -> toggleAutoKnock());
    }

    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build();
        knockSoundId = soundPool.load(this, R.raw.sound, 1);
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> isSoundLoaded = (status == 0));
    }

    private void initPlusOnePool() {
        // XÓA BỎ việc load animation ở đây: plusOneAnimation = ...
        for (int i = 0; i < POOL_SIZE; i++) {
            TextView plusOneText = new TextView(this);
            plusOneText.setText("+1 Công Đức");
            plusOneText.setTextColor(Color.parseColor("#c8a064"));
            plusOneText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            plusOneText.setTypeface(null, Typeface.BOLD);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.ABOVE, R.id.fish_container);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.setMargins(0, 0, 0, 20);
            plusOneText.setLayoutParams(params);
            plusOneText.setVisibility(View.GONE);
            mainContainer.addView(plusOneText);
            plusOnePool.add(plusOneText);
        }
    }

    private void knock() {
        if (isKnocking) return;
        isKnocking = true;

        meritCount++;
        meritCountElement.setText("Công đức: " + meritCount);
        saveMerit();

        if (isSoundLoaded) {
            soundPool.play(knockSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }

        woodenFish.setImageResource(R.drawable.fish_open);
        animateFish();
        animateMallet();
        updateAllWidgets();
        showPlusOneAnimation();

        autoKnockHandler.postDelayed(() -> {
            woodenFish.setImageResource(R.drawable.fish_closed);
            isKnocking = false;
        }, 150);
    }

    private void showPlusOneAnimation() {
        TextView plusOneText = plusOnePool.get(poolIndex);
        poolIndex = (poolIndex + 1) % POOL_SIZE;

        plusOneText.clearAnimation();
        plusOneText.setVisibility(View.VISIBLE);

        // --- SỬA LỖI: Load một instance Animation mới mỗi lần ---
        // Điều này đảm bảo mỗi TextView có một chu trình animation riêng biệt với listener của riêng nó.
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.plus_one_animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                plusOneText.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        plusOneText.startAnimation(animation);
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
                    autoKnockHandler.postDelayed(this, 300);
                }
            };
            autoKnockHandler.post(autoKnockRunnable);
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
        autoKnockHandler.removeCallbacksAndMessages(null);
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}