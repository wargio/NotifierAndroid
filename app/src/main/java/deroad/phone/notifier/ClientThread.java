package deroad.phone.notifier;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

class ClientThread implements Runnable {

    String who, msg;
    int access = 0;
    Context context;
    Data data = null;

    public ClientThread(String who, String msg, int access, Context ctx){
        this.who = who;
        this.msg = msg;
        this.access = access;
        this.context = ctx;

        Log.i("ClientThread", "who: " + who + " msg: " + msg);
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.setSoTimeout(2500); // 2.5 sec
            socket.setKeepAlive(false);
            InetSocketAddress addr = new InetSocketAddress(NotificationService.ip, NotificationService.port);
            Log.i("ClientThread", "Connecting..");
            socket.connect(addr);
            if (!socket.isConnected() && access == 0){
                MainActivity.self.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.resetUI(null);
                    }
                });
                Log.i("ClientThread", "Bad connection");
            }
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Log.i("ClientThread", "Sending data..");
            out.writeObject(new Data(who, msg, access));
            if(access == 0) {
                data = (Data) in.readObject();
                NotificationService.access = data.getAccess();
                MainActivity.self.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.connectedUI(data.getMessage());
                    }
                });

                Log.i("ClientThread", "Got access data " + data.getAccess());
            }
            socket.close();
        } catch (Exception e1) {
            e1.printStackTrace();
            MainActivity.self.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.resetUI("Disconnected from the host.");
                }
            });
        }
        try {
            if(socket != null && !socket.isClosed())
                socket.close();
        }catch (Exception e){
        }
    }

}