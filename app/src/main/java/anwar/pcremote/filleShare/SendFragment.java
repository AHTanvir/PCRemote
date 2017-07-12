package anwar.pcremote.filleShare;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import anwar.pcremote.ListView.CustomAdapter;
import anwar.pcremote.ListView.RowItem;
import anwar.pcremote.R;
import anwar.pcremote.Service.Database;
import anwar.pcremote.Service.SendIntentService;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class SendFragment extends Fragment implements View.OnClickListener,ListView.OnItemLongClickListener {
    private FloatingActionButton fabsend;
    private ListView send_listView;
    private List<RowItem> send_list=new ArrayList<>();
    private CustomAdapter adapter;
    private Database db;
    private String ip=null;
    private static  final int port=5555;
    private ListPopupWindow popupWindow;
    private Timer timer;
    private Handler handler;
    private static final int PERMISSION_REQUEST_READ = 2;
    private SendFragment sendFragment;
    public SendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_send, container, false);
        db=new Database(getActivity());
        fabsend= (FloatingActionButton) view.findViewById(R.id.fabSend);
        send_listView= (ListView) view.findViewById(R.id.send_listView);
        send_list=db.getSendeList();
        adapter= new CustomAdapter(getActivity(),send_list);
        send_listView.setAdapter(adapter);
        send_listView.setOnItemLongClickListener(this);
        fabsend.setOnClickListener(this);
        timer=new Timer();
        handler=new Handler();
        timer.scheduleAtFixedRate(timerTask,200,500);
        showDialog();
        return view;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.fabSend:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    checkReadPermission();
                }
                else{
                    new MaterialFilePicker()
                            .withActivity(this.getActivity())
                            .withRequestCode(11)
                            .withFilter(Pattern.compile(".*\\.*$"))
                            .withFilterDirectories(true)
                            .withHiddenFiles(true)
                            .start();
                }
        }
        if(ip==null)
            showDialog();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final RowItem itm=(RowItem)parent.getItemAtPosition(position);
        final String []arr={"Open","Clear","Clear All"};
        popupWindow = new ListPopupWindow(getActivity());
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getActivity(),R.layout.list_item,arr);
        popupWindow.setAnchorView(view.findViewById(R.id.percentage));
        popupWindow.setAdapter(arrayAdapter);
        popupWindow.setWidth(100);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.white));
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                popupWindow.dismiss();
                if(arr[position]=="Open") {
                    Toast.makeText(getActivity(),"Open",Toast.LENGTH_SHORT).show();
                }
                else if(arr[position]=="Clear") {
                    db.DeleteSendItem(String.valueOf(itm.getId()));
                    Toast.makeText(getActivity(),"Item Clear",Toast.LENGTH_SHORT).show();
                }
                else if(arr[position]=="Clear All") {
                    db.DeleteSendList();
                    Toast.makeText(getActivity(),"Clear all item",Toast.LENGTH_SHORT).show();
                }
            }
        });
        popupWindow.show();
        return false;
    }
    private void checkReadPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            //do tsk
        } else {
            //ask for pemission
        }
    }
    private TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    send_list=db.getSendeList();
                    adapter.updateAdapter(send_list);
                }
            });
        }
    };
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(timer !=null)
            timer.cancel();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    //    super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            Intent intent= new Intent(getActivity(), SendIntentService.class);
            intent.putExtra("SERVER_IP",ip);
            intent.putExtra("SERVER_PORT",port);
            intent.putExtra("PATH",filePath);
            getActivity().startService(intent);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkReadPermission();
                } else {
                    Toast.makeText(getActivity(),"PERMISSION_DENIED",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    public void showDialog()
    {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.promot, null);
        promptsView.setBackgroundColor(Color.parseColor("Lightgray"));
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptsView);
        TextView tv=(TextView)promptsView.findViewById(R.id.textView1);
        tv.setText("Enter Receiver device IP");
        tv.setBackgroundColor(Color.parseColor("Lightgray"));
        final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        userInput.setText("192.168.43.95");
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                         ip= (userInput.getText()).toString();
                    }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
