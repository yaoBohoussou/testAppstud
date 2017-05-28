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
 * Created by yann on 28/05/17.
 */

public class ResultCallbackImpl implements ResultCallback
{

    private GoogleApiClient mGoogleApiClient;
    private Boolean fromMapActivity;
    Handler handler;

    public ResultCallbackImpl(GoogleApiClient mGoogleApiClient, Boolean fromMapActivity, Handler handler) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.fromMapActivity = fromMapActivity;
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
                /*PhotoTask photoTask;
                if(this.fromMapActivity) {
                    photoTask = new PhotoTask(mGoogleApiClient, placeLikelihood, handler);
                }
                else
                {
                    photoTask = new PhotoTask(mGoogleApiClient, placeLikelihood, handler);
                }*/
                PhotoTask photoTask = new PhotoTask(mGoogleApiClient, placeLikelihood, handler);
                new Thread(photoTask).start();
            }
            //likelyPlaces.release();
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
