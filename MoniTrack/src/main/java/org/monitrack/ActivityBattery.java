package org.monitrack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ActivityBattery extends Activity {

    TextView tv1,tv2,tv3,tv4;

    private String s;
    private BufferedReader r1,r2,r3,r4,r5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bat);
        tv1 = (TextView) findViewById(R.id.bat1);
        tv2 = (TextView) findViewById(R.id.bat2);
        tv3 = (TextView) findViewById(R.id.bat3);
        tv4 = (TextView) findViewById(R.id.bat4);
        Intent sample = new Intent("android.intent.action.POWER_USAGE_SUMMARY");



            Handler h1 = new Handler();
            final Runnable r = new Runnable() {
                int count = 0;
                @Override
                public void run() {
                    count++;
                    try {
                        r1 = new BufferedReader(new FileReader("sys/class/power_supply/battery/status"));
                        r2 = new BufferedReader(new FileReader("sys/class/power_supply/battery/capacity"));
                        r3 = new BufferedReader(new FileReader("sys/class/power_supply/battery/health"));

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        s = r1.readLine();
                       tv1.setText("\nBattery Status: "+s);
                        s = r2.readLine();
                        tv2.setText("\nBattery Capacity: "+s+"%");
                        s = r3.readLine();
                        tv3.setText("\nBattery Health: "+s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    h1.postDelayed(this, 1000); //ms

                }

            };
            h1.postDelayed(r, 1000); // one second in ms


        try {
            r4 = new BufferedReader(new FileReader("sys/class/power_supply/battery/technology"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            s = r4.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tv4.setText("\nBattery Technology: "+s);
        Button mBCsv = (Button) findViewById(R.id.powbutt);
        mBCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(sample);

            }
        });

    }


}
