package anwar.pcremote.Streming;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.port;

/**
 * Created by anwar on 7/1/2017.
 */

public class UdpClient {
    private DatagramSocket RTPsocket;
    private static final String LOG_TAG = "UdpClient";
    private DatagramPacket rdp;
    private int PORT= 25000;
    private int AUDIOPORT= 25001;
    private boolean shouldRun;
    public static int HEADER_SIZE = 5;
    public static int DATAGRAM_MAX_SIZE = 1450;
    public static int DATA_MAX_SIZE = DATAGRAM_MAX_SIZE - HEADER_SIZE;
    private static final int BUF_SIZE = 512;
    private Timer timer;
    private Handler msgHandeler;
    private Context context;

    public UdpClient(Context context) {
        this.context = context;
    }

    public void StartStreming(final int PORT, final Handler msgHandeler){
        System.out.println("Udp client start");
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[DATAGRAM_MAX_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                int current_frame = -1;
                int slicesStored = 0;
                int conter=0;
                byte[] imageData = null;
                int cu=0;
                shouldRun=true;
                try {
                    RTPsocket=new DatagramSocket(PORT);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                while (shouldRun)
                {
                    try {
                        RTPsocket.receive(packet);
                    } catch (SocketTimeoutException e) {
                        RTPsocket.close();
                    } catch (IOException e) {
                        RTPsocket.close();
                        e.printStackTrace();
                    }catch (NullPointerException nu){
                        System.out.println("Null pointer exception +"+nu);
                    }

                    byte[] data = packet.getData();
                    int frame_nb = (int) data[0];
                    int nb_packets = (int) data[1] & 0xff;
                    int packet_nb =data[2] & 0xff ;;
                    System.out.println("For loop index in android==== "+packet_nb);
                    int size_packet = (int) ((data[3] & 0xff) << 8 | (data[4] & 0xff));

                    if ((packet_nb == 0) && (current_frame != frame_nb)) {
                        current_frame = frame_nb ;
                        slicesStored = 0;
                        imageData = new byte[nb_packets * DATA_MAX_SIZE];
                    }
                    if (frame_nb == current_frame) {
                        try {
                            System.out.println("Udp client "+ data.length+ " ; "+HEADER_SIZE+" ; "+ imageData.length+" ; "+packet_nb*DATA_MAX_SIZE+" ; "+size_packet);
                            System.arraycopy(data, HEADER_SIZE, imageData, packet_nb * DATA_MAX_SIZE, size_packet);
                            slicesStored++;
                        }catch (ArrayIndexOutOfBoundsException e){
                        }
                    }
                    System.out.println("recived_packets  "+cu++ );
				/* If image is complete display it */
                    if (slicesStored == nb_packets) {
                        Message msg = msgHandeler.obtainMessage();
                        msg.obj=imageData;
                        msgHandeler.sendMessage(msg);
                    } else {

                    }
                }
                RTPsocket.close();
                System.out.println("Udp client close");
            }
        }).start();
        audio();
    }
    public void StopStraming(){
        shouldRun=false;
        System.out.println("Udp client Stop");
        if(RTPsocket!=null)
            RTPsocket.close();
    }
    public int getRtpPort(){return this.PORT;}
    public  byte[] addByte(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }
    private void audio(){
        Thread receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
                track.play();
                try {
                    DatagramSocket socket = new DatagramSocket(AUDIOPORT);
                    byte[] buf = new byte[BUF_SIZE];
                    while(shouldRun) {
                        System.out.println("Audio Receving");
                        DatagramPacket packet = new DatagramPacket(buf, BUF_SIZE);
                        socket.receive(packet);
                        Log.i(LOG_TAG, "Packet received: " + packet.getLength());
                        track.write(packet.getData(), 0, BUF_SIZE);
                    }
                    socket.disconnect();
                    socket.close();
                    track.stop();
                    track.flush();
                    track.release();
                    return;
                }
                catch(SocketException e) {
                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                }
                catch(IOException e) {
                    Log.e(LOG_TAG, "IOException: " + e.toString());
                }
            }
        });
        receiveThread.start();
    }

}
