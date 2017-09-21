/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcremote;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Secur32;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.stage.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import pcremote.comunicaton.Client;
import pcremote.comunicaton.Server;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.filechooser.FileSystemView;
import pcremote.Model.DeviceModel;
import pcremote.comunicaton.CommandExecutor;
import pcremote.comunicaton.UdpServer;
import pcremote.fileShare.ProgressFXMLController;
import pcremote.fileShare.TaskReceive;
import pcremote.fileShare.Tasksend;

/**
 *
 * @author anwar
 */
public class FXMLDocumentController implements Initializable,CommandExecutor.callBack {
   private boolean listening;
   private boolean receiving=false;
   private UdpServer udpServer=new UdpServer();;
   private Client client;
   private SearchThread searchThread;
   private  FXMLDocumentController Controller;
   private InetAddress address;
   private List<SearchThread> threads=new ArrayList<>();
   private List<DeviceModel> device=new ArrayList<>();
   private boolean dialog=false;
   private ServerSocket serverSocket;
   private  int ServerResult=1;
   private Socket ReceiveSocket;
   private Socket socket=null;
   private Thread ListenningThread;
   private TaskReceive ReceiveThread;
   private String currentClientIP=null   ;
   private  ProgressFXMLController pController;
   private Server TcpServer=new Server(udpServer);
   private FileChooser fileChooser = new FileChooser();
  
    @FXML
    private Label label_ip;
    @FXML
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
    public ListView<String> listView;
   
