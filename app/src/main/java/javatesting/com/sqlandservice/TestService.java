package javatesting.com.sqlandservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TestService extends Service {
    public TestService() {
    }

    private TestBinder testBinder = new TestBinder();

    class TestBinder extends Binder {
        public void startSomething() {
            Log.e("TestService", "Binder startSomething");
        }

        public int getProgress(int pro) {
            Log.e("TestService", "Binder getProgress");
            return pro;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("TestService", "Service onBind");
        return testBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TestService", "Service onStartCommand");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //...
//                stopSelf();
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.e("TestService", "Service onCreate");
        super.onCreate();
    }
}
