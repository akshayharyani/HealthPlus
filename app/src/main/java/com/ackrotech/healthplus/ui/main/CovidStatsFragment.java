package com.ackrotech.healthplus.ui.main;

import android.graphics.fonts.SystemFonts;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ackrotech.healthplus.R;
import com.ackrotech.healthplus.Utility.VolleyUtility;
import com.ackrotech.healthplus.data.model.CovidStats;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CovidStatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CovidStatsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private String statsUrl = "https://covidtracking.com/api/v1/us/daily.json";
    private static final String TAG = CovidStatsFragment.class.getSimpleName();
    private LineChart chart;
    private  List<CovidStats> data;

    public CovidStatsFragment() {
        // Required empty public constructor
    }


    public static CovidStatsFragment newInstance() {
        CovidStatsFragment fragment = new CovidStatsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_covid_stats, container, false);
        chart = (LineChart) view.findViewById(R.id.chart);
        chart.setTouchEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(2f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);

            @Override
            public String getFormattedValue(float value) {

                try {
                    Date date = new Date(new Float(value).longValue());
                return mFormat.format(date);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return String.valueOf(value);
            }
        });

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
        data = new ArrayList<>();
        makeDataRequest();

        return view;
    }


    private void makeDataRequest() {

        JsonArrayRequest req = new JsonArrayRequest(statsUrl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = (JSONObject) response.get(i);
                                CovidStats covidStats;
                                covidStats = new CovidStats();
                                if (obj.has("positive") && !obj.isNull("positive")) {
                                    covidStats.setConfirmed_cases(obj.getLong("positive"));
                                } else {
                                    covidStats.setConfirmed_cases(0);
                                }
                                if (obj.has("death") && !obj.isNull("death")) {
                                    covidStats.setDeaths(obj.getLong("death"));
                                } else {
                                    covidStats.setDeaths(0);
                                }
                                if (obj.has("recovered") && !obj.isNull("recovered")) {
                                    covidStats.setRecovered(obj.getLong("recovered"));
                                } else {
                                    covidStats.setRecovered(0);
                                }
                                if (obj.has("date") && !obj.isNull("date")) {
                                    SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd");
                                    Date date = originalFormat.parse(obj.getString("date"));
                                    covidStats.setDate(date);
                                    covidStats.setDateLong(obj.getLong("date"));
                                } else {
                                    SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd");
                                    Date date = originalFormat.parse("19900101");
                                    covidStats.setDate(date);
                                }

                                if(obj.has("positiveIncrease") && !obj.isNull("positiveIncrease")){
                                    covidStats.setPositiveIncrease(obj.getLong("positiveIncrease"));
                                }else {
                                    covidStats.setPositiveIncrease(0);
                                }
                                data.add(covidStats);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                        initializeGraph(data);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        VolleyUtility.getInstance(getActivity()).addToRequestQueue(req);
    }

    public void initializeGraph(List<CovidStats> data) {

        System.out.println(data.size());
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.size()-50; i++) {
            CovidStats stat = data.get(i);

            entries.add(new Entry(new Long(stat.getDate().getTime()).floatValue(), stat.getPositiveIncrease()));
        }

        Collections.sort(entries, new EntryXComparator());



        LineDataSet dataSet = new LineDataSet(entries, "Increase in number of Cases");
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(3);
        dataSet.setColor(R.color.colorPrimary);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

}
