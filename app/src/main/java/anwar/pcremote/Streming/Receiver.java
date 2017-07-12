package anwar.pcremote.Streming;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by anwar on 6/27/2017.
 */

public class Receiver {
    private Handler handler;
    private ServerSocket serverSocket;
    private Socket socket;
    private Context context;
    private boolean listening;
    private int listeningPort=0;
    private HashMap<InetAddress, Socket> sockets = new HashMap<InetAddress, Socket>();

    public Receiver(Handler handler) {
        this.handler = handler;
    }

    public Receiver() {}

    private class server extends Thread{
        Socket clientSocket = null;
        public server(Socket socket)
        {
            this.clientSocket = socket;
            Receiver.this.sockets.put(socket.getInetAddress(), socket);
        }
        public void run() {
            try {
                System.out.println("Receving date from client");
                //BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());
                int length = dIn.readInt();                    // read length of incoming message
                if(length>0) {
                    byte[] buff = new byte[length];
                    dIn.readFully(buff, 0, buff.length);
                    Message msg = handler.obtainMessage();
                    msg.obj=buff;
                   // msg.obj = Base64.encodeToString(message,Base64.DEFAULT);
                    Receiver.this.handler.sendMessage(msg);
                }
                dIn.close();
                        clientSocket.shutdownInput();
                        clientSocket.shutdownOutput();
                        clientSocket.close();
                        Receiver.this.sockets.remove(clientSocket.getInetAddress());


            } catch (IOException e) {
                System.out.println("Receving exception+e");
                Log.e("AndroidIM_CON", "ReceiveConnection.run: when receiving connection");
            }
        }
    }
    public int startListening(int portNo,Context context)
    {
        listening = true;
        System.out.println("L prepar.. listenning");
        try {
            serverSocket = new ServerSocket(portNo);
            this.listeningPort = portNo;
            this.context=context;
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("L server exception 1"+e);
            this.listeningPort = 0;
            return 0;
        }

        while (listening) {
            try {
                System.out.println("Listenning"+listening);
                new server(serverSocket.accept()).start();
            } catch (IOException e) {
                System.out.println("L server exception 2"+e);
                return 2;
            }
        }
        try {
            System.out.println("L server close");
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("L server exception 3"+e);
            Log.e("Exception server socket", "Exception when closing server socket");
            return 3;
        }
        return 1;
    }

    public void stopListening()
    {
        this.listening = false;
    }

    public void exit()
    {
        for (Iterator<Socket> iterator = sockets.values().iterator(); iterator.hasNext();)
        {
            Socket socket = (Socket) iterator.next();
            try {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            } catch (IOException e) {
            }
        }

        sockets.clear();
        this.stopListening();
    }
    public int getListeningPort() {
        return this.listeningPort;
    }
}
