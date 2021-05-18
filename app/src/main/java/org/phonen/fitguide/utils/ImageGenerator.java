package org.phonen.fitguide.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class ImageGenerator {
    public static byte[] bytesFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
}
