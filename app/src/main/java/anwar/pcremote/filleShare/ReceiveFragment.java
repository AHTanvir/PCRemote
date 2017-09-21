package anwar.pcremote.filleShare;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.ListMenuItemView;
import android.support.v7.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ResponseCache;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import anwar.pcremote.Adapter.CustomAdapter;
import anwar.pcremote.Manager.SharedPref;
import anwar.pcremote.Model.ListModel;
import anwar.pcremote.Model.RowItem;
import anwar.pcremote.R;
import anwar.pcremote.Service.Database;
import anwar.pcremote.Service.ReceiveService;
import anwar.pcremote.MainiActivity;
import anwar.pcremote.Streming.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReceiveFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReceiveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiveFragment extends Fragment implements View.OnClickListener,AbsListView.OnScrollListener,AbsListView.OnItemLongClickListener {
    public CustomAdapter adapter;
    public static int mLastFirstVisibleItem;
    private List<RowItem> down_List;
    private MainiActivity activity;
    private ListView receive_listView;
    private TextView ip_txtView;
    private SharedPref sharedPref;
    private Button receive_btn;
    private ListPopupWindow popupWindow;
    private  Timer timer;
    private ResponseThread resThread;
    private  TimerTask timerTask;
    private LinearLayout receive_layout;
    private ServerSocket serverSocket;
    //private ListView <RowItem> list;
    private static final int PERMISSION_REQUEST_WRITE = 1;
    private Database db;
    private Handler handler=new Handler();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ReceiveFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReceiveFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReceiveFragment newInstance(String param1, String param2) {
        ReceiveFragment fragment = new ReceiveFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_receive, container, false);
        receive_layout= (LinearLayout) view.findViewById(R.id.receive_layout);
        receive_listView= (ListView) view.findViewById(R.id.receive_listView);
        receive_btn= (Button) view.findViewById(R.id.start_service_btn);
        activity=((MainiActivity)getActivity());
        receive_btn.setOnClickListener(this);
        adapter= new CustomAdapter(getActivity(), ListModel.getmInstance().getRecivelist());
        receive_listView.setAdapter(adapter);
        receive_listView.setOnScrollListener(this);
        receive_listView.setOnItemLongClickListener(this);
        resThread=new ResponseThread();
        resThread.start();
        timer=new Timer();
        timerTask=new MyTimerTask();
        timer.scheduleAtFixedRate(timerTask,100,500);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkWritePermission();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
/*        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.start_service_btn:
                receive_btn.setVisibility(View.GONE);
                if(resThread.isAlive())
                    resThread.interrupt();
  /*                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkWritePermission();
                    }*/
                break;
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view.getId() == receive_listView.getId()) {
            final int currentFirstVisibleItem = receive_listView.getFirstVisiblePosition();

            if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                receive_layout.setVisibility(View.GONE);
            } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                receive_layout.setVisibility(View.VISIBLE);
            }
            mLastFirstVisibleItem = currentFirstVisibleItem;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopEverything();
        System.out.println("OnStop called");
    }
    public void stopEverything(){
        if(timer !=null)
            timer.cancel();
        if(timerTask!=null)
            timerTask.cancel();
        if(!serverSocket.isClosed())
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if(resThread.isAlive())
            resThread.interrupt();

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
                    adapter.removeItem(position);
                    Toast.makeText(getActivity(),"Item Clear",Toast.LENGTH_SHORT).show();
                }
                else if(arr[position]=="Clear All") {
                    adapter.clearList();
                    Toast.makeText(getActivity(),"Item Clear",Toast.LENGTH_SHORT).show();
                }
            }
        });
        popupWindow.show();
        return false;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    private  TimerTask timerTask12=new TimerTask() {
        @Override
        public void run() {
         handler.post(new Runnable() {
             @Override
             public void run() {
                 adapter.updateAdapter(ListModel.getmInstance().getRecivelist());
             }
         });
        }
    };
    private void checkWritePermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            getActivity().startService(new Intent(getActivity(), ReceiveService.class ));
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getActivity(),"PERMISSION DENIED",Toast.LENGTH_SHORT).show();
                    ((MainiActivity)getActivity()).onBackPressed();
                }
                break;
        }
    }

    public String getWifiApIpAddress() {
        WifiManager wifiManager=(WifiManager)getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(((MainiActivity)getActivity()).isConnectedViaWifi()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return String.valueOf(wifiInfo.getIpAddress());
        }
        else if(((MainiActivity)getActivity()).isHotspotEnable()){
            return "192.168.43.1";
        }
        return null;
    }
    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    adapter.updateAdapter(ListModel.getmInstance().getRecivelist());
                }
            });
        }
    }
    private class ResponseThread extends Thread{
       private SharedPref sharedPref=new SharedPref(getActivity());
        public ResponseThread() {
        }
        @Override
        public void run() {
            try {
                System.out.println("SEARCH PORT LISTENNING");
                 serverSocket=new ServerSocket(Constants.SEARCH_PORT);
                Socket socket=serverSocket.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = br.readLine();
                if(str.equals("query")){
                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os, true);
                    pw.println(sharedPref.getUserName());
                    pw.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!activity.isMyServiceRunning())
                                getActivity().startService(new Intent(getActivity(), ReceiveService.class ));
                        }
                    });
                }else str=null;
                br.close();
                serverSocket.close();
                System.out.println("Just said hello to:" + str);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }catch (IOException e){}
        }
    }
}
