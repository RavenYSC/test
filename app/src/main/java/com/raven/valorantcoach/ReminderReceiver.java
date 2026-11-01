package com.raven.valorantcoach;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import java.util.Calendar;

public class ReminderReceiver extends android.content.BroadcastReceiver {
    public static final String CHANNEL="training";
    static final String[] TITLES={"Time to train", "Check your training progress", "Finish strong"};
    static final String[] BODIES={"Warm up and set your focus for today.","Check off completed drills and take a recovery break.","Log match results and review your mistakes."};
    public static void schedule(Context context){
        SharedPreferences p=context.getSharedPreferences("settings",Context.MODE_PRIVATE);
        AlarmManager alarms=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        for(int i=0;i<3;i++){
            Intent intent=new Intent(context,ReminderReceiver.class).putExtra("kind",i);
            PendingIntent pi=PendingIntent.getBroadcast(context,900+i,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            alarms.cancel(pi);
            if(!p.getBoolean("enabled",false))continue;
            int hour=p.getInt("hour"+i,new int[]{10,14,19}[i]);
            int minute=p.getInt("minute"+i,0);
            Calendar at=Calendar.getInstance();at.set(Calendar.HOUR_OF_DAY,hour);at.set(Calendar.MINUTE,minute);at.set(Calendar.SECOND,0);at.set(Calendar.MILLISECOND,0);
            if(at.getTimeInMillis()<=System.currentTimeMillis())at.add(Calendar.DAY_OF_YEAR,1);
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at.getTimeInMillis(),pi);
        }
    }
    @Override public void onReceive(Context context,Intent intent){
        if(!context.getSharedPreferences("settings",Context.MODE_PRIVATE).getBoolean("enabled",false))return;
        int kind=intent.getIntExtra("kind",0);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Training reminders",NotificationManager.IMPORTANCE_DEFAULT));
        Intent launch=new Intent(context,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(context,17,launch,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification=new Notification.Builder(context,CHANNEL).setSmallIcon(com.raven.valorantcoach.R.drawable.icon).setContentTitle(TITLES[kind]).setContentText(BODIES[kind]).setAutoCancel(true).setContentIntent(pi).build();
        try {nm.notify(600+kind,notification);} catch(SecurityException ignored) { }
        schedule(context);
    }
}
