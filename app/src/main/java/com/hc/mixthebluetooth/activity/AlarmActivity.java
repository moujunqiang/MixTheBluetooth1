package com.hc.mixthebluetooth.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


/**
 * 闹钟响应界面 - 注意两个权限
 * <p>
 * <!--震动权限-->
 * <uses-permission android:name="android.permission.VIBRATE" />
 * <!--解锁权限-->
 * <uses-permission android:name="android.permission.WAKE_LOCK" />
 *
 */
public class AlarmActivity extends AppCompatActivity {
    private PowerManager.WakeLock mWakelock;
    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;
    private AlertDialog.Builder dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        startMedia();
        startVibrator();
        createDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 唤醒屏幕
        acquireWakeLock();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseWakeLock();
    }

    /**
     * 唤醒屏幕
     */
    private void acquireWakeLock() {
        if (mWakelock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                                | PowerManager.SCREEN_DIM_WAKE_LOCK,
                        this.getClass().getCanonicalName());
                mWakelock.acquire(5 * 60 * 1000L /*5 minutes*/);
            }
        }
    }

    /**
     * 释放锁屏
     */
    private void releaseWakeLock() {
        if (mWakelock != null && mWakelock.isHeld()) {
            mWakelock.release();
            mWakelock = null;
        }
    }

    /**
     * 开始播放铃声
     */
    private void startMedia() {
        try {
            if (mMediaPlayer == null)
                mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)); // 铃声类型为默认闹钟铃声
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 震动 - 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
     */
    private void startVibrator() {
        if (mVibrator == null)
            mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator != null) {
            long[] pattern = {500, 1000, 500, 1000}; // 停止 开启 停止 开启
            mVibrator.vibrate(pattern, 0);
        }
    }

    // 创建Dialog
    private void createDialog() {
        if (dialog == null)
            dialog = new AlertDialog.Builder(this)
//                .setIcon(R.mipmap.ic_launcher)
                    .setTitle("健康预警")
                    .setMessage("超出健康数值")
                    .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            mMediaPlayer.stop();
                            mVibrator.cancel();
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            mMediaPlayer.stop();
                            mVibrator.cancel();
                            dialog.dismiss();
                            finish();
                        }
                    });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer!=null){
            mMediaPlayer.stop();
            mMediaPlayer=null;
        }
    }
}
