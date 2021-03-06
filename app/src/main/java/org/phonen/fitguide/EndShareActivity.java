package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.Manifest;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.phonen.fitguide.model.Session;
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.utils.Constants;
import org.phonen.fitguide.utils.ImageGenerator;
import org.phonen.fitguide.utils.PermissionManager;
import org.phonen.fitguide.utils.PostUploader;

public class EndShareActivity extends AppCompatActivity {


    //Data
    private Session session;
    private String sessionID;
    private Bitmap bitmap;
    //Permissions
    private static final int CAMERA_PERMISSION_ID = 1;
    private static final int IMAGE_PICKER_PERMISSION_ID = 2;
    private static final int SAVE_PHOTO_ID = 3;
    private static final String CAMERA_NAME = Manifest.permission.CAMERA;
    private static final String SAVE_PHOTO_NAME = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String IMAGE_PICKER_NAME = Manifest.permission.READ_EXTERNAL_STORAGE;
    //View
    ImageView final_imageView;
    Button buttonOpenCamera;
    Button buttonGallery;
    Button buttonShare;
    Button buttonFinishShare;
    TextView headername;
    //Google
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    User user;

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
        headername = findViewById(R.id.labelName);

        mAuth = FirebaseAuth.getInstance();
        Bundle bundle = getIntent().getBundleExtra("sessionBundle");
        this.session = (Session)bundle.getSerializable("sessionObject");
        this.sessionID = bundle.getString("sessionID");
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        String uId = mAuth.getUid();
        myRef = database.getReference(Constants.USERS_PATH + uId);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                headername.setText("¡VAMOS " + user.getName().toUpperCase() + "!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void camera(View view) {
        PermissionManager.requestPermission(
                this,
                CAMERA_NAME,
                "Se necesita la cámara para capturar la foto",
                CAMERA_PERMISSION_ID
        );
        if (PermissionManager.checkPermission(this, CAMERA_NAME)) {
            take_picture();
        }
    }

    public void uploadPicture(View view) {
        PermissionManager.requestPermission(
                this,
                IMAGE_PICKER_NAME,
                "Se necesita acceder al album de fotos",
                IMAGE_PICKER_PERMISSION_ID
        );
        if (PermissionManager.checkPermission(this, IMAGE_PICKER_NAME)) {
            pick_image();
        }
    }

    public void share(View view) {
        Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (this.final_imageView.getDrawable() != null){
            String imagePath = Constants.POSTS_IMAGES +
                    mAuth.getCurrentUser().getUid() +
                    "/" +
                    sessionID +
                    ".jpeg";
            PostUploader.uploadPost(
                    PostUploader.createPostFromSession(
                            session, mAuth.getCurrentUser().getUid(),imagePath
                    ),
                    ImageGenerator.bytesFromBitmap(bitmap),
                    FirebaseDatabase.getInstance(),
                    FirebaseStorage.getInstance(),
                    intent,
                    getApplicationContext()
            );
        }else {
            PostUploader.uploadPost(
                    PostUploader.createPostFromSession(
                            session, mAuth.getCurrentUser().getUid(), ""
                    ),
                    null,
                    FirebaseDatabase.getInstance(),
                    FirebaseStorage.getInstance(),
                    intent,
                    getApplicationContext()
            );
        }
    }

    public void endSocialShare(View view) {
        startActivity(new Intent(getApplicationContext(), FeedActivity.class));
    }

    private void take_picture() {
        Intent take_picture_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(take_picture_intent, CAMERA_PERMISSION_ID);
    }

    private void pick_image() {
        Intent pick_imag_intent = new Intent(Intent.ACTION_PICK);
        pick_imag_intent.setType("image/*");
        startActivityForResult(pick_imag_intent, IMAGE_PICKER_PERMISSION_ID);
    }

    @Override
    protected void onActivityResult(int request_code, int result_code, Intent data) {
        super.onActivityResult(request_code, result_code, data);
        if (request_code == CAMERA_PERMISSION_ID && result_code == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            final_imageView.setImageBitmap(bitmap);
        } else if (request_code == IMAGE_PICKER_PERMISSION_ID && result_code == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(imageStream);
                final_imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_ID) {
            if (PermissionManager.checkPermission(this, CAMERA_NAME)) {
                take_picture();
            }
        }
        if (requestCode == IMAGE_PICKER_PERMISSION_ID) {
            if (PermissionManager.checkPermission(this, IMAGE_PICKER_NAME)) {
                pick_image();
            }
        }
    }
}