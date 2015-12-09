//Copyright (c) Microsoft Corporation All rights reserved.  
// 
//MIT License: 
// 
//Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
//documentation files (the  "Software"), to deal in the Software without restriction, including without limitation
//the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
//to permit persons to whom the Software is furnished to do so, subject to the following conditions: 
// 
//The above copyright notice and this permission notice shall be included in all copies or substantial portions of
//the Software. 
// 
//THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
//TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
//THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
//CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
//IN THE SOFTWARE.
package com.microsoft.band.sdk.sampleapp;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sdk.sampleapp.streaming.R;
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
import com.microsoft.band.sensors.SampleRate;
import com.microsoft.band.sensors.HeartRateConsentListener;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class BandStreamingAppActivity extends Activity  implements AdapterView.OnItemSelectedListener ,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String REQUESTING_LOCATION_UPDATES_KEY = "request_location_update";
    private static final String LOCATION_KEY = "current_location";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "last_update_time" ;
    private BandClient client = null;
	private TextView txtStatus;

    public static ArrayList<String> locationList = new ArrayList<>();
    private TextView locationStatus;
    private TextView acceleratorStatus;
    private TextView caloriesStatus;
    private TextView gyroscopeStatus;
    private TextView hearbeatStatus;
    private TextView uvStatus;
    private TextView skinStatus;
    private TextView pedometerStatus;
    private TextView contactStatus;
    private Button monitorButton;
    private Button sensingButton;
    private Button mapButton;
    private Spinner activitySpinner;
    public static Activity mainActivity;



    public static boolean isMonitoring = false;
    public static String activityType="";
    public static long startTime;
    public static long endTime;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates =true;
    private GoogleApiClient mGoogleApiClient;

    public BroadcastReceiver receiver;


    /** Defines callbacks for service binding, passed to bindService() */
//    private ServiceConnection mConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            SensingService.LocalBinder binder = (SensingService.LocalBinder) service;
//            sensingService = binder.getService();
//            mBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mBound = false;
//        }
//    };

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        activityType = (String)parent.getItemAtPosition(pos);

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        activityType = "";
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        monitorButton = (Button) findViewById(R.id.button_monitor);
        activitySpinner = (Spinner) findViewById(R.id.activity_spinner);
        locationStatus = (TextView) findViewById(R.id.location);
        acceleratorStatus = (TextView)findViewById(R.id.accelerometer);
        hearbeatStatus = (TextView)findViewById(R.id.txtHeartRate);
        caloriesStatus = (TextView)findViewById(R.id.txtCaloriesStatus);
        gyroscopeStatus = (TextView)findViewById(R.id.txtGyroscope);
        pedometerStatus = (TextView)findViewById(R.id.pedometer);
        uvStatus = (TextView)findViewById(R.id.txtUV);
        skinStatus = (TextView)findViewById(R.id.skin);
        contactStatus = (TextView)findViewById(R.id.txtContact);
        sensingButton = (Button) findViewById(R.id.btnService);
        mapButton = (Button) findViewById(R.id.map);



