package anwar.pcremote.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anwar on 9/15/2017.
 */

public class ListModel {
    List<RowItem> sendlist=new ArrayList<>();
    List<RowItem> recivelist=new ArrayList<>();
    private static ListModel mInstance;;

    public ListModel(List<RowItem> sendlist, List<RowItem> recivelist) {
        this.sendlist = sendlist;
        this.recivelist = recivelist;
    }
    public ListModel() {
        sendlist=new ArrayList<>();
        recivelist=new ArrayList<>();
    }
    public static ListModel getmInstance(){
        if(mInstance==null)
        {
            mInstance=new ListModel();
        }
        return mInstance;
    }

    public List<RowItem> getSendlist() {
        return sendlist;
    }

    public void setSendlist(List<RowItem> sendlist) {
        this.sendlist = sendlist;
    }

    public List<RowItem> getRecivelist() {
        return recivelist;
    }

    public void setRecivelist(List<RowItem> recivelist) {
        this.recivelist = recivelist;
    }
    public void addSendItem(RowItem r){
        this.sendlist.add(r);
    }
    public void addReceiveItem(RowItem r){
        this.recivelist.add(r);
    }
}
