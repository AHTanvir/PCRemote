/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcremote.comunicaton;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import pcremote.Constant;

/**
 *
 * @author anwar
 */
public class CommandExecutor {
    private  UdpServer udpserver;
    private Robot robot;
    private Server server=new Server();
   public CommandExecutor (){
        
    }
    public void execute(String line,InetAddress ClientIPAddr,UdpServer udpserver){
        try{
            robot=new Robot();
            if(line.equalsIgnoreCase("next")){
                robot.keyPress(KeyEvent.VK_PAGE_DOWN);
                robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
            }
            else if(line.contains("ScreenSize")){
                Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
                int sw=size.width;
                int sh=size.height;
                int cw=Integer.parseInt(line.split(";")[1]);
                int ch=Integer.parseInt(line.split(";")[2]);
                Constant.fx=((double)(sw-cw)/cw);
                Constant.fy=((double)(sh-ch)/ch);
            }
            else if(line.contains("udpPort;")){
                if(udpserver.isRunning())
                    udpserver.stopStrem();
                this.udpserver=udpserver;
                final String s[]=line.split(";");
                System.out.println("Strat strem to udp port "+s[1]+" ip"+ ClientIPAddr);
                udpserver. startStrem(ClientIPAddr,Integer.parseInt(s[1]));
            }
            else if(line.equalsIgnoreCase("previous")){
                robot.keyPress(KeyEvent.VK_PAGE_UP);
                robot.keyRelease(KeyEvent.VK_PAGE_UP);
            }
            else if(line.equalsIgnoreCase("play")){
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyRelease(KeyEvent.VK_SPACE);
            }
            else if(line.equalsIgnoreCase("forward")){
                robot.keyPress(KeyEvent.VK_RIGHT);
                robot.keyRelease(KeyEvent.VK_RIGHT);
            }
            else if(line.equalsIgnoreCase("backward")){
                robot.keyPress(KeyEvent.VK_LEFT);
                robot.keyRelease(KeyEvent.VK_LEFT);
            }
            else if(line.equalsIgnoreCase("next")){
                robot.keyPress(KeyEvent.VK_PAGE_DOWN);
                robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
            }
            else if(line.equalsIgnoreCase("enter")){
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
            }
            else if(line.equalsIgnoreCase("vol_up")){
                robot.keyPress(KeyEvent.VK_UP);
                robot.keyRelease(KeyEvent.VK_UP);
            }
            else if(line.equalsIgnoreCase("vol_down")){
                robot.keyPress(KeyEvent.VK_DOWN);
                robot.keyRelease(KeyEvent.VK_DOWN);
            }
            else if(line.equalsIgnoreCase("mclick")){
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            }
            else if(line.contains(",")){
                float movex=Float.parseFloat(line.split(",")[0]);
                float movey=Float.parseFloat(line.split(",")[1]);
                Point point = MouseInfo.getPointerInfo().getLocation();
                float nowx=point.x;
                float nowy=point.y;
                robot.mouseMove((int)(nowx+movex),(int)(nowy+movey));
            }
            else if(line.contains("left_click")){
                float movex=Float.parseFloat(line.split(";")[1]);
                float movey=Float.parseFloat(line.split(";")[2]);
                Point point = MouseInfo.getPointerInfo().getLocation();
                float fx=(float) ((Constant.fx*movex)+movex);
                float fy=(float) ((Constant.fy*movey)+movey);
                robot.mouseMove((int)(fx),(int)(fy));
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            }
            else if(line.equalsIgnoreCase("exit")){
                server.stopListening(); 
            }
        }catch (Exception e) {System.out.println("Read failed"+e);}
    }
}
