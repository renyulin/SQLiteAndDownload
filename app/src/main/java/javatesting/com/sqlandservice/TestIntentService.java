package javatesting.com.sqlandservice;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

public class TestIntentService extends IntentService {

    public TestIntentService() {
        super("TestIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.e("TestIntentService", Thread.currentThread().getThreadGroup().getName());
    }
}
