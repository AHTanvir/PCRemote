/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcremote.comunicaton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.handler.Handler;
import sun.rmi.runtime.Log;
import pcremote.comunicaton.Client;
/**
 *
 * @author anwar
 */
public class Server {
    private int listeningPort=0;
    private ReceiveConnection receiveThread;
    private Socket client=null;
    private HashMap<InetAddress, Socket> sockets = new HashMap<InetAddress, Socket>();
    private ServerSocket serverSocket = null;
    private boolean listening;
    private Client cc;
    private UdpServer udpServer;
    private InetAddress ClientIPAddr;
    CommandExecutor  com;
    private class ReceiveConnection extends Thread {
        Socket clientSocket = null;
         public ReceiveConnection(){}
        public ReceiveConnection(Socket socket){
            this.clientSocket = socket;
            ClientIPAddr=socket.getInetAddress();
           Server.this.sockets.put(socket.getInetAddress(), socket);
        }
        
        @Override
        public void run() {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader( clientSocket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null){
                    if (inputLine.equals("exit") == false && st() ){
                        Server.this.com.execute(inputLine,ClientIPAddr);
                    }
                    else{
                        Server.this.com.execute("exit",ClientIPAddr);
                        clientSocket.shutdownInput();
                        clientSocket.shutdownOutput();
                        clientSocket.close();
                        Server.this.sockets.remove(clientSocket.getInetAddress());
                    }
                }  
            }catch (NullPointerException nu){}
            catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

   public Server(UdpServer udpServer){
       this.udpServer=udpServer;
   }
    public int startListening(int portNo){
        listening = true;
        try {
            serverSocket=new ServerSocket(portNo);
            this.listeningPort = portNo;
        } catch (IOException e) {
            this.listeningPort = 0;
            return 0;
        }

        while (listening) {
            System.out.println("Server Listenning ................................");
            try {
                  com=new CommandExecutor(udpServer);
                receiveThread=new ReceiveConnection(serverSocket.accept());
                receiveThread.start();
            } catch (IOException e) {return 1;}
        }
        try {
            serverSocket.close();
        } catch (IOException e) { return 0;}


        return 1;
    }
public boolean  st(){return this.listening ;}

    public void stopListening(){
         if(receiveThread !=null && receiveThread.isAlive())
            receiveThread.stop();
        System.out.println("Server Stop Listenning");
        this.listening = false;
        
    }

    public void exit(){
        for (Iterator<Socket> iterator = sockets.values().iterator(); iterator.hasNext();)
        {
            Socket socket = (Socket) iterator.next();
            try {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            } catch (IOException e){e.printStackTrace();}
        }

        sockets.clear();
        this.stopListening();
    }


    public int getListeningPort() {

        return this.listeningPort;
    }
    public InetAddress getClientIP(){ 
        return ClientIPAddr;
    }

}
