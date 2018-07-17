package com.cse535.assignments.testapplication;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HelperClass {

    public static ArrayList<String> generateLabelsAsArrayList(int capacity, int start, int interval, boolean includeExtremas) {
        ArrayList<String> queue = new ArrayList<String>();
        if(includeExtremas)
            queue.add(Integer.toString(start));
        while(capacity > 0) {
            start += interval;
            queue.add(Integer.toString(start));
            capacity--;
        }
        if(includeExtremas)
            queue.add(Integer.toString((start + interval)));
        return queue;
    }

    public static String[] generateLabels(int start, int end, int interval, boolean includeExtremas) {
        int count = (int)(Math.ceil((end - start) / interval)) - 1;
        if(includeExtremas)
            count += 2;
        String[] labels = new String[count];
        int i = 0;
        if(includeExtremas) {
            labels[i++] = Integer.toString(start);
            labels[count - 1] = Integer.toString(end);
        }
        else
            start += interval;
        while(start < end){
            labels[i++] = Integer.toString(start);
            start += interval;
        }
        return labels;
    }

    public static float[] generateRandomNumbers(int size, float low, float high, int desiredMedian) {
        float [] arr = new float[size];
        Random rand = new Random();
        for(int i = 0;i<size;i++)
            arr[i] = low + rand.nextFloat() * (high - low);
        float newMedian = calculateMedian(arr);
        for(int i=0;i<size;i++) {
            arr[i] = (arr[i] - newMedian + desiredMedian);
            if(arr[i] < 0)
                arr[i] += high;
        }
        return arr;
    }

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