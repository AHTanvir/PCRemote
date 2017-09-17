package anwar.pcremote;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import anwar.pcremote.filleShare.DeviceListFragment;
import anwar.pcremote.filleShare.ReceiveFragment;
import anwar.pcremote.Streming.MainFragment;
import anwar.pcremote.filleShare.SendFragment;

import static anwar.pcremote.R.id.Relative_layoutfor_fragments;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OptionFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG_SEND_FRAGMENT ="SendFragment";
    private static final int PERMISSION_REQUEST_WRITE = 1;
    private Button browse_pc;
    private Button Receive_file;
    private Button Send_file;
    private Button downloads;
    private FragmentManager fragmentManager;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public OptionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OptionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OptionFragment newInstance(String param1, String param2) {
        OptionFragment fragment = new OptionFragment();
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
        View v= inflater.inflate(R.layout.fragment_option, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        browse_pc=(Button)v.findViewById(R.id.Browse_Pc);
        Receive_file=(Button)v.findViewById(R.id.receivee_file);
        Send_file=(Button)v.findViewById(R.id.Send_file);
        browse_pc.setOnClickListener(this);
        Receive_file.setOnClickListener(this);
        Send_file.setOnClickListener(this);
        return v;
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
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.Browse_Pc:
                    //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    MainFragment mainFragmentFragment=new MainFragment();
                     fragmentManager =getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(Relative_layoutfor_fragments, mainFragmentFragment,
                            mainFragmentFragment.getTag()).addToBackStack(null).commit();
                break;
            case R.id.receivee_file:
                ReceiveFragment rf=new ReceiveFragment();
                FragmentManager fragmentManager =getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(Relative_layoutfor_fragments, rf,
                        rf.getTag()).addToBackStack(null).commit();
                break;
            case R.id.Send_file:
                SendFragment sf=new SendFragment();
                 fragmentManager =getActivity().getSupportFragmentManager();
                DeviceListFragment listFragment = new DeviceListFragment();
                fragmentManager.beginTransaction().replace(Relative_layoutfor_fragments, listFragment, listFragment.getTag()).addToBackStack(null).commit();
/*                fragmentManager.beginTransaction().replace(Relative_layoutfor_fragments, sf,
                        TAG_SEND_FRAGMENT).addToBackStack(null).commit();*/
                break;
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


    }
