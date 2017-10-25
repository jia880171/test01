package com.example.unick.sensordemo.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.example.unick.sensordemo.R;
import com.example.unick.sensordemo.UploadService;

import static com.example.unick.sensordemo.R.id.button_lift;
import static com.example.unick.sensordemo.R.id.button_start;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ServiceContralFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ServiceContralFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServiceContralFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //bind to uploadService
    UploadService mUploadService;
    boolean mBound = false;

    private Button button_stop;
    private Button button_start;
    private Button button_drive;
    private Button button_lift;
    private Button button_motor;
    private Button button_bus;
    private Button button_train;
    private Button button_taxi;
    private Button button_mrt;
    Intent intent;

    //private OnFragmentInteractionListener mListener;

    public ServiceContralFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ServiceContralFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ServiceContralFragment newInstance(String param1, String param2) {
        ServiceContralFragment fragment = new ServiceContralFragment();
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

        intent = new Intent(getActivity(), UploadService.class);
    }


    @Override
    public void onResume() {
        super.onResume();
        final Intent intent = new Intent(getActivity(), UploadService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d("in service control", "onResume");
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("in service control","click start");
                getActivity().startService(intent);
            }
        });
        button_drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    mUploadService.setTransportType("drive");
                }
            }
        });
        button_lift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    mUploadService.setTransportType("lift");
                }
            }
        });
        button_motor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    mUploadService.setTransportType("motor");
                }
            }
        });
        button_bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    mUploadService.setTransportType("bus");
                }
            }
        });
        button_train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    mUploadService.setTransportType("train");
                }
            }
        });
        button_taxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    mUploadService.setTransportType("taxi");
                }
            }
        });
        button_mrt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    mUploadService.setTransportType("mrt");
                }
            }
        });
        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    getActivity().unbindService(mConnection);
                    mBound = false;
                }
                getActivity().stopService(intent);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            UploadService.LocalBinder binder = (UploadService.LocalBinder) service;
            mUploadService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View myInflatedView = inflater.inflate(R.layout.fragment_service_contral, container,false);

        button_lift = (Button) myInflatedView.findViewById(R.id.button_lift);
        button_drive = (Button) myInflatedView.findViewById(R.id.button_drive);
        button_motor = (Button) myInflatedView.findViewById(R.id.button_motor);
        button_bus = (Button) myInflatedView.findViewById(R.id.button_bus);
        button_train = (Button) myInflatedView.findViewById(R.id.button_train);
        button_taxi = (Button) myInflatedView.findViewById(R.id.button_taxi);
        button_mrt = (Button) myInflatedView.findViewById(R.id.button_mrt);
        button_start = (Button) myInflatedView.findViewById(R.id.button_start);
        button_stop = (Button) myInflatedView.findViewById(R.id.button_stop);

        return myInflatedView;
    }


    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
