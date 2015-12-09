package com.microsoft.band.sdk.sampleapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandCaloriesEvent;
import com.microsoft.band.sensors.BandCaloriesEventListener;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.SampleRate;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class SensingService extends Service {

    private static final String TAG = "Sensing Service";
    private BandClient client = null;
    public static ArrayList<String> heartRateList = new ArrayList<>();
    public static  ArrayList<String> acceleratorList = new ArrayList<>();
    public static ArrayList<String> caloriesList = new ArrayList<>();
    public static ArrayList<String> contactList = new ArrayList<>();
    public static ArrayList<String> distanceList = new ArrayList<>();
    public static ArrayList<String> gyroscopeList = new ArrayList<>();
    public static ArrayList<String> pedometerList = new ArrayList<>();
    public static ArrayList<String> skinTemperatureList = new ArrayList<>();
    public static ArrayList<String> uvEventsList = new ArrayList<>();
    public static ArrayList<String> locationList = new ArrayList<>();


    public  boolean isSensing = true;


    LocalBroadcastManager broadcaster;


    public void sendResult(String message, String type){
        Intent intent = new Intent("SENSING_RESULT");
        if(message != null) {
            intent.putExtra("message", message);
            intent.putExtra("type",type);
        }
        if(broadcaster!=null);
        broadcaster.sendBroadcast(intent);
    }

    public void checkIsSensing(){
        if(!isSensing)
            try {
                client.getSensorManager().unregisterAllListeners();
            } catch (BandIOException e) {
                e.printStackTrace();
            }

    }


    private void appendToList(final String string, final String sensor_type){

        checkIsSensing();
//        Log.d(TAG,"Sensing service is running!!!!");
        if(sensor_type.equals("accelerometer"))
        {
            acceleratorList.add(string);
            if(acceleratorList.size()>=1000)
                saveToFile(acceleratorList, "accelerometer_"+System.currentTimeMillis(),"accelerometer");

        }
        else if(sensor_type.equals("calories")) {
            caloriesList.add(string);
            if(caloriesList.size() >= 60)
                saveToFile(caloriesList,"calories_"+System.currentTimeMillis(),"calories");
        }
        else if(sensor_type.equals("contact")) {
            contactList.add(string);
            if(contactList.size() >= 60)
                saveToFile(contactList,"contact_"+System.currentTimeMillis(),"contact");
        }
        else if(sensor_type.equals("distance")) {
            distanceList.add(string);
            if(distanceList.size() >= 60)
                saveToFile(distanceList,"distance_"+System.currentTimeMillis(),"distance");
        }
        else if(sensor_type.equals("gyroscope")) {
            gyroscopeList.add(string);
            if(gyroscopeList.size() >= 60)
                saveToFile(gyroscopeList,"gyroscope_"+System.currentTimeMillis(),"gyroscope");
        }
        else if(sensor_type.equals("heartrate")) {
            heartRateList.add(string);
            if(heartRateList.size() >= 60)
                saveToFile(heartRateList,"heartrate_"+System.currentTimeMillis(),"heartrate");
        }
        else if(sensor_type.equals("pedometer")) {
            pedometerList.add(string);
            if(pedometerList.size() >= 60)
                saveToFile(pedometerList,"pedometer_"+System.currentTimeMillis(),"pedometer");
        }
        else if(sensor_type.equals("skin")) {
            skinTemperatureList.add(string);
            if(skinTemperatureList.size() >= 60)
                saveToFile(skinTemperatureList,"skin_"+System.currentTimeMillis(),"skin");
        }
        else if(sensor_type.equals("uv")) {
            uvEventsList.add(string);
            if(uvEventsList.size() >= 60)
                saveToFile(uvEventsList,"uv_"+System.currentTimeMillis(),"uv");
        }
        else if(sensor_type.equals("location") ){
            locationList.add(string);
            if(locationList.size() >= 50)
                saveToFile(locationList, "location_"+System.currentTimeMillis(),"location");
        }


    }


    public SensingService() {


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG, "Sensing Service onStart");

        //check getDataThread
       new appTask().execute();
        //getDataFromBand();
        broadcaster = LocalBroadcastManager.getInstance(this);
        return START_STICKY;



    }

    private void saveToFile(ArrayList<String> array, final String filename, final String directory ){

        if(array.size() == 0 ) return;

        Log.d("Write file", "Van dang chay va write file");
        if(filename.equals("activity"))
            Log.d("Write file", "writing activities");
        //save to file
        try{
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_APPEND);
                for(String string: array) {
                    outputStream.write(string.getBytes());

                }
                outputStream.close();
                array.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        catch(Exception e){};


    }
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        SensingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SensingService.this;
        }
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();



    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    private BandUVEventListener mUVEventListener = new BandUVEventListener() {
        @Override
        public void onBandUVChanged(BandUVEvent bandUVEvent) {
            if(bandUVEvent != null){
                appendToList(System.currentTimeMillis()+","+bandUVEvent.getUVIndexLevel()+","
                        + bandUVEvent.getTimestamp()+"\n","uv");
                sendResult("UV="+bandUVEvent.getUVIndexLevel(),"uv");
            }
        }
    };

    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null) {
        //
                appendToList(System.currentTimeMillis() + "," + event.getAccelerationX() + "," +
                                event.getAccelerationY() + "," + event.getAccelerationZ() + "," + event.getTimestamp() + "\n",
                        "accelerometer");
               // Log.d("Sensing",event.getAccelerationX()+",");
                sendResult("accX = "+event.getAccelerationX()+"\n"+
                            "accY= "+event.getAccelerationY()+"\n"+
                            "accZ= "+event.getAccelerationZ(),"accelerometer");
            }


        }
    };

    private BandCaloriesEventListener mCaloriesEventListener = new BandCaloriesEventListener() {
        @Override
        public void onBandCaloriesChanged(final BandCaloriesEvent bandCaloriesEvent) {
            if(bandCaloriesEvent != null){
                //do something update UI
                appendToList(System.currentTimeMillis()+","+bandCaloriesEvent.getCalories()+
                        ","+bandCaloriesEvent.getTimestamp()+"\n","calories");
                sendResult("Calories = "+bandCaloriesEvent.getCalories(),"calories");
            }
        }
    };

    private BandContactEventListener mContactEventListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(final BandContactEvent bandContactEvent) {
            if(bandContactEvent != null){
                appendToList(System.currentTimeMillis()+","+bandContactEvent.getContactState()
                        +","+bandContactEvent.getTimestamp()+"\n"  ,"contact");
                sendResult("contact = "+bandContactEvent.getContactState(),"contact");
            }
        }
    };

    private BandDistanceEventListener mDistanceEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(BandDistanceEvent bandDistanceEvent) {
            if(bandDistanceEvent != null){

                appendToList(System.currentTimeMillis()+","+bandDistanceEvent.getTotalDistance()+
                        ","+bandDistanceEvent.getSpeed()+","+bandDistanceEvent.getPace()+","+
                        bandDistanceEvent.getMotionType()+","+bandDistanceEvent.getTimestamp()+"\n" ,"distance");
                sendResult("distance = "+bandDistanceEvent.getTotalDistance()+"\n Motion type="+
                        bandDistanceEvent.getMotionType(),"distance");
            }
        }
    };

    private BandGyroscopeEventListener mGyroscopeEventListener = new BandGyroscopeEventListener() {
        @Override
        public void onBandGyroscopeChanged(BandGyroscopeEvent bandGyroscopeEvent) {
            if(bandGyroscopeEvent != null){

                appendToList(System.currentTimeMillis()+","+bandGyroscopeEvent.getAccelerationX()+
                                ","+bandGyroscopeEvent.getAccelerationY()+","+bandGyroscopeEvent.getAccelerationZ()+
                                ","+bandGyroscopeEvent.getAngularVelocityX()+","
                                +bandGyroscopeEvent.getAngularVelocityY()+","
                                +bandGyroscopeEvent.getAngularVelocityZ()+","
                                +bandGyroscopeEvent.getTimestamp()+"\n","gyroscope"
                );
                sendResult("accelerationX = "+ bandGyroscopeEvent.getAccelerationX()
                    + ", accelerationY="+bandGyroscopeEvent.getAccelerationY()+
                    "accelerationZ= "+bandGyroscopeEvent.getAccelerationZ(),"gyroscope");

            }
        }
    };

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
            if(bandHeartRateEvent!= null){

                appendToList(System.currentTimeMillis()+","
                        +bandHeartRateEvent.getHeartRate()+","
                        +bandHeartRateEvent.getQuality()+","
                        +bandHeartRateEvent.getTimestamp()+"\n","heartrate");
                sendResult("heart rate="+bandHeartRateEvent.getHeartRate()+",quality="+bandHeartRateEvent.getQuality(),
                        "heartrate");

            }
        }
    };

    private BandPedometerEventListener mPedometerEventListener = new BandPedometerEventListener() {
        @Override
        public void onBandPedometerChanged(BandPedometerEvent bandPedometerEvent) {
            if(bandPedometerEvent != null){
                appendToList(System.currentTimeMillis()+","+bandPedometerEvent.getTotalSteps()
                        +","+bandPedometerEvent.getTimestamp()+"\n","pedometer");
                sendResult("total steps = "+bandPedometerEvent.getTimestamp()+"\n","pedometer");
            }
        }
    };

    private BandSkinTemperatureEventListener mSkinTemperatureEventListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
            if(bandSkinTemperatureEvent != null){

                appendToList(System.currentTimeMillis()+","+bandSkinTemperatureEvent.getTemperature()+
                        ","+bandSkinTemperatureEvent.getTimestamp()+"\n","skin");
                sendResult("Skin temp = "+bandSkinTemperatureEvent.getTemperature()+"\n","skin");
            }
        }
    };

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        return ConnectionState.CONNECTED == client.connect().await();
    }

    public   class appTask extends AsyncTask<Void, Void, Void> {






        private HeartRateConsentListener heartRateConsentListener = new HeartRateConsentListener() {
            @Override
            public void userAccepted(boolean b) {
                if(b == true){
                    //register
                    try {
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                    } catch (BandException e) {
                        String exceptionMessage = "Unknown error occured: " + e.getMessage();
                    }
                }
            }
        };

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if(getConnectedBandClient()) {
                    Log.d(TAG, "Sensing Service do in background");
                    sendResult("Band is connected!!!!",null);
                    client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS128);
                    client.getSensorManager().registerCaloriesEventListener(mCaloriesEventListener);
                    client.getSensorManager().registerContactEventListener(mContactEventListener);
                    client.getSensorManager().registerDistanceEventListener(mDistanceEventListener);
                   // client.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS128);
                    client.getSensorManager().registerPedometerEventListener(mPedometerEventListener);
                    client.getSensorManager().registerSkinTemperatureEventListener(mSkinTemperatureEventListener);
                    client.getSensorManager().registerUVEventListener(mUVEventListener);
                    // check current user heart rate consent
                    if (client.getSensorManager().getCurrentHeartRateConsent() !=
                            UserConsent.GRANTED) {
                        // user has not consented, request it
                        // the calling class is both an Activity and implements
                        // HeartRateConsentListener
                         client.getSensorManager().requestHeartRateConsent(BandStreamingAppActivity.mainActivity, heartRateConsentListener);
                    } else
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                }
                else{
                    sendResult("Band is not connected!!!!",null);
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage();
                        break;
                }

            } catch (Exception e) {
            }
            return null;
        }
    }


}
