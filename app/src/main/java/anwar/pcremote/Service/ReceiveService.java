package anwar.pcremote.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import static android.R.attr.path;

public class ReceiveService extends Service {
    private boolean listenning;
    private ServerSocket serverSocket;
    private Socket socket;
    private Database db;
    private Thread ListeningThread;
    private DownloadTask ReceiveThread;
    public ReceiveService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db=new Database(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Statr start");
        listenning=true;
        ListeningThread= new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(5555);
                    while (listenning)
                    {
                        ReceiveThread=  new DownloadTask(serverSocket.accept());
                        ReceiveThread.start();
                    }
                } catch (IOException e) {

                }
            }
        });
        ListeningThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listenning=false;
        if( ListeningThread.isAlive())
            ListeningThread.interrupt();
        if(ReceiveThread !=null&& ReceiveThread.isAlive())
            ReceiveThread.interrupt();
    }
    private class DownloadTask extends Thread {
        Socket clientSocket = null;
        private long file_size;
        private String file_name;
        public DownloadTask(Socket socket)
        {
            this.clientSocket = socket;
        }
        int id;
        @Override
        public void run() {
            try {
                System.out.println("Receiving service start............ ");
                File sdCard = Environment.getExternalStorageDirectory();
                String Path=sdCard.getAbsolutePath().toString()+"/PcRemote/Received/";
                File file = new File(Path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                byte[] buffer = new byte[4096];
                int read = 0;
                int totalRead = 0;
                file_size=dis.readLong();
                file_name=dis.readUTF();
                 id=db.addReceiveFile(file_name,"Downloding",0);
                FileOutputStream fos = new FileOutputStream(new File(Path+file_name));
                int remaining = (int) file_size;
                int i=0;
                while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                    totalRead += read;
                    remaining -= read;
                    fos.write(buffer, 0, read);
                    int pro=(int) ((float)((float)totalRead/file_size)*100);
                    System.out.println(" download =" +pro + " %.");
                    if(i==1000) {
                        System.out.println(file_name+" progress "+pro);
                        db.updateReceiveItem(String.valueOf(id),"Dowinloading",pro);
                        i=0;
                    }i++;
                }
                db.updateReceiveItem(String.valueOf(id),"Completed",100);
                fos.close();
                dis.close();
                clientSocket.close();
                System.out.println("Received successfull");
                stopSelf();

            } catch (IOException e) {
                db.updateReceiveItem(String.valueOf(id),"Dowinloading",0);
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
                System.out.println("Exception during download file "+e);
               // stopSelf();
            }
         //   stopSelf();
        }
    }

}
