package com.cse535.assignments;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HelperClass {

    public static DataPoint[] generateRandomData(List<DataPoint> dataPointList) {
        Random mRandom = new Random();
        int count = 31;
        DataPoint[] values = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            double x = i;
            double y = mRandom.nextDouble() * 50.0;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        for (int i = 0; i < count; i++) {
            dataPointList.add(values[i]);
        }
        return values;
    }

    public static DataPoint[] regenerateRandomData(List<DataPoint> dataPointList) {

        DataPoint[] values = new DataPoint[dataPointList.size()];
        for (int i = 0; i < dataPointList.size(); i++) {
            values[i] = dataPointList.get(i);
        }
        return values;
    }

    private static float calculateMedian(float[] arr) {
        float [] copiedArray = arr.clone();
        Arrays.sort(copiedArray);
        int middle = copiedArray.length/2;
        float medianValue = 0;
        if (copiedArray.length%2 == 1)
            medianValue = copiedArray[middle];
        else
            medianValue = (copiedArray[middle-1] + copiedArray[middle]) / 2;
        return medianValue;
    }
}
