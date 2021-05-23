package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.phonen.fitguide.model.Session;
import org.phonen.fitguide.utils.Level;
import org.phonen.fitguide.utils.References;
import org.phonen.fitguide.model.User;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference myRefS;

    User user;
    BottomNavigationView bottomNavigationView;
    TextView textVPoint;
    TextView textLevel;
    TextView textViewN;
    TextView textVRank;
    ScrollView scrollView4;
    Button semanal,mensual;
    ValueEventListener EventChart;
    Date date;
    private BarChart chart;
    private LineChart lineChart;
    private String[] months = new String[]{"Enero", "Febrero", "Marzo","Abril", "Mayo","Junio","Julio","Agosto", "Septiembre", "Octubre", "Noviembre", "Dicimebre"};
    private String[] week = new String[]{"Lunes", "Martes", "Miércoles","Jueves", "Viernes","Sábado","Domingo"};
    private List<Double> dataCalories=  new ArrayList<Double>();
    private List<Double> dataDistance=  new ArrayList<Double>();
    private int[] colors = new int[] { R.color.main_purple, R.color.dark_green};
    private String uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        navBarSettings();
        scrollView4 = findViewById(R.id.scrollView4);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //-----------------------
        textViewN = findViewById(R.id.textNameV);
        textLevel = findViewById(R.id.levelVText);
        textVPoint =  findViewById(R.id.textVPoints);
        textVRank = findViewById(R.id.textVRank);
        mensual = findViewById(R.id.mensual);
        semanal = findViewById(R.id.semanal);
        chart = (BarChart) findViewById(R.id.chart1);
        lineChart = (LineChart) findViewById(R.id.chart2);


         uId =mAuth.getUid();
        Log.i("DEBUG uID",uId);
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child(References.PATH_USERS).child(uId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            }
            else {
                Level level = new Level();
                user = task.getResult().getValue(User.class);
                textViewN.setText(user.getName().toUpperCase());
                textLevel.setText(level.Season(user.getRank()));
                textVRank.setText("NIVEL:     "+user.getRank());
                textVPoint.setText(String.valueOf(user.getPoints()));
                getDataChartWeek();


            }
        });

        semanal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                semanal.setTextColor(getResources().getColor(R.color.main_green));
                mensual.setTextColor(getResources().getColor(R.color.white));
                getDataChartWeek();
            }
        });

        mensual.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                mensual.setTextColor(getResources().getColor(R.color.main_green));
                semanal.setTextColor(getResources().getColor(R.color.white));
                getDataChartMonth();
            }
        });


    }

    public void navBarSettings(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.profileActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.challengesActivity:
                    startActivity(new Intent(getApplicationContext(), ChallengesActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.feedActivity:
                    startActivity(new Intent(getApplicationContext(), FeedActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.profileActivity:
                    return true;
                case R.id.startActivity:
                    startActivity(new Intent(getApplicationContext(), StartActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }

    public void EditarPerfil(View view) {
        startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
        overridePendingTransition(0, 0);
    }

    public void ListaAmigos(View view) {
        startActivity(new Intent(getApplicationContext(), FriendsListActivity.class));
        overridePendingTransition(0, 0);
    }

    public void logOut(View view)
    {
        mAuth.signOut();
        Intent intent = new Intent (getApplicationContext(),LoggedOutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    private Chart getChart(Chart chart, String description, int limit)
    {
        chart.getDescription().setText(description);
        chart.getDescription().setTextSize(15);
        chart.setBackgroundColor(getResources().getColor(R.color.soft_white2));
        chart.animateY(2);
        legend(chart, week, limit);
        return chart;
    }
    private void legend(Chart chart, String[] DataLegend, int limit )
    {
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        ArrayList<LegendEntry> entries = new ArrayList<>();
        for(int i =0; i < limit; i++)
        {
            LegendEntry entry = new LegendEntry();

            entry.label = DataLegend[i];
            entry.formColor= getResources().getColor(R.color.main_purple);
            entries.add(entry);

        }
        legend.setCustom(entries);
    }
    private ArrayList<BarEntry> getBarEntriesCalories(int limit){

        ArrayList<BarEntry> entries = new ArrayList<>();
        for( int i =0; i < limit ;i++)
        {
            double value = dataCalories.get(i);
            entries.add(new BarEntry(i, (float) value));

        }

        return entries;
    }
    private ArrayList<Entry> getBarEntriesDistance(int limit){

        ArrayList<Entry> entries = new ArrayList<>();
        for( int i =0; i < limit ;i++)
        {
            double value = (double) Math.round(dataDistance.get(i) * 100) / 100;
            entries.add(new BarEntry(i, (float) value));

        }

        return entries;
    }
    private void axisX(XAxis axis, String[] week){
        axis.setGranularityEnabled(true);
        axis.setPosition(XAxis.XAxisPosition.BOTTOM);
        axis.setValueFormatter(new IndexAxisValueFormatter(week));

        chart.getXAxis().setAxisLineColor(getResources().getColor(R.color.main_purple));
        chart.getXAxis().setTextColor(getResources().getColor(R.color.main_purple));
        chart.getXAxis().setDrawGridLines(false);
    }
    private void axisLeft(YAxis axis)
    {
        chart.getAxisLeft().setAxisLineColor(getResources().getColor(R.color.main_purple));
        chart.getAxisLeft().setTextColor(getResources().getColor(R.color.main_purple));
        axis.setSpaceTop(30);
        axis.setAxisMinimum(0);
    }
    private void axisRight(YAxis axis){
        axis.setEnabled(false);
    }
    public void createChartd(String[] legend, int limit){
        chart = (BarChart)getChart(chart, "",limit);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.invalidate();
        chart.setData(getDataChart(limit));
        axisX(chart.getXAxis(), legend);
        axisLeft(chart.getAxisLeft());
        axisRight(chart.getAxisRight());
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getLegend().setEnabled(false);
        chart.getLegend().setTextColor(getResources().getColor(R.color.main_purple));

       lineChart = (LineChart)getChart(lineChart, "",limit);
        lineChart.setDrawGridBackground(false);
        lineChart.invalidate();
        lineChart.setData(getDataLineChart(limit));
        axisX(lineChart.getXAxis(), legend);
        axisLeft(lineChart.getAxisLeft());
        axisRight(lineChart.getAxisRight());
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getLegend().setTextColor(getResources().getColor(R.color.main_purple));
    }
    private DataSet getData(DataSet dataSet){
        ArrayList<Integer> colorC = new ArrayList<Integer>();


        colorC.add(ContextCompat.getColor(this, R.color.main_purple));
        colorC.add(ContextCompat.getColor(this, R.color.main_green));
        dataSet.setColors(colorC);

        dataSet.setValueTextSize(10);
        return dataSet;
    }
    private BarData getDataChart(int limit){
        BarDataSet data = (BarDataSet)getData(new BarDataSet(getBarEntriesCalories(limit),""));

        BarData barData = new BarData(data);
        barData.setBarWidth(0.45f);
        return barData;
    }
    private LineData getDataLineChart(int limit){
        LineDataSet set = new LineDataSet(getBarEntriesDistance(limit), "");
        set.setColor(getResources().getColor(R.color.main_purple));
        set.setCircleColor(getResources().getColor(R.color.dark_green));
        ArrayList<ILineDataSet> dataSet = new ArrayList<>();
        dataSet.add(set);

        LineData barData = new LineData(dataSet);
        return barData;
    }

    private void getDataChartWeek(){
        date =  new Date();
        myRefS = FirebaseDatabase.getInstance().getReference("sessions/" + uId);
        myRefS.get().addOnSuccessListener(v->{
            Log.i("DEBUG SESSION ",References.PATH_SESSIONS+ uId +"/");
            dataCalories.clear();
            dataCalories = new ArrayList<Double>(Collections.nCopies(7, 0.0));
            dataDistance.clear();
            dataDistance = new ArrayList<Double>(Collections.nCopies(7, 0.0));
            for( DataSnapshot single: v.getChildren())
            {
                Session session = single.getValue(Session.class);
                LocalDate dateSession = session.getDate().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
                LocalDate dateA = LocalDate.now().minusDays((date.getDay()-1) );
                LocalDate dateB = dateA.plusDays(6);
                if( dateSession.compareTo(dateA) >= 0  &&  dateSession.compareTo(dateB) <= 0){
                    int day =  session.getDate().getDay();
                    dataCalories.set(day-1,dataCalories.get(day-1)+ session.getCalories());
                   dataDistance.set(day-1,dataDistance.get(day-1)+ session.getDistanceTraveled());

                }


            }
            createChartd(week, date.getDay());
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        getDataChartWeek();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myRefS.removeEventListener(EventChart);
    }

    public void getDataChartMonth() {
        date =  new Date();
        myRefS = FirebaseDatabase.getInstance().getReference("sessions/" + uId);
        myRefS.get().addOnSuccessListener(v->{
            Log.i("MONYH::::  ",References.PATH_SESSIONS+ uId +"/");
            dataCalories.clear();
            dataCalories = new ArrayList<Double>(Collections.nCopies(12, 0.0));
            dataDistance.clear();
            dataDistance = new ArrayList<Double>(Collections.nCopies(12, 0.0));
            for( DataSnapshot single: v.getChildren())
            {
                Session session = single.getValue(Session.class);

                LocalDate dateSession = session.getDate().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
                LocalDate dateB = LocalDate.now().plusDays(1);
                LocalDate dateA = LocalDate.of(dateB.getYear(), 1, 1);
                if( dateSession.compareTo(dateA) >= 0  &&  dateSession.compareTo(dateB) <= 0){
                    int month =  session.getDate().getMonth();
                    dataCalories.set(month,dataCalories.get(month)+ session.getCalories());
                    dataDistance.set(month,dataDistance.get(month)+ session.getDistanceTraveled());
                }

            }
            createChartd(months, date.getMonth()+1);
        });
    }


}