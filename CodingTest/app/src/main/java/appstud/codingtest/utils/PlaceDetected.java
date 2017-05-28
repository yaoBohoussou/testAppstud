package appstud.codingtest.utils;

import android.graphics.Bitmap;

import com.google.android.gms.location.places.Place;

import java.io.Serializable;

/**
 * Created by yann on 28/05/17.
 */

public class PlaceDetected implements Serializable
{
    Place place;
    Bitmap image;

    public PlaceDetected(Place place, Bitmap image) {
        this.place = place;
        this.image = image;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
