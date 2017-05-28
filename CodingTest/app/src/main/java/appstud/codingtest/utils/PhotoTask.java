package appstud.codingtest.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import java.util.Iterator;

import appstud.codingtest.MapActivity;

/**
 * Created by yann on 28/05/17.
 */

public class PhotoTask implements Runnable
{
    private GoogleApiClient mGoogleApiClient;
    private PlaceLikelihood placeLikelihood;
    //private Bitmap result;
    private Handler handler;

    public PhotoTask(GoogleApiClient mGoogleApiClient, PlaceLikelihood placeLikelihood, Handler handler) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.placeLikelihood = placeLikelihood;
        this.handler = handler;
    }

    @Override
    public void run()
    {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        PlacePhotoMetadataResult photoMetadataResult = Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, placeLikelihood.getPlace().getId()).await();
        PlacePhotoMetadataBuffer photoMetadataBuffer = photoMetadataResult.getPhotoMetadata();
        Iterator<PlacePhotoMetadata> iterator = photoMetadataBuffer.iterator();
        if (iterator.hasNext())
        {
            //On récupère la première photo
            PlacePhotoMetadata photo = iterator.next();
            PlacePhotoResult photoResult = photo.getPhoto(mGoogleApiClient).await();
            Bitmap result = photoResult.getBitmap();

            PlaceDetected placeDetected = new PlaceDetected(placeLikelihood.getPlace(),result);
            Message completeMessage = MapActivity.handler.obtainMessage(0, placeDetected);
            completeMessage.sendToTarget();


        }
        //Cas ou la place traitée n'a pas de photos disponibles
        else
        {

            Bitmap result = Bitmap.createBitmap(60, 60, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            canvas.drawColor(Color.WHITE);

            PlaceDetected placeDetected = new PlaceDetected(placeLikelihood.getPlace(),result);
            Message completeMessage = MapActivity.handler.obtainMessage(1, placeDetected);
            completeMessage.sendToTarget();

        }
        photoMetadataBuffer.release();
    }
}
