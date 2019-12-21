package com.het.fir;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.het.fir.util.RxFileTool;
import com.het.fir.util.RxZipTool;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void testZip(Context context) {
        File file = context.getExternalFilesDir("test.zix");
        File zz =  RxZipTool.unzip(file);
        //List<File> dirs = RxZipTool.unZipCurrDir(file.getAbsolutePath(),"tmp");
        //System.out.println("===="+dirs.toString());
        //RxFileTool.moveDir(dirs.get(0).getParentFile(),dirs.get(0).getParentFile().getParentFile());

        System.out.println("");

    }

    public void onZip(View view) {
        testZip(this);
    }
}
