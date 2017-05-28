package appstud.codingtest.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import appstud.codingtest.R;

/**
 * Created by yann on 28/05/17.
 */

public class PlaceDetectedAdapter extends ArrayAdapter<PlaceDetected>
{
    private List<PlaceDetected> placeDetectedList;
    private Context context;

    public PlaceDetectedAdapter(@NonNull Context context, @LayoutRes int resource, List<PlaceDetected> placeDetectedList)
    {
        super(context, resource);
        this.placeDetectedList = placeDetectedList;
        this.context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(R.layout.list, parent, false);
        } else {
            view = convertView;
        }


        if(placeDetectedList.size() >0) {
            PlaceDetected placeDetected = placeDetectedList.get(position);

            ImageView imageView = (ImageView) view.findViewById(R.id.icon);
            TextView textView = (TextView) view.findViewById(R.id.itemname);

            imageView.setImageBitmap(placeDetected.getImage());
            textView.setText(placeDetected.getPlace().getName().toString());
        }

        return view;
    }

    @Override
    public int getCount(){
        return this.placeDetectedList.size();
    }

}
