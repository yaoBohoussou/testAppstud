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

/**
 *
 * Created by yann on 28/05/17.
 */

public class PhotoTask implements Runnable
{
    private GoogleApiClient mGoogleApiClient;
    private PlaceLikelihood placeLikelihood;
    private Handler handler;

    /**
     * Création d'une place détectée
     * @param mGoogleApiClient Client google API avec leque les éventuelles photos des places détectées seront récupérées
     * @param placeLikelihood Carratéristiques de la place détectées. Cet objet est utilisé pour obtenir une photo si pssible du lieu
     * @param handler Handler qui permetra Pour communiquer Les places détectées (PlaceDetected) au Thread UI
     */
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
            Message completeMessage = handler.obtainMessage(0, placeDetected);
            completeMessage.sendToTarget();


        }
        //Cas ou la place traitée n'a pas de photos disponibles
        else
        {

            Bitmap result = Bitmap.createBitmap(60, 60, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            canvas.drawColor(Color.WHITE);

            PlaceDetected placeDetected = new PlaceDetected(placeLikelihood.getPlace(),result);
            Message completeMessage = handler.obtainMessage(1, placeDetected);
            completeMessage.sendToTarget();

        }
        photoMetadataBuffer.release();
    }
}
