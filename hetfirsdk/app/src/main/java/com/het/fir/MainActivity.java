package com.het.fir;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.het.fir.util.RxZipTool;
import com.het.ws.WsSDK;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void testZip(Context context) {
        File file = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            file = context.getExternalFilesDir("test.zix");
        }
        File zz = RxZipTool.unzip(file);
        //List<File> dirs = RxZipTool.unZipCurrDir(file.getAbsolutePath(),"tmp");
        //System.out.println("===="+dirs.toString());
        //RxFileTool.moveDir(dirs.get(0).getParentFile(),dirs.get(0).getParentFile().getParentFile());

        System.out.println("");

    }

    public void onLogPrint(View view) {
        System.out.println("System.out.println");
        Log.i("Android log tag ","Android log msg...onLogPrint");
    }

    public void onZip(View view) {
        testZip(this);
    }

    public void onStartWebSocketServer(View view) {
        WsSDK.bootstrap(this);
    }

    public void onStopWebSocketServer(View view) {
        System.out.println("System.out.println");
        Log.i("Android log tag ","Android log msg...");
        WsSDK.shutdown(this);
    }
}
