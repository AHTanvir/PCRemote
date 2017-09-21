package anwar.pcremote.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import anwar.pcremote.Model.DeviceNameModel;
import anwar.pcremote.R;

/**
 * Created by anwar on 9/14/2017.
 */

public class DeviceListAdapter extends BaseAdapter  {
    List<DeviceNameModel> list=new ArrayList<>();

    public DeviceListAdapter(List<DeviceNameModel> list) {
        this.list = list;
    }

    public DeviceListAdapter() {
    }

    public int getCount(){
        return list.size();
    }
    public Object getItem(int position){
        return  list.get(position);
    }
    public long getItemId(int position){
        return list.indexOf(getItem(position));
    }

    private class ViewHolder{
        ProgressBar progressBar;
        TextView name;
        TextView status;
        TextView percentage;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder=null;
        LayoutInflater mInflater=(LayoutInflater)parent.getContext().
                getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if(convertView==null)
        {
            holder=new ViewHolder();
            convertView=mInflater.inflate(R.layout.device_list_item,null);
           // convertView.setBackgroundColor(Color.parseColor("#4dFFFFFF"));
            holder.name=(TextView)convertView.findViewById(R.id.list_device_name);
            convertView.setTag(holder);
        }
        else{
            holder=(ViewHolder)convertView.getTag();
        }
        DeviceNameModel row_pos=list.get(position);
        holder.name.setText(row_pos.getName());
        holder.name.setTextColor(Color.WHITE);
        return convertView;
    }


    public void updateAdapter(List<DeviceNameModel> device ) {
        //and call notifyDataSetChanged
        this.list=device;
        notifyDataSetChanged();
    }
    public void addDevice(DeviceNameModel device){
        this.list.add(device);
        notifyDataSetChanged();
    }
    public void removeItem(int position){
        this.list.remove(position);
        notifyDataSetChanged();
    }
    public void clearList(){
        this.list.clear();
        notifyDataSetChanged();
    }
}
