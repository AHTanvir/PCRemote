package anwar.pcremote.filleShare;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import anwar.pcremote.Adapter.DeviceListAdapter;
import anwar.pcremote.Animation.CircleProgress;
import anwar.pcremote.MainiActivity;
import anwar.pcremote.Model.DeviceNameModel;
import anwar.pcremote.R;
import anwar.pcremote.Streming.Constants;

import static anwar.pcremote.R.id.Relative_layoutfor_fragments;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeviceListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceListFragment extends Fragment implements AdapterView.OnItemClickListener,SearchCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private SearchThread searchThread;
    private static final String TAG_SEND_FRAGMENT ="SendFragment";
    private List<SearchThread> threadsList=new ArrayList<>();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Handler handler;
    private CircleProgress progress;
    private DeviceListAdapter adapter;
    private ListView listView;
    private int ThreadCounter=0;
    private Button btnAgain;
    private OnFragmentInteractionListener mListener;

    public DeviceListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeviceListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DeviceListFragment newInstance(String param1, String param2) {
        DeviceListFragment fragment = new DeviceListFragment();
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
        View view= inflater.inflate(R.layout.fragment_device_list, container, false);
        progress=(CircleProgress)view.findViewById(R.id.progress);
        listView=(ListView)view.findViewById(R.id.device_list_view);
        btnAgain=(Button)view.findViewById(R.id.btn_again);
        progress.startAnim();
        handler=new Handler();
        adapter=new DeviceListAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        getAllConnectedDevice(DeviceListFragment.this);
        btnAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAgain.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                progress.startAnim();
                threadsList.clear();
                getAllConnectedDevice(DeviceListFragment.this);
            }
        });
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
    /*    if (context instanceof OnFragmentInteractionListener) {
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceNameModel name=(DeviceNameModel) adapter.getItem(position);
        Constants.SERVER_IP=name.getIp();
        Constants.LAST_DEVICE=name.getName();
        SendFragment s=new SendFragment();
        FragmentManager fm=getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(Relative_layoutfor_fragments,s,TAG_SEND_FRAGMENT).commit();
        for (int i = 0; i <255 ; i++) {
            if (threadsList.get(i).isAlive())
                threadsList.get(i).interrupt();
        }
        threadsList.clear();
    }

    @Override
    public void addDevice(final String name, final String ip) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                System.out.println("Listening "+ip);
                adapter.addDevice(new DeviceNameModel(name,ip));
            }
        });
    }

    @Override
    public void finish(int position) {
        ThreadCounter++;
        //threadsList.remove(position);
        System.out.println("thread remove "+position);
        if(ThreadCounter==(threadsList.size()-1)){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progress.stopAnim();
                    progress.setVisibility(View.GONE);
                    btnAgain.setVisibility(View.VISIBLE);
                    System.out.println(threadsList.size()+"thread list size "+ThreadCounter);
                }
            });
            for (int i = 0; i <threadsList.size() ; i++) {
                if(threadsList.get(i).isAlive()){
                    threadsList.get(i).interrupt();
                    System.out.println(threadsList.size()+"thread alive position "+i);
                }
            }
        }
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
    public void getAllConnectedDevice(SearchCallback listener){
        threadsList.clear();
        if(Constants.LAST_DEVICE!=null)
            addDevice(Constants.LAST_DEVICE,Constants.SERVER_IP);
        try {
            ConnectivityManager cm = (ConnectivityManager)getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            WifiManager wm = (WifiManager)getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo connectionInfo = wm.getConnectionInfo();
            int ipAddress = connectionInfo.getIpAddress();
            //String ipString = Formatter.formatIpAddress(ipAddress);
            String ipString="192.168.0.103";
            String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
            for (int i = 0; i <255; i++) {
                String testIp = prefix + String.valueOf(i);
                InetAddress address = InetAddress.getByName(testIp.replace("/",""));
                searchThread =new SearchThread(this,address,threadsList.size());
                searchThread.start();
                threadsList.add(searchThread);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
