package appstud.codingtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import appstud.codingtest.Callbacks.ResultCallbackImpl;
import appstud.codingtest.utils.PhotoTask;
import appstud.codingtest.utils.PlaceDetected;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    public static String TAG = "Appstud TAG";
    private static GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static Handler handler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message inputMessage)
        {
            if (inputMessage.obj instanceof PlaceDetected)
            {
                PlaceDetected place = (PlaceDetected) inputMessage.obj ;
                switch (inputMessage.what) {
                    case 0:
                        Log.v(MapActivity.TAG, "Sucess 0");
                        // Superposition de deux marqueurs L'un étant un disque noir et l'autre une photo de la place .
                        Bitmap image = getCroppedBitmap(Bitmap.createScaledBitmap(place.getImage(), 60, 60, false));

                        // Superposition de deux marqueurs L'un étant un disque noir et l'autre une photo de la place .
                        mMap.addMarker(new MarkerOptions()
                                .position(place.getPlace().getLatLng())
                                .icon(BitmapDescriptorFactory.fromBitmap(getBlackCircle(image)))
                        );
                        mMap.addMarker(new MarkerOptions()
                                .position(place.getPlace().getLatLng())
                                .title(String.valueOf(place.getPlace().getName()))
                                .icon(BitmapDescriptorFactory.fromBitmap(image))
                        );
                        break;
                    case 1:
                        Log.v(MapActivity.TAG, "Sucess 1");
                        mMap.addMarker(new MarkerOptions().position(place.getPlace().getLatLng()).title(String.valueOf(place.getPlace().getName())));

                        break;
                    default:
                        super.handleMessage(inputMessage);
                }
            }
            else
            {
                Log.e(MapActivity.TAG, "Not a PlaceDetected");
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                .addOnConnectionFailedListener(this)
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "permission to access position granted");
            mMap.setMyLocationEnabled(true);
            //LocationServices.FusedLocationApi.
            retrievePlaces();

        } else {
            Log.v(TAG, "Not permission to access position");
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorCode() + " : " + connectionResult.getErrorMessage());
    }

    private void retrievePlaces()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(this.mGoogleApiClient, null);
        ResultCallback callbalck = new ResultCallbackImpl(this.mMap, this.mGoogleApiClient);
        result.setResultCallback(callbalck);

    }

    /**
     * Prend une image au format bitmap et retourne la même image mais sous la forme d'un cercle
     * @param bitmap image qui sera redimensionée
     */
    static Bitmap getCroppedBitmap(Bitmap bitmap)
    {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 9, 9, 9);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * Prend une image au format bitmap et retourne une image circulaire noire ayant les dimensions de l'image passée en paramètre +15 de chaque côté
     * @param bitmap image qui sera redimensionée
     */
    static Bitmap getBlackCircle(Bitmap bitmap)
    {
        Bitmap blackCircle = Bitmap.createBitmap(bitmap.getWidth()+15, bitmap.getHeight()+15, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(blackCircle);

        Paint p = new Paint();
        RectF rectf = new RectF(0, 0, blackCircle.getWidth(), blackCircle.getHeight());
        p.setColor(Color.BLACK);
        cv.drawOval(rectf,p);

        return blackCircle;
    }
}
