package com.disarm.testapp_newdesing01;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.app.DialogFragment;
import android.app.FragmentManager;
//import android.support.v4.app.DialogFragment;
import android.support.v7.internal.widget.AppCompatPopupWindow;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.disarm.testapp_newdesing01.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;
import java.util.regex.Matcher;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.pm.ActivityInfo.*;
import static com.disarm.testapp_newdesing01.ModeOfTransport.getMode;


/**
 * Created by ajalan065 on 17-11-2016.
 */
public class Extras extends Fragment {
    FragmentTransaction fragmentTransaction;
    private FileOutputStream fosGPS,fosACC,fosLACC,fosCOM,fosGYR,fosGSM, fosWiFi , fosLGT,fosBatteryLog, fosSND;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private SensorManager accSensorManager, laccSensorManager,comSensorManager,gyrSensorManager, lightSensorManager;
    private TelephonyManager mTelManager;
    private SensorEventListener accSensorEventListener,laccSensorEventListener,comSensorEventListener,gyrSensorEventListener, lightSensorEventListener;
    PhoneStateListener mSignalListener;
    MediaRecorder mRecorder = null;
    Timer t = new  Timer();
    Timer t1 = new Timer();
    TimerTask tt = new TimerTask() {
        @Override
        public void run() {
            mainWifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            if (receiverWifi==null) {
                receiverWifi = new WifiReceiver();
            }
            //receiverWifi = new WifiReceiver();
            getActivity().registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            mainWifi.startScan();
        }
    };
    private final Handler handler = new Handler();
    private Sensor accSensor,laccSensor,comSensor,gyrSensor, lightSensor;

    private int fbmpbrk=0, fbmpwobrk=0, fpothole=0,fimmd=0,fslow=0,futrn=0,fltrn=0,frtrn=0,fjup=0,fjdown=0, fnrml=0, frgh=0;
    private int cbmpbrk=0, cbmpwobrk=0, cpothole=0,cimmd=0,cslow=0,cutrn=0,cltrn=0,crtrn=0,cjup=0,cjdown=0, cnrml=0, crgh=0;
    private String bumpbrkStr="Bump with Brake" ,bumpwobrkStr="Bump w/o Brake",potholeStr="PothHole",immdStr="Immediate Brake",slowStr="Slow Brake", uStr="U Turn", lStr="Turn", jupStr="Jerk Up", jdownStr="Jerk Down",nrmlStr="Normal Road",rghStr="Rough Road",bsyStr="Busy Road";

    private String marker="";
    public static String subFolderName;
    private Map<String, Integer> landmark =new HashMap<String,Integer>();

    private String appFolderName="GPSAndSensorRecorder";
    public static File folder,subfolder;
    private static Timestamp timestamp;

    private String timestampStr;
    private Date accStartTime,laccStartTime,comStartTime,gyrStartTime, soundStartTime, accStopTime,laccStopTime,comStopTime,gyrStopTime, lightStartTime;
    private float batteryUsage;
    private Date date;
    private Long time;
    private WifiManager mainWifi;
    private WifiReceiver receiverWifi;

    TextView errorTextView;

    Button bmpbrkBtn, potholeBtn, bmpwobrkBtn, immdBtn, slowBtn, uBtn, lBtn, rBtn, jupBtn, jdownBtn, nrmlBtn, rghBtn , bsyBtn;
    ImageView bmpbrkImg,potholeImg,bmpwobrkImg,immdImg,slowImg,uImg,lImg,rImg,jupImg,jdownImg, nrmlImg, rghImg;
    private boolean checkACC=true, checkLACC=true, checkGPS=true, checkGYR=true, checkCOM=true, checkGSM=true, checkWiFi=true, checkLGT=true;
    private boolean lightStarted = false, gpsStarted = false, gsmStarted = false, accStarted = false, laccStarted = false, comStarted = false, gyrStarted = false, wifiStarted = false, folder_exists=true, subfolder_exists=true;
    //boolean GPS_STARTED=false;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    ProgressBar lightMeter, soundMeter, accMeter;
    Logger logger;

    private FileOutputStream fosrate;
    public static int rating,rating_final;
    public static File rateFile;

    ////////////Noise Variable
    private float gain;
    String gainString ;
    String timeLogString ;
    String timeDisplayString;
    private int timeLog;
    private String timeLogStringMinSec;
    //    private int timeDisplay;
    private double timeDisplay;
    private final static int BLOCK_SIZE_FFT = 1764;
    private final static int RECORDER_SAMPLERATE = 44100;
    private final static int NUMBER_OF_FFT_PER_SECOND = RECORDER_SAMPLERATE
            / BLOCK_SIZE_FFT;
    NoiseCapture noiseCapture = new NoiseCapture();

    /**
     * Create a subfolder whenever the fragment is started
     */
    public void createFolder() {
        folder = new File(Environment.getExternalStorageDirectory() + "/" + appFolderName);
        folder_exists = true;
            if (!folder.exists()) {
                folder_exists = folder.mkdir();
            }
            if (folder_exists) {
                subfolder_exists=true;
                date=new Date();
                time=date.getTime();
                timestamp=new Timestamp(time);
                timestampStr=timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_');
                subFolderName=  "DATA_" + getMode() +'_' + timestampStr;
                    subfolder = new File(Environment.getExternalStorageDirectory() + "/" + appFolderName + "/" + subFolderName);
                    if (!subfolder.exists()) {
                        subfolder_exists = subfolder.mkdir();
                    }
                    if (subfolder_exists) {
                    }
                    else{
                        ShowMessage.ShowMessage(getActivity(),"Failed..!","Failed to create Folder for Application.\nPlease retry.");
                    }

            }
            else{
                ShowMessage.ShowMessage(getActivity(),"Failed..!","Failed to create Folder for Application.\nPlease retry.");
            }
    }

