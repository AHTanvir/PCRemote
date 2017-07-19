/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcremote.fileShare;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;

/**
 *
 * @author anwar
 */
public class Tasksend{
    private ProgressFXMLController controller;
    private String ip;
    private int port;
    private String path;
    private   int pro;
       final SimpleDoubleProperty prop=new SimpleDoubleProperty();

    public Tasksend(ProgressFXMLController controller, String ip, int port, String path) {
        this.controller = controller;
        this.ip = ip;
        this.port = port;
        this.path = path;

    }
   public void Send(){
       this.pro=0;
        try {
            File sourceFile = new File(this.path);
            int len=(int)sourceFile.length()/1024;
            if (!sourceFile.isFile()) {
                 System.out.println("Sending..return");
                return;
            }
            String filename=sourceFile.getName();
            Platform.runLater(() -> controller.label_name.setText(filename));
            Socket socket = new Socket(this.ip, this.port);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(this.path);
            long  length= sourceFile.length();
            dos.writeLong(sourceFile.length());
            dos.writeUTF(filename);
            int read = 0;
            int totalRead = 0;
            byte[] buffer = new byte[4096];
            int remaining= (int) length;
            while ((read =fis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                dos.write(buffer);
                totalRead += read;
                remaining -= read;
              this.pro=(int)((float)((float)totalRead/length)*100);
               progress();
              System.out.println(length+" Send =" +pro);
            }
            socket.close();
            fis.close();
            dos.close();
            System.out.println("Send-");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Sending..........exception"+e);
        }
   }
    public void progress(){
        Platform.runLater(() -> controller.label_progress.setText(String.valueOf(pro)+"%"));
        prop.set((double)pro/100);
        Platform.runLater(() -> controller.progress_bar.progressProperty().bind(prop));
    }
    
}
