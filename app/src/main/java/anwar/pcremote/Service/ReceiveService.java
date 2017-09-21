package anwar.pcremote.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import anwar.pcremote.Manager.SharedPref;
import anwar.pcremote.Model.ListModel;
import anwar.pcremote.Model.RowItem;
import anwar.pcremote.filleShare.SendThreadListener;

public class ReceiveService extends Service {
    private boolean listenning;
    private ServerSocket serverSocket;
    private Socket socket;
    private Database db;
    private Thread ListeningThread;
    private SendTask sendThread;
    private DownloadTask ReceiveThread;
    private List<DownloadTask> downthreads=new ArrayList<>();
    private SharedPref sharedPref;
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
        sharedPref=new SharedPref(this);
        listenning=true;
        ListeningThread= new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        serverSocket = new ServerSocket(5555);
                        while (listenning) {
                            ReceiveThread=  new DownloadTask(serverSocket.accept());
                            ReceiveThread.start();
                            downthreads.add(ReceiveThread);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            ListeningThread.start();

        return START_NOT_STICKY;
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
        String line;
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
            RowItem rowItem=null;
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
                    //id=ReceiveService.this.db.addReceiveFile(file_name,"Downloding",0);
                    rowItem=new RowItem(file_name,"Downloding",0);
                    ListModel.getmInstance().addReceiveItem(rowItem);
                    FileOutputStream fos = new FileOutputStream(new File(Path+file_name));
                    int remaining = (int) file_size;
                    while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                        totalRead += read;
                        remaining -= read;
                        fos.write(buffer, 0, read);
                        int pro=(int) ((float)((float)totalRead/file_size)*100);
                        System.out.println(" download =" +pro + " %.");
                        if(pro%4==0)
                            rowItem.setProgress(pro);
                    }
                    rowItem.setProgress(100);
                    rowItem.setStatus("Completed");
                    fos.close();
                    dis.close();
                    clientSocket.close();
                    System.out.println("Received successfull");
                    stopSelf();

                } catch (IOException e) {
                    //db.updateReceiveItem(String.valueOf(id),"Dowinloading",0);
                    rowItem.setStatus("Failed");
                    try {
                        clientSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                    System.out.println("Exception during download file "+e);
                    // stopSelf();
                }

        }
    }
}