    /**
     * Return the sub folder name
     * @return String
     */
    public static String getSubFolderName() {
        return subFolderName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //called only once in the lifetime of fragment. When the fragment is added to the app first.
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d("Surji","LoggerFrag_OnCreate Has Called");
        createFolder();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Called whenever the view is recreated. ie: whenever selecting the logger tab

        Log.d("Surji","LoggerFrag_OnCreateView Has Called");
        return inflater.inflate(R.layout.extras_layout,container,false);
    }

    @Override
    public void onStop() {
        //Called whenever the fragment view is destroyed.ie: when selecting other section tabs and whenever home button is pressed.
        //ie: the app is working in background
        super.onStop();
        Log.d("Surji","LoggerFrag_OnStop Has Called");

    }

    @Override
    public void onDestroyView() {
        //Called whenever the fragment view is destroyed.ie: when selecting other section tabs.

        if(((Button)getActivity().findViewById(R.id.btnStopAll)).isEnabled()){
            ShowMessage.ShowMessage(getActivity(),"Alert..!","Recording has stopped");
            stopRecordingAll();
        }

        super.onDestroyView();

        Log.d("Surji","LoggerFrag_OnDestroyView Has Called");
    }

    @Override
    public void onDestroy() {
        //Only Called once when the App is closed completely
        super.onDestroy();
        Log.d("Surji","LoggerFrag_OnDestroy Has Called");
    }

    @Override
    public void onDetach() {
        //Only Called once when the App is closed completely
        super.onDetach();
        Log.d("Surji","LoggerFrag_OnDetach Has Called");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings ) {
            return true;
        }*/
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.checkGPS:
                if (item.isChecked()) {
                    item.setChecked(false);
                    checkGPS=false;
                }
                else {
                    item.setChecked(true);
                    checkGPS=true;
                }

                return true;
            case R.id.checkACC:
                if (item.isChecked()) {
                    item.setChecked(false);
                    checkACC=false;
                }
                else {
                    item.setChecked(true);
                    checkACC=true;
                }

                return true;
            case R.id.checkLACC:
                if (item.isChecked()) {
                    item.setChecked(false);
                    checkLACC=false;
                }
                else {
                    item.setChecked(true);
                    checkLACC=true;
                }

                return true;
            case R.id.checkGYR:
                if (item.isChecked()) {
                    item.setChecked(false);
                    checkGYR = false;
                }
                else {
                    item.setChecked(true);
                    checkGYR=true;
                }

                return true;
            case R.id.checkCOM:
                if (item.isChecked()) {
                    item.setChecked(false);
                    checkCOM = false;
                }
                else {
                    item.setChecked(true);
                    checkCOM=true;
                }

                return true;
            case R.id.checkGSM:
                if (item.isChecked()) {
                    item.setChecked(false);
                    checkGSM = false;
                }
                else {
                    item.setChecked(true);
                    checkGSM=true;
                }

                return true;
            case R.id.checkWiFi:
                if (item.isChecked()) {
                    item.setChecked(false);
                    checkWiFi = false;
                }
                else {
                    item.setChecked(true);
                    checkWiFi=true;
                }

