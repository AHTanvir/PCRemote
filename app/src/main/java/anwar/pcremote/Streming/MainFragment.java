package anwar.pcremote.Streming;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ListPopupWindow;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.net.Socket;

import anwar.pcremote.R;
import anwar.pcremote.MainiActivity;
import uk.co.senab.photoview.PhotoViewAttacher;

import static java.lang.System.out;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements FloatingActionButton.OnClickListener,
        ImageView.OnTouchListener,GestureDetector.OnGestureListener,View.OnKeyListener{
    // TODO: Rename parameter arguments, choose names that match
    private boolean isConnected = false;
    private boolean mouseMoved = false;;
    private Socket socket;
    private boolean isShowing=false;
    private GestureDetectorCompat gesterDector;
    private AlertDialog.Builder builder;
    private PhotoViewAttacher pAttacher;
    private MainiActivity mainiActivity;
    private View TouchView;
    private float disx = 0;
    private float disy = 0;
    private  Receiver receiver;
    private PopupWindow popupWindow;
    private static final int udpPort=12123;
    private Handler handler=new Handler();;
    private byte[] buff;
    private UdpClient udpClient;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private LinearLayout menuLayout;
    private ImageView imageView;
    private FloatingActionButton fabHide,fabShow,fabEnter,fabNext,fabPlay,fabPause,fabBackward,fabForward,fabPrev;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
   private Handler msghandeler=new Handler() {
        public void handleMessage(Message msg) {
            buff= (byte[]) msg.obj;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap new_img=BitmapFactory.decodeByteArray(buff, 0, buff.length);
                            imageView.setImageBitmap(new_img);
                        }
                    });
                }
            }).start();
        }
    };
    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
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
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(this);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_main, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        gesterDector=new GestureDetectorCompat(getActivity(),this);
        mainiActivity=((MainiActivity)getActivity());
        fabBtnView(view);
        udpClient=new UdpClient(getActivity().getApplicationContext());
        return view;
    }

    private void fabBtnView(View view) {
        menuLayout= (LinearLayout) view.findViewById(R.id.menuLayout);
        menuLayout.setVisibility(View.GONE);
        imageView= (ImageView) view.findViewById(R.id.imageView);
        imageView.setOnTouchListener(this);
        fabShow=(FloatingActionButton) view.findViewById(R.id.fabShow);
        fabHide=(FloatingActionButton) view.findViewById(R.id.fabHide);
        fabPlay=(FloatingActionButton) view.findViewById(R.id.fabPlay);
        fabPause=(FloatingActionButton) view.findViewById(R.id.fabPause);
        fabEnter=(FloatingActionButton) view.findViewById(R.id.fabEnter);
        fabForward=(FloatingActionButton) view.findViewById(R.id.fabForward);
        fabBackward=(FloatingActionButton) view.findViewById(R.id.fabBackward);
        fabNext=(FloatingActionButton) view.findViewById(R.id.fabNext);
        fabPrev=(FloatingActionButton) view.findViewById(R.id.fabPrev);

        fabPrev.setOnClickListener(this);
        fabShow.setOnClickListener(this);
        fabHide.setOnClickListener(this);
        fabPlay.setOnClickListener(this);
        fabPause.setOnClickListener(this);
        fabEnter.setOnClickListener(this);
        fabForward.setOnClickListener(this);
        fabBackward.setOnClickListener(this);
        fabNext.setOnClickListener(this);
        fabPrev.setOnClickListener(this);
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
            switch (v.getId()) {
                case R.id.fabShow:
                    menuLayout.setVisibility(View.VISIBLE);
                    fabShow.setVisibility(View.GONE);
                    break;
                case R.id.fabHide:
                    menuLayout.setVisibility(View.GONE);
                    fabShow.setVisibility(View.VISIBLE);
                    break;
                case R.id.fabPlay:
                    print("KEY_EVENT ;"+Constants.PLAY);
                    break;
                case R.id.fabPause:
                    print("KEY_EVENT ;"+Constants.PAUSE);
                    break;
                case R.id.fabEnter:
                    print("KEY_EVENT ;"+Constants.ENTER);
                    break;
                case R.id.fabForward:
                    print("KEY_EVENT ;"+Constants.FORWARD);
                    break;
                case R.id.fabBackward:
                    print("KEY_EVENT ;"+Constants.BACKWORD);
                    break;
                case R.id.fabNext:
                    print("KEY_EVENT ;"+Constants.NEXT);
                    break;
                case R.id.fabPrev:
                    print("KEY_EVENT ;"+Constants.PREVIOUS);
                    break;
            }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        TouchView=v;
         gesterDector.onTouchEvent(event);
        return true;
    }
    private void print(String t){
        if(mainiActivity.isConnectedToPc())
        {
            mainiActivity.printToServer(t);
        }
        else if(!isShowing){
            isConnected=false;
            isShowing=true;
            udpClient.StopStraming();
            showDialog();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //if(mainiActivity.isConnectedToPc())
          //  mainiActivity.ConnectToServer("");
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isConnected) {
            print("exit");
            udpClient.StopStraming();
        }

    }
    private void showDialog(){
         builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("IP ADDRESS");
        builder.setMessage("ENTER PC IP");
        final EditText input = new EditText(getActivity());
        input.setText(Constants.SERVER_IP);
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);
        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Constants.SERVER_IP= input.getText().toString();
                mainiActivity.ConnectToServer(Constants.SERVER_IP);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mainiActivity.isConnectedToPc()){
                           new Thread(new Runnable() {
                               @Override
                               public void run() {
                                   udpClient.StartStreming(udpPort,msghandeler);
                                   print("udpPort;"+udpPort);
                                   isConnected=true;
                                   isShowing=false;
                                   System.out.println(" print udp client start");
                               }
                           }).start();
                        }else {
                            String message = "Connect failed." + " \n \n" + "Please try again!";
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Error");
                            builder.setMessage(message);
                            builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    showDialog();
                                }
                            });
                            builder.create().show();
                        }
                    }
                }, 2000);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowing=false;
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        print("Mouse_Event;"+e.getX()+";"+e.getY()+";"+Constants.MOUSE_LEFT_CLICK);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        disx = (e2.getX() - e1.getX())/3;
        disy = (e2.getY() - e1.getY())/3;
        print(disx + "," + disy);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        final int x=(int)e.getX();
        final int y=(int)e.getY();
        final String []arr={"Left Click","Right Click","Download File"};
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getActivity(),R.layout.list_item,arr);
        popupWindow = new PopupWindow(getActivity());
        ListView poplist=new ListView(getActivity());
        poplist.setBackgroundColor(Color.WHITE);
        poplist.setAdapter(arrayAdapter);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(160);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(poplist);
        popupWindow.showAtLocation(TouchView,Gravity.NO_GRAVITY,x,y);
        poplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (arr[position].equals("Download File")){
                    print("Download;"+x+";"+y);
                }
                else if (arr[position].equals("Right Click")){
                    print("Mouse_Event;"+x+";"+y+";"+Constants.MOUSE_RIGHT_CLICK);
                }
                else if (arr[position].equals("Left " +
                        "" +
                        "Click")){
                    print("Mouse_Event;"+x+";"+y+";"+Constants.MOUSE_LEFT_CLICK);
                }
                System.out.println("popup item "+ arr[position]);
                popupWindow.dismiss();
            }
        });
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                print("KEY_EVENT ;"+Constants.VOL_DOWN);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                print("KEY_EVENT ;"+Constants.VOL_UP);
                return true;
            }
        }
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
}
