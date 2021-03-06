package com.example.wildlifeapplication.Store;

import android.Manifest;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wildlifeapplication.Map.MapFragment;
import com.example.wildlifeapplication.Map.Spotting;
import com.example.wildlifeapplication.Map.SpottingOfAnimalsDatabase;
import com.example.wildlifeapplication.R;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class StoreFragment extends Fragment {

    private static final int CAMERA_PIC_REQUEST = 1337;
    private static final int REQUEST_PERM_WRITE_STORAGE = 102;
    private String pictureFilePath;
    private File image = null;
    private Activity activity;
    private MapFragment mapFragment;
    private LatLng location;
    private boolean manual = false;
    private String bodyLength;
    private String wingspan;
    private String colour;
    private String habitat;
    private String toy;

    public StoreFragment() {
    }

    public static StoreFragment newInstance() {
        StoreFragment fragment = new StoreFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final SharedPreferences sp = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        mapFragment = (MapFragment) getFragmentManager().findFragmentByTag("Map");
        activity = getActivity();
        final View view = inflater.inflate(R.layout.fragment_store, container, false);
        Switch switch1 = view.findViewById(R.id.switch1);
        final Button button3 = view.findViewById(R.id.button3);
        final Button button1 = view.findViewById(R.id.button);
        final Button button2 = view.findViewById(R.id.button2);
        final TextView nounDisplay = view.findViewById(R.id.textView15);
        final TextView scientificNounDisplay = view.findViewById(R.id.textView16);
        String noun = getArguments().getString("noun");
        String scientificNoun = getArguments().getString("scientific_noun");

        button3.setVisibility(View.INVISIBLE);
        nounDisplay.setText(noun);
        scientificNounDisplay.setText(scientificNoun);

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    manual = true;
                    button3.setVisibility(View.VISIBLE);
                } else {
                    manual = false;
                    button3.setVisibility(View.INVISIBLE);
                }
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PIC_REQUEST);
                    }
                }
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERM_WRITE_STORAGE);
                } else {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                        String pictureFile = "Wildlife_" + timeStamp;
                        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        image = File.createTempFile(pictureFile, ".jpg", storageDir);
                        pictureFilePath = image.getAbsolutePath();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Photo file could not be created, please try again", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (image != null) {
                        Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.wildlifeapplication", image);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        activity.startActivityForResult(intent, CAMERA_PIC_REQUEST);
                    }
                }
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FragmentTransaction tr = getFragmentManager().beginTransaction();
                tr.replace(R.id.fragment_container, mapFragment, "Map").addToBackStack(null).commit();
                mapFragment.setStoreFragManualLocation(true);
                Toast.makeText(getContext(), "Tap on the Location", Toast.LENGTH_LONG).show();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!manual) {
                    if (sp.getString("OnlineStatus", "Online").equals("Online")) {
                        LatLng pos = mapFragment.storeFragGetLoc(); // crashing here
                        setLatLng(pos);
                    } else {
                        setLatLng(new LatLng(51.481580, -3.179089));
                    }
                }


                final Spotting spotting = new Spotting(nounDisplay.getText().toString(),
                        scientificNounDisplay.getText().toString(),
                        ((float) getLocation().latitude),
                        (float) getLocation().longitude,
                        new Date());
                final SpottingOfAnimalsDatabase spottingOfAnimalsDatabase = Room.databaseBuilder(getContext(), SpottingOfAnimalsDatabase.class, "animal sighting database").build();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        spottingOfAnimalsDatabase.spottingAnimalDao().insertSpotting(spotting);
                        spottingOfAnimalsDatabase.close();
                    }
                });

                Toast.makeText(getContext(), "Thank you for reporting your sighting", Toast.LENGTH_LONG).show();
                FragmentTransaction tr = getFragmentManager().beginTransaction();
                tr.replace(R.id.fragment_container, mapFragment).commit();


            }
        });
        return view;
    }


    public String getPictureFilePath() {
        return this.pictureFilePath;
    }

    public void setLatLng(LatLng latLng) {
        this.location = latLng;
    }

    public LatLng getLocation() {
        return location;
    }

}
