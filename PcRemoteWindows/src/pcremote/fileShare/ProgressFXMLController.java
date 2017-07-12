/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcremote.fileShare;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pcremote.Constant;
import pcremote.FXMLDocumentController;
import pcremote.comunicaton.Client;

/**
 * FXML Controller class
 *
 * @author anwar
 */
public class ProgressFXMLController implements Initializable {
    private String FileName;
    private Thread sendThread;
    @FXML
    Label label_name;
    @FXML
     Label label_progress;
    @FXML
    private Button btn_cancel;
    @FXML
  ProgressBar progress_bar=new ProgressBar(0);
     final SimpleDoubleProperty prop=new SimpleDoubleProperty();
     
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /*
                  new Thread(new Runnable() {
                   @Override
                   public void run() {
                       while(FileName==null){}
                     label_name.setText(FileName);
                   }}).start();*/
    } 
      @FXML
     private void button_Cancel(ActionEvent event) {
                this.sendThread.stop();
                Stage stage=(Stage)btn_cancel.getScene().getWindow();
                stage.close();
     }
   

    public void setFileName(String FileName) {
        this.FileName= FileName;
    }

    public void setSendThread(Thread sendThread) {
        this.sendThread = sendThread;
    }
    
}
