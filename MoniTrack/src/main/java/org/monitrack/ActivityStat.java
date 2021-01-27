

package org.monitrack;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.os.Bundle;
import android.view.View;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.widget.TextView;

public class ActivityStat extends Activity {
    TextView tv;
    private Thread t1;
    private boolean intr=false;
    private Button btn1;
    private Button btn2;
    private Button btn3;
    private int x=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);

        tv = (TextView) findViewById(R.id.cmdOp);
        btn1 = (Button) findViewById(R.id.button1);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        topthread();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                x=1;
                if(!(intr)) {
                    t1.interrupt();
                }
                else{
                    cpuInterr();
                }

            }

        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topthread();
            }

        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                x=2;
                if(!(intr)) {
                    t1.interrupt();
                }
                else{
                    devInterr();
                }
            }

        });
    }



    public String runAsRoot(String s) {

        try {
            Process process = Runtime.getRuntime().exec(s);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();

            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

public void cpuInterr(){
    new Handler(Looper.getMainLooper()).post(new Runnable(){
        @Override
        public void run() {
            tv.setText(runAsRoot("cat /proc/cpuinfo"));
        }
    });
}
    public void devInterr(){
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {

                tv.setText(
                        "SERIAL:\t" + Build.SERIAL + "\n" +
                                "MODEL:\t" + Build.MODEL + "\n" +
                                "ID:\t" + Build.ID + "\n" +
                                "Manufacture:\t" + Build.MANUFACTURER + "\n" +
                                "Brand:\t" + Build.BRAND + "\n" +
                                "Type:\t" + Build.TYPE + "\n" +
                                "User:\t" + Build.USER + "\n" +
                                "BASE:\t" + Build.VERSION_CODES.BASE + "\n" +
                                "INCREMENTAL:\t" + Build.VERSION.INCREMENTAL + "\n" +
                                "SDK:\t" + Build.VERSION.SDK + "\n" +
                                "BOARD:\t" + Build.BOARD + "\n" +
                                "BRAND:\t" + Build.BRAND + "\n" +
                                "HOST:\t" + Build.HOST + "\n" +
                                "FINGERPRINT:\t"+Build.FINGERPRINT + "\n" +
                                "Version Code:\t" + Build.VERSION.RELEASE
                );
            }
        });
    }
    public void topthread(){
        intr=false;
        t1 = new Thread() {

            @Override
            public void run() {
                try {
                    while (!t1.isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText(runAsRoot("top -n 1 -d 1"));
                            }
                        });
                    }
                }
                catch (InterruptedException e) {
                    intr=true;
                    if(x==1){
                    cpuInterr();
                    }
                    else if(x==2){
                        devInterr();
                    }
                }
            }
        };

        t1.start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
