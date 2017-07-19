/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcremote.comunicaton;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import com.sun.glass.events.KeyEvent;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import javafx.application.Platform;
import pcremote.Constant;
import pcremote.FXMLDocumentController;

/**
 *
 * @author anwar
 */
public class CommandExecutor {
    private  UdpServer udpserver;
    private Robot robot;
    String path="NotFile";
    Clipboard board;
    private Server server;
   public CommandExecutor (UdpServer udpServer){
      this.udpserver=udpServer;
    }
    public void execute(String line,InetAddress ClientIPAddr){
        path="NotFile";
        try{
            robot=new Robot();
         if(line.contains("ScreenSize")){
                Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
                int sw=size.width;
                int sh=size.height;
                int cw=Integer.parseInt(line.split(";")[1]);
                int ch=Integer.parseInt(line.split(";")[2]);
                Constant.fx=((double)(sw-cw)/cw);
                Constant.fy=((double)(sh-ch)/ch);
            }
            else if(line.contains("Download")){
                int xy[]=getTouchPoint(line);
                robot.mouseMove(xy[0],xy[1]);//
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_C);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.keyRelease(KeyEvent.VK_C);
                Thread.sleep(150);
                 board=Toolkit.getDefaultToolkit().getSystemClipboard();
                List<File> list=(List<File>)board.getData(DataFlavor.javaFileListFlavor);
                if(list!=null){
                    Iterator<File> it=list.iterator();
                    while(it.hasNext()){
                        path=it.next().getAbsolutePath();
                  }
              }
               callBack c=new FXMLDocumentController();
               if(!path.equals("NotFile"))
               Platform.runLater(() ->  c.ChooseFile(path));
            }
            else if(line.contains("udpPort;")){
                if(this.udpserver.isRunning())
                    this.udpserver.stopStrem();
                final String s[]=line.split(";");
                System.out.println("Strat strem to udp port "+s[1]+" ip"+ ClientIPAddr);
                this.udpserver. startStrem(ClientIPAddr,Integer.parseInt(s[1]));
            }
            else if(line.contains("KEY_EVENT")){
                int event=Integer.parseInt(line.split(";")[1]);
                robot.keyPress(event);
                robot.keyRelease(event);
            }
            else if(line.contains(",")){
                float movex=Float.parseFloat(line.split(",")[0]);
                float movey=Float.parseFloat(line.split(",")[1]);
                Point point = MouseInfo.getPointerInfo().getLocation();
                float nowx=point.x;
                float nowy=point.y;
                robot.mouseMove((int)(nowx+movex),(int)(nowy+movey));
            }
            else if(line.contains("Mouse_Event")){
                //left click 1024 and right click 4096
                int xy[]=getTouchPoint(line);
                robot.mouseMove(xy[0],xy[1]);//
                int event=Integer.parseInt(line.split(";")[3]);
                Thread.sleep(100);
                robot.mousePress(event);
                robot.mouseRelease(event);
            }
            else if(line.equalsIgnoreCase("exit")){
                this.udpserver.stopStrem();
                System.out.print("exit");
            }
        }catch (Exception e) {System.out.println("Read failed"+e);}
    }
    private int[] getTouchPoint(String line){
        int xy[]=new int[2];
                float movex=Float.parseFloat(line.split(";")[1]);
                float movey=Float.parseFloat(line.split(";")[2]);
                Point point = MouseInfo.getPointerInfo().getLocation();
                xy[0]=(int) ((Constant.fx*movex)+movex);
                xy[1]=(int) ((Constant.fy*movey)+movey);
        return xy;
    }
     public interface callBack{
       public void ChooseFile(String path);
    }
}
