package com.example.my_sensor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;
    private float mAzimut,mPitch,mRoll;
    private TextView mResultView;

    float[] mGravity;
    float[] mGeomagnetic;
    find_path fp;
    TextView tv;


    List<ScanResult> scanResult;
    Vector<AccessPoint> myAPs;
    Vector<AccessPoint> accessPoints;
    WifiManager wifiManager;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREAN);
    int curX;
    int curY;

    /* Location permission 을 위한 필드 */
    public static final int MULTIPLE_PERMISSIONS = 10;
    //권한 배열로 넣기
    String[] permissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onStart(){
        super.onStart();
        if (Build.VERSION.SDK_INT >= 23){
            if(!checkPermissions()){
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize our AP list
        myAPs = new Vector<>();
        //you must input ssid, bssid, x, y data.
        myAPs.add(new AccessPoint("iptime", "72:5d:cc:4d:6f:88", "-1", "0", "-1", 0, 0));
        myAPs.add(new AccessPoint("iptime", "72:5d:cc:4d:aa:94", "-1", "0", "-1", 0, 0));
        myAPs.add(new AccessPoint("iptime", "72:5d:cc:4d:a5:f8", "-1", "0", "-1", 20, 0));
        myAPs.add(new AccessPoint("iptime", "72:5d:cc:4d:a9:f8", "-1", "0", "-1", 0, 20));
        curX=0;
        curY=0;
        /* get current positiion */
        /*
        TimerTask wifiScan = new TimerTask() {
            @Override
            public void run() {
                wifiManager.startScan();
            }
        };
        */
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        accessPoints = new Vector<>();
        if(wifiManager!=null){
            if(!wifiManager.isWifiEnabled()){
                wifiManager.setWifiEnabled(true);
            }
            final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(mWifiScanReceiver, filter);
            //Timer wifiScanTimer = new Timer();
            //wifiScanTimer.schedule(wifiScan, 0 , 2000); // wifiScan Delay 걸기
        }
        wifiManager.startScan();
        mResultView = (TextView) findViewById(R.id.text);
        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        fp = new find_path(this);
        fp.set_current_position(10,23);
        tv = (TextView) findViewById(R.id.position);
        String str = "y : "+fp.getCurrent_y()+", x : "+fp.getCurrent_x();
        tv.setText(str);

    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //execute when get wifi data
    private BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action!=null){
                if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                    context.sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
                    getWIFIScanResult();
                    //wifiManager.startScan();
                    Log.d("wifiAction : ", "SCAN_RESULTS");
                }
                else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                    context.sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
                    Log.d("wifiAction : ", "NETWORK_STATE");
                }
            }
        }
    };
    // get wifiscan result and input to variables
    public void getWIFIScanResult(){
        Log.d("getWIFIScanResult() : ", "getResult start");
        int addMax=4; // 계산에 쓸 공유기 정보는 4개까지 받을 예정
        Vector<AccessPoint> nowAP = new Vector<>(); // list에 있는 AP는 이곳에 저장
        WifiInfo info = wifiManager.getConnectionInfo();
        scanResult = wifiManager.getScanResults();
        Log.d("길이 : ", scanResult.size()+"");
        if(accessPoints.size()!=0){
            accessPoints.clear();
        }
        for(int i=0; i<scanResult.size(); i++){
            ScanResult result = scanResult.get(i);
            double TxPower = -25; // 무선공유기 25dbM 잡고 대충...
            int n = 2;// free space에서는 2로 잡는다고한다.
            String distance = String.format("%.2f", Math.pow(10, ((double)TxPower-result.level)/(10 * n))); // result.level의 단위는 dbM
            if(result.frequency < 3000){
                Log.d(". SSID : "+result.SSID, result.level + ", "+result.BSSID);
                AccessPoint addAP = new AccessPoint(result.SSID, result.BSSID, String.valueOf(result.level), String.valueOf(simpleDateFormat.format(new Date())), distance, -1, -1);
                for(int j=0; j<myAPs.size(); j++){
                    if((myAPs.get(j).getBssid().equals(addAP.getBssid())) && (myAPs.get(j).getSsid().equals(addAP.getSsid())) && (nowAP.size()<addMax)){
                        addAP.setX(myAPs.get(j).getX());
                        addAP.setY(myAPs.get(j).getY());
                        Log.d("findResult : ", "I found it!!!("+myAPs.get(j).getBssid()+")");
                        nowAP.add(addAP);
                        break;
                    }
                }
                accessPoints.add(addAP);
            }
            wifiManager.setWifiEnabled(false);
            wifiManager.setWifiEnabled(true);
        }

        //삼변측위Alg 활용, 현재 위치 계산 후 반환
        //데이터 저장
        if(nowAP.size()>=3){
            int APcnt=nowAP.size();
            double[][] positions = new double[APcnt][2];
            double[] distances = new double[APcnt];
            for(int i=0; i<APcnt; i++){
                positions[i][0] = nowAP.get(i).getX();
                positions[i][1] = nowAP.get(i).getY();
                distances[i] = Double.parseDouble(nowAP.get(i).getDistance());
            }
            //계산(github 라이브러리 사용)
            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();
            double[] centroid = optimum.getPoint().toArray();
            curX = (int)centroid[1];
            curY = (int)centroid[0];
            Log.d("curX : ", String.valueOf(curX));
            Log.d("curY : ", String.valueOf(curY));
        }
        else{
            Log.d("error : ", "3개이상의 AP 정보가 탐색되지 않았습니다.("+nowAP.size()+"개 탐색, resultSize : "+scanResult.size()+")");
        }

    }

    public void onSensorChanged(SensorEvent event) {
        wifiManager.startScan();
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                mAzimut = (float)Math.toDegrees(orientation[0]);
                if(mAzimut < 0){
                    float tmp = 360 + mAzimut;
                    mAzimut = tmp;
                }
                mPitch = (float)Math.toDegrees(orientation[1]);
                mRoll = (float)Math.toDegrees(orientation[2]);
                String msg = "기본값";
                msg = fp.find_direction(mAzimut);

                String result;
                result = "Azimut:"+mAzimut+"\n"+"Pitch:"+mPitch+"\n"+"Roll:"+mRoll+"\n"+msg+"\n";
                mResultView.setText(result);

            }
        }
    }

    //for unregist mWifiScanReceiver
    @Override
    protected  void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mWifiScanReceiver);
    }
    /* Location permission 을 위한 메서드들 */
    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for(String p : permissions){
            result = ContextCompat.checkSelfPermission(MainActivity.this, p);
            if(result!= PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(p);
            }
        }
        if(!listPermissionsNeeded.isEmpty()){
            ActivityCompat.requestPermissions(MainActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults){
        switch (requestCode){
            case MULTIPLE_PERMISSIONS:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("permission", "granted");
                }
            }
        }
    }
    public void onclick(View view) {
        if (fp.move_front() == -1)
            Toast.makeText(this, "목적지에 도착하였습니다.", Toast.LENGTH_SHORT);
        else{
            String str = "y : "+fp.getCurrent_y()+", x : "+fp.getCurrent_x();
            tv.setText(str);
        }
    }

}

