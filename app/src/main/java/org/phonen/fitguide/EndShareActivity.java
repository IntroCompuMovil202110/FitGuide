package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EndShareActivity extends AppCompatActivity {


    //para evaluar si tomo la foto desde la aplicacion
    private boolean tookPhoto = false;

    //permisos
    private static final int CAMERA_PERMISSION_ID = 1;
    private static final int IMAGE_PICKER_PERMISSION_ID = 2;
    private static final int SAVE_PHOTO_ID = 3;

    private static final String CAMERA_NAME = Manifest.permission.CAMERA;
    private static final String SAVE_PHOTO_NAME = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String IMAGE_PICKER_NAME = Manifest.permission.READ_EXTERNAL_STORAGE;

    ImageView final_imageView;
    Button buttonOpenCamera;
    Button buttonGallery;
    Button buttonShare;
    Button buttonFinishShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_share);
        //inflate
        final_imageView = (ImageView) findViewById(R.id.final_imageView);

        buttonOpenCamera = (Button) findViewById(R.id.buttonOpenCamera);
        buttonGallery = (Button) findViewById(R.id.buttonGallery);
        buttonShare = (Button) findViewById(R.id.buttonShare); //save image
        buttonFinishShare = (Button) findViewById(R.id.buttonFinishShare);

    }
    public void camera(View view) {
        request_permission(this, CAMERA_NAME, "Se necesita la camara", CAMERA_PERMISSION_ID);
        if(ContextCompat.checkSelfPermission(this, CAMERA_NAME) == PackageManager.PERMISSION_GRANTED) {
            tookPhoto = true;
            take_picture();
        }
    }

    public void uploadPicture(View view) {
        request_permission(this, IMAGE_PICKER_NAME, "Se neceista acceder al album de fotos", IMAGE_PICKER_PERMISSION_ID);
        if(ContextCompat.checkSelfPermission(this, IMAGE_PICKER_NAME) == PackageManager.PERMISSION_GRANTED){
            tookPhoto = false;
            pick_image();
        }
    }

    public void share(View view) {
        request_permission(this, SAVE_PHOTO_NAME, "Se necesita acceder al album de fotos", SAVE_PHOTO_ID);
        if(ContextCompat.checkSelfPermission(this, SAVE_PHOTO_NAME) == PackageManager.PERMISSION_GRANTED) {

            if(tookPhoto){
                saveToGallery();
            }
            startActivity(new Intent(getApplicationContext(), FeedActivity.class));
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


    private void pick_image(){
        Intent pick_imag_intent = new Intent(Intent.ACTION_PICK);
        pick_imag_intent.setType("image/*");
        startActivityForResult(pick_imag_intent, IMAGE_PICKER_PERMISSION_ID);
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

        try {
            FileOutputStream out = new FileOutputStream(filename);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);

            out.flush();
            out.close();
            isImageCreated(filename);
            savedSuccessfully();
        }catch (FileNotFoundException e){
            unableToSave();
        } catch (IOException e){
            unableToSave();
        }
    }

    private String getCurrentTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return dateFormat.format(c.getTime());
    }

    private void isImageCreated(File dir){

        MediaScannerConnection.scanFile(this,
                new String[]{dir.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {


                    }
                });
    }

    private void savedSuccessfully(){

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

        }else if(request_code == IMAGE_PICKER_PERMISSION_ID && result_code == RESULT_OK){
            try{
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                final_imageView.setImageBitmap(selectedImage);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
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

        if (requestCode == CAMERA_PERMISSION_ID) {
            if(ContextCompat.checkSelfPermission(this, CAMERA_NAME) == PackageManager.PERMISSION_GRANTED){
                tookPhoto = true;
                take_picture();
            }
        }
        if(requestCode == IMAGE_PICKER_PERMISSION_ID){
            if(ContextCompat.checkSelfPermission(this, IMAGE_PICKER_NAME) == PackageManager.PERMISSION_GRANTED){
                tookPhoto = false;
                pick_image();
            }
        }
       if (requestCode == SAVE_PHOTO_ID){
           if(ContextCompat.checkSelfPermission(this, SAVE_PHOTO_NAME) == PackageManager.PERMISSION_GRANTED){
               if(tookPhoto){
                   saveToGallery();
               }
               startActivity(new Intent(getApplicationContext(), FeedActivity.class));
           }
        }

    }
}