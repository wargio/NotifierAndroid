package deroad.phone.notifier;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Date;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    static final String lastIP = "lastIP";
    static TextView ipaddr = null;
    static TextView hostname = null;
    static EditText remote = null;
    static Button connect = null;
    static Button ping = null;
    static Context ctx = null;
    static Activity self;
    static long lastPing = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        self = this;


        ctx = this.getApplicationContext();
        ipaddr = (TextView)this.findViewById(R.id.ipaddress);
        hostname = (TextView)this.findViewById(R.id.hostname);
        remote = (EditText)this.findViewById(R.id.remoteaddress);
        connect = (Button)this.findViewById(R.id.button);
        ping = (Button)this.findViewById(R.id.ping);
        ping.setVisibility(View.GONE);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isWifi()){
                    Toast.makeText(ctx, "Not connected to WiFi.", Toast.LENGTH_SHORT).show();
                    return;
                }

                remote.setEnabled(false);
                connect.setEnabled(false);
                String remoteaddr = null;
                boolean bad = false;
                if(remote.getText() != null) {
                    remoteaddr = remote.getText().toString();
                } else {
                    bad = true;
                }

                if(!bad && validate(remoteaddr)){
                    NotificationService.ip = remoteaddr;
                    Toast.makeText(ctx, "Connecting...", Toast.LENGTH_SHORT).show();
                    Thread t = new Thread(new ClientThread(null, Build.MODEL, 0, ctx));
                    t.start();
                    SharedPreferences sharedPref = self.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(lastIP, remoteaddr);
                    editor.commit();
                }
                else
                    resetUI(null);
            }
        });

        ping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date now = new Date();
                Log.i("Ping", "Ping " + lastPing);
                if(lastPing < now.getTime() ){
                    lastPing = now.getTime() + 5000;
                    Toast.makeText(ctx, "Sending ping.", Toast.LENGTH_SHORT).show();
                    Thread t = new Thread(new ClientThread("Ping", "" + now, NotificationService.access, ctx));
                    t.start();
                } else {
                    long lnow = (lastPing - now.getTime())/1000;
                    lnow++;
                    Toast.makeText(ctx, "You need to wait " + lnow, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(!isWifi()){
            Toast.makeText(ctx, "Not connected to Wifi.", Toast.LENGTH_SHORT).show();
            this.finish();
        }
        try {
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            String ipString = String.format(
                    "%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));
            ipaddr.setText("IP: " + ipString);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(ctx, "Not connected to Wifi.", Toast.LENGTH_SHORT).show();
            this.finish();
        }
        hostname.setText("Name: " + Build.MODEL);


        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String remoteip = sharedPref.getString(lastIP, null);
        if(remoteip != null)
            remote.setText(remoteip);
    }

    static void resetUI(String reset){
        if(reset == null)
            reset = "Invalid remote ip address.";
        Toast.makeText(ctx, reset, Toast.LENGTH_SHORT).show();
        remote.setEnabled(true);
        connect.setEnabled(true);
        ping.setEnabled(false);
        ping.setVisibility(View.GONE);
        connect.setVisibility(View.VISIBLE);
        NotificationService.access = 0;
        NotificationService.ip = null;
    }

    static void connectedUI(String host){
        ping.setEnabled(true);
        ping.setVisibility(View.VISIBLE);
        connect.setVisibility(View.GONE);
        Toast.makeText(ctx, "Connected to: " + host, Toast.LENGTH_SHORT);
    }

    boolean isWifi() {
        final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (wifi == null) ? false : wifi.isConnectedOrConnecting();
    }

    public boolean validate(final String ip){
        if(ip == null)
            return false;
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }
}
