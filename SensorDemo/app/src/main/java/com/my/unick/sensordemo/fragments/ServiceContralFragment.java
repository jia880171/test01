package com.my.unick.sensordemo.fragments;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.my.unick.sensordemo.MainActivity;
import com.my.unick.sensordemo.R;
import com.my.unick.sensordemo.UploadService;

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
    boolean mBound=false;

    private Button button_stop;
    private Button button_start;
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
        Log.d("in service control","onResume,mBound: "+mBound );
        //要求權限
        int permission = ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            Log.d("lepu in service", "ask permission");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("in service control","click start");
                new AlertDialog.Builder(getActivity())
                        .setTitle("已啟動背景GPS服務")
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().startService(intent);

                                Intent intent = new Intent();
                                intent.setClass(getActivity(), MainActivity.class);
                                startActivity(intent);
                                Log.d("in service Control","startService,mBound:" +mBound);
                            }
                        })
                        .show();
            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("已關閉背景GPS服務")
                        .setPositiveButton("確認", null)
                        .show();
                unBind();
            }
        });
    }

    public void unBind(){
        Log.d("in service Control","button stop,mBound"+mBound);
        if (mBound==true) {
            Log.d("in service Control","mBound==true");
            getActivity().unbindService(mConnection);
            mBound = false;
            Log.d("in service Control","mBound==false");
        }else if(mBound==false){
            Log.d("in service Control","mBound==false: "+mBound);
        }
        getActivity().stopService(intent);
        Log.d("in service Control","stopService,mBound:"+mBound);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("in service control","onPause,mBound: "+mBound);
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
            Log.d("in service control","set mBound: "+mBound);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("in service control","onServiceDisconnected");
            mBound = false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View myInflatedView = inflater.inflate(R.layout.fragment_service_contral, container,false);
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
