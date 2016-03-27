package com.example.abhishekmadan.mypaint.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.abhishekmadan.mypaint.R;
import com.example.abhishekmadan.mypaint.activity.DrawingBoard;
import com.example.abhishekmadan.mypaint.util.Constants;

/*
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import java.util.ArrayList;
*/


/**
 * Service to upload the image to the server : '000webHost'
 */
public class UploadImageToServer extends Service {

    private String result=null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.mipmap.ic_launcher_new);
        builder.setContentTitle("My Paint");
        builder.setTicker("MyPaint image upload status!");
        builder.setProgress(10,0,true);
        builder.setContentText("Upload in progress!");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(21,builder.build());

        String imageName = intent.getStringExtra("image_name");
   /*     ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("image", DrawingBoard.sEncodedImage));
        nameValuePairs.add(new BasicNameValuePair("image_name", imageName));
        Thread upload = new StartUpload(nameValuePairs);
        upload.start();
   */     return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class StartUpload extends Thread{

       /* private ArrayList<NameValuePair> valuePairs;
        public StartUpload(ArrayList<NameValuePair> nameValuePairs){
            valuePairs = nameValuePairs;
        }
*/
        @Override
        public void run() {
            try
            {
  /*              HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(Constants.UPLOAD_URL);
                httppost.setEntity(new UrlEncodedFormEntity(valuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity);
  */          }
            catch(Exception e){
                Log.d("inside", e.toString());
                result = "Image Uploading failed!";
            }
            showNotification(result);
        }
    }
    /**
     * Method to show a notification on receiving a response status from the server.
     * @param result is the string indicating the status of the upload operation.
     */
    public void showNotification(String result){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.mipmap.ic_launcher_new);
        builder.setContentTitle("My Paint");
        builder.setTicker("MyPaint image upload status!");
        builder.setContentText(result);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(21,builder.build());
    }

}
