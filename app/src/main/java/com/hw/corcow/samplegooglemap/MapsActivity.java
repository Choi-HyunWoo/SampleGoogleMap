package com.hw.corcow.samplegooglemap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.jar.Manifest;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {         // FragmentActivity >> AppCompatActivity

    private GoogleMap mMap;
    LocationManager mLM;            // 지도 정보 얻어오려면 LocationManager !

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);                                             // activity_maps > activity_main (새로만든)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        /** 지도 띄우기 */
        /*
        // 배치 방식 1. 레이아웃으로 Map을 배치할 경우
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        */

        // 배치 방식 2. 코드로 Fragment 안에 Map을 배치한경우 (이렇게 Fragment를 처리하지 않으면 ERROR)
        SupportMapFragment smf;
        if (savedInstanceState == null) {
            smf = new SupportMapFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, smf, "map").commit();
        } else {
            smf = (SupportMapFragment)getSupportFragmentManager().findFragmentByTag("map");
        }
        smf.getMapAsync(this);
        // 이제 받은 MapAsync 에 MapReady callback을 호출하면 된다. (아래에 onMapReady() 있음)


        /** Location manager setting !*/
        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    Location cacheLocation;         // 캐싱한 위치
    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mMap != null) {
                moveMap(location.getLatitude(), location.getLongitude());           // location의 위도 경도 받아옴.
            } else {
                cacheLocation = location;                                           // 수집되었던 위치
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT > 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.requestSingleUpdate(LocationManager.GPS_PROVIDER, mListener, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT > 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.removeUpdates(mListener);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (cacheLocation != null) {
            moveMap(cacheLocation.getLatitude(), cacheLocation.getLongitude());
            cacheLocation = null;
        }
    }


    /** moveMap : 지도 이동 */
    private void moveMap(double lat, double lng) {  // 위도(latitude) , 경도(longtitude)
        CameraPosition.Builder builder = new CameraPosition.Builder();            // Google Map은 카메라가 지도를 내려다본다고 설계되어있음.
        builder.target(new LatLng(lat, lng));       // 타겟의 위도, 경도 정보
        builder.zoom(17);                           // 줌 정도
//        builder.bearing(30);                      // 회전 정도
//        builder.tilt(30);                         // 지도 기울기
        CameraPosition position = builder.build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
//        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10);
        mMap.moveCamera(update);
    }

}
