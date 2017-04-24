package com.example.rex_wang.allapplication.utility;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Rex_Wang on 2017/4/24.
 */

public class DL {
    public static ProgressDialog createProgressDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context, android.R.style.Theme_Holo_Dialog);
        dialog.setMessage(message);
        dialog.setCancelable(false); //關閉點選其他空白區塊跳脫對話框
        return dialog;
    }
    //HttpConnection  GET
    public static InputStream getURL(String urlString) {
        InputStream is = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            is = connection.getInputStream();
        } catch (MalformedURLException e) {
            Log.i("網址格式有問題", e.getMessage());
        } catch (IOException e) {
            Log.i("連線出錯", e.getMessage());
        }
        return is;
    }

    //HttpConnection  PUT
    public static InputStream putURL(String urlString, String json) {
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(json.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();
            is = connection.getInputStream();
            return is;
        } catch (MalformedURLException e) {
            Log.i("URL 錯誤", e.getMessage());
        } catch (IOException e) {
            Log.i("Connection 錯誤", e.getMessage());
        }
        return is;
    }

    //InputStrem change to String
    public static String streamToString(InputStream is, String encoder) {
        String content = null;
        try {
            InputStreamReader reader = new InputStreamReader(is, encoder);
            char[] step = new char[10];
            int realLength = -1;
            StringBuilder builder = new StringBuilder();
            while ((realLength = reader.read(step)) > -1) {
                builder.append(step, 0, realLength);
            }
            content = builder.toString();
        } catch (UnsupportedEncodingException e) {
            Log.e("錯誤", e.getMessage());
        } catch (IOException e) {
            Log.e("錯誤", e.getMessage());
        }
        return content;
    }
    public static AlertDialog createTextDialog(Context context, String title, String message) {

        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton("確認", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
        return dialog;
    }
}
