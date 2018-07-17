package com.cse535.assignments.testapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;

enum GraphState {
    START, CONTINUE, STOP
}

public class MainActivity extends AppCompatActivity {

    int horViewLow = 0, horViewCapacity = 10, horViewInterval = 50;
    int verViewLow = 0, verViewCapacity = 4, verViewInterval = 500;
    int horViewHigh = horViewLow + horViewCapacity * horViewInterval;
    int verViewHigh = verViewLow + verViewCapacity * verViewInterval;
    float[] randomValArray;
    int randIndexLeft = 0, randIndexRight = 0;
    int randArrayMaxCapacity = horViewCapacity * horViewInterval * 2;
    int randViewCapacity = horViewCapacity * 10;
    int currPointsInNextHorInterval = 0 , maxPointsPerHorInterval = randArrayMaxCapacity / randViewCapacity;

    GraphView gvGraph;
    private final Handler handler = new Handler();
    Intent intent;
    ArrayList<String> horLabelsArrayList = null, verLabelsArrayList = null;

    private void resetValues() {
        randIndexLeft = randIndexRight = currPointsInNextHorInterval = 0;
    }

    private String[] getHorLabelsAsArray() {
        return (String[]) horLabelsArrayList.toArray(new String[0]);
    }

    private String[] getVerLabelsAsArray() {
        return (String[]) verLabelsArrayList.toArray(new String[0]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        randomValArray = HelperClass.generateRandomNumbers(randArrayMaxCapacity, verViewLow, verViewHigh, 60);

        horLabelsArrayList = HelperClass.generateLabelsAsArrayList(horViewCapacity, horViewLow, horViewInterval, false);
        verLabelsArrayList = HelperClass.generateLabelsAsArrayList(verViewCapacity, verViewLow, verViewInterval, false);

        gvGraph = new GraphView(this.getApplicationContext(), new float[]{}, "Please wait...", getHorLabelsAsArray(), getVerLabelsAsArray(),true);
        gvGraph.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout linearLayoutGraph = (LinearLayout) findViewById(R.id.linearLayoutGraph);
        linearLayoutGraph.addView(gvGraph);
    }

    public void onRun(View v) {
        Button btnRun = (Button)findViewById(R.id.buttonRun);
        Button btnStop = (Button)findViewById(R.id.buttonStop);
//        btnRun.setEnabled(false);
        btnStop.setEnabled(true);
        //TODO: Change contents below as required
        Toast.makeText(this, "Running...", Toast.LENGTH_SHORT).show();

//        intent = new Intent(v.getContext(), MainActivity.class);
//        intent.putExtra("graphState", GraphState.START);
        resetValues();
        updateGraphAndValuesPerViewRange();
//        startActivity(intent);
    }

    private void updateGraphAndValuesPerViewRange() {
        int currSize = randIndexRight - randIndexLeft + 1;
        randIndexRight = (randIndexRight + 1) % randViewCapacity;
        if (currSize < 0 || currSize == randViewCapacity) {
            randIndexLeft = (randIndexLeft + 1) % randViewCapacity;
            currPointsInNextHorInterval++;
        }
        int newArrayCapacity = Math.max(randViewCapacity, randIndexRight - randIndexLeft);
        int i = 0;
        float [] partialRandomValArray = new float[newArrayCapacity];
        while(i < newArrayCapacity) {
            partialRandomValArray[i] = randomValArray[((i + randIndexLeft) % randViewCapacity)];
            i++;
        }
        if(currPointsInNextHorInterval == maxPointsPerHorInterval) {
            currPointsInNextHorInterval = 0;
            horLabelsArrayList.remove(0);
            horViewHigh += horViewCapacity;
            horLabelsArrayList.add(Integer.toString((horViewHigh)));
            gvGraph.setHorlabels((String[]) horLabelsArrayList.toArray());
        }
        gvGraph.setValues(partialRandomValArray);
        gvGraph.invalidate();
        gvGraph.refreshDrawableState();
    }

//    public void onRun(View v) {
//
//        handler.removeCallbacks(sendUpdatesToUI);
//        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
//
//
//        Button btnRun = (Button)findViewById(R.id.buttonRun);
//        Button btnStop = (Button)findViewById(R.id.buttonStop);
//        btnRun.setEnabled(false);
//        btnStop.setEnabled(true);
//        //TODO: Change contents below as required
//        Toast.makeText(this, "Running...", Toast.LENGTH_SHORT).show();
//
//        String[] horLabels = generateLabels(low, high, 500, false);
//        String[] verLabels = generateLabels(2600, 3200, 50, false);
//
//        gvGraph.setValues(randValArray);
//        gvGraph.refreshDrawableState();
////        gvGraph.setVerlabels(horLabels);
////        gvGraph.setHorlabels(verLabels);
//    }

    public void onStop(View v) {
        Button btnRun = (Button)findViewById(R.id.buttonRun);
        Button btnStop = (Button)findViewById(R.id.buttonStop);
        btnStop.setEnabled(false);
        btnRun.setEnabled(true);
        //TODO: Change contents below as required
        Toast.makeText(this, "Stopped...!!!", Toast.LENGTH_SHORT).show();
    }


}
