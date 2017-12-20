package javatesting.com.sqlandservice.download;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AlarmService extends Service {
    public AlarmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (null != intent.getStringExtra("data")) {
            Log.e("AlarmServiceMsg", intent.getStringExtra("data"));
        } else {
            Log.e("AlarmServiceMsg", "start");
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
        if (null != intent.getStringExtra("data")) {
            Log.e("AlarmServiceMsg", intent.getStringExtra("data"));
        } else {
            Log.e("AlarmServiceMsg", "start");
        }
        alarm();
        return super.onStartCommand(intent, flags, startId);
    }

    private void alarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        /**
         * SystemClock.elapsedRealtime()
         * 系统开机至今所经历时间的毫秒数
         * System.currentTimeMillis()
         * 从1970.1.1.零点至今所经历的毫秒数
         * ELAPSED_REALTIME  让定时任务的出发时间从系统开机时算起 不唤醒CPU  --SystemClock
         * ELAPSED_REALTIME_WAKEUP  让定时任务的出发时间从系统开机时算起 唤醒CPU  --SystemClock
         * RTC  让定时任务的出发时间从1970.1.1.零点算起 不唤醒CPU  --System
         * RTC_WAKEUP  让定时任务的出发时间从1970.1.1.零点算起 唤醒CPU  --System
         */
        Intent intent = new Intent(this, AlarmService.class);
        intent.putExtra("data", "come on baby");
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        long triggerAtTime = System.currentTimeMillis() + 10 * 1000;
        //setExact 准确执行时间   set系统统一执行，时间不准确但是属于安卓的系统优化，主要是省电
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
//        }
        manager.setExact(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
    }
}
