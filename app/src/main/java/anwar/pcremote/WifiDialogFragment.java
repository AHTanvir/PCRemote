package anwar.pcremote;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WifiDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    private ListView networkList;
    private String ssid;
    private String key;
    private ArrayList<String> wifis;
    private  AlertDialog alertDialog;
    private WifiManager mWifiManager ;
    private List<ScanResult> mScanResults;
    private ArrayAdapter<String> ArrayAdapter;
    private WifiScanReceiver mWifiScanReceiver;
    private Handler handler = new Handler();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public WifiDialogFragment() {
        // Required empty public constructor
    }
    public interface DialogListener {

        void onFinishDialog(String ssid);

    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WifiDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WifiDialogFragment newInstance(String param1, String param2) {
        WifiDialogFragment fragment = new WifiDialogFragment();
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
        View v= inflater.inflate(R.layout.wifi_list, container, false);
        mWifiScanReceiver=new WifiScanReceiver();
        wifis = new ArrayList<String>();
        wifis.add("loading...");
        mWifiManager = (WifiManager)getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiManager.startScan();
        networkList= (ListView) v.findViewById(R.id.network_list);
        ArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.device_name,wifis);
        networkList.setAdapter(ArrayAdapter);
        networkList.setOnItemClickListener(this);
        return v;
    }
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // creating the fullscreen dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }
    public void showPasswordDialog(final String sid) {
        final String message = "The password you have entered is incorrect." + " \n \n" + "Please try again!";
        LayoutInflater li = LayoutInflater.from(getActivity());
        final View promptsView = li.inflate(R.layout.promot, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptsView);
        final TextView tv=(TextView)promptsView.findViewById(R.id.textView1);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        final Button button=(Button)promptsView.findViewById(R.id.connect_wifi);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setClickable(false);
                key = (userInput.getText()).toString();
                ((MainiActivity)getActivity()).connect(sid,userInput.getText().toString());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (((MainiActivity)getActivity()).isConnectedViaWifi()) {
                            alertDialog.dismiss();
                            DialogListener listener=(DialogListener)getActivity();
                            listener.onFinishDialog(sid);
                            dismiss();
                        }else {
                            tv.setText(message);
                            button.setClickable(true);
                        }
                    }
                }, 1500);
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mWifiScanReceiver);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ssid= ((TextView)view).getText().toString();
        showPasswordDialog(ssid);
    }
    class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                mScanResults = mWifiManager.getScanResults();
                wifis.clear();
                for (int i = 0; i < mScanResults.size(); i++) {
                    wifis.add(mScanResults.get(i).SSID.toString() );
                }
                ArrayAdapter.notifyDataSetChanged();

            }

        }
    }
   /* public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString("title");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle(title);

        alertDialogBuilder.setMessage("Are you sure?");

        alertDialogBuilder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int which) {

                // on success

            }

        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int which) {

                if (dialog != null && dialog.isShowing()) {

                    dialog.dismiss();

                }

            }
        });



        return alertDialogBuilder.create();

    }*/

}
