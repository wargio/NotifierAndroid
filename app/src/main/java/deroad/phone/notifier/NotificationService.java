package deroad.phone.notifier;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;


public class NotificationService extends NotificationListenerService {
    Context context;
    static String ip = null;
    static int port = 10550;
    static int access = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        String title = "";
        String text = "";
        String pack = sbn.getPackageName();
        String ticker = "";

        if(sbn.getNotification().tickerText != null)
            ticker = sbn.getNotification().tickerText.toString();
        Bundle extras = sbn.getNotification().extras;
        if(extras != null) {
            title = extras.getString("android.title");
            text = extras.getCharSequence("android.text").toString();
        }

        if(ip != null && access != 0 && !pack.equalsIgnoreCase("android") && isWifi()){
            Log.i("Package", pack);
            Log.i("Ticker", ticker);
            Log.i("Title", title);
            Log.i("Text", text);
            Thread t = new Thread(new ClientThread(title, text, access, context));
            t.start();
        }
    }

    boolean isWifi() {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (wifi == null) ? false : wifi.isConnectedOrConnecting();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Msg", "Notification removed");
    }
}