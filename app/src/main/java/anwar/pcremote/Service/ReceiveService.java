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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import anwar.pcremote.Manager.SharedPref;
import anwar.pcremote.Model.ListModel;
import anwar.pcremote.Model.RowItem;
import anwar.pcremote.Streming.Constants;

import static android.R.attr.path;

public class ReceiveService extends Service {
    private boolean listenning;
    private ServerSocket serverSocket;
    private Socket socket;
    private Database db;
    private Thread ListeningThread;
    private DownloadTask ReceiveThread;
    private List<DownloadTask> threads=new ArrayList<>();
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
                if(sendQuery() !=null){
                    try {
                        serverSocket = new ServerSocket(5555);
                        while (listenning) {
                            ReceiveThread=  new DownloadTask(serverSocket.accept());
                            ReceiveThread.start();
                            threads.add(ReceiveThread);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                    int i=0;
                    while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                        totalRead += read;
                        remaining -= read;
                        fos.write(buffer, 0, read);
                        int pro=(int) ((float)((float)totalRead/file_size)*100);
                        System.out.println(" download =" +pro + " %.");
                        if(pro%5==0) {
                            System.out.println(file_name+" progress "+pro);
                            //ReceiveService.this.db.updateReceiveItem(String.valueOf(id),"Dowinloading",pro);
                            rowItem.setProgress(pro);
                        }
                    }
                    //ReceiveService.this.db.updateReceiveItem(String.valueOf(id),"Completed",100);
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
    String sendQuery(){
        String str=null;
        try {
            System.out.println("SEARCH PORT LISTENNING");
            ServerSocket server=new ServerSocket(Constants.SEARCH_PORT);
            Socket socket=server.accept();
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            str = br.readLine();
            if(str.equals("query")){
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os, true);
                pw.println(sharedPref.getUserName());
                pw.close();
            }else str=null;
            br.close();
            server.close();
            System.out.println("Just said hello to:" + str);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return str;
    }
}
