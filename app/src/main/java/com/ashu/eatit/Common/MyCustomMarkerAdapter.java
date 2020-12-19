package com.ashu.eatit.Common;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.view.menu.MenuView;

import com.ashu.eatit.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MyCustomMarkerAdapter implements GoogleMap.InfoWindowAdapter {

    private View itemView;

    public MyCustomMarkerAdapter(LayoutInflater layoutInflater) {
        itemView = layoutInflater.inflate(R.layout.layout_marker_display, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView txt_shipper_name = itemView.findViewById(R.id.txt_shipper_name);
        TextView txt_shipper_info = itemView.findViewById(R.id.txt_shipper_info);
        txt_shipper_name.setText(marker.getTitle());
        txt_shipper_info.setText(marker.getSnippet());
        return itemView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
