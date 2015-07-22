package com.microsoft.band.sdk.sampleapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Luan on 7/16/2015.
 */
public class MyService extends Service {


    private static final String TAG = "MyService";

    public Thread getDataThread = null;
    public Thread uploadDataThread = null;

    public static MyService service;
    private String android_id=null;

    public MyService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();
        service = this;



    }

    private boolean haveNetworkConnection(Context context)
    {
        boolean haveConnectedWifi = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo)
        {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
            {
                if (ni.isConnected())
                {
                    haveConnectedWifi = true;
                    Log.v("WIFI CONNECTION ", "AVAILABLE");
                } else
                {
                    Log.v("WIFI CONNECTION ", "NOT AVAILABLE");
                }
            }
        }
        return haveConnectedWifi;
    }

    private void  uploadFile(final Context ac){
        if(!haveNetworkConnection(ac)) return;
        android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if(android_id == null) return;
        Log.v("Device id",android_id);
        String[] fileList = ac.fileList();
        for(final String filename: fileList){
            final File myFile = new File(ac.getFilesDir(), filename);
            RequestParams params = new RequestParams();
            try {
                params.put("uploaded_file", myFile);
                params.add("device_id",android_id);
            } catch(FileNotFoundException e) {}

            // send request
            SyncHttpClient client = new SyncHttpClient();
            client.post("http://infolab.usc.edu/Luan/uploadData.php",
                    params, new AsyncHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                            // handle success response
                            if(!filename.equals("activity"))
                            myFile.delete();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                            // handle failure response
                        }
                    });

        }

        return;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG, "onStart");

        //check getDataThread
        if(uploadDataThread == null)
            uploadDataThread = new Thread(){
                @Override
                public void run() {
                    while(true){

                        uploadFile(MyService.service);
                        try {
                            Log.d(TAG,"Da vao day!");
                            sleep(300000);
                        }
                        catch(Exception e){}

                    }
                }


            };
        if(!uploadDataThread.isAlive())
            uploadDataThread.start();
        return START_STICKY;

    }

    public void onDestroy() {
        Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
