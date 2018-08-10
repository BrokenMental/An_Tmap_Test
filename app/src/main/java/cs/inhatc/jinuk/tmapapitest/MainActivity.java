package cs.inhatc.jinuk.tmapapitest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TMapPoint tMapPoint1 = new TMapPoint(37.4588197, 126.63411719999999); // 인하대병원
    TMapPoint tMapPoint2 = new TMapPoint(37.4500221, 126.65348799999992); // 인하대
    TMapPoint tMapPoint3 = new TMapPoint(37.4480158, 126.65750409999998); // 인하공전
    TMapPoint tMapPoint4 = new TMapPoint(37.441546, 126.70149600000002); // 인천시외버스터미널
    TMapPoint tMapPoint5 = new TMapPoint(37.4565562, 126.68458069999997); // 주안역

    private TMapView tMapView;
    private TMapData tMapData;
    private Bitmap bitmap;
    private TMapMarkerItem markerItem;
    private PlaceAutocompleteFragment placeAutoComplete;
    private ImageView gpsi;
    private PermissionCall PC;
    private boolean m_bTrackingMode;
    private ArrayList passList;

    //뒤로가기 버튼 Delay
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout linearLayoutTmap = findViewById(R.id.linearLayoutTmap);

        // TMap 지도 생성 관련 변수
        tMapView = new TMapView(this);
        // TMap에서 사용되는 데이터 관련 변수
        tMapData = new TMapData();

        // 마커 관련 변수
        markerItem = new TMapMarkerItem();

        // 취소버튼 관련 변수
        m_bTrackingMode = true;

        // Permission 확인
        PC = new PermissionCall(this);

        // 경유지 추가를 위한 ArrayList
        passList = new ArrayList<>();
        passList.add(tMapPoint2);
        passList.add(tMapPoint3);
        passList.add(tMapPoint4);

        // GPS 아이콘 생성
        gpsi = findViewById(R.id.gps_icon);

        // GPS 아이콘 클릭 시 이벤트
        gpsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 권한 확인
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
                    }
                    return;
                }
                setGps(); // 현재위치 찾기
                Toast.makeText(getApplicationContext(), "현재위치를 찾는 중입니다. GPS를 켜주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 마커 아이콘
        bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.pin_icon);

        // 지도 생성
        tMapView.setSKTMapApiKey(getString(R.string.tmap_api_key));
        linearLayoutTmap.addView(tMapView);

        tMapView.setCenterPoint(126.63411719999999,37.4588197);

        // 경로 검색 이벤트
        //findPathCarOne();
        //findPathType(); // 출발지, 도착지의 마커가 표시된다.
        findPathDataWithType();

        //Google 검색
        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d("Maps", "장소 이름: " + place.getName() + "/ 주소 : " + place.getAddress().toString() + "/ 위&경도 : " + place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });

        // 숏 클릭 이벤트 설정
        /*tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
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
        });*/

        // 롱 클릭 이벤트 설정
        tMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint) {
                //Toast.makeText(getApplicationContext(), "onLongPress~!", Toast.LENGTH_SHORT).show();

                markerItem.setIcon(bitmap); // 마커 아이콘 지정
                markerItem.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                markerItem.setTMapPoint(tMapPoint); // 마커의 좌표 지정
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
                                Log.d("Maps", "클릭 위치: " + strAddress.toString());
                            }
                        });
            }
        });

        // 지도 드래그 종료
        /*tMapView.setOnDisableScrollWithZoomLevelListener(new TMapView.OnDisableScrollWithZoomLevelCallback() {
            @Override
            public void onDisableScrollWithZoomLevelEvent(float zoom, TMapPoint centerPoint) {
                Toast.makeText(getApplicationContext(), "zoomLevel=" + zoom + "\nlon=" + centerPoint.getLongitude() + "\nlat=" + centerPoint.getLatitude(), Toast.LENGTH_SHORT).show();
            }
        });*/

    }

    // 이전버튼 이벤트
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(this, "이전 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 현재위치 관련 메서드
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                /* 해당 위치에 포인트 찍기 */
                tMapView.setLocationPoint(longitude, latitude);

                /* 해당 위치의 중앙으로 화면 이동 */
                tMapView.setCenterPoint(longitude, latitude);

                /* 현재 보는 방향표시 */
                tMapView.setCompassMode(true);

                /* 현위치 아이콘표시 */
                tMapView.setIconVisibility(true);
            }

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    // GPS 얻어오기 메서드
    public void setGps() {
        final LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        }
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자(실내에선 NETWORK_PROVIDER 권장)
                1000, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
    }

    // Type별 경로 거리
    public void findPathAllType() {
        tMapData.findPathDataAllType(TMapData.TMapPathType.CAR_PATH, tMapPoint1, tMapPoint5, new TMapData.FindPathDataAllListenerCallback() {
            @Override
            public void onFindPathDataAll(Document document) {
                Element root = document.getDocumentElement();
                NodeList nodeListPlacemark = root.getElementsByTagName("Document");
                for (int i = 0; i < nodeListPlacemark.getLength(); i++) {
                    NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();
                    for (int j = 0; j < nodeListPlacemarkItem.getLength(); j++) {
                        if (nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:totalDistance")) {
                            Log.d("Distance", nodeListPlacemarkItem.item(j).getTextContent().trim()+"M"); // 총 거리 표시(M)
                        }else if (nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:totalTime")) {
                            int TM = Integer.parseInt(nodeListPlacemarkItem.item(j).getTextContent().trim())/60;
                            int TS = Integer.parseInt(nodeListPlacemarkItem.item(j).getTextContent().trim())%60;
                            Log.d("Time", TM+" min"+TS+" sec"); // 총 시간 표시(초)
                        }else if (nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:totalFare")) {
                            Log.d("Money", nodeListPlacemarkItem.item(j).getTextContent().trim()+" Won"); // 총 요금 표시
                        }
                    }
                }
            }
        });
    }

    // 단일 자동차 경로찾기
    public void findPathCarOne() {
        try {
            tMapData.findPathData(tMapPoint1, tMapPoint5, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine tMapPolyLine) {
                    tMapPolyLine.setLineColor(Color.BLUE);
                    tMapPolyLine.setLineWidth(2);
                    tMapView.addTMapPolyLine("Line", tMapPolyLine);
                }
            });
            findPathAllType();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Type별 경로찾기
    /*
    TMapPathType.CAR_PATH - 자동차 경로 Type
    TMapPathType.PEDESTRIAN_PATH - 보행자 경로 Type
    */
    public void findPathType() {
        tMapData.findPathDataWithType(TMapData.TMapPathType.CAR_PATH, tMapPoint1, tMapPoint5, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                polyLine.setLineColor(Color.BLUE);
                polyLine.setLineWidth(10);
                tMapView.addTMapPath(polyLine);
            }
        });
        findPathAllType();
    }

    // 경유지를 추가하는 Type별 경로찾기
    /*
    passList - 경유지에 대한 좌표

    searchOption - 경로 탐색 옵션(번호)
    0: 교통최적+추천(기본값)
	1: 교통최적+무료우선
	2: 교통최적+최소시간
	3: 교통최적+초보
	4: 교통최적+고속도로우선
	10: 최단
	12: 교통최적 + 일반도로우선
    */
    public void findPathDataWithType() {
        tMapData.findPathDataWithType(TMapData.TMapPathType.CAR_PATH, tMapPoint1, tMapPoint5, passList, 0,
        new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                polyLine.setLineColor(Color.GREEN);
                polyLine.setLineWidth(10);
                tMapView.addTMapPath(polyLine);
            }
        });
    }

    // 이거 뭐지...
    /*public void findPathCarAll() {
        tMapData.findPathDataAll(tMapPointStart, tMapPointEnd, new TMapData.FindPathDataAllListenerCallback() {
            @Override
            public void onFindPathDataAll(Document doc) {

            }
        });
    }*/
}