//        final File myFile = new File(getFilesDir(), "activity");
//        myFile.delete();

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.activity_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        activitySpinner.setAdapter(adapter);
//        btnStart.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				txtStatus.setText("");
//				new appTask().execute();
//			}
//		});

        monitorButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMonitoring){
                    //change to monitoring mode
                    new appTask().execute();
                    isMonitoring = true;
                    startTime = System.currentTimeMillis();
                    monitorButton.setText("Stop monitoring");
                    mainActivity.getWindow().getDecorView().setBackgroundColor(Color.RED);

                }
                else{
                    //disable monitoring mode
                    mainActivity.getWindow().getDecorView().setBackgroundColor(Color.WHITE);
                    isMonitoring = false;
                    monitorButton.setText("Start monitoring");
                    endTime = System.currentTimeMillis();
                    //save to file
                    writeAllArrayToFile();
                    try {
                        client.getSensorManager().unregisterAllListeners();
                    } catch (BandIOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mapButton.setOnClickListener(new OnClickListener() {

             @Override
             public void onClick(View v) {

                Intent intent = new Intent(mainActivity, MapsActivity.class);
                startActivity(intent);
             }

        });

        sensingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMyServiceRunning(SensingService.class)){

                    if(mService!=null) mService.isSensing = false;

                    stopService(new Intent(mainActivity, SensingService.class));

                    try {
                        unbindService(mConnection);
                    }catch(Exception e){}
                    mBound = false;
                    stopLocationUpdates();

                    sensingButton.setText("Start sensing service");
                    mainActivity.getWindow().getDecorView().setBackgroundColor(Color.WHITE);

                }
                else {
                    if(mService!=null) mService.isSensing = true;


                    Intent intent = new Intent(mainActivity, SensingService.class);
                    startService(intent);
                    try {
                    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    }catch(Exception e){}
                    mBound= true;
                    startLocationUpdates();
                    sensingButton.setText("Stop sensing service");
                    mainActivity.getWindow().getDecorView().setBackgroundColor(Color.RED);
                }

            }
        });




        //call google play service
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        updateValuesFromBundle(savedInstanceState);
        createLocationRequest();


        // uploadFile(getApplicationContext());
        if(!isMyServiceRunning(MyService.class)){
            // use this to start and trigger a service
            Intent i= new Intent(this, MyService.class);
            // potentially add data to the intent
            this.startService(i);


        }


        // Bind to LocalService
        Intent intent = new Intent(this, SensingService.class);
        this.startService(intent);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //create a thread to get new values from sensor // Bind to LocalService

        intent = new Intent(this, SensingService.class);
       // this.startService(intent);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //create a thread to get new values from sensor



        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra("message");
                String type = intent.getStringExtra("type");

             //   Log.d("Sensing", "RECEIVE: "+s+" , type= "+type);
                appendToUI(s, type);
                // do something here.
            }
        };







    }
    SensingService mService;
    boolean mBound = false;
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SensingService.LocalBinder binder = (SensingService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    @Override
    protected void onStop() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();


        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter("SENSING_RESULT")
        );

//
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
              //  setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }
            appendToUI("longitude = " + mCurrentLocation.getLongitude() + "latitude = " +
                    mCurrentLocation.getLatitude() +
                    "updated at: " + mLastUpdateTime, "location");


        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


	@Override
	protected void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }


	}
	
    @Override
	protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		super.onPause();
