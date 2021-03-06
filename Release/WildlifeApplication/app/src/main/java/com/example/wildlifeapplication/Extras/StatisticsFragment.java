package com.example.wildlifeapplication.Extras;

import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wildlifeapplication.Map.Spotting;
import com.example.wildlifeapplication.Map.SpottingOfAnimalsDatabase;
import com.example.wildlifeapplication.R;
import com.example.wildlifeapplication.Search.AnimalInformation.Animal;
import com.example.wildlifeapplication.Search.AnimalInformation.AnimalDatabase;
import com.example.wildlifeapplication.Search.SearchForAnimalService;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class StatisticsFragment extends Fragment {

    private List<Animal> mAllAnimals;
    private List<Animal> mAllBirds;
    private List<Animal> mAllInvertebrates;
    private List<Spotting> mAllSpottings;

    public static StatisticsFragment newInstance() {
        StatisticsFragment fragment = new StatisticsFragment();
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
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        final TextView total_Sightings_Stored = view.findViewById(R.id.number1);
        final TextView types_of_animal_count = view.findViewById(R.id.number2);
        final TextView types_of_bird_count = view.findViewById(R.id.number3);
        final TextView types_of_invertebrates_count = view.findViewById(R.id.number6);

        final AnimalDatabase db = Room.databaseBuilder(getContext(), AnimalDatabase.class, "animal database").build();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                mAllAnimals = db.animalDao().getAllAnimals();
                mAllBirds = db.animalDao().getAnimalOfType("Bird");
                mAllInvertebrates = db.animalDao().getAnimalOfType("Invertebrate");


                db.close();
            }
        });

        final SpottingOfAnimalsDatabase spottingOfAnimalsDB = Room.databaseBuilder(getContext(), SpottingOfAnimalsDatabase.class, "animal sighting database").build();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mAllSpottings = spottingOfAnimalsDB.spottingAnimalDao().getAllSpottingOfAnimals();
                spottingOfAnimalsDB.close();
            }
        });

        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (java.lang.InterruptedException e) {
            e.printStackTrace();
        }

        types_of_animal_count.setText(Integer.toString(mAllAnimals.size()));
        total_Sightings_Stored.setText(Integer.toString(mAllSpottings.size()));
        types_of_bird_count.setText(Integer.toString(mAllBirds.size()));
        types_of_invertebrates_count.setText(Integer.toString(mAllInvertebrates.size()));
        return view;
    }

}
