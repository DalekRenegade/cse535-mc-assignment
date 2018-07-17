package com.cse535.assignments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    int randIndexRight = 31;
    private final Handler handler = new Handler();

    private List<DataPoint> dataPointList;
    private LineGraphSeries<DataPoint> dataSeries;
    private Random mRandom;

    private Runnable graphThread;
    private boolean graphState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRandom = new Random();
        graphThread = new Runnable() {
            @Override
            public void run() {
                DataPoint appendDataPoint = new DataPoint(randIndexRight, mRandom.nextDouble() * 50.0);
                dataPointList.add(appendDataPoint);
                randIndexRight += 1;
                dataSeries.appendData(appendDataPoint, true, 32);
                handler.postDelayed(this, 500);
            }
        };
        //NEW GRAPH
        GraphView gvGraph = findViewById(R.id.graph);
        dataPointList = new ArrayList<>();
        dataSeries = new LineGraphSeries<>(HelperClass.generateRandomData(dataPointList));
        dataSeries.setColor(Color.GREEN);
        gvGraph.addSeries(dataSeries);

        // set viewport properties
        gvGraph.getViewport().setXAxisBoundsManual(true);
        gvGraph.getViewport().setYAxisBoundsManual(true);
        gvGraph.getViewport().setMinX(0);
        gvGraph.getViewport().setMaxX(30);
        gvGraph.getViewport().setMinY(0);
        gvGraph.getViewport().setMaxY(80);
        gvGraph.getViewport().setBackgroundColor(Color.BLACK);
        gvGraph.getViewport().setBorderColor(Color.WHITE);
        gvGraph.getViewport().setDrawBorder(true);

        // set grid & label properties
        gvGraph.getGridLabelRenderer().setHorizontalAxisTitle(getString(R.string.horizontal_axis_title));
        gvGraph.getGridLabelRenderer().setVerticalAxisTitle(getString(R.string.vertical_axis_title));
        gvGraph.getGridLabelRenderer().setHighlightZeroLines(true);
        gvGraph.getGridLabelRenderer().setLabelsSpace(5);
        gvGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        gvGraph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.WHITE);
        gvGraph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.WHITE);
        gvGraph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        gvGraph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        gvGraph.getGridLabelRenderer().setGridColor(Color.WHITE);
        gvGraph.getGridLabelRenderer().reloadStyles();
    }

    public void onRun(View v) {
        controlGraph(true);
    }

    public void onStop(View v) {
        controlGraph(false);
    }

    public void controlGraph(boolean execution) {
        if (execution) {
            if (graphState) {
                if (randIndexRight > 32) {
                    dataSeries.resetData(HelperClass.regenerateRandomData(dataPointList));
                }
                handler.postDelayed(graphThread, 1000);
                Log.d("State", "started");
                graphState = false;
            }
        } else {
            if (!graphState) {
                handler.removeCallbacks(graphThread);
                DataPoint[] dataPoints = new DataPoint[1];
                dataPoints[0] = new DataPoint(0, 0);
                dataSeries.resetData(dataPoints);
                Log.d("State", "stopped");
                graphState = true;
            }
        }
    }
}
