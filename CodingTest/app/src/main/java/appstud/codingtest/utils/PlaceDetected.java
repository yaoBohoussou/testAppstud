package appstud.codingtest.utils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;

/**
 * Description d'une place détectée
 * Created by yann on 28/05/17.
 */

public class PlaceDetected implements Serializable
{
    private Place place;
    transient private Bitmap image;


    /**
     * Création d'une place détectée
     * @param place Détails de la place
     * @param image Image de la place / recevra une image de couleur blanche si aucune image n'est reçue
     */
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
