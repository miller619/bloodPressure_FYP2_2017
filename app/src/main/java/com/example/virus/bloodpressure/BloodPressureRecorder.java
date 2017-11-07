package com.example.virus.bloodpressure;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.opencsv.CSVReader;

import com.example.virus.bloodpressure.Math.Fft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.ceil;

public class BloodPressureRecorder extends Activity implements SurfaceHolder.Callback, android.hardware.Camera.PreviewCallback{

    private static SurfaceHolder mHolder = null;
    private static android.hardware.Camera mCamera;
    private static SurfaceView mView;
    private static double h, w, ag;
    private static long startTime = 0;
    private static long endTime = 0;
    private static double y_Sum;
    private static Button calculateBP;
    Handler handler;
    double Q;
    TextView heartRate, systolic, diastolic;
    LinearLayout hr, sys, dia;

    ImageView animationImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_pressure_recorder);

        calculateBP = findViewById(R.id.calculate);

        String height = getIntent().getExtras().getString("user_height");
        h = Double.parseDouble(height);
        String weight = getIntent().getExtras().getString("user_weight");
        w = Double.parseDouble(weight);
        String age = getIntent().getExtras().getString("user_age");
        ag = Double.parseDouble(age);
        Q = getIntent().getExtras().getDouble("user_cardiac_output");

        animationImage = findViewById(R.id.animationImage);
        ((AnimationDrawable) animationImage.getBackground()).start();


        hr = findViewById(R.id.hr);
        sys = findViewById(R.id.sys);
        dia = findViewById(R.id.dia);


        calculateBP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readCsv();
                hr.setVisibility(View.VISIBLE);
                sys.setVisibility(View.VISIBLE);
                dia.setVisibility(View.VISIBLE);
            }
        });


        mView = findViewById(R.id.preview_window);
        mHolder = mView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

        try {

            handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mCamera.stopPreview();
                    mCamera.release();
                    calculateBP.setVisibility(View.VISIBLE);
                    animationImage.setVisibility(View.GONE);


                }
            };
            handler.postDelayed(runnable, 40000);

        } catch (Exception e) {
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = android.hardware.Camera.open();
        startTime = System.currentTimeMillis();
    }




    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d("ERROR", "Camera error on SurfaceCreated" + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (mHolder.getSurface() == null)
            return;
        try {
            if (mCamera != null) {
                //mCamera.stopPreview();
            }
        } catch (Exception e) {
            Log.d("ERROR", "Camera error on SurfaceChanged" + e.getMessage());
        }
        try {
            assert mCamera != null;
            android.hardware.Camera.Parameters parameters = mCamera.getParameters();

            parameters.setPreviewSize(176, 144);
            mCamera.cancelAutoFocus();
            parameters.setAutoExposureLock(false);
            mCamera.setDisplayOrientation(90);
            //set fps
            parameters.setPreviewFpsRange(16000, 16000);
            //on flash
            parameters.setFlashMode(parameters.FLASH_MODE_TORCH);
            parameters.setAutoWhiteBalanceLock(true);
            parameters.setPreviewFormat(ImageFormat.NV21);

            //mCamera.setDisplayOrientation(90);
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();

        } catch (IOException e) {
            Log.d("ERROR", "Camera error on SurfaceChanged" + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, android.hardware.Camera camera) {
        //check if data is null
        if (bytes == null)
            throw new NullPointerException();

        android.hardware.Camera.Size size = camera.getParameters().getPreviewSize();

        //check if size is null
        if (size == null)
            throw new NullPointerException();

        //set resolution of camera view to optimal setting
        int width = size.width;
        int height = size.height;
        Log.d("Resolution ", " " + String.valueOf(width) + " " + String.valueOf(height));


        //call ImageProcess on the data to decode YUV420SP to RGB
        y_Sum = ImageProcessing.YUV420SPtoYSum(bytes.clone(), width, height, 4);
        ArrayList<Integer> yAverage = new ArrayList<>();
        yAverage.add((int) y_Sum);

        //end time in milliseconds
        endTime = System.currentTimeMillis();
        long EndDurationInMs = TimeUnit.MILLISECONDS.convert(endTime, TimeUnit.MILLISECONDS);
        ArrayList<Long> endOfTime = new ArrayList<>();
        endOfTime.add(EndDurationInMs);


        ArrayList<Long> getValues = new ArrayList<>();

        for (int i = 0; i < yAverage.size(); i++) {
            getValues.add(endOfTime.get(i));
            getValues.add((long)yAverage.get(i));
        }


        storeCsv(yAverage, getValues);
        Log.d("MyEntryData", String.valueOf(getValues));
    }

    public void storeCsv(ArrayList<Integer> yAverage, ArrayList<Long> getValues) {

        String filename = "temporary.csv";
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bpReader";
        File logDir = new File(path);
        logDir.mkdirs();
        File file = new File(logDir, filename);


        FileOutputStream outputStream = null;
        try {
            file.createNewFile();
            outputStream = new FileOutputStream(file, true);
            for (int i = 0; i < yAverage.size(); i += 2) {
                outputStream.write((getValues.get(i) + ",").getBytes());
                outputStream.write((getValues.get(i + 1) + "\n").getBytes());
            }
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //read from csv file
    public void readCsv() {
        String getPath = Environment.getExternalStorageDirectory() + "/bpReader";
        String csvFile = "temporary.csv";
        String path = getPath + "/" + csvFile;
        int length = 956;
        Double[] xCoords = new Double[length];
        final double[] tempY = new double[length];
        final Double[] yCoords = new Double[length];
        final double[] newXcord = new double[length];
        int i;
        int feqCount = 0;
        double[] HRFreq = new double[length];
        double HR = 0;
        double Beats = 0;


        CSVReader reader;
        try {
            File myFile = new File(path);
            reader = new CSVReader(new FileReader(myFile));
            String[] line;
            for (i = 0; i < yCoords.length; i++) {
                if ((line = reader.readNext()) != null) {
                    xCoords[i] = Double.valueOf(line[0]);
                    newXcord[i] = (xCoords[i] - xCoords[0]) / 1000d;
                    tempY[i] = Double.valueOf(line[1]);
                    yCoords[i] =tempY[i];
                    Log.d("read ", "Time: " + String.valueOf(newXcord[i]) + " Y-Sum " + String.valueOf(yCoords[i]));

                    if (newXcord[i] > 10 ) {
                        feqCount++;
                        HRFreq[i] = Fft.FFT(yCoords, feqCount, 16);


                        if (HRFreq[i] >= 1){
                            HR = HRFreq[i];
                        }
                        Log.d("FFTFreq ", "y Value " + yCoords[i] + " Freq Count: " + feqCount + " FFT: " + HR);
                    }

                }
            }
            myFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Beats = (int) ceil(HR * 60);



        Log.d("BPM ", "bpm1 "+Beats);

        Log.d("Data", "Height "+h+" Weight "+w+" Age "+ag+" Cardiac Output "+Q);

        //Resistivity of blood
        double ROB = 18.31;
        //Ejection Time
        double ET = (364.5-1.23*Beats);
        //Body Surface Area
        double BSA = 0.007184*(Math.pow(w,0.425))*(Math.pow(h,0.725));
        //Stroke volume
        double SV = (-6.6 + (0.25*(ET-35)) - (0.62*Beats) + (40.4*BSA) - (0.51*ag));
        //Pulse Pressure
        double PP = SV / ((0.013*w - 0.007*ag-0.004*Beats)+1.307);

        double MAP = Q*ROB;

        int SP = (int) (MAP + 3/2*PP);
        int DP = (int) (MAP - PP/3);

        Log.d("BPM ", "Sys "+SP+" Dys "+DP+" Beats "+Beats);

        //plotGraph(newXcord, tempY);

        heartRate = findViewById(R.id.show_heart_rate);
        systolic = findViewById(R.id.show_systolic);
        diastolic = findViewById(R.id.show_diastolic);

        heartRate.setText(String.valueOf(Beats));


        systolic.setText(String.valueOf(SP));


        diastolic.setText(String.valueOf(DP));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }


}
