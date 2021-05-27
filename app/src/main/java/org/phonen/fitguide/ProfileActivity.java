package org.phonen.fitguide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
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
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import org.phonen.fitguide.model.Session;
import org.phonen.fitguide.services.MessageListener;
import org.phonen.fitguide.services.RequestsListenerService;

import org.phonen.fitguide.utils.Constants;
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.utils.Level;
import org.phonen.fitguide.utils.MyAxisValueFormatter;
import org.phonen.fitguide.utils.navBar;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {
    //Google
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference myRefS;
    //Data
    private String CHANNEL_ID_REQ = "RequestChannel";
    public static String CHANNEL_ID = "NOTI_APP";



    User user;
    BottomNavigationView bottomNavigationView;
    TextView textVPoint;
    TextView textLevel;
    TextView textViewN;
    TextView textVRank;
    TextView textphrase;
    Button semanal,mensual, textcomplement;


    ScrollView scrollView4;
    Date date;
    private BarChart chart;
    private LineChart lineChart;
    private PieChart pieChart;
    private String[] months = new String[]{"Enero", "Febrero", "Marzo","Abril", "Mayo","Junio","Julio","Agosto", "Septiembre", "Octubre", "Noviembre", "Dicimebre"};
    private String[] week = new String[]{"Lunes", "Martes", "Miércoles","Jueves", "Viernes","Sábado","Domingo"};
    private String[] levelOx = new String[]{"Alta", "Medio", "Bajo"};
    private List<Double> dataCalories =  new ArrayList<Double>();
    private List<Double> dataDistance =  new ArrayList<Double>();
    private List<Double> dataOx =  new ArrayList<Double>();
    ArrayList<Integer> colors;
    private String uId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.main_green));
        colors.add(getResources().getColor(R.color.clear_purple));
        colors.add(getResources().getColor(R.color.main_purple));
        colors.add(getResources().getColor(R.color.dark_green));
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
        textphrase = findViewById(R.id.textphrase);
        textcomplement = findViewById(R.id.textcomplement);
        mensual = findViewById(R.id.mensual);
        semanal = findViewById(R.id.semanal);
        chart = (BarChart) findViewById(R.id.chart1);
        lineChart = (LineChart) findViewById(R.id.chart2);
        pieChart = (PieChart) findViewById(R.id.chart3);

        getPhrase();

         uId =mAuth.getUid();
        Log.i("DEBUG uID",uId);
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child(Constants.USERS_PATH).child(uId).get().addOnCompleteListener(task -> {
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
                semanal.setTextColor(colors.get(0));
                mensual.setTextColor(getResources().getColor(R.color.white));
                getDataChartWeek();
            }
        });

        mensual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mensual.setTextColor(colors.get(0));
                semanal.setTextColor(getResources().getColor(R.color.white));
                getDataChartMonth();
            }
        });
        textcomplement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPhrase();
            }
        });

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Friends request notifications
            CharSequence nameReq ="Friends requests channel";
            String descriptionReq = "Channel used to notify new incoming friend requests";
            CharSequence nameMessage = "Message requests channel";
            String descriptionMessage = "Channel used to notify new messages";
            int importanceReq = NotificationManager.IMPORTANCE_DEFAULT;
            //IMPORTANCE_MAX MUESTRA LA NOTIFICACIÓN ANIMADA
            NotificationChannel channelReq = new NotificationChannel(CHANNEL_ID_REQ, nameReq, importanceReq);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, nameMessage, importanceReq);
            channel.setDescription(descriptionMessage);
            channelReq.setDescription(descriptionReq);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelReq);
            NotificationManager notificationManager2 = getSystemService(NotificationManager.class);
            notificationManager2.createNotificationChannel(channel);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        createNotificationChannel();
    }

    private void getPhrase() {
        Random r = new Random();
        int i = r.nextInt(Constants.PHRASE.length);
        textphrase.setText(Constants.PHRASE[i]);
        textcomplement.setText(Constants.COMPLEMENT[i]);
    }

    private void initNotificationService() {
        Intent intentReq = new Intent(ProfileActivity.this, RequestsListenerService.class);
        RequestsListenerService.enqueueWork(ProfileActivity.this, intentReq);

        Intent intent = new Intent(ProfileActivity.this, MessageListener.class);
        MessageListener.enqueueWork(ProfileActivity.this, intent);
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
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.startActivity:
                    startActivity(new Intent(getApplicationContext(), StartActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.chatActivity:
                    startActivity(new Intent(getApplicationContext(), ChatActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }

    /* public void navBarSettings(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.profileActivity);
         navBar navbar = new navBar();
         Intent i = navbar.navBarSettings(bottomNavigationView, getApplicationContext());
        startActivity(i);
        overridePendingTransition(0, 0);
    }*/

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
    @Override
    protected void onStart(){
        super.onStart();
        getDataChartWeek();
        this.initNotificationService();
    }

    private Chart getChart(String[] legend, Chart chart, String description, int limit)
    {
        chart.getDescription().setText(description);
        chart.getDescription().setTextSize(15);
        chart.setBackgroundColor(getResources().getColor(R.color.soft_white2));
        chart.animateY(2);
        //legend(chart, legend, limit);
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
            entry.formColor= colors.get(2);
            entries.add(entry);

        }

        chart.getLegend().setTextColor(colors.get(2));
        legend.setCustom(entries);
    }
    private ArrayList<BarEntry> getBarEntries(int limit,List<Double> data ){

        ArrayList<BarEntry> entries = new ArrayList<>();
        for( int i =0; i < limit ;i++)
        {
            double value = data.get(i);
            entries.add(new BarEntry(i, (float) value));

        }

        return entries;
    }
    private ArrayList<PieEntry> getBarEntriesOxy(){

        ArrayList<PieEntry> entries = new ArrayList<>();
        for( int i =0; i < 3 ;i++)
        {
            double value = dataOx.get(i);
            if(value != 0)
                entries.add(new PieEntry((float) value,levelOx[i]));

        }

        return entries;
    }
    private ArrayList<Entry> getBarEntriesDistance(int limit){

        ArrayList<Entry> entries = new ArrayList<>();
        for( int i =0; i < limit ;i++)
        {
            double value = (double) Math.round(dataDistance.get(i) * 100) / 100;
            entries.add(new BarEntry(i, (float) value/1000));

        }

        return entries;
    }
    private void axisX(XAxis axis, String[] week){
        axis.setGranularityEnabled(true);
        axis.setPosition(XAxis.XAxisPosition.BOTTOM);
        axis.setValueFormatter(new IndexAxisValueFormatter(week));
        axis.setAxisLineColor(colors.get(2));
        axis.setTextColor(colors.get(2));
        axis.setDrawGridLines(false);
    }
    private void axisLeft(YAxis axis)
    {
        axis.setAxisLineColor(colors.get(2));
        axis.setTextColor(colors.get(2));
        axis.setSpaceTop(30);
        axis.setDrawGridLines(false);
        axis.setAxisMinimum(0);
    }
    private void axisRight(YAxis axis){
        axis.setEnabled(false);
        axis.setDrawGridLines(false);
    }
    public void createChart(String[] legend, int limit){
        //Bar Chart
        chart = (BarChart)getChart(legend, chart, "",limit);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.invalidate();
        chart.setData(getDataChart(limit));
        axisX(chart.getXAxis(), legend);
        axisLeft(chart.getAxisLeft());
        axisRight(chart.getAxisRight());
        chart.getLegend().setEnabled(false);

        //LineChart
        lineChart = (LineChart)getChart(legend, lineChart, "",limit);
        lineChart.setDrawGridBackground(false);
        lineChart.invalidate();
        lineChart.setData(getDataLineChart(limit));
        axisX(lineChart.getXAxis(), legend);
        axisLeft(lineChart.getAxisLeft());
        axisRight(lineChart.getAxisRight());
        lineChart.getAxisLeft().setValueFormatter(new MyAxisValueFormatter());
        lineChart.getLegend().setEnabled(false);
    }

    public void createPieChart(String[] legend){

        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(0);
        pieChart.getDescription().setEnabled(false);
        legend(chart, legend, 3);
        pieChart.getLegend().setTextColor(colors.get(2));

        pieChart.setTransparentCircleAlpha(0);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setCenterTextSize(10);
        pieChart.setData(getDataPieChart());
        pieChart.invalidate();

    }


    private DataSet getData(DataSet dataSet){

        dataSet.setColors(colors);

        dataSet.setValueTextSize(10);
        return dataSet;
    }
    private BarData getDataChart(int limit){
        BarDataSet data = (BarDataSet)getData(new BarDataSet(getBarEntries(limit, dataCalories),""));

        BarData barData = new BarData(data);
        barData.setValueTextColor(colors.get(2));
        barData.setValueTextSize(9f);
        barData.setBarWidth(0.45f);
        return barData;
    }
    private LineData getDataLineChart(int limit){
        LineDataSet set = new LineDataSet(getBarEntriesDistance(limit), "");
        set.setColor(colors.get(2));

        set.setCircleColor(colors.get(3));
        ArrayList<ILineDataSet> dataSet = new ArrayList<>();
        dataSet.add(set);


        LineData barData = new LineData(dataSet);
        barData.setValueTextColor(colors.get(2));
        barData.setValueTextSize(10f);

        return barData;
    }
    private PieData getDataPieChart(){
        PieDataSet pieDataSet = new PieDataSet(getBarEntriesOxy(), "");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);
        pieDataSet.setColors(colors);

        PieData barData = new PieData(pieDataSet);
        barData.setValueTextColor(Color.WHITE);
        barData.setValueTextSize(10f);

        return barData;
    }

    private void getDataChartWeek(){
        date =  new Date();
        myRefS = FirebaseDatabase.getInstance().getReference("sessions/" + uId);
        myRefS.get().addOnSuccessListener(v->{
            setZero();
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

                    int i = getIndex(session.getOxygenLevel());
                    dataOx.set(i, dataOx.get(i)+1);
                }
            }
            createChart(week, date.getDay());
            createPieChart(levelOx);
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void getDataChartMonth() {
        date =  new Date();
        myRefS = FirebaseDatabase.getInstance().getReference("sessions/" + uId);
        myRefS.get().addOnSuccessListener(v->{
            setZero();
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

                    int i = getIndex(session.getOxygenLevel());
                    dataOx.set(i, dataOx.get(i)+1);
                }

            }
            createChart(months, date.getMonth()+1);
        });

    }
    private void setZero(){
        dataCalories.clear();
        dataCalories = new ArrayList<Double>(Collections.nCopies(7, 0.0));
        dataDistance.clear();
        dataDistance = new ArrayList<Double>(Collections.nCopies(7, 0.0));
        dataOx.clear();
        dataOx  = new ArrayList<Double>(Collections.nCopies(3, 0.0));
    }
    private int getIndex(String oxigenation){
        if(oxigenation.equals("ALTO"))
        {
            return 0;
        }
        else if(oxigenation.equals("MEDIO"))
        {
            return 1;
        }
        else if(oxigenation.equals("BAJO"))
        {
            return 2;
        }
        return 2;

    }





}