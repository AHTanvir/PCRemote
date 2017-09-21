package anwar.pcremote.Service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import anwar.pcremote.Model.ListModel;
import anwar.pcremote.Model.RowItem;
import anwar.pcremote.filleShare.SendThreadListener;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SendIntentService extends IntentService implements SendThreadListener {
    private Socket socket;
    private Database db;
    private int jj;
    private List<Thread> threads=new ArrayList<>();
    private SendTask sendThread;

    private List<SendTask> sendthreads=new ArrayList<>();
    public SendIntentService() {
        super("SendIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("intent service Start for new Task");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        db=new Database(this);
        if(((intent !=null)&&(intent.getExtras())!=null)) {
            final String server_ip = intent.getExtras().getString("SERVER_IP");
            final int port = intent.getExtras().getInt("SERVER_PORT");
            final String path[] = intent.getExtras().getStringArray("PATH");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < path.length; i++) {
                        final String p = path[i];
                        sendThread = new SendTask(p, server_ip, port, SendIntentService.this);
                        sendThread.start();
                        sendthreads.add(sendThread);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

    }
    private void sendTask(String selectedPath,String ip,int port){
        RowItem rowItem;
        System.out.println("send task start");
       // String filename= Uri.parse(selectedPath).getLastPathSegment().toString();
        File sourceFile = new File(selectedPath);
        String filename=sourceFile.getName();
        int len=(int)sourceFile.length()/1024;
        if (!sourceFile.isFile()) {
            return;
        }
        //int id=db.addSendFile(filename,"Sending..",0);
        rowItem=new RowItem(filename,"Sending..",0);
        ListModel.getmInstance().addSendItem(rowItem);
        int pro=0;
        try {
            socket = new Socket(ip, port);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(selectedPath);
            long length=sourceFile.length();
            dos.writeUTF(filename);
            dos.writeLong(sourceFile.length());
            int read = 0;
            int totalRead = 0;
            byte[] buffer = new byte[4096];
            int remaining= (int) length;
            int i=0;
            while ((read =fis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                dos.write(buffer);
                totalRead += read;
                remaining -= read;
                 pro=(int)((float)((float)totalRead/length)*100);
                if(i==100){
                    //db.updateSendItem(String.valueOf(id),"sending",pro);
                    rowItem.setProgress(pro);
                    i=0;
                }
                i++;
                // int progress = ((int) (((double) read / (double) length) * 100));
                System.out.println("send =" +pro);
            }
            //db.updateSendItem(String.valueOf(id),"send",pro);
            rowItem.setStatus("Send");
            rowItem.setProgress(pro);
            socket.close();
            fis.close();
            dos.close();
        }catch (UnknownHostException ex){
            System.out.println("send task unknown host exception "+ex);
        } catch (Exception e) {
            System.out.println("send task exception "+e);
            //db.updateSendItem(String.valueOf(id),"Failed",pro);
            rowItem.setStatus("Failed");
            e.printStackTrace();
        }
    }

    @Override
    public void successfull() {

    }

    @Override
    public void failed() {

    }
}