                return true;
            case R.id.checkLGT:
                if (item.isChecked()) {
                    item.setChecked(false);
                    checkLGT = false;
                }
                else {
                    item.setChecked(true);
                    checkLGT=true;
                }

                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }

    private void startNoiseRecording(){
        try {
            gain = Float.parseFloat(gainString);
        } catch (Exception e) {
            gain = 0.0f;
        }
        try {
            timeDisplay = Double.parseDouble(timeDisplayString);
        } catch (Exception e) {
            timeDisplay = 0.1;
        }
        try {
            timeLog = Integer.parseInt(timeLogString);
        } catch (Exception e) {
            timeLog = 1;
        }

        final int finalCountTimeDisplay = (int) (timeDisplay * NUMBER_OF_FFT_PER_SECOND);
        final int finalCountTimeLog = (int) (timeLog * NUMBER_OF_FFT_PER_SECOND);

        noiseCapture.precalculateWeightedA();

        noiseCapture.startRecording((Float) gain, (Integer) finalCountTimeDisplay, (Integer) finalCountTimeLog, timestampStr, subfolder, soundMeter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        errorTextView=(TextView)getActivity().findViewById(R.id.txtError);

        initiateLandMarks();
        checkAvailableSensors();

        //initiateDetails();

        powerManager=(PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock=powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"LoggerWakeLock");

        accMeter = (ProgressBar)getActivity().findViewById(R.id.accmeter);
        lightMeter = (ProgressBar)getActivity().findViewById(R.id.lightmeter);
        soundMeter = (ProgressBar)getActivity().findViewById(R.id.soundmeter);

        /*bmpbrkBtn=(Button)getActivity().findViewById(R.id.btnLM1);
        potholeBtn=(Button)getActivity().findViewById(R.id.btnLM2);
        bmpwobrkBtn=(Button)getActivity().findViewById(R.id.btnLM3);
        immdBtn=(Button)getActivity().findViewById(R.id.btnLM4);
        slowBtn=(Button)getActivity().findViewById(R.id.btnLM5);
        uBtn=(Button)getActivity().findViewById(R.id.btnLM6);
        lBtn=(Button)getActivity().findViewById(R.id.btnLM7);
        //rBtn=(Button)getActivity().findViewById(R.id.btnLM8);
        jupBtn=(Button)getActivity().findViewById(R.id.btnLM9);
        jdownBtn=(Button)getActivity().findViewById(R.id.btnLM10);
        nrmlBtn = (Button)getActivity().findViewById(R.id.btnLM11);
        rghBtn = (Button)getActivity().findViewById(R.id.btnLM12);
        bsyBtn = (Button)getActivity().findViewById(R.id.btnLM13);

        bmpbrkImg=(ImageView)getActivity().findViewById(R.id.imgLM1);
        potholeImg=(ImageView)getActivity().findViewById(R.id.imgLM2);
        bmpwobrkImg=(ImageView)getActivity().findViewById(R.id.imgLM3);
        immdImg=(ImageView)getActivity().findViewById(R.id.imgLM4);
        slowImg=(ImageView)getActivity().findViewById(R.id.imgLM5);
        uImg=(ImageView)getActivity().findViewById(R.id.imgLM6);
        lImg=(ImageView)getActivity().findViewById(R.id.imgLM7);
        rImg=(ImageView)getActivity().findViewById(R.id.imgLM8);
        jupImg=(ImageView)getActivity().findViewById(R.id.imgLM9);
        jdownImg=(ImageView)getActivity().findViewById(R.id.imgLM10);
        nrmlImg=(ImageView)getActivity().findViewById(R.id.imgLM11);
        rghImg=(ImageView)getActivity().findViewById(R.id.imgLM12);*/

        /*
        checkGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                }
            }
        });
        checkACC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    accStartRecord();
                }else{
                    try{
                        accSensorManager.unregisterListener(accSensorEventListener);
                        fosACC.close();
                        batteryUsage=accSensor.getPower();
                        batteryLoger("ACC", batteryUsage, accStartTime, date);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
        */

        /*bmpbrkBtn.setOnClickListener(new LMButtonClickListener());
        potholeBtn.setOnClickListener(new LMButtonClickListener());
        bmpwobrkBtn.setOnClickListener(new LMButtonClickListener());
        immdBtn.setOnClickListener(new LMButtonClickListener());
        slowBtn.setOnClickListener(new LMButtonClickListener());
        uBtn.setOnClickListener(new LMButtonClickListener());
        lBtn.setOnClickListener(new LMButtonClickListener());
//        rBtn.setOnClickListener(new LMButtonClickListener());
        jupBtn.setOnClickListener(new LMButtonClickListener());
        jdownBtn.setOnClickListener(new LMButtonClickListener());
        nrmlBtn.setOnClickListener(new LMButtonClickListener());
        rghBtn.setOnClickListener(new LMButtonClickListener());
        bsyBtn.setOnClickListener(new LMButtonClickListener());*/



        final ToggleButton pauseBtn=(ToggleButton)getActivity().findViewById(R.id.btnPause);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String state=(String) pauseBtn.getText();
                Log.d("test1", state);

                if(state.equals("Continue")){
                    //recording paused
                    Log.d("test","Paused");

                    if(gpsStarted){
                        locationManager.removeUpdates(locationListener);
                    }
                    if(accStarted){
                        accSensorManager.unregisterListener(accSensorEventListener, accSensor);
                    }
                    if(laccStarted){
                        laccSensorManager.unregisterListener(laccSensorEventListener, laccSensor);
                    }
                    if(gyrStarted){
                        gyrSensorManager.unregisterListener(gyrSensorEventListener, gyrSensor);
                    }
                    if(comStarted){
                        comSensorManager.unregisterListener(comSensorEventListener, comSensor);
                    }
                    if(gsmStarted){
                        mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_NONE);
                    }
                    if(wifiStarted){
                        t1.cancel();
                        getActivity().unregisterReceiver(receiverWifi);
                    }


                    //Calling batteryLoger function
                    date=new Date();
                    try{
                        batteryUsage=accSensor.getPower();
                        batteryLoger("ACC",batteryUsage,accStartTime,date);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                    try{
                        batteryUsage=laccSensor.getPower();
                        batteryLoger("LACC",batteryUsage,laccStartTime,date);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                    try{
                        batteryUsage=gyrSensor.getPower();
                        batteryLoger("GYR",batteryUsage,gyrStartTime,date);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                    try{
                        batteryUsage=comSensor.getPower();
                        batteryLoger("COM",batteryUsage,comStartTime,date);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }



                }else{
                    //recording continuous
                    Log.d("test", "Continue");
                    date=new Date();
                    accStartTime=date;
                    laccStartTime=date;
                    gyrStartTime=date;
                    comStartTime=date;
                    if(gpsStarted)
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                    if(accStarted)
                        accSensorManager.registerListener(accSensorEventListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    if(laccStarted)
                        laccSensorManager.registerListener(laccSensorEventListener, laccSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    if(gyrStarted)
                        gyrSensorManager.registerListener(gyrSensorEventListener, gyrSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    if(comStarted)
                        comSensorManager.registerListener(comSensorEventListener, comSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    if(gsmStarted)
                        mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                    if(wifiStarted)
                    {
                        getActivity().registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                        t1.scheduleAtFixedRate(tt,0,1000);
                    }
                }
            }
        });


        /*final Button gpsStartBtn=(Button)getActivity().findViewById(R.id.btnGpsStart);
        final Button accStartBtn=(Button)getActivity().findViewById(R.id.btnAccStart);
        final Button laccStartBtn=(Button)getActivity().findViewById(R.id.btnLaccStart);
        final Button gyrStartBtn=(Button)getActivity().findViewById(R.id.btnGyrStart);
        final Button comStartBtn=(Button)getActivity().findViewById(R.id.btnComStart);*/

        final Button startAllBtn=(Button)getActivity().findViewById(R.id.btnStartAll);

        /*final Button gpsStopBtn=(Button)getActivity().findViewById(R.id.btnGpsStop);
        final Button accStopBtn=(Button)getActivity().findViewById(R.id.btnAccStop);
        final Button laccStopBtn=(Button)getActivity().findViewById(R.id.btnLaccStop);
        final Button gyrStopBtn=(Button)getActivity().findViewById(R.id.btnGyrStop);
        final Button comStopBtn=(Button)getActivity().findViewById(R.id.btnComStop);*/
        final Button stopAllBtn=(Button)getActivity().findViewById(R.id.btnStopAll);

        startAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                startRecordingAll();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).show();

            }
        });
        stopAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                stopRecordingAll();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).setCancelable(false).show();

            }
        });

        // For the light sensor
        final ToggleButton pauseLightBtn = (ToggleButton)getActivity().findViewById(R.id.btnlightPause);
        pauseLightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String state = (String) pauseLightBtn.getText();
                Log.d("test1", state);

                if (state.equals("Continue")) {
                    Log.d("test", "Paused");

                    if (lightStarted) {
                        lightSensorManager.unregisterListener(lightSensorEventListener, lightSensor);
                    }

                    //Calling batteryLoger function
                    date = new Date();
                    try {
                        batteryUsage = lightSensor.getPower();
                        batteryLoger("Light", batteryUsage, lightStartTime, date);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else{
                    //recording continuous
                    Log.d("test", "Continue");
                    date=new Date();
                    lightStartTime = date;
                    if(accStarted)
                        lightSensorManager.registerListener(lightSensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });

        final Button startlightAllBtn=(Button)getActivity().findViewById(R.id.btnlightStartAll);

        final Button stoplightAllBtn=(Button)getActivity().findViewById(R.id.btnlightStopAll);

        startlightAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                startLightRecordingAll();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).show();

            }
        });
        stoplightAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                stopLightRecordingAll();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).setCancelable(false).show();

            }
        });

        // For sound applications
        final Button startsoundAllBtn=(Button)getActivity().findViewById(R.id.btnsoundStartAll);

        final Button stopsoundAllBtn=(Button)getActivity().findViewById(R.id.btnsoundStopAll);
        //logger = new Logger();

        startsoundAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                getActivity().findViewById(R.id.btnsoundStartAll).setEnabled(false);
                                getActivity().findViewById(R.id.btnsoundPause).setEnabled(true);
                                getActivity().findViewById(R.id.btnsoundStopAll).setEnabled(true);
                                wakeLock.acquire();
                                startNoiseRecording();

                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).show();

            }
        });

        stopsoundAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                try {
                                    noiseCapture.stopRecording(soundMeter);
                                    if (mRecorder != null) {
                                        mRecorder.stop();
                                        mRecorder.release();
                                        mRecorder = null;
                                    }
                                    t.cancel();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                }
                                getActivity().findViewById(R.id.btnsoundStartAll).setEnabled(true);
                                getActivity().findViewById(R.id.btnsoundPause).setEnabled(false);
                                getActivity().findViewById(R.id.btnsoundStopAll).setEnabled(false);

                                wakeLock.release();

                                date=new Date();
                                if(((ToggleButton)getActivity().findViewById(R.id.btnsoundPause)).isChecked()) {
                                    ((ToggleButton) getActivity().findViewById(R.id.btnsoundPause)).setChecked(false);
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).setCancelable(false).show();

            }
        });
    }

    private void initiateLandMarks(){
        landmark.put(bumpbrkStr,0);
        landmark.put(bumpwobrkStr,0);
        landmark.put(potholeStr,0);
        landmark.put(immdStr,0);
        landmark.put(slowStr,0);
        landmark.put(uStr,0);
        landmark.put(lStr,0);
        //landmark.put(rStr,0);
        landmark.put(jupStr,0);
        landmark.put(jdownStr,0);
        landmark.put(nrmlStr,0);
        landmark.put(rghStr,0);
        landmark.put(bsyStr,0);
    }

    /**
     * Lists the available sensors on the phone.
     */
    private void checkAvailableSensors(){
        String sensors="" + System.lineSeparator();
        SensorManager testManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if(testManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensors += "Accelerometer" + System.lineSeparator();
        }
        if(testManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensors += "Linear Accelerometer" + System.lineSeparator();
        }
        if(testManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            sensors += "Gyroscope" + System.lineSeparator();
        }
        if(testManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            sensors += "Compass" + System.lineSeparator();
        }
        if (testManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            sensors += "Light" + System.lineSeparator();
        }
        //detailsTextView.setText("Available Sensors:"+sensors);
        ShowMessage.ShowMessage(getActivity(),"Caution","Available Sensors:"+sensors+"\nRecord will be only done if the sensor is available in your device");
    }

    private void batteryLoger(String sensor,float usedMA,Date start,Date stop){

        String fileName="BATTERY_CONSUMPTION.txt";
        long timeB1,timeB2;
        timeB1=start.getTime();
        timeB2=stop.getTime();
        folder= new File(Environment.getExternalStorageDirectory()+"/"+appFolderName);
        final File batteryLogFile =new File(folder,fileName);

        if(!batteryLogFile.exists()){
            try{
                fosBatteryLog=new FileOutputStream(batteryLogFile);
                fosBatteryLog.write("#sensor,startTime,stopTime,usageConst,batteryUsage".getBytes());
            }catch (FileNotFoundException e2) {
                e2.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            try{
                fosBatteryLog=new FileOutputStream(batteryLogFile, true);
            }catch(FileNotFoundException ex){
                ex.printStackTrace();
            }
        }
        long timeDiff=stop.getTime()-start.getTime();
        float timeDiffInHr=(float)(timeDiff*0.000000278);
        float timeDiffInSec=(float)(timeDiff*0.001);
        float usage=usedMA*timeDiffInHr;

        String logDetails="\n"+sensor+","+new Timestamp(timeB1).toString()+","+new Timestamp(timeB2).toString()+","+usedMA+","+usage;
        try{
            fosBatteryLog.write(logDetails.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /*private boolean isAnyOptionChecked(){
        if ((checkACC.isChecked() || checkLACC.isChecked() || checkCOM.isChecked() || checkGYR.isChecked() ||checkGPS.isChecked()) ||checkGSM.isChecked()){
            return true;
        }else {
            return false;
        }
    }

    private boolean isLightSensorChecked() {
       if (checkLGT.isChecked())
           return true;
        else return false;
    }*/

    private boolean isAnyOptionChecked(){
        if (checkACC || checkLACC || checkCOM || checkGYR ||checkGSM){
            return true;
        }else {
            return false;
        }
    }

    private boolean isLightSensorChecked() {
        if (checkLGT)
            return true;
        else return false;
    }



    /**
     * Start recording the light data
     */
    public void startLightRecordingAll() {
        //folder = new File(Environment.getExternalStorageDirectory() + "/" + appFolderName);
        //boolean folder_exists = true;
        if (isLightSensorChecked()) {
            if (folder_exists) {
                /*boolean subfolder_exists=true;
                date=new Date();
                time=date.getTime();
                timestamp=new Timestamp(time);
                timestampStr=timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_');
                String subFolderName=getMode() + "_DATA_"+timestampStr;*/

                locationManager =(LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                if(checkGPS && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    ShowMessage.ShowMessage(getActivity(),"Warning..!","Your GPS is disabled. Please Enable GPS and try again.");
                }
                else {
                    if (subfolder_exists) {
                        getActivity().findViewById(R.id.btnlightStartAll).setEnabled(false);
                        getActivity().findViewById(R.id.btnlightPause).setEnabled(true);
                        getActivity().findViewById(R.id.btnlightStopAll).setEnabled(true);

                        wakeLock.acquire();

                        if (checkLGT) {
                            lightStartRecord();
                        }
                    }
                    else{
                        ShowMessage.ShowMessage(getActivity(),"Failed..!","Failed to create Folder for Application.\nPlease retry.");
                    }
                }
            }
            else{
                ShowMessage.ShowMessage(getActivity(),"Failed..!","Failed to create Folder for Application.\nPlease retry.");
            }
        }
        else{
            ShowMessage.ShowMessage(getActivity(),"Caution..!","Please Check Light Option to Record");
        }

    }

    public void startRecordingAll(){
        /*folder= new File(Environment.getExternalStorageDirectory()+"/"+appFolderName);
        boolean folder_exists=true;*/

        if(isAnyOptionChecked()){
            if(folder_exists){
                /*boolean subfolder_exists=true;
                date=new Date();
                time=date.getTime();
                timestamp=new Timestamp(time);
                timestampStr=timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_');

                String subFolderName=getMode()+"_DATA_"+timestampStr;*/

                locationManager =(LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                if(checkGPS && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    ShowMessage.ShowMessage(getActivity(),"Warning..!","Your GPS is disabled. Please Enable GPS and try again.");
                } else {
                    if(subfolder_exists){
                        getActivity().findViewById(R.id.btnStartAll).setEnabled(false);
                        getActivity().findViewById(R.id.btnPause).setEnabled(true);
                        getActivity().findViewById(R.id.btnStopAll).setEnabled(true);

                        wakeLock.acquire();
                        //Getting GPS Data and writing into a file
                        //getActivity().findViewById(R.id.btnGpsStart).setEnabled(false);

                        if(checkGPS)
                            gpsStartRecord();

                        //Getting AcceleroMeter Data and writing to file
                        //getActivity().findViewById(R.id.btnAccStart).setEnabled(false);
                        if(checkACC)
                            accStartRecord();

                        //Getting Linear AcceleroMeter Data and writing to file
                        //getActivity().findViewById(R.id.btnLaccStart).setEnabled(false);
                        if(checkLACC)
                            laccStartRecord();

                        //Getting Compass Data and writing to file
                        //getActivity().findViewById(R.id.btnComStart).setEnabled(false);
                        if(checkCOM)
                            comStartRecord();

                        //Getting GYROSCOPE data and writing it to file
                        //getActivity().findViewById(R.id.btnGyrStart).setEnabled(false);
                        if(checkGYR)
                            gyrStartRecord();

                        if(checkGSM)
                            gsmStartRecord();

                        if(checkWiFi)
                            wifiStartRecord();
                    }
                    else{
                        ShowMessage.ShowMessage(getActivity(),"Failed..!","Failed to create Folder for Application.\nPlease retry.");
                    }
                }

            }else{
                ShowMessage.ShowMessage(getActivity(),"Failed..!","Failed to create Folder for Application.\nPlease retry.");
                //errorTag.setText("Failed to create Folder for application");
            }

        }
        else{
            ShowMessage.ShowMessage(getActivity(),"Caution..!","Please Check at least one Option to Record");
        }
    }

    private void gsmStartRecord() {
        String gsmFilename= "GSM_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File gsmFile=new File(subfolder,gsmFilename);

        mTelManager=(TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        String carrierName=mTelManager.getNetworkOperatorName();
        try {
            fosGSM=new FileOutputStream(gsmFile);
            fosGSM.write((carrierName + "\n#signalStrength,time").getBytes());
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSignalListener=new PhoneStateListener(){

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                int gsmStrength=signalStrength.getGsmSignalStrength();

                long systemTimeInMilli=(new Date()).getTime();
                String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));
                marker="";
                for(String key: landmark.keySet())
                {
                    if(landmark.get(key)!=0)
                    {
                        marker+=key+"_"+landmark.get(key)+"+";
                    }
                }
                String gsmSignalDetails="\n"+gsmStrength+","+timestampFormatted+","+marker;
                try{
                    fosGSM.write(gsmSignalDetails.getBytes());
                }catch(Exception e){
                    e.printStackTrace();
                }
                super.onSignalStrengthsChanged(signalStrength);
            }

        };
        mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        gsmStarted=true;
    }

    private void wifiStartRecord(){

        String wifiFileName;
        date=new Date();
        time=date.getTime();
        timestamp=new Timestamp(time);
        timestampStr=timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_');
        wifiFileName="WiFi_"+timestampStr+".txt";
        File WifiLog =new File(subfolder,wifiFileName);
        try {
            fosWiFi=new FileOutputStream(WifiLog);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        mainWifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new WifiReceiver();
        getActivity().registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //lines added
        //mainWifi.startScan();

        t1.scheduleAtFixedRate(tt,0,1000);
        //doinback();

        //  end
        wifiStarted=true;
    }
    /*private void doinback(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mainWifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                if (receiverWifi==null) {
                    receiverWifi = new WifiReceiver();
                }
                //receiverWifi = new WifiReceiver();
                getActivity().registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                mainWifi.startScan();
                doinback();
            }
        },3000);
    }*/
    private void gpsStartRecord(){

        String gpsFilename="GPS_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        File gpsFile=new File(subfolder,gpsFilename);
        //locationManager =(LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        /*if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //getActivity().findViewById(R.id.btnGpsStart).setEnabled(true);
            ShowMessage.ShowMessage(getActivity(),"Alert..!","Your GPS is disabled. GPS Recording wont be done.\n Restart after enabling GPS if you want to record GPS data");
        } else {*/

        try{
            fosGPS=new FileOutputStream(gpsFile);
            fosGPS.write("#lat,long,speed,altitude,time".getBytes());
            locationListener=new LocationListener(){

                @Override
                public void onLocationChanged(Location location) {
                    //float accuracy=location.getAccuracy();
                    double altitude=location.getAltitude();
                    double latitude=location.getLatitude();
                    double longitude=location.getLongitude();
                    double speed=location.getSpeed();

                        /*long time=location.getTime();
                        Date date1=new Date(time);
                        SimpleDateFormat dateFormat=new SimpleDateFormat("hh:mm:ss");
                        String timeStamp=dateFormat.format(date1);
                        **/

                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";
                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }

                    String locDetails="\n"+latitude+","+longitude+","+speed+","+altitude+","+timestampFormatted+","+marker;

                    try {
                        fosGPS.write(locDetails.getBytes());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onProviderDisabled(String provider) {
                    // TODO Auto-generated method stub
                    ShowMessage.ShowMessage(getActivity(),"Alert..!","GPS is got disabled. GPS Recording will be stopped");
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                    // TODO Auto-generated method stub
                }

            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            gpsStarted=true;
        } catch(Exception e){
            e.printStackTrace();
        }
        /*}*/
    }

    private void accStartRecord(){
        String accFilename="ACC_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File accFile=new File(subfolder,accFilename);

        try {
            fosACC=new FileOutputStream(accFile);
            fosACC.write("#x,y,z,time".getBytes());
        } catch (FileNotFoundException e2) {

            e2.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        accSensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accSensor=accSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        date=new Date();
        accStartTime=date;
        if(accSensor!=null){
            accSensorEventListener = new SensorEventListener(){
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // TODO Auto-generated method stub
                }
                @Override
                public void onSensorChanged(SensorEvent event) {

                    float x=event.values[0];
                    float y=event.values[1];
                    float z=event.values[2];
                    //float timestamp=event.timestamp/1000000;
                    //long timeInMilli=(new Date()).getTime()+((event.timestamp-System.nanoTime())/1000000L);

                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";
                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }
                    String accSensorDetails="\n"+x+","+y+","+z+","+timestampFormatted+","+marker;
                    accMeter.setProgress((int)Math.sqrt((Math.pow(event.values[0],2)+ Math.pow(event.values[1],2)+ Math.pow(event.values[2],2))));
                    try{
                        fosACC.write(accSensorDetails.getBytes());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            };
            accSensorManager.registerListener(accSensorEventListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            accStarted=true;
        }
    }

    private void laccStartRecord(){
        String laccFilename="LACC_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File laccFile=new File(subfolder,laccFilename);

        try {
            fosLACC=new FileOutputStream(laccFile);
            fosLACC.write("#x,y,z,time".getBytes());
        } catch (FileNotFoundException e2) {

            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        laccSensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        laccSensor=laccSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        date=new Date();
        laccStartTime=date;

        if(laccSensor!=null){

            laccSensorEventListener = new SensorEventListener(){

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onSensorChanged(SensorEvent event) {

                    float x=event.values[0];
                    float y=event.values[1];
                    float z=event.values[2];
                    //long timestamp=event.timestamp/1000000;
                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";
                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }
                    String laccSensorDetails="\n"+x+","+y+","+z+","+timestampFormatted+","+marker;

                    try{

                        fosLACC.write(laccSensorDetails.getBytes());


                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }


            };
            laccSensorManager.registerListener(laccSensorEventListener, laccSensor, SensorManager.SENSOR_DELAY_NORMAL);
            laccStarted=true;
        }
    }

    private void comStartRecord(){
        String comFilename="COM_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File comFile=new File(subfolder,comFilename);

        try {
            fosCOM=new FileOutputStream(comFile);
            fosCOM.write("#x,y,z,time".getBytes());
        } catch (FileNotFoundException e2) {

            e2.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        comSensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        comSensor=comSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        date=new Date();
        comStartTime=date;

        if(comSensor!=null){

            comSensorEventListener = new SensorEventListener(){

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onSensorChanged(SensorEvent event) {

                    float x=event.values[0];
                    float y=event.values[1];
                    float z=event.values[2];
                    //long timestamp=event.timestamp/1000000;

                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";
                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }
                    String comSensorDetails="\n"+x+","+y+","+z+","+timestampFormatted+","+marker;

                    try{

                        fosCOM.write(comSensorDetails.getBytes());


                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }


            };
            comSensorManager.registerListener(comSensorEventListener, comSensor, SensorManager.SENSOR_DELAY_NORMAL);
            comStarted=true;

        }
    }

    private void gyrStartRecord(){
        String gyrFilename="GYR_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File gyrFile=new File(subfolder,gyrFilename);
        try {
            fosGYR=new FileOutputStream(gyrFile);
            fosGYR.write("#x,y,z,time".getBytes());
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        gyrSensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        gyrSensor=gyrSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        date=new Date();
        gyrStartTime=date;

        if(gyrSensor!=null){

            gyrSensorEventListener = new SensorEventListener(){

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onSensorChanged(SensorEvent event) {

                    float x=event.values[0];
                    float y=event.values[1];
                    float z=event.values[2];
                    //float timestamp=event.timestamp/1000000000;

                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";
                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }

                    String gyrSensorDetails="\n"+x+","+y+","+z+","+timestampFormatted+","+marker;

                    try{

                        fosGYR.write(gyrSensorDetails.getBytes());


                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }


            };
            gyrSensorManager.registerListener(gyrSensorEventListener, gyrSensor, SensorManager.SENSOR_DELAY_NORMAL);
            gyrStarted=true;
        }
    }

    /**
     * Light record
     */
    private void lightStartRecord() {
        String lgtFilename="LIGHT_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File gyrFile=new File(subfolder,lgtFilename);
        try {
            fosLGT=new FileOutputStream(gyrFile);
            fosLGT.write("#x,time".getBytes());
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        lightSensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor=lightSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        date=new Date();
        lightStartTime = date;

        if (lightSensor != null) {
            lightSensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float x = event.values[0];
                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";

                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }

                    String lightSensorDetails="\n"+x+","+timestampFormatted+","+marker;
                    lightMeter.setProgress((int)event.values[0]);
                    try{

                        fosLGT.write(lightSensorDetails.getBytes());


                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }

            };

            lightSensorManager.registerListener(lightSensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            lightStarted = true;
        }
    }

    /**
     *  Stop Light Recordings
     */
    public void stopLightRecordingAll() {
        getActivity().findViewById(R.id.btnlightStartAll).setEnabled(true);
        getActivity().findViewById(R.id.btnlightPause).setEnabled(false);
        getActivity().findViewById(R.id.btnlightStopAll).setEnabled(false);

        wakeLock.release();

        date=new Date();
        if(((ToggleButton)getActivity().findViewById(R.id.btnlightPause)).isChecked()){
            ((ToggleButton)getActivity().findViewById(R.id.btnlightPause)).setChecked(false);
        }

        if(lightStarted){
            lightMeter.setProgress(0);
            try{
                lightSensorManager.unregisterListener(lightSensorEventListener);
                fosLGT.close();

                batteryUsage=lightSensor.getPower();
                batteryLoger("LIGHT", batteryUsage, lightStartTime, date);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }

    }

    public void stopRecordingAll(){
        getActivity().findViewById(R.id.btnStartAll).setEnabled(true);
        getActivity().findViewById(R.id.btnPause).setEnabled(false);
        getActivity().findViewById(R.id.btnStopAll).setEnabled(false);

        wakeLock.release();

        initiateLandMarks();

        date=new Date();
        if(((ToggleButton)getActivity().findViewById(R.id.btnPause)).isChecked()){
            ((ToggleButton)getActivity().findViewById(R.id.btnPause)).setChecked(false);
        }

        if(gpsStarted){
            try {
                locationManager.removeUpdates(locationListener);

                fosGPS.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        if(accStarted){
            accMeter.setProgress(0);
            try{
                accSensorManager.unregisterListener(accSensorEventListener);
                fosACC.close();

                batteryUsage=accSensor.getPower();
                batteryLoger("ACC", batteryUsage, accStartTime, date);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        if(laccStarted){
            try{
                laccSensorManager.unregisterListener(laccSensorEventListener);
                fosLACC.close();

                batteryUsage=laccSensor.getPower();
                batteryLoger("LACC", batteryUsage, laccStartTime, date);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        if(gyrStarted){
            try{
                gyrSensorManager.unregisterListener(gyrSensorEventListener);
                fosGYR.close();
                batteryUsage=gyrSensor.getPower();
                batteryLoger("GYR", batteryUsage, gyrStartTime, date);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        if(comStarted){
            try{
                comSensorManager.unregisterListener(comSensorEventListener);
                fosCOM.close();

                batteryUsage=comSensor.getPower();
                batteryLoger("COM", batteryUsage, comStartTime, date);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        if(gsmStarted){
            try{
                mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_NONE);
                fosGSM.close();

            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        if(wifiStarted){
            try{
                t1.cancel();
                getActivity().unregisterReceiver(receiverWifi);

                fosWiFi.close();

            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    void showDialog(String str) {
        date = new Date();
        time = date.getTime();
        timestamp = new Timestamp(time);
        String rateFilename= getMode() + "_Rating_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        rateFile=new File(subfolder,rateFilename);
        if (!rateFile.exists()) {
            try {
                fosrate = new FileOutputStream(rateFile);
                String rate = str + " ";
                fosrate.write((rate).getBytes());
                FragmentManager fragmentManager;
                DialogFragment newFragment = Rating.newInstance(R.string.alert_dialog_rating);
                fragmentManager = getFragmentManager();
                newFragment.show(fragmentManager, "dialog");
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else {
            try {
                fosrate = new FileOutputStream(rateFile, true);
            }catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    /*class LMButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v){
            int buttonId=v.getId();
            switch (buttonId){
                case R.id.btnLM1:{
                    if(fbmpbrk==0){
                        bmpbrkBtn.setTextColor(Color.RED);
                        bmpbrkImg.setImageResource(R.drawable.btn_bumper1);
                        fbmpbrk=1;
                        cbmpbrk++;
                        landmark.put(bumpbrkStr,cbmpbrk);
                    }else{
                        bmpbrkBtn.setTextColor(Color.BLACK);
                        bmpbrkImg.setImageResource(R.drawable.btn_bumper0);
                        fbmpbrk=0;
                        landmark.put(bumpbrkStr,0);
                        showDialog("Bump with Brake");
                    }
                    break;
                }
                case R.id.btnLM2:{
                    if(fpothole==0){
                        potholeBtn.setTextColor(Color.RED);
                        potholeImg.setImageResource(R.drawable.btn_pothole1);
                        fpothole=1;
                        cpothole++;
                        landmark.put(potholeStr,cpothole);
                    }else{
                        potholeBtn.setTextColor(Color.BLACK);
                        potholeImg.setImageResource(R.drawable.btn_pothole0);
                        fpothole=0;
                        landmark.put(potholeStr,0);
                        showDialog("Pothole");
                    }
                    break;
                }
                case R.id.btnLM3:{
                    if(fbmpwobrk==0){
                        bmpwobrkBtn.setTextColor(Color.RED);
                        bmpwobrkImg.setImageResource(R.drawable.btn_busstop1);
                        fbmpwobrk=1;
                        cbmpwobrk++;
                        landmark.put(bumpwobrkStr,cbmpwobrk);
                    }else{
                        bmpwobrkBtn.setTextColor(Color.BLACK);
                        bmpwobrkImg.setImageResource(R.drawable.btn_busstop0);
                        fbmpwobrk=0;
                        landmark.put(bumpwobrkStr,0);
                        showDialog("Bump without Brake");
                    }
                    break;
                }
                case R.id.btnLM4:{
                    if(fimmd==0){
                        immdBtn.setTextColor(Color.RED);
                        immdImg.setImageResource(R.drawable.btn_junction1);
                        fimmd=1;
                        cimmd++;
                        landmark.put(immdStr,cimmd);
                    }else{
                        immdBtn.setTextColor(Color.BLACK);
                        immdImg.setImageResource(R.drawable.btn_junction0);
                        fimmd=0;
                        landmark.put(immdStr,0);
                        showDialog("Immediate Brake");
                    }
                    break;
                }
                case R.id.btnLM5:{
                    if(fslow==0){
                        slowBtn.setTextColor(Color.RED);
                        slowImg.setImageResource(R.drawable.btn_busyroad1);
                        fslow=1;
                        cslow++;
                        landmark.put(slowStr,cslow);
                    }else{
                        slowBtn.setTextColor(Color.BLACK);
                        slowImg.setImageResource(R.drawable.btn_busyroad0);
                        fslow=0;
                        landmark.put(slowStr,0);
                        showDialog("Slow Brake");
                    }
                    break;
                }
                case R.id.btnLM6:{
                    if(futrn==0){
                        uBtn.setTextColor(Color.RED);
                        uImg.setImageResource(R.drawable.btn_brokeroad1);
                        futrn=1;
                        cutrn++;
                        landmark.put(uStr,cutrn);
                    }else{
                        uBtn.setTextColor(Color.BLACK);
                        uImg.setImageResource(R.drawable.btn_brokeroad0);
                        futrn=0;
                        landmark.put(uStr,0);
                        showDialog("U Turn");
                    }
                    break;
                }
                case R.id.btnLM7:{
                    if(fltrn==0){
                        lBtn.setTextColor(Color.RED);
                        lImg.setImageResource(R.drawable.btn_turn1);
                        fltrn=1;
                        cltrn++;
                        landmark.put(lStr,cltrn);
                    }else{
                        lBtn.setTextColor(Color.BLACK);
                        lImg.setImageResource(R.drawable.btn_turn0);
                        fltrn=0;
                        landmark.put(lStr,0);
                        showDialog("Turn");
                    }
                    break;
                }*/
                /*
                case R.id.btnLM8:{
                    if(frtrn==0){
                        rBtn.setTextColor(Color.RED);
                        rImg.setImageResource(R.drawable.btn_overtake1);
                        frtrn=1;
                        crtrn++;
                        landmark.put(rStr,crtrn);
                    }else{
                        rBtn.setTextColor(Color.BLACK);
                        rImg.setImageResource(R.drawable.btn_overtake0);
                        frtrn=0;
                        landmark.put(rStr,0);
                        showDialog("Right Turn");
                    }
                    break;
                }*/


                /*case R.id.btnLM13:{
                    if(frtrn==0){
                        rBtn.setTextColor(Color.RED);
                        rImg.setImageResource(R.drawable.btn_overtake1);
                        frtrn=1;
                        crtrn++;
                        landmark.put(bsyStr,crtrn);
                    }else{
                        rBtn.setTextColor(Color.BLACK);
                        rImg.setImageResource(R.drawable.btn_overtake0);
                        frtrn=0;
                        landmark.put(bsyStr,0);
                        showDialog("Busy Road");
                    }
                    break;
                }



                case R.id.btnLM9:{
                    if(fjup==0){
                        jupBtn.setTextColor(Color.RED);
                        jupImg.setImageResource(R.drawable.btn_islend1);
                        fjup=1;
                        cjup++;
                        landmark.put(jupStr,cjup);
                    }else{
                        jupBtn.setTextColor(Color.BLACK);
                        jupImg.setImageResource(R.drawable.btn_islend0);
                        fjup=0;
                        landmark.put(jupStr,0);
                        showDialog("Jerk Up");
                    }
                    break;
                }
                case R.id.btnLM10:{
                    if(fjdown==0){
                        jdownBtn.setTextColor(Color.RED);
                        jdownImg.setImageResource(R.drawable.btn_dummy1);
                        fjdown=1;
                        cjdown++;
                        landmark.put(jdownStr,cjdown);
                    }else{
                        jdownBtn.setTextColor(Color.BLACK);
                        jdownImg.setImageResource(R.drawable.btn_dummy0);
                        fjdown=0;
                        landmark.put(jdownStr,0);
                        showDialog("Jerk Down");
                    }
                    break;
                }
                case R.id.btnLM11:{
                    if(fnrml==0){
                        nrmlBtn.setTextColor(Color.RED);
                        nrmlImg.setImageResource(R.drawable.btn_dummy1);
                        fnrml=1;
                        cnrml++;
                        landmark.put(nrmlStr,cnrml);
                    }else{
                        nrmlBtn.setTextColor(Color.BLACK);
                        nrmlImg.setImageResource(R.drawable.btn_dummy0);
                        fnrml=0;
                        landmark.put(nrmlStr,0);
                        showDialog("Normal Road");
                    }
                    break;
                }
                case R.id.btnLM12:{
                    if(frgh==0){
                        rghBtn.setTextColor(Color.RED);
                        rghImg.setImageResource(R.drawable.btn_dummy1);
                        frgh=1;
                        crgh++;
                        landmark.put(rghStr,crgh);
                    }else{
                        rghBtn.setTextColor(Color.BLACK);
                        rghImg.setImageResource(R.drawable.btn_dummy0);
                        frgh=0;
                        landmark.put(rghStr,0);
                        showDialog("Rough Road");
                    }
                    break;
                }
            }
        }
    }*/
    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List wifiList = mainWifi.getScanResults();

            for (int i = 0; i < wifiList.size(); i++) {
                ScanResult scanResult = (ScanResult)wifiList.get(i);
// I have to put a try catch block here otherwise it returns an exception.
                Toast.makeText(getActivity(), scanResult.BSSID, Toast.LENGTH_SHORT).show();
                try{
                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    fosWiFi.write((scanResult.BSSID+","+scanResult.SSID+","+scanResult.level+","+timestampFormatted+"\n").getBytes());

                }
                catch(Exception e){

                }
            }
        }
    }
}
