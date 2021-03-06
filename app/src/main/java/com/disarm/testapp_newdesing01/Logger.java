package com.disarm.testapp_newdesing01;


import android.content.Context;
import android.location.LocationManager;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.FileHandler;
import java.io.*;

import static com.disarm.testapp_newdesing01.ModeOfTransport.getMode;
import static com.disarm.testapp_newdesing01.R.id.checkGPS;

/**
 * Created by ajalan065 on 22-11-2016.
 */
public class Logger {

    public static FileHandler logger = null;
    private static String filename = getMode() + "_Sound";
    private String appFolderName="GPSAndSensorRecorder";
    private File folder,subfolder;
    private Timestamp timestamp;

    private String timestampStr;
    private Date date;
    private Long time;
    private String subfolderName;
    private LocationManager locationManager;


    public Logger(String timestampStr, File subfolder) {
        this.timestampStr = timestampStr;
        this.subfolder = subfolder;
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
        //String date = df.format(Calendar.getInstance().getTime());
        filename += "_" + date;
        subfolderName = Extras.getSubFolderName();
        /*folder = new File(Environment.getExternalStorageDirectory() + "/" + appFolderName);
        boolean folder_exists = true;
        if (!folder.exists()) {
            folder_exists = folder.mkdir();
        }

        if (folder_exists) {
            boolean subfolder_exists = true;
            //this.timestampStr = timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_');
            String subFolderName = "DATA_" + getMode() +'_' + this.timestampStr;

            //else {
            subfolder = new File(Environment.getExternalStorageDirectory() + "/" + appFolderName + "/" + subFolderName);
            if (!subfolder.exists()) {
                subfolder_exists = subfolder.mkdir();
            }
        }*/
    }

    public void addRecordToLog(double message) {
        date=new Date();
        time=date.getTime();
        timestamp=new Timestamp(time);
        String lgtFilename="SOUND_"+this.timestampStr+".txt";
        final File gyrFile=new File(this.subfolder,lgtFilename);

        if (!gyrFile.exists())  {
            try  {
                Log.d("File created ", "File created ");
                gyrFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            Date date = new Date();
            long systemTimeInMilli=(new Date()).getTime();
            String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS").format(new Date(systemTimeInMilli));

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(gyrFile, true));
            buf.write("time, #x");
            buf.write(timestampFormatted + ", " + message +"\n");
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
