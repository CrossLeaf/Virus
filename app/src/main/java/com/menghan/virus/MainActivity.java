package com.menghan.virus;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends Activity {
    private TextView mTextView01;
    private EditText mEditText01;
    private Button mButton01;
    private String strURL = "";
    private String fileEx = "";
    private String fileNa = "";
    private String currentFilePath = "";
    private String currentTempFilePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView01 = (TextView) findViewById(R.id.mTextView1);
        mEditText01 = (EditText) findViewById(R.id.mEditText1);
        mButton01 = (Button) findViewById(R.id.mButton1);
        mEditText01.setText("https://dl.dropboxusercontent.com/u/102796189/test.apk");
        mButton01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*文建會下載至local端*/
                mTextView01.setText("下載中...");
                strURL = mEditText01.getText().toString();
                /*取得欲安裝程序的文件名稱*/
                fileEx = strURL.substring(strURL.lastIndexOf(".") + 1, strURL.length()).toLowerCase();
                fileNa = strURL.substring(strURL.lastIndexOf("/") + 1, strURL.lastIndexOf("."));
                Log.e("Tag", fileNa + "." + fileEx);
                getFile(strURL);
            }
        });
        mEditText01.setOnClickListener(new EditText.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditText01.setText("");
                mTextView01.setText("遠端安裝程序(請輸入URL)");
            }
        });
    }

    /*處理下載url文件自定義函數*/
    private void getFile(final String strPath) {
        try {
            if (strPath.equals(currentFilePath)) {
                getDataSource(strPath);
            }
            currentFilePath = strPath;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        getDataSource(strPath);
                    } catch (Exception e) {
                        Log.e("Tag", e.getMessage(), e);
                    }
                }
            };
            new Thread(r).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*取得遠端文件*/
    private void getDataSource(String strPath) throws Exception {
        if (!URLUtil.isNetworkUrl(strPath)) {
            mTextView01.setText("錯誤的URL");
        } else {
            /*取得URL*/
            URL myURL = new URL(strPath);
            /*創建連接*/
            URLConnection conn = myURL.openConnection();
            conn.connect();
            /*InputStream 下載文件*/
            InputStream is = conn.getInputStream();
            Log.e("Tag", "有 Stream");
            if (is == null) {
                Log.e("Tag", "stream is null.");
                throw new RuntimeException("stream is null.");
            }
            Log.e("Tag", "進入創建臨時文件");
            /*創建臨時文件*/
            File myTempFile = File.createTempFile(fileNa, "." + fileEx);
            Log.e("Tag", fileNa + "." + fileEx);
            /*取得臨時文件路徑*/
            currentTempFilePath = myTempFile.getAbsolutePath();
            /*將文件寫入暫存檔*/
            /**********這段要google一下***************/
            FileOutputStream fos = new FileOutputStream(myTempFile);
            Log.e("Tag", String.valueOf(myTempFile));
            Log.e("Tag", currentTempFilePath);
            byte buf[] = new byte[4096];
            do {
                int numread = is.read(buf);
                if (numread <= 0) {
                    break;
                }
                Log.e("Tag", numread + " ");
                fos.write(buf, 0, numread);
            } while (true);
            Log.e("Tag", "結束迴圈");
            /*打開文件進行安裝*/
            openFile(myTempFile);
            try {
                is.close();
            } catch (Exception e) {
                Log.e("TAG", "error : " + e.getMessage(), e);
            }
        }
    }

    /*在手機上打開文件的method*/
    private void openFile(File f) {
        Intent it = new Intent();
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        it.setAction(Intent.ACTION_VIEW);

        /*調用getMIMEType()來取得MimeType*/
        String type = getMIMEType(f);
        /*設置intent的file與MimeType*/
//        it.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.apk"), type);
        it.setDataAndType(Uri.fromFile(f), type);
        startActivity(it);
    }

    /*判斷文件MimeType的方法*/
    private String getMIMEType(File f) {
        String type;
        String fName = f.getName();
        /*取得擴展名*/
        String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

        /*依擴展名的類型決定MimeType*/
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") ||
                end.equals("ogg") || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") ||
                end.equals("jpeg") || end.equals("bmp")) {
            type = "image";
        } else if (end.equals("apk")) {
            type = "application/vnd.android.package-archive";
        } else {
            type = "*";
        }
        /*如果無法直接打開就跳出軟件列表給用戶選擇*/
        if (end.equals("apk")) {

        } else {
            type += "/*";
        }
        Log.e("Tag", type);
        return type;
    }

    @Override
    protected void onPause() {
        mTextView01 = (TextView) findViewById(R.id.mTextView1);
//        mTextView01.setText("下載成功");
        super.onPause();
    }


}
