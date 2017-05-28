package appstud.codingtest;

import android.Manifest;
import android.content.Intent;
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
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.PlaceReport;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedList;
import java.util.List;

import appstud.codingtest.Callbacks.ResultCallbackImpl;
import appstud.codingtest.utils.PhotoTask;
import appstud.codingtest.utils.PlaceDetected;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, PlaceSelectionListener
{
    public static String TAG = "Appstud TAG";
    private static GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private BottomNavigationView bottomNavigationView;
    static LinkedList<PlaceDetected> placeDetecteds ;

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            Intent secondeActivite;
            switch (item.getItemId())
            {
                case R.id.navigation_map:
                    return true;
                case R.id.navigation_list:
                    secondeActivite = new Intent(MapActivity.this, ListActivity.class);
                    MapActivity.this.startActivity(secondeActivite);
                    return true;

            }
            return false;
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message inputMessage)
        {
            if (inputMessage.obj instanceof PlaceDetected)
            {
                PlaceDetected place = (PlaceDetected) inputMessage.obj ;
                placeDetecteds.add(place);
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

        this.placeDetecteds = new LinkedList<>();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                .addOnConnectionFailedListener(this)
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);

        this.bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        this.bottomNavigationView.setSelectedItemId(R.id.navigation_map);
        this.bottomNavigationView.setOnNavigationItemSelectedListener(this.navListener);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "permission to access position granted");
            mMap.setMyLocationEnabled(true);
            retrievePlaces();

        } else {
            Log.v(TAG, "Not permission to access position");
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorCode() + " : " + connectionResult.getErrorMessage());
    }

    //FOnction qui récupère les places sÀ partir de la position courante du user ou à partir des données entrées dans le champ texte
    private void retrievePlaces()
    {
        if(placeDetecteds == null )
            placeDetecteds = new LinkedList<>();

        if(placeDetecteds.size() == 0 )
        {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(this.mGoogleApiClient, null);
            ResultCallback callbalck = new ResultCallbackImpl(this.mGoogleApiClient, this.handler);
            result.setResultCallback(callbalck);
        }
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

    @Override
    public void onPlaceSelected(Place place)
    {
        placeDetecteds.clear();
        mMap.clear();
        Log.i(MapActivity.TAG, "Place Selected: " + place.getName());

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), (float)10)) ;
        mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(String.valueOf(place.getName())));


        LinkedList<String> restrict = new LinkedList<String>();
        restrict.add(place.getId());
        PlaceFilter placeFilter = new PlaceFilter(false,restrict);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        PendingResult<Status> status =  Places.PlaceDetectionApi.reportDeviceAtPlace(mGoogleApiClient, PlaceReport.create(place.getId(),"test"));

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, placeFilter);
        ResultCallback callbalck = new ResultCallbackImpl(this.mGoogleApiClient, this.handler);
        result.setResultCallback(callbalck);
    }

    @Override
    public void onError(Status status)
    {
        Log.e(MapActivity.TAG, status.toString());
    }

    public void onResume()
    {
        super.onResume();
        if(this.bottomNavigationView !=  null)
            this.bottomNavigationView.setSelectedItemId(R.id.navigation_map);

    }
}
