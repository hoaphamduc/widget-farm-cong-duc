package com.example.farmcongduc;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.RemoteViews;
import androidx.annotation.Nullable;

public class WidgetUpdateService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && CongDucWidgetProvider.ACTION_KNOCK_WIDGET.equals(intent.getAction())) {
            // Tăng công đức
            SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
            int currentCount = prefs.getInt(MainActivity.PREF_MERIT_COUNT_KEY, 0);
            currentCount++;

            // Lưu lại
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(MainActivity.PREF_MERIT_COUNT_KEY, currentCount);
            editor.apply();

            // Phát âm thanh
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sound);
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            mediaPlayer.start();

            // --- BẮT ĐẦU "ANIMATION" ---

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            ComponentName thisWidget = new ComponentName(this, CongDucWidgetProvider.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            // BƯỚC 1: Cập nhật giao diện sang trạng thái "BỊ GÕ" ngay lập tức
            RemoteViews strikeViews = new RemoteViews(getPackageName(), R.layout.widget_layout);
            strikeViews.setTextViewText(R.id.tv_widget_merit_count, "" + currentCount);
            strikeViews.setImageViewResource(R.id.iv_widget_fish, R.drawable.fish_open); // Mở miệng
            strikeViews.setViewVisibility(R.id.iv_widget_mallet, View.VISIBLE); // Hiện gậy
            appWidgetManager.updateAppWidget(allWidgetIds, strikeViews);


            // BƯỚC 2: Dùng Handler để lên lịch reset giao diện về trạng thái "BÌNH THƯỜNG"
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                RemoteViews normalViews = new RemoteViews(getPackageName(), R.layout.widget_layout);
                normalViews.setImageViewResource(R.id.iv_widget_fish, R.drawable.fish_closed); // Đóng miệng
                normalViews.setViewVisibility(R.id.iv_widget_mallet, View.GONE); // Ẩn gậy
                // Cần set lại cả text count, phòng trường hợp có click khác xảy ra
                SharedPreferences latestPrefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
                int latestCount = latestPrefs.getInt(MainActivity.PREF_MERIT_COUNT_KEY, 0);
                normalViews.setTextViewText(R.id.tv_widget_merit_count, "" + latestCount);

                appWidgetManager.updateAppWidget(allWidgetIds, normalViews);

                // Dừng service sau khi hoàn thành animation
                stopSelf();
            }, 200); // 200 mili giây sau sẽ reset
        }
        // Trả về START_NOT_STICKY để service không tự khởi động lại
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}