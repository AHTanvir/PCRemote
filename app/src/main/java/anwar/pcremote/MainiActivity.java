package anwar.pcremote;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static anwar.pcremote.R.id.Relative_layoutfor_fragments;

import anwar.pcremote.Manager.SharedPref;
import anwar.pcremote.Service.ReceiveService;
import anwar.pcremote.Streming.Constants;
import anwar.pcremote.Streming.MainFragment;
import anwar.pcremote.filleShare.SearchCallback;
import anwar.pcremote.filleShare.DeviceListFragment;
import anwar.pcremote.filleShare.SearchThread;

public class MainiActivity extends AppCompatActivity implements WifiDialogFragment.DialogListener,MainFragment.OnFragmentInteractionListener {
    private static final String TAG_RETAINED_FRAGMENT = "optionFragment";
    private static final int HOTSPOT_ENABLING = 12;
    private static final int HOTSPOT_ENABLED = 13;
    private OptionFragment optionFragment;
    private static final int PERMISSION_REQUEST = 111;
    private boolean isConnected = false;
    private Socket socket = null;
    private PrintWriter out = null;
    private Handler handler;
    private SearchCallback listener;
    private SharedPref sharedPref;
    private List<SearchThread> threadsList = new ArrayList<>();
    private SearchThread searchThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = new SharedPref(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkWritePermission();
        FragmentManager fm = getSupportFragmentManager();
        optionFragment = (OptionFragment) fm.findFragmentByTag(TAG_RETAINED_FRAGMENT);
        if (optionFragment == null) {
            optionFragment = new OptionFragment();
            fm.beginTransaction().add(Relative_layoutfor_fragments, optionFragment, TAG_RETAINED_FRAGMENT).commit();
        }
        if (!isConnectedViaWifi() && !isHotspotEnable()) {
            enableWifi();
            showWifiListDialog();
        }
        if (sharedPref.getUserName() == null)
            showUserNameDialog();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isConnected && out != null) {
            out.close();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
     /*   if(isMyServiceRunning(this))
            stopService(new Intent(this, ReceiveService.class ));*/
    }


    public boolean isConnectedToPc() {
        return isConnected && out != null;
    }

    @Override
    public void onFinishDialog(String ssid) {
        // if
    }

    public void printToServer(String com) {
        try {
            out.println(com);
            if (out.checkError()) {
                isConnected = false;
                out.close();
                socket.close();
            }

        } catch (Exception e) {
            System.out.println("out" + e);
            isConnected = false;
            out.close();
        }
    }
    public boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ReceiveService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void ConnectToServer(final String ip) {
        isConnected = true;
        if (Constants.SERVER_IP != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Display display = ((WindowManager) MainiActivity.this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    int mW = display.getWidth();
                    int mH = display.getHeight();
                    try {
                        InetAddress serverAddr = InetAddress.getByName(Constants.SERVER_IP);
                        socket = new Socket(serverAddr, Constants.SERVER_PORT);
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                                .getOutputStream())), true);
                        out.println("ScreenSize;" + mW + ";" + mH);
                        if (out.checkError())
                            isConnected = false;
                    } catch (UnknownHostException e) {
                        isConnected = false;
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        isConnected = false;
                    }
                }
            }).start();
        }
    }

    private void sshowDialog() {
        String m_Text = "";
        AlertDialog.Builder builder = new AlertDialog.Builder(MainiActivity.this);
        builder.setTitle("IP ADDRESS");
        builder.setMessage("ENTER PC IP");
        final EditText input = new EditText(MainiActivity.this);
        input.setText(Constants.SERVER_IP);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Constants.SERVER_IP = input.getText().toString();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //  super.onActivityResult(requestCode, resultCode, data);
        if(data !=null){
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("SendFragment");
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    public void connect(String ssid, String pass) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", pass);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    private void showWifiListDialog() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = getSupportFragmentManager();
                WifiDialogFragment wifiDialogFragment = new WifiDialogFragment();
                wifiDialogFragment.show(fm, "fragment_dialog");
            }
        }, 1000);
    }

    private void showUserNameDialog() {
        FragmentManager fm = getSupportFragmentManager();
        UserNameDialog userName = new UserNameDialog();
        userName.show(fm, " UserNameDialog");
    }

    public void enableWifi() {
        WifiManager wifi = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            Toast.makeText(this, "Truning on wifi", Toast.LENGTH_SHORT).show();
            wifi.setWifiEnabled(true);
        }
    }

    public boolean isHotspotEnable() {
        WifiManager wifi = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifi.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            int state = (Integer) method.invoke(wifi, (Object[]) null);
            return state == HOTSPOT_ENABLED || state == HOTSPOT_ENABLING;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void checkWritePermission() {
        int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if ((write == PackageManager.PERMISSION_GRANTED) && (read == PackageManager.PERMISSION_GRANTED)) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    finish();
                }
                break;
        }
    }
}

    /*   WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (!wifi.isWifiEnabled()){
        Toast.makeText(this, "Truning on wifi", Toast.LENGTH_SHORT).show();
        wifi.setWifiEnabled(true);
    }*/
/* public  String getMobileIP() {
     try {
         for (Enumeration<NetworkInterface> en = NetworkInterface
                 .getNetworkInterfaces(); en.hasMoreElements();) {
             NetworkInterface intf = (NetworkInterface) en.nextElement();
             for (Enumeration<InetAddress> enumIpAddr = intf
                     .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                 InetAddress inetAddress = enumIpAddr.nextElement();
                 if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())){
                     String ipaddress = inetAddress .getHostAddress().toString();
                     return ipaddress;
                 }
             }
         }
     } catch (SocketException ex) {
         Log.e("MainActivity", "Exception in Get IP Address: " + ex.toString());
     }
     return null;
 }*/