//		if (client != null) {
//			//try {
////				client.getSensorManager().unregisterAccelerometerEventListeners();
////			//}
//		}

      //  stopLocationUpdates();
	}
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        appendToUI("Longitude = " + location.getLongitude() + "\nLatitude = " + location.getLatitude() +
                "\nUpdated at: " + mLastUpdateTime, "location");
        appendToList(mCurrentLocation.getLongitude()+","+
                mCurrentLocation.getLatitude()+"," +
                mCurrentLocation.getAltitude()+","+
                mCurrentLocation.getProvider()+","+
                mCurrentLocation.getAccuracy()+","+
                System.currentTimeMillis()+"\n","location");
    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
                        appendToUI(exceptionMessage, null);
                    }
                }
            }
        };

		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (getConnectedBandClient()) {
					appendToUI("Band is connected.\n",null);
                    // check current user heart rate consent
                    if(client.getSensorManager().getCurrentHeartRateConsent() !=
                            UserConsent.GRANTED) {
                        // user has not consented, request it
                        // the calling class is both an Activity and implements
                        // HeartRateConsentListener
                        client.getSensorManager().requestHeartRateConsent(BandStreamingAppActivity.mainActivity, heartRateConsentListener);
                    }


                } else {
					appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n",null);
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
				appendToUI(exceptionMessage,null);

			} catch (Exception e) {
				appendToUI(e.getMessage(),null);
			}
			return null;
		}
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

    public void writeAllArrayToFile(){

        saveToFile(new ArrayList<>(Arrays.asList(activitySpinner.getSelectedItem().toString()+","+startTime+","+endTime+"\n")),
                "activity","activity");
        saveToFile(locationList,"location_"+System.currentTimeMillis(),"location");

    }
    private void appendToList(final String string, final String sensor_type){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(sensor_type.equals("location") ){
                    locationList.add(string);
                    if(locationList.size() >= 20)
                        saveToFile(locationList, "location_"+System.currentTimeMillis(),"location");
                }

            }
        });
    }
	
	private void appendToUI(final String string, final String sensor_type) {
		this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(sensor_type == null){
                    txtStatus.setText(string);
                }
                else if(sensor_type.equals("accelerometer"))
                    acceleratorStatus.setText(string);

                else if(sensor_type.equals("calories")){
                    caloriesStatus.setText(string);
                }
                else if(sensor_type.equals("contact")) {
                    contactStatus.setText(string);
                }

                else if(sensor_type.equals("distance")) {
                    pedometerStatus.setText(string);
                }
                else if(sensor_type.equals("gyroscope")) {
                    gyroscopeStatus.setText(string);
                }
                else if(sensor_type.equals("heartrate")) {
                    hearbeatStatus.setText(string);
                }
                else if(sensor_type.equals("pedometer")) {}
                else if(sensor_type.equals("skin")) {
                    skinStatus.setText(string);
                }
                else if(sensor_type.equals("uv")) {
                    uvStatus.setText(string);
                }
                else if(sensor_type.equals("location"))

                    locationStatus.setText(string);



            }
        });
	}

    private BandUVEventListener mUVEventListener = new BandUVEventListener() {
        @Override
        public void onBandUVChanged(BandUVEvent bandUVEvent) {
            if(bandUVEvent != null){
                appendToUI("UV level= "+ bandUVEvent.getUVIndexLevel(), "uv");
                //
                appendToList(System.currentTimeMillis()+","+bandUVEvent.getUVIndexLevel()+","
                        + bandUVEvent.getTimestamp()+"\n","uv");
            }
        }
    };
	
    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null) {
            	appendToUI(String.format(" Accelerator \n X = %.3f \n Y = %.3f\n Z = %.3f", event.getAccelerationX(),
            			event.getAccelerationY(), event.getAccelerationZ()),"accelerometer");
                appendToList(System.currentTimeMillis()+","+event.getAccelerationX()+","+
                                event.getAccelerationY()+","+event.getAccelerationZ() + ","+event.getTimestamp()+"\n",
                        "accelerometer");
            }

        }
    };

    private BandCaloriesEventListener mCaloriesEventListener = new BandCaloriesEventListener() {
        @Override
        public void onBandCaloriesChanged(final BandCaloriesEvent bandCaloriesEvent) {
            if(bandCaloriesEvent != null){
                //do something update UI
                appendToUI(" Calories = "+bandCaloriesEvent.getCalories(),"calories");

                appendToList(System.currentTimeMillis()+","+bandCaloriesEvent.getCalories()+
                        ","+bandCaloriesEvent.getTimestamp()+"\n","calories");
            }
        }
    };

    private BandContactEventListener mContactEventListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(final BandContactEvent bandContactEvent) {
            if(bandContactEvent != null){
                appendToUI("Being worn = "+bandContactEvent.getContactState().toString(),"contact");

                appendToList(System.currentTimeMillis()+","+bandContactEvent.getContactState()
                        +","+bandContactEvent.getTimestamp()+"\n"  ,"contact");
            }
        }
    };

    private BandDistanceEventListener mDistanceEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(BandDistanceEvent bandDistanceEvent) {
            if(bandDistanceEvent != null){
                appendToUI("Distance = "+bandDistanceEvent.getTotalDistance()+
                        ", Speed = "+bandDistanceEvent.getSpeed()+", Pace = "+bandDistanceEvent.getPace()
                + ", Motion type="+bandDistanceEvent.getMotionType(),"distance");


                appendToList(System.currentTimeMillis()+","+bandDistanceEvent.getTotalDistance()+
                ","+bandDistanceEvent.getSpeed()+","+bandDistanceEvent.getPace()+","+
                bandDistanceEvent.getMotionType()+","+bandDistanceEvent.getTimestamp()+"\n" ,"distance");
            }
        }
    };

    private BandGyroscopeEventListener mGyroscopeEventListener = new BandGyroscopeEventListener() {
        @Override
        public void onBandGyroscopeChanged(BandGyroscopeEvent bandGyroscopeEvent) {
            if(bandGyroscopeEvent != null){
                appendToUI("Gyroscope \n, acce X = "+ bandGyroscopeEvent.getAccelerationX()+
                        ", acce Y = "+bandGyroscopeEvent.getAccelerationY()
                        + ", acce Z = "+ bandGyroscopeEvent.getAccelerationZ()
                        + ", Angular X="+bandGyroscopeEvent.getAngularVelocityX()
                        + ", Angular Y="+ bandGyroscopeEvent.getAngularVelocityY()
                        + ", Angular Z="+bandGyroscopeEvent.getAngularVelocityZ(),"gyroscope");

                appendToList(System.currentTimeMillis()+","+bandGyroscopeEvent.getAccelerationX()+
                ","+bandGyroscopeEvent.getAccelerationY()+","+bandGyroscopeEvent.getAccelerationZ()+
                                ","+bandGyroscopeEvent.getAngularVelocityX()+","
                        +bandGyroscopeEvent.getAngularVelocityY()+","
                        +bandGyroscopeEvent.getAngularVelocityZ()+","
                        +bandGyroscopeEvent.getTimestamp()+"\n","gyroscope"
                );

            }
        }
    };

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
            if(bandHeartRateEvent!= null){
                appendToUI("Heart Rate = "+bandHeartRateEvent.getHeartRate()+", Quality = "
                + bandHeartRateEvent.getQuality(), "heartrate");

                appendToList(System.currentTimeMillis()+","
                    +bandHeartRateEvent.getHeartRate()+","
                    +bandHeartRateEvent.getQuality()+","
                    +bandHeartRateEvent.getTimestamp()+"\n","heartrate");

            }
        }
    };

    private BandPedometerEventListener mPedometerEventListener = new BandPedometerEventListener() {
        @Override
        public void onBandPedometerChanged(BandPedometerEvent bandPedometerEvent) {
            if(bandPedometerEvent != null){
                appendToUI("#Steps = "+ bandPedometerEvent.getTotalSteps(),"pedometer");

                appendToList(System.currentTimeMillis()+","+bandPedometerEvent.getTotalSteps()
                +","+bandPedometerEvent.getTimestamp()+"\n","pedometer");
            }
        }
    };

    private BandSkinTemperatureEventListener mSkinTemperatureEventListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
            if(bandSkinTemperatureEvent != null){
                appendToUI("Skin Temperature  = "+ bandSkinTemperatureEvent.getTemperature(),"skin");

                appendToList(System.currentTimeMillis()+","+bandSkinTemperatureEvent.getTemperature()+
                ","+bandSkinTemperatureEvent.getTimestamp()+"\n","skin");
            }
        }
    };
    
	private boolean getConnectedBandClient() throws InterruptedException, BandException {
		if (client == null) {
			BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
			if (devices.length == 0) {
				appendToUI("Band isn't paired with your phone.\n",null);
				return false;
			}
			client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
		} else if (ConnectionState.CONNECTED == client.getConnectionState()) {
			return true;
		}
		
		appendToUI("Band is connecting...\n",null);
		return ConnectionState.CONNECTED == client.connect().await();
	}
}