     @FXML
    private void buttonStart(ActionEvent event) {
         if (btn_start.getText().toString().contains("Start Server")) {
             listening = true;
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     ServerResult = TcpServer.startListening(8998);
                 }
             }
             ).start();
             if (ServerResult == 1) {
                 btn_start.setText("Stop Server");
             }
         } else if (btn_start.getText().equals("Stop Server")) {
             this.listening = false;
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
       try {
            FXMLLoader  loader=new FXMLLoader(getClass().getResource("DeviceListFXML.fxml"));
           Stage instage=new Stage();
           Parent root1=loader.load();
           instage.setScene(new Scene(root1));
           instage.initModality(Modality.APPLICATION_MODAL);
           Controller=loader.<FXMLDocumentController>getController();
           instage.setResizable(false);
           instage.show();
           startSearch();
           if(Constant.LAST_CONNECTED_DEVICE!=null){
               Controller.listView.getItems().add(Constant.LAST_CONNECTED_DEVICE);
               Controller.addItem(new DeviceModel(Constant.LAST_CONNECTED_DEVICE,InetAddress.getByName(Constant.currentClientIP)));
           }
       } catch (IOException ex) {
           Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
       }catch(NullPointerException e){e.printStackTrace();}

                  /*
           try{
           Constant.currentClientIP=TcpServer.getClientIP().toString();
           Constant.currentClientIP=Constant.currentClientIP.replace("/", "");
           File selectedFile = fileChooser.showOpenDialog(null);
           ChooseFile(selectedFile.getAbsolutePath());
           }catch(NullPointerException nu){
           try {
           Stage instage=new Stage();
           Parent root1 = FXMLLoader.load(getClass().getResource("ipaddressFXML.fxml"));
           instage.setScene(new Scene(root1));
           instage.initModality(Modality.APPLICATION_MODAL);
           instage.setResizable(false);
           instage.show();
           } catch (IOException ex) {Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);}
           } */   
    }
     public void startSearch(){
         String ipString=getDeviceIP();
         String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
         for (int i = 0; i <255; i++) {
             try {
                 String testIp = prefix + String.valueOf(i);
                 InetAddress address = InetAddress.getByName(testIp.replace("/",""));
                 searchThread =new SearchThread(Controller,address);
                 searchThread.start();
                 threads.add(searchThread);
             } catch (UnknownHostException ex) {
                 Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
             }
            } 
     }
     public void addItem(DeviceModel d){
         device.add(d);
     }
     @FXML
     private void buttonReceive(ActionEvent event) {
         Thread thread=null;
            if(btn_receive_file.getText().toString().equals("Receive File")){
                 btn_receive_file.setText("Stop Receiveing");
               thread=new Thread(new Runnable(){
                   @Override
                   public void run() {
                       if(sendQuery()!=null){
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
                                                Parent root1 = loader.load();
                                                pController = loader.<ProgressFXMLController>getController();
                                                ReceiveThread = new TaskReceive(pController, ReceiveSocket);
                                                ReceiveThread.start();
                                                pController.setSendThread(ReceiveThread);
                                                instage.setScene(new Scene(root1));
                                                instage.setTitle("Receving.....");
                                                instage.show();
                                                } catch (IOException ex) {
                                                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                                                }
                                            });
                                        }
                                        System.out.println("Receiving Stop");
                               
                                    } catch (IOException ex) {
                                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            });
                            ListenningThread.start();
                        }
                    }
                }); 
               thread.start();
            }else{
                try {
                    this.receiving=false;
                    if( serverSocket!=null &&!serverSocket.isClosed())
                        serverSocket.close();
                    if(ListenningThread!=null &&ListenningThread.isAlive())
                        ListenningThread.stop();
                    if(ReceiveThread !=null && ReceiveThread.isAlive())
                        ReceiveThread.stop();
                    if(thread!=null &&thread.isAlive())
                        thread.stop();
                    btn_receive_file.setText("Receive File");
                } catch(java.net.SocketException soc){
            }catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
     }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String path=FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath()+"/PcRemote/";
        Constant.Home_Dirctory=path.replace("\\", "/");
        createFolder(Constant.Home_Dirctory);
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
             File selectedFile = fileChooser.showOpenDialog(null);
             ChooseFile(selectedFile.getAbsolutePath());
   }
    @FXML
   public void handleMouseClick(MouseEvent arg0) {
       Stage stage=(Stage)listView.getScene().getWindow();
       stage.close();
       Constant.LAST_CONNECTED_DEVICE=listView.getSelectionModel().getSelectedItem();
       Constant.currentClientIP=device.get(listView.getSelectionModel().getSelectedIndex()).getIp().toString().replace("/", "");
       File selectedFile = fileChooser.showOpenDialog(null);
       if(selectedFile!=null){
           ChooseFile(selectedFile.getAbsolutePath());
       }
       System.out.println("clicked on " + listView.getSelectionModel().getSelectedItem());
       threadsFinsh();
   }
   @FXML                                                                     
   private void buttonTryAgain(ActionEvent event) throws IOException {
       threadsFinsh();
       startSearch();
   }
   public void threadsFinsh(){
       for(int i=0;i<threads.size();i++){
           if(threads.get(i).isAlive()){
               threads.get(i).interrupt();
               System.out.println("Thread interrupt "+i);
           }
       }
       threads.clear();
   }
   public  void ChooseFile(String path){
       if(path !=null){
           try {
           FXMLLoader loader=new FXMLLoader(getClass().getResource("/pcremote/fileShare/progressFXML.fxml"));
           Stage  instage=new Stage();
           Parent root1 =loader.load();
           ProgressFXMLController con =loader.<ProgressFXMLController>getController();
           Thread sendThread=new Thread(new Runnable() {
                   @Override
                   public void run() {   new Tasksend(con,Constant.currentClientIP,5555,path).Send();}});
           con.setSendThread(sendThread);
           sendThread.start();
           instage.setScene(new Scene(root1));
           instage.setTitle("Sending");
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
String sendQuery(){
    String str=null;
            try {
                serverSocket=new ServerSocket(Constant.SSEARCH_PORT); 
                socket=serverSocket.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                str = br.readLine();
                if(str.equals("query")){
                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os, true);
                    String username=getCurrentUserName().replace("\\", "@");
                    pw.println(username.split("@")[1].toUpperCase());
                    pw.close();
                     serverSocket.close();
                     socket.close();
                     System.out.println("SERVER LISTENNING");
                }else str=null;
            } catch (IOException ex) {
               ex.printStackTrace();
               return null;
            }
            return str;
        }
public String getCurrentUserName() {
       Secur32 secur32 =(Secur32) Native.loadLibrary("secur32", Secur32.class, W32APIOptions.DEFAULT_OPTIONS);
      char[] userNameBuf = new char[10000];
      IntByReference size = new IntByReference(userNameBuf.length);
      boolean result = secur32.GetUserNameEx(Secur32.EXTENDED_NAME_FORMAT.NameSamCompatible, userNameBuf, size);

      if (!result)
          throw new IllegalStateException("Cannot retreive name of the currently logged-in user");

      return new String(userNameBuf, 0, size.getValue());
  }
     private boolean createFolder(String theFilePath){
         boolean result = false;
         File directory = new File(theFilePath);
         if (directory.exists()) {
             System.out.println("Folder already exists");
         } else {
             result = directory.mkdirs();
         }
         return result;
     }
}
