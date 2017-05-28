package appstud.codingtest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import appstud.codingtest.Callbacks.ResultCallbackImpl;
import appstud.codingtest.utils.PlaceDetected;
import appstud.codingtest.utils.PlaceDetectedAdapter;

public class ListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, SwipeRefreshLayout.OnRefreshListener
{

    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            Intent secondeActivite;
            switch (item.getItemId())
            {
                case R.id.navigation_map:
                    secondeActivite = new Intent(ListActivity.this, MapActivity.class);
                    ListActivity.this.startActivity(secondeActivite);
                    return true;
                case R.id.navigation_list:
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
            swipeRefreshLayout.setRefreshing(true);
            if (inputMessage.obj instanceof PlaceDetected)
            {
                PlaceDetected place = (PlaceDetected) inputMessage.obj ;
                MapActivity.placeDetecteds.add(place);
                ((PlaceDetectedAdapter)listView.getAdapter()).notifyDataSetChanged();
            }
            else
            {
                Log.e(MapActivity.TAG, "Not a PlaceDetected");
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        this.listView = (ListView) findViewById(R.id.list);
        this.listView.setAdapter(new PlaceDetectedAdapter(this, R.layout.list, MapActivity.placeDetecteds));

        this.swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content);
        this.swipeRefreshLayout.setOnRefreshListener(this);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_list);
        bottomNavigationView.setOnNavigationItemSelectedListener(this.navListener);
    }

    @Override
    public void onRefresh()
    {
        MapActivity.placeDetecteds.clear();
        retrievePlaces();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(MapActivity.TAG, connectionResult.getErrorCode() + " : " + connectionResult.getErrorMessage());
    }


    private void retrievePlaces()
    {
        GoogleApiClient mGoogleApiClient =new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                .addOnConnectionFailedListener(this)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        ResultCallback callbalck = new ResultCallbackImpl(mGoogleApiClient,false, this.handler);
        result.setResultCallback(callbalck);

    }
}
