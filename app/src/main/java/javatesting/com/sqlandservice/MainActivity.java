package javatesting.com.sqlandservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import javatesting.com.sqlandservice.download.DownActivity;
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
        }
    }
}
