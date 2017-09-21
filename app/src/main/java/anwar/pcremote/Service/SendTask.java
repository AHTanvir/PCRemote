package anwar.pcremote.Service;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import anwar.pcremote.Model.ListModel;
import anwar.pcremote.Model.RowItem;
import anwar.pcremote.filleShare.SendThreadListener;

/**
 * Created by anwar on 9/21/2017.
 */

public class SendTask extends Thread{
    private String path;
    private String ip;
    private int port;
    private Socket socket;
    private SendThreadListener listener;
    public SendTask(String path, String ip, int port,SendThreadListener listener) {
        this.path = path;
        this.ip = ip;
        this.port = port;
        this.listener=listener;
    }
    @Override
    public void run() {
        RowItem rowItem;
        System.out.println("send task start");
        // String filename= Uri.parse(selectedPath).getLastPathSegment().toString();
        File sourceFile = new File(this.path);
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
            Socket socket=new Socket(this.ip, this.port);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(this.path);
            long length=sourceFile.length();
            dos.writeUTF(filename);
            dos.writeLong(sourceFile.length());
            int read = 0;
            int totalRead = 0;
            byte[] buffer = new byte[4096];
            int remaining= (int) length;
            while ((read =fis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                dos.write(buffer);
                totalRead += read;
                remaining -= read;
                pro=(int)((float)((float)totalRead/length)*100);
                if(pro%4==0)
                    rowItem.setProgress(pro);
                System.out.println("send =" +pro);
            }
            rowItem.setStatus("Send");
            rowItem.setProgress(pro);
            socket.close();
            fis.close();
            dos.close();
            listener.successfull();
        }catch (UnknownHostException ex){
            System.out.println("send task unknown host exception "+ex);
            rowItem.setStatus("Failed");
            listener.failed();
        } catch (Exception e) {
            System.out.println("send task exception "+e);
            //db.updateSendItem(String.valueOf(id),"Failed",pro);
            rowItem.setStatus("Failed");
            listener.failed();
            e.printStackTrace();
        }
    }
}
