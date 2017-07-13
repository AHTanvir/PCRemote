/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcremote;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import pcremote.comunicaton.Client;
import pcremote.comunicaton.Server;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pcremote.comunicaton.UdpServer;
import pcremote.fileShare.ProgressFXMLController;
import pcremote.fileShare.TaskReceive;
import pcremote.fileShare.Tasksend;

/**
 *
 * @author anwar
 */
public class FXMLDocumentController implements Initializable {
   private boolean listening;
   private boolean receiving=false;
   private UdpServer udpServer;
   private Client client;
   private boolean dialog=false;
   private ServerSocket serverSocket;
   private Socket ReceiveSocket;
   private Socket soc;
   private Thread ListenningThread;
   private TaskReceive ReceiveThread;
   private String currentClientIP=null   ;
   private  ProgressFXMLController pController;
   private Server TcpServer=new Server();
   private FileChooser fileChooser = new FileChooser();
    @FXML
    private Label label_ip;
    private Button btn_send_file;
     @FXML
    private Button btn_receive_file;
    @FXML
    private Button btn_start;
    @FXML
    private Button btn_exit;
    @FXML
    private Button btn_dialog;
    @FXML
   private AnchorPane dialogpane;
    @FXML
    private TextField dialog_ip;
    @FXML
    private void buttonStart(ActionEvent event) {
        udpServer=new UdpServer();
          if(btn_start.getText().toString().contains("Start Server")){ 
              btn_start.setText("Stop Server");
              listening=true;
               new Thread(new Runnable() {
                   @Override
                   public void run() {
                       TcpServer.startListening(8998,udpServer);
                   }}
               ).start();
           }
          else if(btn_start.getText().equals("Stop Server")){
               this.listening=false; 
               TcpServer.stopListening();
                 udpServer.stopStrem();
                 btn_start.setText("Start Server");
           }
        
    }
     @FXML
     private void buttonExit(ActionEvent event) throws IOException {
        this.listening=false;
        TcpServer.stopListening();
        System.exit(-1);
     }
      @FXML
     private void buttonSend(ActionEvent event){
         try{
             Constant.currentClientIP=TcpServer.getClientIP().toString();
             Constant.currentClientIP=Constant.currentClientIP.replace("/", "");
             ChooseFile();
         }catch(NullPointerException nu){
             try {
                 Stage instage=new Stage();
                 Parent root1 = FXMLLoader.load(getClass().getResource("ipaddressFXML.fxml"));
                 instage.setScene(new Scene(root1));
                 instage.initModality(Modality.APPLICATION_MODAL);
                 instage.setResizable(false);
                 instage.show();
             } catch (IOException ex) {Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);}
       }      
    }
     
     @FXML
     private void buttonReceive(ActionEvent event) {
            if(btn_receive_file.getText().toString().equals("Receive File")){
                btn_receive_file.setText("Stop Receiveing");
                receiving=true;
                 ListenningThread=  new Thread(new Runnable() {
                   @Override
                   public void run() {
                       try { 
                           serverSocket=new ServerSocket(5555);
                               while(receiving){
                                   System.out.println("Receiving......................");
                                   ReceiveSocket=serverSocket.accept();
                                   Platform.runLater(() ->{ try {
                                   FXMLLoader  loader=new FXMLLoader(getClass().getResource("/pcremote/fileShare/progressFXML.fxml"));
                                   Stage  instage=new Stage();
                                   Parent root1 =loader.load();
                                   pController=loader.<ProgressFXMLController>getController();
                                   ReceiveThread=new TaskReceive(pController,ReceiveSocket);
                                   ReceiveThread.start();
                                   pController.setSendThread(ReceiveThread);
                                   instage.setScene(new Scene(root1));
                                   instage.setTitle("Receving.....");
                                   instage.show();
                               } catch (IOException ex) {Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);}});
                               }
                               System.out.println("Receiving Stop");
                               
                           } catch (IOException ex) {Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);}
                   }});
                 ListenningThread.start();
            }
            else{
                try {
                    this.receiving=false;
                    if(!serverSocket.isClosed())
                        serverSocket.close();
                    if(ListenningThread.isAlive())
                        ListenningThread.stop();
                    if(ReceiveThread !=null && ReceiveThread.isAlive())
                        ReceiveThread.stop();
                    btn_receive_file.setText("Receive File");
                } catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
     }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try{
            label_ip.setText("This Pc IP: "+ getDeviceIP());
        }catch(NullPointerException nu){}
    }    
        @FXML                                                                     
   private void buttondialog(ActionEvent event) throws IOException {
             Constant.currentClientIP=dialog_ip.getText();
             System.out.println(Constant.currentClientIP);
             Stage stage=(Stage)btn_dialog.getScene().getWindow();
             stage.close();
             ChooseFile();
   }
   private void ChooseFile(){
       File selectedFile = fileChooser.showOpenDialog(null);
       if(selectedFile !=null){
           try {
           FXMLLoader loader=new FXMLLoader(getClass().getResource("/pcremote/fileShare/progressFXML.fxml"));
           Stage  instage=new Stage();
           Parent root1 =loader.load();
           ProgressFXMLController con =loader.<ProgressFXMLController>getController();
           Thread sendThread=new Thread(new Runnable() {
                   @Override
                   public void run() {   new Tasksend(con,Constant.currentClientIP,5555,selectedFile.getAbsolutePath()).Send();}});
           con.setSendThread(sendThread);
           sendThread.start();
           instage.setScene(new Scene(root1));
           //instage.initModality(Modality.APPLICATION_MODAL);
           instage.setTitle("Sending");
          // instage.setResizable(false);
           instage.show();
       } catch (IOException ex) {
           Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
       }
       }
    }
      private String getDeviceIP(){
       String ip=null;
        try {
            Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
            while( b.hasMoreElements()){
                for ( InterfaceAddress f : b.nextElement().getInterfaceAddresses())
                    if ( f.getAddress().isSiteLocalAddress())
                        ip= f.getAddress().toString();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
       System.out.println(ip);
   return ip.replace("/", "");
   }
}
