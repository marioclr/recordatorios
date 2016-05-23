package com.mclr.mini.recordatorios;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Date;

/**
 * Created by mini on 22/05/16.
 */
public class RecordatorioAlarma extends BroadcastReceiver {
    public static final String TEXTO_RECORDATORIO = "TEXTO RECORDATORIO";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
        String textoRecordatorio = intent.getStringExtra(TEXTO_RECORDATORIO);
        Intent intentAction = new Intent(context, RecordatoriosActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intentAction, 0);
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Recordatorio!")
                .setWhen(new Date().getTime())
                .setContentText(textoRecordatorio)
                .setContentIntent(pi)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
