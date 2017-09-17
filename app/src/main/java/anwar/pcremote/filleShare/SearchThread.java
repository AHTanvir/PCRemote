package anwar.pcremote.filleShare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import anwar.pcremote.Streming.Constants;

/**
 * Created by anwar on 9/15/2017.
 */

public class SearchThread extends Thread {
    private SearchCallback listener;
    private InetAddress address;
    private String name=null;
    private Socket socket=null;
    private PrintWriter out=null;
    private int position;

    public SearchThread(SearchCallback listener, InetAddress address, int position) {
        this.listener = listener;
        this.address = address;
        this.position = position;
    }

    @Override
    public void run() {
        BufferedReader br;
        try {
            System.out.println("thread Checking ip "+address);
            socket = new Socket(address, Constants.SEARCH_PORT);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                    .getOutputStream())), true);
            out.println("query");
            br= new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if((name = br.readLine())!=null) {
                listener.addDevice(name,address.getHostName());
            }
            socket.close();
            br.close();
            out.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("query exception "+e);
        }
        listener.finish(position);
    }
}
