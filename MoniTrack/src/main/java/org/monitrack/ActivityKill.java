

package org.monitrack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ActivityKill extends Activity {

    private BroadcastReceiver receiverFinish = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

private Button mButton;
private Button mButton1;
private EditText mEdit;
    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kill);

        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

        mButton = (Button) findViewById(R.id.btn);
        mButton1 = (Button) findViewById(R.id.btn1);
        mEdit   = (EditText)findViewById(R.id.editText1);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                am.killBackgroundProcesses(mEdit.getText().toString());
                Toast.makeText(ActivityKill.this, "Process Killed Successfully.", Toast.LENGTH_SHORT).show();
            }
        });
        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityKill.this, ActivityStat.class));    }
        });
    }




    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(receiverFinish, new IntentFilter(C.actionFinishActivity));
    }





    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiverFinish);
    }
}