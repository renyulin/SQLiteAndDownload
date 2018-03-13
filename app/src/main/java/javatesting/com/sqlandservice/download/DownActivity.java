package javatesting.com.sqlandservice.download;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import javatesting.com.sqlandservice.R;
import javatesting.com.sqlandservice.TestService;

/**
 *
 */
public class DownActivity extends AppCompatActivity {
    private MyService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder = (MyService.DownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down);
        init();
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);
        addPerssion();
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
    }

    private void addPerssion() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void edit() {
        //        EditText editText = findViewById(R.id.dddddd);
//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Log.e("ddddddddddddddddddddd", charSequence + "");
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
    }

    private boolean aBoolean;

    public void downloadClick(View view) {
//        if (downloadBinder == null) {
//            return;
//        }
        switch (view.getId()) {
            case R.id.start:
                String url;
                if (aBoolean) {
                    url = "https://raw.githubusercontent.com/guolindev/eclipse/master/eclipse-inst-win64.exe";
                } else {
                    aBoolean = !aBoolean;
                    url = "http://download.fangxiaoer.com/download/agentV2.1.1.apk";
                }
                downloadBinder.startDownload(url);
                break;
            case R.id.pause:
                downloadBinder.pauseDownload();
                break;
            case R.id.cancel:
                downloadBinder.cancelDownload();
                break;
            case R.id.alarm:
                startService(new Intent(this, AlarmService.class));
                break;
            case R.id.floatingActionButton:
                snackInit(view);
                break;
            case R.id.serviceStartTest:
                Intent testStart = new Intent(this, TestService.class);
                startService(testStart);
                break;
            case R.id.serviceStopTest:
                Intent testStop = new Intent(this, TestService.class);
                stopService(testStop);
                break;
        }
    }

    private void snackInit(View view) {
        Snackbar.make(view, "Data delete", Snackbar.LENGTH_LONG)
                .setText("txt")
//                .setAction("dddddd", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Toast.makeText(DownActivity.this,
//                                "Data ddddddd", Toast.LENGTH_LONG).show();//复原
//                    }
//                })
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(DownActivity.this,
                                "Data Restored", Toast.LENGTH_LONG).show();//复原
                    }
                }).setActionTextColor(Color.parseColor("#ff5200"))
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onShown(Snackbar sb) {
                        super.onShown(sb);
                    }

                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "permission add success", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "please add permission too", Toast.LENGTH_LONG).show();
                    addPerssion();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toobar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.backUp:
                Toast.makeText(this, "backUp", Toast.LENGTH_LONG).show();
                break;
            case R.id.delete:
                Toast.makeText(this, "delete", Toast.LENGTH_LONG).show();
                break;
            case R.id.setting:
                Toast.makeText(this, "setting", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
        return true;
    }
}
