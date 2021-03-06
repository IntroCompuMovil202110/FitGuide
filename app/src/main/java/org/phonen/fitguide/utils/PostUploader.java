package org.phonen.fitguide.utils;
import org.phonen.fitguide.model.Post;
import org.phonen.fitguide.model.Session;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class PostUploader {
    public static void uploadPost(Post post, byte[] imgData, FirebaseDatabase mDatabase, FirebaseStorage mStorage, Intent targetIntent, Context context){
        if (post != null){
            mDatabase.getReference(Constants.POSTS_PATH + post.getUserUID()).push().setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (!post.getImagePath().equals("")){
                        mStorage.getReference(post.getImagePath()).putBytes(imgData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                context.startActivity(targetIntent);
                            }
                        });
                    }else{
                        context.startActivity(targetIntent);
                    }
                }
            });
        }
    }
    public static void uploadPost(Post post, byte[] imgData, FirebaseDatabase mDatabase, FirebaseStorage mStorage){
        if (post != null){
            mDatabase.getReference(Constants.POSTS_PATH + post.getUserUID()).push().setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if(!post.getImagePath().equals("")){
                        mStorage.getReference(post.getImagePath()).putBytes(imgData);
                    }
                }
            });
        }
    }
    private static String generateSessionDescription(Session session){
        StringBuilder stringBuilder = new StringBuilder();
        switch (session.getType()){
            case 0:
                stringBuilder.append("Caminó");
                break;
            case 1:
                stringBuilder.append("Corrió");
                break;
            case 2:
                stringBuilder.append("Montó bici");
                break;
            default:
                stringBuilder.append("Caminó");
                break;
        }
        stringBuilder.append(" durante ");
        stringBuilder.append(getNaturalTime(session.getTime()));
        stringBuilder.append(" y quemó ");
        stringBuilder.append((int)session.getCalories());
        stringBuilder.append(" KCAL. ");
        Random rand = new Random();
        switch (rand.nextInt(4)){
            case 0:
                stringBuilder.append("Genial!");
                break;
            case 1:
                stringBuilder.append("Espectacular!");
                break;
            case 2:
                stringBuilder.append("Asombroso!");
                break;
            case 3:
                stringBuilder.append("Increíble!");
                break;
        }
        return stringBuilder.toString();
    }
    private static String generateNewFriendDescription(String userAName, String userBName) {
        return userAName + " ahora es amigo de " + userBName + "!";
    }

    public static String getNaturalTime(double time) {
        StringBuilder sb = new StringBuilder();
        int horas = ((int) time / 3600);
        int minutos = (int) ((time - horas * 3600) / 60);
        int segundos = (int) (time - (horas * 3600 + minutos * 60));
        sb.append(horas < 10 ? "0" + horas : horas).append(":");
        sb.append(minutos < 10 ? "0" + minutos : minutos).append(":");
        sb.append(segundos < 10 ? "0" + segundos : segundos);
        return sb.toString();
        //Date date = new Date((long) time * 1000L);
        //SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        //return df.format(date);
    }

    public static Post createPostFromSession(Session session, String userUID, String imagePath){
        Post newPost = new Post();
        newPost.setType(0);
        newPost.setDate(new Date());
        newPost.setDescription(generateSessionDescription(session));
        newPost.setUserUID(userUID);
        newPost.setImagePath(imagePath);
        return newPost;
    }

    public static Post createNewFriendPost(String userUID, String userAName, String userBName) {
        Post newPost = new Post();
        newPost.setType(2);
        newPost.setDate(new Date());
        newPost.setDescription(generateNewFriendDescription(userAName, userBName));
        newPost.setUserUID(userUID);
        newPost.setImagePath("");
        return newPost;

    }

}
