package cs.inhatc.jinuk.tmapapitest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    //Permission Values
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    private TMapView tMapView ;
    private TMapData tMapData;
    private Bitmap bitmap;
    private TMapMarkerItem markerItem;
    private TMapGpsManager tmapgps;
    private PlaceAutocompleteFragment placeAutoComplete;
    private ImageView gpsi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout linearLayoutTmap = findViewById(R.id.linearLayoutTmap);
        tMapView = new TMapView(this);
        tMapData = new TMapData();
        markerItem = new TMapMarkerItem();

        //GPS 아이콘
        gpsi = findViewById(R.id.gps_icon);
        gpsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmapgps = new TMapGpsManager(MainActivity.this);
                tmapgps.setMinTime(1000);
                tmapgps.setMinDistance(5);
                tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);
                tmapgps.setProvider(tmapgps.GPS_PROVIDER);

                /* 현위치 아이콘표시 */
                tMapView.setIconVisibility(true);

                /*  화면중심을 단말의 현재위치로 이동 */
                tMapView.setTrackingMode(true);
                tMapView.setSightVisible(true);
            }
        });

        // 마커 아이콘
        bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.pin_icon);

        tMapView.setSKTMapApiKey(getString(R.string.tmap_api_key));
        linearLayoutTmap.addView(tMapView);

        //Google 검색
        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d("Maps", "Place selected: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });

        // 클릭 이벤트 설정
        tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint, PointF pointF) {
                //Toast.makeText(getApplicationContext(), "tMapPoint : " + tMapPoint, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint, PointF pointF) {
                //Toast.makeText(getApplicationContext(), "onPressUp~!", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // 롱 클릭 이벤트 설정
        tMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint) {
                //Toast.makeText(getApplicationContext(), "onLongPress~!", Toast.LENGTH_SHORT).show();

                markerItem.setIcon(bitmap); // 마커 아이콘 지정
                markerItem.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                markerItem.setTMapPoint( tMapPoint ); // 마커의 좌표 지정
                //markerItem.setName("SKT타워"); // 마커의 타이틀 지정
                tMapView.addMarkerItem("markerItem", markerItem); // 지도에 마커 추가

                tMapData.convertGpsToAddress(tMapPoint.getLatitude(), tMapPoint.getLongitude(),
                        new TMapData.ConvertGPSToAddressListenerCallback() {
                            @Override
                            public void onConvertToGPSToAddress(String strAddress) {
                                //Toast.makeText(getApplicationContext(),"선택한 위치의 주소는 " + strAddress, Toast.LENGTH_SHORT).show();

                                // Talk View 에 글 지정
                                markerItem.setCalloutTitle(strAddress.toString());
                                markerItem.setCanShowCallout(true);
                                markerItem.setAutoCalloutVisible(true);
                            }
                        });
            }
        });

        // 지도 스크롤 종료
        /*tMapView.setOnDisableScrollWithZoomLevelListener(new TMapView.OnDisableScrollWithZoomLevelCallback() {
            @Override
            public void onDisableScrollWithZoomLevelEvent(float zoom, TMapPoint centerPoint) {
                Toast.makeText(getApplicationContext(), "zoomLevel=" + zoom + "\nlon=" + centerPoint.getLongitude() + "\nlat=" + centerPoint.getLatitude(), Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    // 권한 결과 요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    // 내 위치 찾기
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }
}
