package appstud.codingtest.Callbacks;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;

import appstud.codingtest.ListActivity;
import appstud.codingtest.MapActivity;
import appstud.codingtest.utils.PhotoTask;

/**
 * Callback pour répondre aux évenements de recherche de places autour d'un point donné
 * Created by yann on 28/05/17.
 */

public class ResultCallbackImpl implements ResultCallback
{

    private GoogleApiClient mGoogleApiClient;
    private Handler handler;

    /**
     * Création d'un ResultCallbackImpl
     * @param mGoogleApiClient Client google API avec leque les éventuelles photos des places détectées seront récupérées
     * @param handler Handler qui permetra aux Threads lancés de communiquer avec le Thread UI
     */
    public ResultCallbackImpl(GoogleApiClient mGoogleApiClient,  Handler handler) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.handler = handler;
    }

    @Override
    public void onResult(@NonNull Result result)
    {
        if (result instanceof PlaceLikelihoodBuffer && result.getStatus().isSuccess())
        {
            PlaceLikelihoodBuffer likelyPlaces = (PlaceLikelihoodBuffer) result;
            for (PlaceLikelihood placeLikelihood : likelyPlaces)
            {
                PhotoTask photoTask = new PhotoTask(mGoogleApiClient, placeLikelihood, handler);
                new Thread(photoTask).start();
            }
        }
        else
        {
            if(result.getStatus().isSuccess())
            {
                Log.e(MapActivity.TAG, "result must be instance of PlaceLikelihoodBuffer");
            }
            else{
                Log.e(MapActivity.TAG, "result status is failed");

            }
        }
    }
}
