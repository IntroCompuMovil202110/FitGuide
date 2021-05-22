package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.phonen.fitguide.utils.PermissionManager;
import org.phonen.fitguide.model.Session;
import org.phonen.fitguide.utils.Constants;
import org.phonen.fitguide.utils.PostUploader;

public class FinishActivity extends AppCompatActivity {

    //permisos
    private static final int SAVE_PHOTO_ID = 3;
    private static final String SAVE_PHOTO_NAME = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    //View
    TextView time;
    TextView distance;
    TextView calories;
    TextView oxygen;
    ImageView imagV;
    //Data
    private Session session;
    private byte[] imgData;
    //Google
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        time = findViewById(R.id.labelTotalTimeInfo);
        distance = findViewById(R.id.labelTotalDistanceInfo);
        calories = findViewById(R.id.labelTotalCaloriesInfo);
        oxygen = findViewById(R.id.labelAveragePaceInfo);
        imagV = findViewById(R.id.imageViewFinalMap);
        this.mAuth = FirebaseAuth.getInstance();
        this.mDB = FirebaseDatabase.getInstance();
        //Recover data from intent
        Intent intent = getIntent();
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        this.imgData = intent.getByteArrayExtra("BMP");
        Bitmap bitmap = BitmapFactory.decodeByteArray(this.imgData, 0, this.imgData.length);
        Bitmap resizeBitmap = Bitmap.createScaledBitmap(
                bitmap,
                getIntent().getIntExtra("width", 1080),
                getIntent().getIntExtra("height", 2160),
                true);
        double time = intent.getDoubleExtra("time", 1);
        String timeS = calcularTiempo(time);
        double distance = intent.getDoubleExtra("distance", 1);
        String oxygen = intent.getStringExtra("oxigeno");
        double calories = intent.getDoubleExtra("calories", 1);
        int type = intent.getIntExtra("exerType", 0);
        double temp = intent.getDoubleExtra("temperature", 30);
        double pressure = intent.getDoubleExtra("pressure", 1000);
        // Set View
        this.time.setText(timeS);
        this.distance.setText(decimalFormat.format(distance) + " M");
        this.oxygen.setText(oxygen);
        this.calories.setText(decimalFormat.format(calories) + " KCAL");
        imagV.setImageBitmap(resizeBitmap);
        //Save data
        this.session = new Session();
        this.session.setType(type);
        this.session.setCalories(calories);
        this.session.setDistanceTraveled(distance);
        this.session.setTime(time);
        this.session.setTemperature(temp);
        this.session.setPressure(pressure);
        this.session.setOxygenLevel(oxygen);
        this.session.setScore((int) calories);
        this.session.setDate(new Date());


    }

    public void shareFeed(View view) {
        DatabaseReference myRef = this.mDB.getReference(
                Constants.SESSIONS_PATH +
                        this.mAuth.getCurrentUser().getUid());
        myRef = myRef.push();
        String sessionID = myRef.getKey();
        myRef.setValue(session).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PostUploader.uploadPost(PostUploader.createPostFromSession(
                        session,
                        mAuth.getCurrentUser().getUid(),
                        Constants.POSTS_IMAGES +
                                mAuth.getCurrentUser().getUid() +
                                "/" +
                                sessionID +
                                ".jpeg"),
                        imgData,
                        mDB,
                        FirebaseStorage.getInstance(),
                        intent,
                        getApplicationContext());
            }
        });
    }

    public void sharePhoto(View view) {

        DatabaseReference myRef = this.mDB.getReference(
                Constants.SESSIONS_PATH +
                        this.mAuth.getCurrentUser().getUid());
        myRef = myRef.push();
        String sessionID = myRef.getKey();
        myRef.setValue(session).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(getApplicationContext(), EndShareActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("sessionID", sessionID);
                bundle.putSerializable("sessionObject", session);
                intent.putExtra("sessionBundle", bundle);
                startActivity(intent);
            }
        });
    }

    public void saveMap(View view) {
        PermissionManager.requestPermission(
                this,
                SAVE_PHOTO_NAME,
                "Se necesita acceder al album de fotos",
                SAVE_PHOTO_ID);
        if (PermissionManager.checkPermission(this, SAVE_PHOTO_NAME)) {
            saveToGallery();
        }
    }

    public void finishActivity(View view) {
        DatabaseReference myRef = this.mDB.getReference(
                Constants.SESSIONS_PATH +
                        this.mAuth.getCurrentUser().getUid());
        myRef.push().setValue(session).addOnSuccessListener(aVoid -> startActivity(new Intent(getApplicationContext(), ProfileActivity.class)));

    }

    private void saveToGallery() {
        //obtener la imagen
        imagV.buildDrawingCache();
        Bitmap bm = imagV.getDrawingCache();

        //crea un album en el carrete
        String name = "fitguide";
        String current_date = getCurrentTime();
        String path = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "FitGuide/";

        //comprueba si ya existe el folder, si no, lo crea
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File filename = new File(dir, name + current_date + ".png");

        try {
            FileOutputStream out = new FileOutputStream(filename);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            isImageCreated(filename);
            savedSuccessfully();
        } catch (IOException e) {
            unableToSave();
        }
    }

    private String calcularTiempo(double time) {
        int horas = ((int) time / 3600);
        int minutos = (int) ((time - horas * 3600) / 60);
        int segundos = (int) (time - (horas * 3600 + minutos * 60));
        String sb = (horas < 10 ? "0" + horas : horas) + ":" +
                (minutos < 10 ? "0" + minutos : minutos) + ":" +
                (segundos < 10 ? "0" + segundos : segundos);
        return sb;
    }

    private String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return dateFormat.format(c.getTime());
    }

    private void isImageCreated(File dir) {
        MediaScannerConnection.scanFile(this,
                new String[]{dir.toString()}, null,
                (path, uri) -> {
                });
    }

    private void savedSuccessfully() {
        Toast.makeText(this, "Imagen guardada con exito en la galeria", Toast.LENGTH_SHORT).show();
    }

    private void unableToSave() {
        Toast.makeText(this, "No se ha podido guardar la imagen!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SAVE_PHOTO_ID) {
            if (PermissionManager.checkPermission(this, SAVE_PHOTO_NAME)) {
                saveToGallery();
            }
        }
    }

}