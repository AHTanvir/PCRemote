package anwar.pcremote.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import anwar.pcremote.Model.DeviceNameModel;
import anwar.pcremote.Model.RowItem;
import anwar.pcremote.R;


/**
 * Created by anwar on 7/6/2017.
 */

public class CustomAdapter extends BaseAdapter{
    Context context;
    List<RowItem> rowItems;
    private LayoutInflater inflater;
    public CustomAdapter(Context context,List<RowItem>rowItems){
        this.context = context;
        this.rowItems = rowItems;
    }

    public CustomAdapter(Context context) {this.context = context;}

    public int getCount(){
        return rowItems.size();
    }
    public Object getItem(int position){
        return  rowItems.get(position);
    }
    public long getItemId(int position){
        return rowItems.indexOf(getItem(position));
    }

    private class ViewHolder{
        ProgressBar progressBar;
        TextView name;
        TextView status;
        TextView percentage;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder=null;
        LayoutInflater mInflater=(LayoutInflater)context.
                getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
       if(convertView==null)
       {
           holder=new ViewHolder();
           convertView=mInflater.inflate(R.layout.status_layout,null);
           holder.progressBar=(ProgressBar) convertView.findViewById(R.id.progress_bar);
           holder.percentage=(TextView)convertView.findViewById(R.id.percentage);
           holder.name=(TextView)convertView.findViewById(R.id.file_name);
           holder.status=(TextView)convertView.findViewById(R.id.file_status);
           convertView.setTag(holder);
       }
       else{
           holder=(ViewHolder)convertView.getTag();
       }
        RowItem row_pos=rowItems.get(position);
        holder.progressBar.setProgress(row_pos.getProgress());
        holder.percentage.setText(row_pos.getProgress()+"%");
        holder.name.setText(row_pos.getName());
        holder.status.setText(row_pos.getStatus());
        return convertView;
    }


    public void updateAdapter(List<RowItem> updateList ) {
        rowItems=updateList;
        notifyDataSetChanged();
    }
    public void removeItem(int position){
        rowItems.remove(position);
        notifyDataSetChanged();
    }
    public void clearList(){
        rowItems.clear();
        notifyDataSetChanged();
    }
}
