/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcremote.comunicaton;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import org.imgscalr.Scalr;

/**
 *
 * @author anwar
 */
public class UdpServer {
    private DatagramPacket packet;
    private DatagramSocket RTPsocket; 
    private DatagramSocket AudioRTPsocket; 
    private int PORT = 25000; 
    private int AudioPORT = 25001; 
    private Timer timer; 
    private InetAddress ClientIPAddr;
    private PrintStream  outt;
    private  BufferedReader in;
    private Robot r = null;
    private UdpServer udpServer;
    private byte[] bte=new byte[10];
    private boolean isSending=false;
    public final static int HEADER_SIZE = 5;
    public final static int DATAGRAM_MAX_SIZE = 1450- HEADER_SIZE;
    int frame_nb = 0;
    private static final int BUF_SIZE = 512;
    private byte[] data=null;
    public UdpServer() {
     /*   timer = new Timer(0, new timerListener());
        timer.setInitialDelay(0);
        timer.setCoalesce(true);*/
    }
    public void startStrem(InetAddress ClientIPAddr,int PORT){
         this.ClientIPAddr=ClientIPAddr;
        this.PORT=PORT;
        isSending=true;
         new Thread(new Runnable() {
            @Override
            public void run() {
                sendPacket();
            }
         }).start();
         sendAudio();
         
    }
    public void stopStrem(){this.isSending=false;}
    public void sendPacket(){
        try {
            RTPsocket=new DatagramSocket();
            while(isSending){
                data=CaptureScreen();
                if(data !=null){
                    int size_p = 0, i;
                    int nb_packets = (int) Math.ceil(UdpServer.this.data.length/ (float) DATAGRAM_MAX_SIZE);
                    int size = DATAGRAM_MAX_SIZE;
                    for(i = 0; i < nb_packets; i++) {
                        if (i > 0 && i == nb_packets - 1)
                            size = UdpServer.this.data.length - i * DATAGRAM_MAX_SIZE;
                        byte[] data2 = new byte[HEADER_SIZE + size];
                        data2[0] = (byte) frame_nb;
                        data2[1] = (byte) nb_packets;
                        data2[2] = (byte) i;
                        data2[3] = (byte) (size >> 8);
                        data2[4] = (byte) size;
                        System.arraycopy(UdpServer.this.data, i * DATAGRAM_MAX_SIZE, data2,HEADER_SIZE, size);
                            size_p = data2.length;
                            packet = new DatagramPacket(data2, size_p,this.ClientIPAddr,PORT);
                            RTPsocket.send(packet);
                    }
                    frame_nb++;
                    if (frame_nb == 110)
                        frame_nb = 0;
                }
            }
            RTPsocket.disconnect();
            RTPsocket.close();
            System.out.println("Udp server Stop");
        } catch (SocketException ex) {
            Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
      public byte[] CaptureScreen(){
        try {
            r = new Robot();
            Rectangle rect = new Rectangle(0,0,1366,768);
            java.awt.image.BufferedImage img =Scalr.resize(r.createScreenCapture(rect),500);
            BufferedImage bi=r.createScreenCapture(rect);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", os);
            os.flush();
            byte[] bytes =os.toByteArray();;
            return bytes;
        }catch (Exception ioe) {System.out.println("exception during capture "+ioe);return bte;}
      }
    public boolean isRunning(){return this.isSending;}
    private void sendAudio(){
          new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AudioRTPsocket=new DatagramSocket();
                    final AudioFormat format = getFormat();
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                    TargetDataLine line = (TargetDataLine)AudioSystem.getLine(info);
                    line.open(format);
                    line.start();
                    int bytes_read=0;
                    byte buffer[] = new byte[BUF_SIZE];
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    while (isSending){
                    bytes_read =line.read(buffer, 0,BUF_SIZE);
                    if (bytes_read > 0) {
                        DatagramPacket Audiopacket = new DatagramPacket(buffer, bytes_read, UdpServer.this.ClientIPAddr, AudioPORT);
                        AudioRTPsocket.send(Audiopacket);
                    }
                }
                out.close();
                AudioRTPsocket.disconnect();
                AudioRTPsocket.close();
                System.out.println("Audio Server Stop");
                } catch (SocketException ex) {
                    Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
                }  
            }
          }).start();
    }
    private AudioFormat getFormat() {
        float sampleRate = 8000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian =false;
        return new AudioFormat(sampleRate,sampleSizeInBits, channels, signed, bigEndian);
  }
}
/*
  public byte[] CaptureScreen(){
        try {
            r = new Robot();
             Rectangle rect = new Rectangle(0,0,1366,768);
            java.awt.image.BufferedImage img =Scalr.resize(r.createScreenCapture(rect),500);
            BufferedImage bi=r.createScreenCapture(rect);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", os);
            os.flush();
            byte[] bytes =os.toByteArray();;
            return bytes;
        }catch (Exception ioe) {System.out.println("exception during capture "+ioe);return bte;}
      }
    public void CaptureScreen(){
         new Thread(new Runnable() {
            @Override
            public void run() {
                while(isSending){
                    try {
                        r = new Robot();
                        Rectangle rect = new Rectangle(0,0,1366,768);
                        java.awt.image.BufferedImage img =Scalr.resize(r.createScreenCapture(rect),500);
                        BufferedImage bi=r.createScreenCapture(rect);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ImageIO.write(bi, "jpg", os);
                        os.flush();
                        UdpServer.this.data=os.toByteArray();
                    } catch (AWTException ex) {
                        System.out.println("exception during capture "+ex);
                        Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        System.out.println("exception during capture "+ex);
                        Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
          }).start();
    }
    class timerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            byte[] data=CaptureScreen();
            if(data !=null)
            {
                int size_p = 0, i;
                int nb_packets = (int) Math.ceil(data.length/ (float) DATAGRAM_MAX_SIZE);
                   System.out.println("Packet size is "+ nb_packets);
                int size = DATAGRAM_MAX_SIZE;
                for(i = 0; i < nb_packets; i++) {
                      System.out.println("For loop index in server==== "+i);
                    if (i > 0 && i == nb_packets - 1)
                        size = data.length - i * DATAGRAM_MAX_SIZE;
                    byte[] data2 = new byte[HEADER_SIZE + size];
                    data2[0] = (byte) frame_nb;
                    data2[1] = (byte) nb_packets;
                    data2[2] = (byte) i;
                    data2[3] = (byte) (size >> 8);
                    data2[4] = (byte) size; 
                    System.arraycopy(data, i * DATAGRAM_MAX_SIZE, data2,HEADER_SIZE, size);
                    try{
                        size_p = data2.length;
                        packet = new DatagramPacket(data2, size_p,InetAddress.getByName("192.168.43.1"),25000);
                         RTPsocket.send(packet);
                    }catch (Exception ioe) {System.out.println("exception during packet send "+ioe);}
                }
                frame_nb++;
                if (frame_nb == 110)
                    frame_nb = 0;
					
            }else System.out.println("data null"+data);
        }
      }
    public void StartTimer( InetAddress ClientIPAddr,int PORT){
        this.ClientIPAddr=ClientIPAddr;
        this.PORT=PORT;
        System.out.println("ip client "+ ClientIPAddr);
        try {
            RTPsocket=new DatagramSocket();
            timer.start();
        } catch (SocketException ex) {
            Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void StopTimer(){
        timer.stop();
    }
    public BufferedImage scale(BufferedImage src, int w, int h){
    BufferedImage img = 
            new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    int x, y;
    int ww = src.getWidth();
    int hh = src.getHeight();
    int[] ys = new int[h];
    for (y = 0; y < h; y++)
        ys[y] = y * hh / h;
    for (x = 0; x < w; x++) {
        int newX = x * ww / w;
        for (y = 0; y < h; y++) {
            int col = src.getRGB(newX, ys[y]);
            img.setRGB(x, y, col);
        }
    }
    return img;
}
    
}
*/