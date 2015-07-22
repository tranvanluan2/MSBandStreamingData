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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.Arrays;

public class BandStreamingAppActivity extends Activity  implements AdapterView.OnItemSelectedListener {

	private BandClient client = null;
	private Button btnStart;
	private TextView txtStatus;
    private TextView caloriesStatus;
    private TextView accelerometerStatus;
    private TextView contactStatus;
    private TextView distanceStatus;
    private TextView gyroscopeStatus;
    private TextView heartRateStatus;
    private TextView pedometerStatus;
    private TextView skinTemperatureStatus;
    private TextView uvStatus;

    private Button monitorButton;
    private Spinner activitySpinner;
    public static Activity mainActivity;


    public static  ArrayList<String> heartRateList = new ArrayList<>();
    public static  ArrayList<String> acceleratorList = new ArrayList<>();
    public static ArrayList<String> caloriesList = new ArrayList<>();
    public static ArrayList<String> contactList = new ArrayList<>();
    public static ArrayList<String> distanceList = new ArrayList<>();
    public static ArrayList<String> gyroscopeList = new ArrayList<>();
    public static ArrayList<String> pedometerList = new ArrayList<>();
    public static ArrayList<String> skinTemperatureList = new ArrayList<>();
    public static ArrayList<String> uvEventsList = new ArrayList<>();
    public static boolean isMonitoring = false;
    public static String activityType="";
    public static long startTime;
    public static long endTime;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        caloriesStatus = (TextView) findViewById(R.id.txtCaloriesStatus);
        accelerometerStatus = (TextView) findViewById(R.id.accelerometer);
        contactStatus = (TextView)findViewById(R.id.txtContact);
        distanceStatus = (TextView) findViewById(R.id.txtDistance);
        gyroscopeStatus = (TextView) findViewById(R.id.txtGyroscope);
        heartRateStatus = (TextView) findViewById(R.id.txtHeartRate);
        pedometerStatus = (TextView) findViewById(R.id.pedometer);
        skinTemperatureStatus = (TextView) findViewById(R.id.skin);
        uvStatus = (TextView) findViewById(R.id.txtUV);
        btnStart = (Button) findViewById(R.id.btnStart);
        monitorButton = (Button) findViewById(R.id.button_monitor);
        activitySpinner = (Spinner) findViewById(R.id.activity_spinner);



//        final File myFile = new File(getFilesDir(), "activity");
//        myFile.delete();

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.activity_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        activitySpinner.setAdapter(adapter);
        btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				txtStatus.setText("");
				new appTask().execute();
			}
		});
        monitorButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMonitoring){
                    //change to monitoring mode
                    new appTask().execute();
                    isMonitoring = true;
                    startTime = System.currentTimeMillis();
                    monitorButton.setText("Stop monitoring");


                }
                else{
                    //disable monitoring mode

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



       // uploadFile(getApplicationContext());
        if(!isMyServiceRunning(MyService.class)){
            // use this to start and trigger a service
            Intent i= new Intent(this, MyService.class);
            // potentially add data to the intent
            this.startService(i);
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
		txtStatus.setText("");
	}
	
    @Override
	protected void onPause() {
		super.onPause();
//		if (client != null) {
//			//try {
////				client.getSensorManager().unregisterAccelerometerEventListeners();
////			//}
//		}
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
					client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS16);
                    client.getSensorManager().registerCaloriesEventListener(mCaloriesEventListener);
                    client.getSensorManager().registerContactEventListener(mContactEventListener);
                    client.getSensorManager().registerDistanceEventListener(mDistanceEventListener);
                    client.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener,SampleRate.MS16);
                    client.getSensorManager().registerPedometerEventListener(mPedometerEventListener);
                    client.getSensorManager().registerSkinTemperatureEventListener(mSkinTemperatureEventListener);
                    client.getSensorManager().registerUVEventListener(mUVEventListener);
                    // check current user heart rate consent
                    if(client.getSensorManager().getCurrentHeartRateConsent() !=
                            UserConsent.GRANTED) {
                        // user has not consented, request it
                        // the calling class is both an Activity and implements
                        // HeartRateConsentListener
                        client.getSensorManager().requestHeartRateConsent(BandStreamingAppActivity.mainActivity, heartRateConsentListener);
                    }
                    else client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);

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

        saveToFile(acceleratorList, "accelerometer_"+System.currentTimeMillis(),"accelerometer");
        saveToFile(caloriesList,"calories_"+System.currentTimeMillis(),"calories");
        saveToFile(contactList,"contact_"+System.currentTimeMillis(),"contact");
        saveToFile(distanceList,"distance_"+System.currentTimeMillis(),"distance");
        saveToFile(gyroscopeList,"gyroscope_"+System.currentTimeMillis(),"gyroscope");
        saveToFile(heartRateList,"heartrate_"+System.currentTimeMillis(),"heartrate");
        saveToFile(pedometerList,"pedometer_"+System.currentTimeMillis(),"pedometer");
        saveToFile(skinTemperatureList,"skin_"+System.currentTimeMillis(),"skin");
        saveToFile(uvEventsList,"uv_"+System.currentTimeMillis(),"uv");
        saveToFile(new ArrayList<>(Arrays.asList(activitySpinner.getSelectedItem().toString()+","+startTime+","+endTime+"\n")),
                "activity","activity");

    }
    private void appendToList(final String string, final String sensor_type){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(sensor_type.equals("accelerometer"))
                {
                    acceleratorList.add(string);
                    if(acceleratorList.size()>=1000)
                        saveToFile(acceleratorList, "accelerometer_"+System.currentTimeMillis(),"accelerometer");

                }
                else if(sensor_type.equals("calories")) {
                    caloriesList.add(string);
                    if(caloriesList.size() >= 1000)
                        saveToFile(caloriesList,"calories_"+System.currentTimeMillis(),"calories");
                }
                else if(sensor_type.equals("contact")) {
                    contactList.add(string);
                    if(contactList.size() >= 1000)
                        saveToFile(contactList,"contact_"+System.currentTimeMillis(),"contact");
                }
                else if(sensor_type.equals("distance")) {
                    distanceList.add(string);
                    if(distanceList.size() >= 1000)
                        saveToFile(distanceList,"distance_"+System.currentTimeMillis(),"distance");
                }
                else if(sensor_type.equals("gyroscope")) {
                    gyroscopeList.add(string);
                    if(gyroscopeList.size() >= 1000)
                        saveToFile(gyroscopeList,"gyroscope_"+System.currentTimeMillis(),"gyroscope");
                }
                else if(sensor_type.equals("heartrate")) {
                    heartRateList.add(string);
                    if(heartRateList.size() >= 1000)
                        saveToFile(heartRateList,"heartrate_"+System.currentTimeMillis(),"heartrate");
                }
                else if(sensor_type.equals("pedometer")) {
                    pedometerList.add(string);
                    if(pedometerList.size() >= 1000)
                        saveToFile(pedometerList,"pedometer_"+System.currentTimeMillis(),"pedometer");
                }
                else if(sensor_type.equals("skin")) {
                    skinTemperatureList.add(string);
                    if(skinTemperatureList.size() >= 1000)
                        saveToFile(skinTemperatureList,"skin_"+System.currentTimeMillis(),"skin");
                }
                else if(sensor_type.equals("uv")) {
                    uvEventsList.add(string);
                    if(uvEventsList.size() >= 1000)
                        saveToFile(uvEventsList,"uv_"+System.currentTimeMillis(),"uv");
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
            	    accelerometerStatus.setText(string);
                else if(sensor_type.equals("calories"))
                    caloriesStatus.setText(string);
                else if(sensor_type.equals("contact"))
                    contactStatus.setText(string);
                else if(sensor_type.equals("distance"))
                    distanceStatus.setText(string);
                else if(sensor_type.equals("gyroscope"))
                    gyroscopeStatus.setText(string);
                else if(sensor_type.equals("heartrate"))
                    heartRateStatus.setText(string);
                else if(sensor_type.equals("pedometer"))
                    pedometerStatus.setText(string);
                else if(sensor_type.equals("skin"))
                    skinTemperatureStatus.setText(string);
                else if(sensor_type.equals("uv"))
                    uvStatus.setText(string);




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

