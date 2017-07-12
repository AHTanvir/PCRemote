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
import javax.swing.ImageIcon;
import javax.swing.Timer;
import org.imgscalr.Scalr;

/**
 *
 * @author anwar
 */
public class UdpServer {
    private DatagramPacket packet; //UDP packet received from the server
    private DatagramSocket RTPsocket; //socket to be used to send and receive UDP packets
    private int PORT = 25000; //port where the client will receive the RTP packet
    private Timer timer; 
    private InetAddress ClientIPAddr;
    private PrintStream  outt;
    private  BufferedReader in;
    private java.awt.image.BufferedImage img = null;
    private javax.swing.ImageIcon icon = null;
    private Robot r = null;
    private UdpServer udpServer;
    private byte[] bte=new byte[10];
    private boolean isSending=false;
    public final static int HEADER_SIZE = 5;
    public final static int DATAGRAM_MAX_SIZE = 1450- HEADER_SIZE;
    int frame_nb = 0;
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
            }}).start();
    }
    public void stopStrem(){this.isSending=false;}
    public void sendPacket(){
          try {
            RTPsocket=new DatagramSocket();
           // timer.start();
        } catch (SocketException ex) {Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);}
        while(isSending)
        {
            byte[] data=CaptureScreen();
            if(data !=null){
                int size_p = 0, i;
                int nb_packets = (int) Math.ceil(data.length/ (float) DATAGRAM_MAX_SIZE);
                int size = DATAGRAM_MAX_SIZE;
                for(i = 0; i < nb_packets; i++) {
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
                        packet = new DatagramPacket(data2, size_p,this.ClientIPAddr,PORT);
                         RTPsocket.send(packet);
                    }catch (Exception ioe) {System.out.println("exception during packet send "+ioe);}
                }
                frame_nb++;
                if (frame_nb == 110)
                    frame_nb = 0;
					
            }
        }
        RTPsocket.close();
        System.out.println("Udp server Stop");
    }
    public byte[] CaptureScreen(){
        try {
            r = new Robot();
             Rectangle rect = new Rectangle(0,0,1366,768);
            img =Scalr.resize(r.createScreenCapture(rect),500);
            BufferedImage bi=r.createScreenCapture(rect);
            BufferedImage imgg = ImageIO.read(new File("D:/r.jpg"));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", os);
            os.flush();
            byte[] bytes =os.toByteArray();;
            return bytes;
        }catch (Exception ioe) {System.out.println("exception during capture "+ioe);return bte;}
      }
    public boolean isRunning(){return this.isSending;}
}
/*
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