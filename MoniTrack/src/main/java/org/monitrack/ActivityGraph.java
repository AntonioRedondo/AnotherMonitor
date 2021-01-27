

package org.monitrack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.util.Linkify;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import android.widget.TextView;
import android.net.Uri;

public class ActivityGraph extends Activity {
    TextView tv,tv1,tv2,tv3;
    private static final int PICKFILE_RESULT_CODE = 8778;
    private StringBuilder text = new StringBuilder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

            Button mBCsv = (Button) findViewById(R.id.button);
            mBCsv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    openFolder();
                }
            });
        }


    public void openFolder()
    {

        Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DropsyncFiles");
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, PICKFILE_RESULT_CODE);


    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {

        tv=(TextView)findViewById(R.id.txtcsv);
        tv1=(TextView)findViewById(R.id.txtcsv1);
        tv2=(TextView)findViewById(R.id.txtcsv2);
        tv3=(TextView)findViewById(R.id.txtcsv3);
        

        Uri uri = data.getData();
        String path = uri.getPath();
        File source = new File(path);
        String filename = uri.getLastPathSegment();
        String fname =path +filename;
        try {
            File csvfile = new File(fname);
            CSVReader reader = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DropsyncFiles/"+filename));
            String mLine="";
            String[] nextLine;
            int i=0;
            while ((nextLine = reader.readNext()) != null) {
                if(i==0) {
                    tv1.append( nextLine[1]+ nextLine[2] + "\n" + nextLine[3]+ nextLine[4]+" \n"  + nextLine[5] +":"+ nextLine[6]);
                ++i;
                }
                else if(i==1){
                    tv2.append(nextLine[0]+" | " + nextLine[1]+" | "  + nextLine[2] + " | "+ nextLine[3]+ " | "  + nextLine[4]+ " | "  + nextLine[5] + " | " + nextLine[6]+ " | " + nextLine[7]+ " | "  + nextLine[8]);
                    ++i;
                }
                else{
                    tv3.append(nextLine[0]+ " | "+ nextLine[1]+ " | "  + nextLine[2] +  " | " + nextLine[3]+ " | " + nextLine[4]+ " | "  + nextLine[5] +  " | " + nextLine[6]+ " | " + nextLine[7]+ " | "  + nextLine[8]+"\n");

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "The specified file was not found", Toast.LENGTH_SHORT).show();
        }
        tv.setText(filename);



    }

    public String getPath(Uri uri) {

        String path = null;
        String[] projection = { MediaStore.Files.FileColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if(cursor == null){
            path = uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}