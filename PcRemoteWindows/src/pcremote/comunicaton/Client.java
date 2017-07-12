/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcremote.comunicaton;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author anwar
 */
public class Client extends Thread{
    private String ip;
    private int port;
    private Socket client;
    private boolean b=true;
    private PrintStream  outt;
    private  BufferedReader in;
    private java.awt.image.BufferedImage img = null;
    private javax.swing.ImageIcon icon = null;
    private Robot r = null;
    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {

        try {
            byte b[]=CaptureScreen();
            System.out.println("len ="+b.length);
            System.out.println("Client thread");
            client=new Socket(this.ip,this.port);
           // outt = new PrintStream(client.getOutputStream());
            //in=new BufferedReader(new InputStreamReader(System.in));
            // String input=URLEncoder.encode(CaptureScreen(), "UTF-8");
            DataOutputStream t = new DataOutputStream(client.getOutputStream());
            t.writeInt(b.length);
             t.write(b);
            t.flush();
             client.close();
             t.close();
            
        } catch (IOException ex) {
            System.out.println("Client thread exception "+ex);
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    public byte[] CaptureScreen(){
       try {
            r = new Robot(); 
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle rect = new Rectangle(0,0,size.width, size.height);
            System.gc();
            //buff siz43254wwwww 1366height 768
            //img = r.createScreenCapture(rect);
            //icon = new javax.swing.ImageIcon(img);
            BufferedImage bi=r.createScreenCapture(rect);
            BufferedImage imgg = ImageIO.read(new File("D:/reeduce.jpg"));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bi, "bmp", os);
            os.flush();
            byte[] bytes =os.toByteArray();
            System.out.println("buff siz"+bytes.length);
            return bytes;
        }catch (Exception ioe) {System.out.println("exception during capture "+ioe);return null;}
        
    }
    public byte[] imgToBase64String(final RenderedImage img, final String formatName) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, formatName, os);
             byte[] bytes =os.toByteArray();
             return bytes;
           // return Base64.getEncoder().encodeToString(bytes);
          } catch (final IOException ioe) {throw new UncheckedIOException(ioe); }
        
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
public void stopclient(){
    b=false;
}
}
