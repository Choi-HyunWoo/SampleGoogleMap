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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,                              // FragmentActivity >> AppCompatActivity
        GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapClickListener {    // Map 요소의 클릭 이벤트를 받을 Listener들 implements

    private GoogleMap mMap;
    LocationManager mLM;            // 지도 정보 얻어오려면 LocationManager !

    /** Marker Info에 대한 자료구조 (Marker 들의 정보 관리를 위한 새로운 자료구조 정의 ; HashMap) */
    final Map<MyPOI, Marker> mMarkerResolver = new HashMap<MyPOI, Marker>();    // 얘를 왜만드는가? : Marker를 눌렀을 때 해당 표시된 위치에 대한 데이터를 얻기 위해
    final Map<Marker, MyPOI> mPOIResolver = new HashMap<Marker, MyPOI>();       // POI도 같이 만들어주자.

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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
        smf.getMapAsync(this);      // 이제 받은 MapAsync 에 MapReady callback을 호출하면 된다. (아래에 onMapReady() 있음)

        /** 위치를 알기 위한 LocationManager setting !*/
        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        /** + Marker 추가 */
        Button btn = (Button)findViewById(R.id.btn_marker);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MarkerOptions options = new MarkerOptions();
                CameraPosition position = mMap.getCameraPosition();
                options.position(position.target);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));         // Default Marker를 그린다.
                options.anchor(0.5f, 1);                // pivot
                options.title("My Marker");             // title
                options.snippet("Marker Test...");      // text message
                options.draggable(true);

                /** T map은 marker option에 ID를 붙여서 관리. >> 얘는 ID가 없어서 Marker를 관리하기 위해선 새로운 자료구조를 만들어야함
                 * 관리할 Marker의 정보를 담을 MyPOI 클래스를 선언하고, MyPOI 객체에 관리할 옵션을 담음. */
                MyPOI poi = new MyPOI();
                poi.title = "My Marker";             // title
                poi.snippet = "Marker Test...";      // text message

                // mMap.addMarker(options);             // option을 정한 Marker를 Map에 추가
                Marker m = mMap.addMarker(options);     // 그냥 추가하지 말고 return된 Marker를 담아서 관리하자.

                // Resolver의 역할은??>????
                mMarkerResolver.put(poi, m);
                /** Option 에 추가로 POI 객체가 생겼으므로 POIResolver 클래스도 선언해주자 */
                mPOIResolver.put(m, poi);
            }
        });
    }

    Location cacheLocation;         // cache 한 위치
    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mMap != null) {
                moveMap(location.getLatitude(), location.getLongitude());       // location의 위도 경도 받아옴.
            } else {
                cacheLocation = location;                                       // 수집되었던 위치
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

        /** 여러 옵션들을 켜고 끄기 가능 */
        mMap.setMyLocationEnabled(true);

        mMap.setIndoorEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        /** Map에 Listener를 할당해준다. */
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(this);

        // 캐싱된 Location이 있다면 >> 그 위치를 보여라
        if (cacheLocation != null) {
            moveMap(cacheLocation.getLatitude(), cacheLocation.getLongitude());
            cacheLocation = null;
        }
    }

    /**
     * Click Listeners 구현
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "infowindow : " + marker.getTitle(), Toast.LENGTH_SHORT).show();
        marker.hideInfoWindow();
    }
    @Override
    public void onMapClick(LatLng latLng) {

    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        MyPOI poi = mPOIResolver.get(marker);
        Toast.makeText(this, "title : " + poi.title , Toast.LENGTH_SHORT).show();
        marker.showInfoWindow();
        return true;
    }

    /** moveMap : 지도 이동 */
    private void moveMap(double lat, double lng) {  // 위도(latitude) , 경도(longtitude)
        CameraPosition.Builder builder = new CameraPosition.Builder();            // Google Map은 카메라가 지도를 내려다본다고 설계되어있음.
        builder.target(new LatLng(lat, lng));       // 타겟의 위도, 경도 정보
        builder.zoom(16);                           // 줌 정도
//        builder.bearing(30);                      // 회전 정도
//        builder.tilt(30);                         // 지도 기울기
        CameraPosition position = builder.build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
//        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10);
        mMap.moveCamera(update);
    }

}
