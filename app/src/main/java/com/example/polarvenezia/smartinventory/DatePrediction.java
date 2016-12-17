package com.example.polarvenezia.smartinventory;

import java.text.SimpleDateFormat;
import java.util.*;
import com.example.polarvenezia.smartinventory.LinearRegression;
public class DatePrediction {
    ArrayList<Double> dateArray;
    ArrayList<Double> weightArray;

    public DatePrediction () {
        this.dateArray = new ArrayList<Double>();
        this.weightArray = new ArrayList<Double>();
        // WHAT YOU NEED TO DO AFTER INSTANTIATE: read all the dates and weights, call addDate() and addWeight()
    }

    public void addDate (String date) {
        this.dateArray.add(dateToRaw(date));
    }

    public void addWeight (String weight) {
        this.weightArray.add(Double.parseDouble(weight));
    }

    public double dateToRaw(String dateString) {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = parser.parse(dateString);
            return date.getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    public String rawToDate(double dateRaw) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateRaw);
    }

    public double linearRegression() {
        LinearRegression myReg = new LinearRegression(arrayListToArray(weightArray), arrayListToArray(dateArray));
        return myReg.intercept();
    }

    public double[] arrayListToArray (ArrayList<Double> input) {
        double[] ans = new double[input.size()];
        for (int i = 0; i < input.size(); i++) {
            ans[i] = input.get(i);
        }
        return ans;
    }

    public String getDate() {
        return rawToDate(linearRegression());
    }
}
