package cc.dync.audio_manager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Objects;

public class MediaPlayerService extends Service {
    private static final String ACTION_NEXT = "MediaPlayerService_next";
    private static final String ACTION_PREVIOUS = "MediaPlayerService_previous";
    private static final String ACTION_PLAY_OR_PAUSE = "MediaPlayerService_playOrPause";
    private static final String ACTION_STOP = "MediaPlayerService_stop";
    private static final String NOTIFICATION_CHANNEL_ID = "MediaPlayerService_1100";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消Notification
        if (notificationManager != null)
            notificationManager.cancel(NOTIFICATION_PENDING_ID);
        stopForeground(true);
        // 停止服务
        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupNotification();
    }

    // 定义Binder类-当然也可以写成外部类
    private ServiceBinder serviceBinder = new ServiceBinder();

    public class ServiceBinder extends Binder {
        Service getService() {
            return MediaPlayerService.this;
        }
    }


    public enum Events {
        next, previous, playOrPause, stop, binder
    }

    public interface ServiceEvents {
        void onEvents(Events events, Object... args);
    }

    private static ServiceEvents serviceEvents;
    private static MediaPlayerService bindService;
    private static boolean isBindService = false;
    private static Context context;

    // 绑定服务 必须先调用 registerReceiver
    public static void bindService(ServiceEvents serviceEvents) {
        MediaPlayerService.serviceEvents = serviceEvents;

        if (!MediaPlayerService.isBindService) {
            Intent intent = new Intent(context, MediaPlayerService.class);
            /*
             * Service：Service的桥梁
             * ServiceConnection：处理链接状态
             * flags：BIND_AUTO_CREATE, BIND_DEBUG_UNBIND, BIND_NOT_FOREGROUND, BIND_ABOVE_CLIENT, BIND_ALLOW_OOM_MANAGEMENT, or BIND_WAIVE_PRIORITY.
             */
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            if (serviceEvents != null) serviceEvents.onEvents(Events.binder, bindService);
        }

    }

    /// 通知事件处理，只能加载一次，否则会重复
    public static void registerReceiver(Context context) {
        MediaPlayerService.context = context;

        BroadcastReceiver playerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("action", intent.getAction());
                switch (Objects.requireNonNull(intent.getAction())) {
                    case ACTION_NEXT:
                        serviceEvents.onEvents(Events.next);
                        break;
                    case ACTION_PREVIOUS:
                        serviceEvents.onEvents(Events.previous);
                        break;
                    case ACTION_PLAY_OR_PAUSE:
                        serviceEvents.onEvents(Events.playOrPause);
                        break;
                    case ACTION_STOP:
                        serviceEvents.onEvents(Events.stop);
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREVIOUS);
        intentFilter.addAction(ACTION_PLAY_OR_PAUSE);
        intentFilter.addAction(ACTION_STOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // اندروید ۱۳ یا بالاتر: باید یکی از حالت‌های EXPORT تعیین شود
            context.registerReceiver(playerReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            // نسخه‌های قدیمی‌تر: روش معمولی
            context.registerReceiver(playerReceiver, intentFilter);
        }
    }


    // 解除绑定
    public static void unBind(Context context) {
        if (isBindService) {
            bindService.onDestroy();
            context.unbindService(serviceConnection);
            isBindService = false;
        }
    }

    /**
     * serviceConnection是一个ServiceConnection类型的对象，它是一个接口，用于监听所绑定服务的状态
     */
    private static ServiceConnection serviceConnection = new ServiceConnection() {
        /**
         * 该方法用于处理与服务已连接时的情况。
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServiceBinder binder = (ServiceBinder) service;
            bindService = (MediaPlayerService) binder.getService();
            isBindService = true;
            if (serviceEvents != null) serviceEvents.onEvents(Events.binder, bindService);
        }

        /**
         * 该方法用于处理与服务断开连接时的情况。
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bindService = null;
        }

    };

    //    private static final int DELETE_PENDING_REQUESTS = 1022;
    private static final int CONTENT_PENDING_REQUESTS = 1023;
    private static final int NEXT_PENDING_REQUESTS = 1024;
    private static final int PLAY_PENDING_REQUESTS = 1025;
    private static final int STOP_PENDING_REQUESTS = 1026;
    private static final int NOTIFICATION_PENDING_ID = 1;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private RemoteViews views;

 private void setupNotification() {
    // Intent اصلی (Explicit)
    Intent intent = new Intent(this, AudioManagerPlugin.class);
    PendingIntent cpi;
    
    // فلگ‌های PendingIntent
    int contentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        contentFlags |= PendingIntent.FLAG_IMMUTABLE;
    }
    cpi = PendingIntent.getActivity(this, 1023, intent, contentFlags);
    
    views = new RemoteViews(getPackageName(), R.layout.layout_mediaplayer);

    // دکمه بعدی
    Intent intentNext = new Intent(ACTION_NEXT);
    intentNext.setPackage(getPackageName()); // Explicit برای Broadcast درون برنامه
    int nextFlags = PendingIntent.FLAG_CANCEL_CURRENT;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        nextFlags |= PendingIntent.FLAG_IMMUTABLE;
    }
    PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 1024, intentNext, nextFlags);
    views.setOnClickPendingIntent(R.id.iv_next, nextPendingIntent);

    // دکمه پخش/مکث
    Intent intentPlay = new Intent(ACTION_PLAY_OR_PAUSE);
    intentPlay.setPackage(getPackageName());
    int playFlags = PendingIntent.FLAG_CANCEL_CURRENT;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        playFlags |= PendingIntent.FLAG_IMMUTABLE;
    }
    PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 1025, intentPlay, playFlags);
    views.setOnClickPendingIntent(R.id.iv_pause, playPendingIntent);

    // دکمه توقف
    Intent intentStop = new Intent(ACTION_STOP);
    intentStop.setPackage(getPackageName());
    int stopFlags = PendingIntent.FLAG_CANCEL_CURRENT;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        stopFlags |= PendingIntent.FLAG_IMMUTABLE;
    }
    PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 1026, intentStop, stopFlags);
    views.setOnClickPendingIntent(R.id.iv_cancel, stopPendingIntent);

    // ساخت Notification
    builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("")
            .setContentText("")
            .setAutoCancel(false)
            .setContentIntent(cpi)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // اینجا NotificationCompat استفاده شده
            .setContent(views);

    // ایجاد کانال Notification (برای API 26+)
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notification display",
                NotificationManager.IMPORTANCE_LOW
        );
        notificationManager.createNotificationChannel(channel);
    }

    // شروع سرویس پیش‌زمینه
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        startForeground(NOTIFICATION_PENDING_ID, builder.build(), 2);
    } else {
        startForeground(NOTIFICATION_PENDING_ID, builder.build());
    }
}

    void updateCover(Bitmap bitmap) {
        views.setImageViewBitmap(R.id.image, bitmap);
        notificationManager.notify(NOTIFICATION_PENDING_ID, builder.build());
    }

    void updateCover(int srcId) {
        views.setImageViewResource(R.id.image, srcId);
    }

    // 更新Notification
    void updateNotification(boolean isPlaying, String title, String desc) {
        if (views != null) {
            views.setTextViewText(R.id.tv_name, title);
            if (desc != null) views.setTextViewText(R.id.tv_author, desc);
            if (isPlaying) {
                views.setImageViewResource(R.id.iv_pause, android.R.drawable.ic_media_pause);
            } else {
                views.setImageViewResource(R.id.iv_pause, android.R.drawable.ic_media_play);
            }
        }

        // 刷新notification
        notificationManager.notify(NOTIFICATION_PENDING_ID, builder.build());
    }
}
