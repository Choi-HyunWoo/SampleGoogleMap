package com.hw.corcow.samplegooglemap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

/**
 * Created by Tacademy on 2015-10-26.
 */
public class MyInfoWindow implements GoogleMap.InfoWindowAdapter {
    View infoWindow;
    TextView titleView, snippetView;
    Map<Marker, MyPOI> mPOIResolver;

    public MyInfoWindow (Context context, Map<Marker,MyPOI> poiResolver) {
        infoWindow = LayoutInflater.from(context).inflate(R.layout.view_info_window, null);
        titleView = (TextView)infoWindow.findViewById(R.id.text_title);
        snippetView=(TextView)infoWindow.findViewById(R.id.text_snippet);
        mPOIResolver = poiResolver;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        MyPOI poi = mPOIResolver.get(marker);
        titleView.setText(poi.title);
        snippetView.setText(poi.snippet);
        return null;
    }
}
