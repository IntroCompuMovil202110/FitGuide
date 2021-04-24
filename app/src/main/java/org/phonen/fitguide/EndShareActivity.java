package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.Manifest;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EndShareActivity extends AppCompatActivity {

    //permisos
    private static final int CAMERA_PERMISSION_ID = 1;
    private static final int SAVE_PHOTO_ID = 2;
    private static final int OPEN_ALBUM_ID = 3;
    private static final String CAMERA_NAME = Manifest.permission.CAMERA;
    private static final String SAVE_PHOTO_NAME = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String OPEN_ALBUM_NAME = Manifest.permission.READ_EXTERNAL_STORAGE;

    ImageView final_imageView;
    Button buttonShare;
    Button buttonFinishShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_share);
        //inflate
        final_imageView = (ImageView) findViewById(R.id.final_imageView);
        buttonShare = (Button) findViewById(R.id.buttonShare); //save image
        buttonFinishShare = (Button) findViewById(R.id.buttonFinishShare);
Log.i("entre a end", "entre a end sin querer");
        //si estoy en esta pantalla significa que ya di permiso a la camara, tomare la foto
        take_picture();

    }

    public void share(View view) {
        request_permission(this, SAVE_PHOTO_NAME, "Se necesita acceder al album de fotos", SAVE_PHOTO_ID);
        if(ContextCompat.checkSelfPermission(this, SAVE_PHOTO_NAME) == PackageManager.PERMISSION_GRANTED) {
            saveToGallery();
        }
    }

    public void endSocialShare(View view) {
        startActivity(new Intent(getApplicationContext(),FeedActivity.class));
    }

    private void take_picture(){
        Intent take_picture_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(take_picture_intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(take_picture_intent, CAMERA_PERMISSION_ID);
        }
    }

    private void saveToGallery() {
        //obtener la imagen
        final_imageView.buildDrawingCache();
        Bitmap bm = final_imageView.getDrawingCache();

        //crea un album en el carrete
        String name = "fitguide";
        String current_date = getCurrentTime();
        String path = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "FitGuide/";

        //comprueba si ya existe el folder, si no, lo crea
        File dir = new File(path);
        if(!dir.exists()) {
            dir.mkdirs();
        }

        File filename = new File(dir, name + current_date + ".png") ;
        Log.i("PATH FINAL:", filename.toString());

        try {
            FileOutputStream out = new FileOutputStream(filename);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.i("bm.compress","despues del compress!!");
            out.flush();
            Log.i("flush","despues del flush");
            out.close();
            Log.i("close","despues del close");
            isImageCreated(filename);
            savedSuccessfully();
        }catch (FileNotFoundException e){
            unableToSave();
            Log.i("EXCEPTION:","FILE NOT FOUND!!!!");
        } catch (IOException e){
            unableToSave();
            Log.i("EXCEPTION2:","IO EXCEPTION");
        }
    }

    private String getCurrentTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return dateFormat.format(c.getTime());
    }

    private void isImageCreated(File dir){
        Log.i("isImageCreated", "ENTRE");
        MediaScannerConnection.scanFile(this,
                new String[]{dir.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("onScanCompleted","LLEGUE A onScanCompleted");
                    }
                });
    }

    private void savedSuccessfully(){
        Log.i("savedSuccesfully","ENTRE");
        Toast.makeText(this, "Imagen guardada con exito en la galeria", Toast.LENGTH_SHORT).show();
    }

    private void unableToSave(){
        Toast.makeText(this,"No se ha podido guardar la imagen!", Toast.LENGTH_SHORT).show();
    }

    protected void onActivityResult(int request_code, int result_code, Intent data) {
        super.onActivityResult(request_code, result_code, data);
        if (request_code == CAMERA_PERMISSION_ID && result_code == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bm = (Bitmap) extras.get("data");
            final_imageView.setImageBitmap(bm);
        }
    }

    private void request_permission(Activity context, String permiso, String justificacion, int idCode){
        if(ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(context, permiso)){
                Toast.makeText(context, justificacion, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(context, new String[]{permiso}, idCode);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SAVE_PHOTO_ID){

            if(ContextCompat.checkSelfPermission(this, SAVE_PHOTO_NAME) == PackageManager.PERMISSION_GRANTED) {
                saveToGallery();
            }
        }
    }

}