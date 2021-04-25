package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.phonen.fitguide.Utils.PermissionManager;

public class FinishActivity extends AppCompatActivity {

    //permisos
    private static final int SAVE_PHOTO_ID = 3;
    private static final String SAVE_PHOTO_NAME = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    TextView time;
    TextView distance;
    TextView calories;
    TextView oxygen;
    ImageView imagV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        time = findViewById(R.id.labelTotalTimeInfo);
        distance = findViewById(R.id.labelTotalDistanceInfo);
        calories = findViewById(R.id.labelTotalCaloriesInfo);
        oxygen = findViewById(R.id.labelAveragePaceInfo);
        imagV = findViewById(R.id.imageViewFinalMap);
        Intent intent = getIntent();

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        byte[] bytes = intent.getByteArrayExtra("BMP");
        Log.i("BYTES", bytes.toString());
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, getIntent().getIntExtra("width", 1080), getIntent().getIntExtra("height",2160), true);

        double time = intent.getDoubleExtra("time", 1);
        String timeS = calcularTiempo(time);
        double distance = intent.getDoubleExtra("distance", 1);

        String oxygen = intent.getStringExtra("oxigeno");
        double calories = intent.getDoubleExtra("calories", 1);

        // setters
        this.time.setText(timeS);
        this.distance.setText(decimalFormat.format(distance) + " M");
        this.oxygen.setText(oxygen);
        this.calories.setText(decimalFormat.format(calories) + " KCAL");
        imagV.setImageBitmap(resizeBitmap);
    }

    public void shareFeed(View view) {
        startActivity(new Intent(getApplicationContext(), FeedActivity.class));
    }

    public void sharePhoto(View view) {
        startActivity(new Intent(getApplicationContext(), EndShareActivity.class));
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
        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
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
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            isImageCreated(filename);
            savedSuccessfully();
        } catch (FileNotFoundException e) {
            unableToSave();
        } catch (IOException e) {
            unableToSave();
        }
    }

    private String calcularTiempo(double time) {
        StringBuilder sb = new StringBuilder();
        int horas = ((int) time / 3600);
        int minutos = (int) ((time - horas * 3600) / 60);
        int segundos = (int) (time - (horas * 3600 + minutos * 60));
        sb.append(horas < 10 ? "0" + horas : horas).append(":");
        sb.append(minutos < 10 ? "0" + minutos : minutos).append(":");
        sb.append(segundos < 10 ? "0" + segundos : segundos);
        return sb.toString();
    }

    private String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return dateFormat.format(c.getTime());
    }

    private void isImageCreated(File dir) {
        MediaScannerConnection.scanFile(this,
                new String[]{dir.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                    }
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
            if (PermissionManager.checkPermission(this,SAVE_PHOTO_NAME)) {
                saveToGallery();
            }
        }
    }

}