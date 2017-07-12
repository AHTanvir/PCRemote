/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcremote.fileShare;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;

/**
 *
 * @author anwar
 */
public class TaskReceive extends Thread{
    private Socket client;
    int pro=0;
    private ProgressFXMLController controller;
    final SimpleDoubleProperty prop=new SimpleDoubleProperty();
    public TaskReceive(ProgressFXMLController controller,Socket client) {
        this.controller= controller;
        this.client = client;
    }

    @Override
    public void run() {
        try{
            String path="D:/";
            DataInputStream dis = new DataInputStream(client.getInputStream());
            byte[] buffer = new byte[4096];
            int read = 0;
            int totalRead = 0;
            String file_name=dis.readUTF();
            controller.label_name.setText(file_name);
            long file_size=dis.readLong();
            System.out.println("Receiving ................");
            FileOutputStream fos = new FileOutputStream(new File(path+file_name));
            int remaining = (int) file_size;
            int i=0;
            while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                fos.write(buffer, 0, read);
                int pro=(int) ((float)((float)totalRead/file_size)*100);
                System.out.println(" download =" +pro + " %.");
                prop.set((double)pro/100);
                controller.progress_bar.progressProperty().bind(prop);
                Platform.runLater(() ->controller.label_progress.setText(Integer.toString(pro)+"%"));
            }
            fos.close();
            dis.close();
            client.close();
            System.out.println("Received successfull");
        } catch (IOException ex) {
            System.out.println("Received exception "+ex);
        }
    }
     public void progress(){
        controller.label_progress.setText("name");
        prop.set((double)pro/100);
        Platform.runLater(() -> controller.progress_bar.progressProperty().bind(prop));
    }
}
