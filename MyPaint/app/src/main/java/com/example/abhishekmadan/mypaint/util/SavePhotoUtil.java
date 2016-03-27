package com.example.abhishekmadan.mypaint.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * Created by abhishek.madan on 2/20/2016.
 */
public class SavePhotoUtil {

    private Context mContext;
    private ContentResolver mResolver;
    private Bitmap mImageBitmap;
    private String mImageTitle;
    private String mImageDescription;
    private static String  sLastImageSavedName;
    private static Uri sLastImageUri;

    public SavePhotoUtil(Context mContext) {
        this.mContext = mContext;
    }

    public String saveToGallery(ContentResolver resolver, Bitmap bitmap, String title, String description) {
        mResolver = resolver;
        mImageBitmap = bitmap;
        mImageTitle = title;
        mImageDescription = description;

        Uri uri = null;
        String stringUri = null;


        if (mImageBitmap != null) {

            if(sLastImageSavedName==null||!sLastImageSavedName.equalsIgnoreCase(title)) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, mImageTitle);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, mImageTitle);
                values.put(MediaStore.Images.Media.DESCRIPTION, mImageDescription);
                values.put(MediaStore.Images.Media.MIME_TYPE, "images/jpeg");
                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.ORIENTATION, 0);
                values.put(MediaStore.Images.Media.HEIGHT, bitmap.getHeight());
                values.put(MediaStore.Images.Media.WIDTH, bitmap.getWidth());

                uri = mResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                sLastImageUri = uri;
                sLastImageSavedName = title;
            }else{
                uri = sLastImageUri;
            }
            OutputStream out = null;
            try {
                out = mResolver.openOutputStream(uri);
                mImageBitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (out != null)
                    try {
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            long id = ContentUris.parseId(uri);
            // Wait until MINI_KIND thumbnail is generated.
            Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(mResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            // This is for backward compatibility.
            storeThumbnail(mResolver, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
        }

        if (uri != null) {
            stringUri = uri.toString();
        }

        return stringUri;
    }


    private static final Bitmap storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id,
            float width,
            float height,
            int kind) {

        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        //createScaledBitmap
        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND, kind);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID, (int) id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.PNG, 100, thumbOut);
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

}
