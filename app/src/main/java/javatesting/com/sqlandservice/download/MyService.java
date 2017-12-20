package javatesting.com.sqlandservice.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

import javatesting.com.sqlandservice.R;

public class MyService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Download...", progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            //下载成功时前台通知服务关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(-1, getNotification("Download Success", -1));
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            //下载失败，通知服务，并发送通知
            stopForeground(true);
            getNotificationManager().notify(-1, getNotification("Download Failed", -1));
            Toast.makeText(MyService.this, "Download Failed", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(MyService.this, "Download Paused", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(MyService.this, "Download Canceled", Toast.LENGTH_LONG).show();
        }
    };

    public MyService() {
    }

    private DownloadBinder binder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class DownloadBinder extends Binder {
        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1, getNotification("Download...", 0));
                Toast.makeText(MyService.this, "Download...", Toast.LENGTH_LONG).show();
            }
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            } else {
                if (null != downloadUrl) {
                    //取消须将文件删除，通知关闭
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(MyService.this, "Canceled", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, DownActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress > 0) {
            //>0 显示进度
            builder.setContentTitle(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }
}