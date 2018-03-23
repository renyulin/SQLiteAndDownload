package javatesting.com.sqlandservice;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import javatesting.com.sqlandservice.download.DownActivity;
import javatesting.com.sqlandservice.lite.LitePalActivity;
import javatesting.com.sqlandservice.pic.PicActivity;
import javatesting.com.sqlandservice.sq.SqlActivity;

/**
 * http://www.runoob.com/sqlite/sqlite-where-clause.html
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.SQLite:
                startActivity(new Intent(this, SqlActivity.class));
                break;
            case R.id.download:
                startActivity(new Intent(this, DownActivity.class));
                break;
            case R.id.LitePal:
                startActivity(new Intent(this, LitePalActivity.class));
                break;
            case R.id.serviceStartTest:
//                Intent testStart = new Intent(this, TestService.class);
//                startService(testStart);
                startTestService();
                break;
            case R.id.serviceStopTest:
//                Intent testStop = new Intent(this, TestService.class);
//                stopService(testStop);
                unbindTestService();
                break;
            case R.id.picAct:
                startActivity(new Intent(this, PicActivity.class));
                break;
        }
    }

    private void unbindTestService() {
//        Intent testStop = new Intent(this, TestService.class);
//        stopService(testStop);
        unbindService(serviceConnection);
    }

    private TestService.TestBinder testBinder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            testBinder = (TestService.TestBinder) iBinder;
            testBinder.getProgress(0);
            testBinder.startSomething();
            testBinder.link(new TestService.BinderFace() {
                @Override
                public void longData(final Object o) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, (String) o, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private void startTestService() {
        Intent testStart = new Intent(this, TestService.class);
        //BIND_AUTO_CREATE  自动创建，不运行onStartCommand方法
        startService(testStart);
        bindService(testStart, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
