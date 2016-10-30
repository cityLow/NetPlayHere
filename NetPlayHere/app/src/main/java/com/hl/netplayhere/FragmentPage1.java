package com.hl.netplayhere;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.TraceLocation;
import com.hl.netplayhere.activity.NavigationActivity;
import com.hl.netplayhere.adapter.SpotAdapter;
import com.hl.netplayhere.bean.HotSpot;
import com.hl.netplayhere.util.Constant;
import com.hl.netplayhere.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


public class FragmentPage1 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    MapView mMapView;
    BaiduMap mBaiduMap;
    LocationClient mLocClient;
    PoiSearch mPoiSearch = PoiSearch.newInstance();
    ListView mListView;
    SpotAdapter mSpotAdapter;
    LatLng mLatLng;
    boolean flag;


    protected static MapStatusUpdate msUpdate = null;
    /**
     * Entity监听器
     */
    private static OnEntityListener entityListener = null;
    /**
     * 图标
     */
    private static BitmapDescriptor realtimeBitmap;

    private static Overlay overlay = null;

    // 覆盖物
    protected static OverlayOptions overlayOptions;

    // 路线覆盖物
    private static PolylineOptions polyline = null;

    private static List<LatLng> pointList = new ArrayList<LatLng>();

    private Intent serviceIntent = null;

    /**
     * 刷新地图线程(获取实时点)
     */
    protected RefreshThread refreshThread = null;
    protected static boolean isInUploadFragment = true;

    private static boolean isRegister = false;

    protected static PowerManager pm = null;

    protected static PowerManager.WakeLock wakeLock = null;

    private TrackReceiver trackReceiver = new TrackReceiver();

    private boolean isTraceStarted = false;

    private MyApplication trackApp;

    public FragmentPage1() {
        // Required empty public constructor
    }

    public static FragmentPage1 newInstance(MyApplication myApplication) {
        FragmentPage1 fragment = new FragmentPage1();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        flag = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page1, container, false);
        mMapView = (MapView) view.findViewById(R.id.bmapView);
        mListView = (ListView) view.findViewById(R.id.hotSpotLv);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("yjm", "onActivityCreated");
        flag = true;
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(getActivity().getApplicationContext());
        mLocClient.registerLocationListener(new MyLocationListener());
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        //拉取热门景点
        BmobQuery<HotSpot> query = new BmobQuery<>();
        query.findObjects(new FindListener<HotSpot>() {
            @Override
            public void done(List<HotSpot> list, BmobException e) {
                if (getContext() != null) {
                    mSpotAdapter = new SpotAdapter(getContext(), list);
                    mListView.setAdapter(mSpotAdapter);
                }
            }
        });
        initNavi();

        trackApp = (MyApplication) getActivity().getApplication();
        trackApp.initBmap(mMapView);
        initOnEntityListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("yjm", "page1 onDestroy");
        // 退出时销毁定位
        if (mLocClient != null)
            mLocClient.stop();
        // 关闭定位图层
        if (mBaiduMap != null) {
            mBaiduMap.setMyLocationEnabled(false);
        }
        if (mMapView != null) {
            mMapView.onDestroy();
            mMapView = null;
        }
        if (mPoiSearch != null)
            mPoiSearch.destroy();
        flag = false;
        startRefreshThread(false);
        MonitorService.isCheck = false;
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            LatLng latLng = new LatLng(location.getLatitude(),
                    location.getLongitude());
            mLatLng = latLng;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (Constant.isMapNeedReload) {
                Constant.isMapNeedReload = false;
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(latLng).zoom(16.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }

    public void searchNearby() {
        if (mLatLng == null)
            return;
        OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
            public void onGetPoiResult(PoiResult result) {
                //获取POI检索结果
                if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    Log.d("yjm PoiResult", "无结果");
                    Toast.makeText(getContext(), "方圆" + Constant.RADIUS + "米之内没有景点", Toast.LENGTH_LONG).show();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    mBaiduMap.clear();
                    PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
                    //设置overlay可以处理标注点击事件
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    //设置PoiOverlay数据
                    overlay.setData(result);
                    //添加PoiOverlay到地图中
                    overlay.addToMap();
                    overlay.zoomToSpan();
                }
            }

            public void onGetPoiDetailResult(PoiDetailResult result) {
                //获取Place详情页检索结果
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        };
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        mPoiSearch.searchNearby(new PoiNearbySearchOption().keyword("景点")
                .pageNum(10).radius(Constant.RADIUS).location(mLatLng).
                        sortType(PoiSortType.comprehensive));
    }


    private class MyPoiOverlay extends PoiOverlay {
        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            final PoiInfo poiInfo = getPoiResult().getAllPoi().get(index);
            Log.d("yjm", "marker click: " + poiInfo.name + ",,," + poiInfo.address);
            //Toast.makeText(getContext(), "景点名称: " + poiInfo.name + "\n地址: " + poiInfo.address, Toast.LENGTH_LONG).show();
            new AlertDialog.Builder(getContext()).setTitle(poiInfo.name)
                    .setMessage(poiInfo.address).setPositiveButton("去这儿", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(BaiduNaviManager.isNaviInited()){
                        routeplanToNavi(mLatLng, poiInfo.location, poiInfo.name);
                    }
                }
            }).setNegativeButton("取消", null).show();
            return true;
        }
    }

    private void initNavi() {
        BaiduNaviManager.getInstance().init(getActivity(), Utils.getSdcardDir(), "NetPlayHere", new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int i, String s) {
                String authinfo;
                if (0 == i) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败, " + s;
                }
                Log.d("navi init", authinfo);
            }

            @Override
            public void initStart() {

            }

            @Override
            public void initSuccess() {

            }

            @Override
            public void initFailed() {

            }
        }, null, null, null);
    }

    private void routeplanToNavi(LatLng start, LatLng end, String spotName) {
        BNRoutePlanNode sNode = new BNRoutePlanNode(start.longitude, start.latitude, "我的位置", null, BNRoutePlanNode.CoordinateType.WGS84);
        BNRoutePlanNode eNode = new BNRoutePlanNode(end.longitude, end.latitude, spotName, null, BNRoutePlanNode.CoordinateType.WGS84);
        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(sNode);
        list.add(eNode);
        BaiduNaviManager.getInstance().launchNavigator(getActivity(), list, 1, true, new DemoRoutePlanListener(sNode));
    }

    public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
            Intent intent = new Intent(getContext(), NavigationActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constant.ROUTE_PLAN_NODE, mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        @Override
        public void onRoutePlanFailed() {
            // TODO Auto-generated method stub
            Toast.makeText(getContext(), "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示实时轨迹
     *
     * @param location
     */
    protected void showRealtimeTrack(TraceLocation location) {

        if (null == refreshThread || !refreshThread.refresh) {
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
//            mHandler.obtainMessage(-1, "当前查询无轨迹点").sendToTarget();
            Toast.makeText(getContext(), "当前查询无轨迹点", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(latitude, longitude);
            if (1 == location.getCoordType()) {
                LatLng sourceLatLng = latLng;
                CoordinateConverter converter = new
                        CoordinateConverter();
                converter.from(CoordinateConverter.CoordType.GPS);
                converter.coord(sourceLatLng);
                latLng = converter.convert();
            }
            pointList.add(latLng);

            if (isInUploadFragment) {
                // 绘制实时点
                drawRealtimePoint(latLng);
            }
        }
    }

    /**
     * 绘制实时点
     *
     * @param point
     */
    private void drawRealtimePoint(LatLng point) {

        if (null != overlay) {
            overlay.remove();
        }

        MapStatus mMapStatus = new MapStatus.Builder().target(point).zoom(19).build();

        msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

        if (null == realtimeBitmap) {
            realtimeBitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_geo);
        }

        overlayOptions = new MarkerOptions().position(point)
                .icon(realtimeBitmap).zIndex(9).draggable(true);

        if (pointList.size() >= 2 && pointList.size() <= 10000) {
            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.BLUE).points(pointList);
        }

        addMarker();

    }

    /**
     * 添加地图覆盖物
     */
    protected void addMarker() {

        if (null != msUpdate) {
            trackApp.getmBaiduMap().setMapStatus(msUpdate);
        }

        // 路线覆盖物
        if (null != polyline) {
            trackApp.getmBaiduMap().addOverlay(polyline);
        }

        // 实时点覆盖物
        if (null != overlayOptions) {
            overlay = trackApp.getmBaiduMap().addOverlay(overlayOptions);
        }

    }

    public void startRefreshThread(boolean isStart) {
        if (null == refreshThread) {
            refreshThread = new RefreshThread();
        }
        refreshThread.refresh = isStart;
        if (isStart) {
            if (!refreshThread.isAlive()) {
                refreshThread.start();
            }
        } else {
            refreshThread = null;
        }
    }

    protected class RefreshThread extends Thread {

        protected boolean refresh = true;

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Looper.prepare();
            while (refresh) {
                // 轨迹服务开启成功后，调用queryEntityList()查询最新轨迹；
                // 未开启轨迹服务时，调用queryRealtimeLoc()进行实时定位。
                if (isTraceStarted) {
                    queryEntityList();
                } else {
                    queryRealtimeLoc();
                }

                try {
                    Thread.sleep(Constant.gatherInterval * 1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    System.out.println("线程休眠失败");
                }
            }
            Looper.loop();
        }
    }

    /**
     * 查询entityList
     */
    private void queryEntityList() {
        // entity标识列表（多个entityName，以英文逗号"," 分割）
        String entityNames = trackApp.getEntityName();
        // 属性名称（格式为 : "key1=value1,key2=value2,....."）
        String columnKey = "";
        // 返回结果的类型（0 : 返回全部结果，1 : 只返回entityName的列表）
        int returnType = 0;
        // 活跃时间（指定该字段时，返回从该时间点之后仍有位置变动的entity的实时点集合）
        int activeTime = (int) (System.currentTimeMillis() / 1000 - Constant.packInterval);
        // 分页大小
        int pageSize = 10;
        // 分页索引
        int pageIndex = 1;

        trackApp.getClient().queryEntityList(trackApp.getServiceId(), entityNames, columnKey, returnType, activeTime,
                pageSize,
                pageIndex, entityListener);
    }

    /**
     * 查询实时轨迹
     */
    private void queryRealtimeLoc() {
        trackApp.getClient().queryRealtimeLoc(trackApp.getServiceId(), entityListener);
    }

    /**
     * 初始化OnEntityListener
     */
    private void initOnEntityListener() {
        entityListener = new OnEntityListener() {

            // 请求失败回调接口
            @Override
            public void onRequestFailedCallback(String arg0) {
                // TODO Auto-generated method stub
                trackApp.getmHandler().obtainMessage(0, "entity请求失败回调接口消息 : " + arg0).sendToTarget();
            }

            // 添加entity回调接口
            public void onAddEntityCallback(String arg0) {
                // TODO Auto-generated method stub
                trackApp.getmHandler().obtainMessage(0, "添加entity回调接口消息 : " + arg0).sendToTarget();
            }

            // 查询entity列表回调接口
            @Override
            public void onQueryEntityListCallback(String message) {
                // TODO Auto-generated method stub
                Log.d("yjm trace", "onQueryEntityListCallback " + message);
                TraceLocation entityLocation = new TraceLocation();
                try {
                    JSONObject dataJson = new JSONObject(message);
                    if (null != dataJson && dataJson.has("status") && dataJson.getInt("status") == 0
                            && dataJson.has("size") && dataJson.getInt("size") > 0) {
                        JSONArray entities = dataJson.getJSONArray("entities");
                        JSONObject entity = entities.getJSONObject(0);
                        JSONObject point = entity.getJSONObject("realtime_point");
                        JSONArray location = point.getJSONArray("location");
                        entityLocation.setLongitude(location.getDouble(0));
                        entityLocation.setLatitude(location.getDouble(1));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    trackApp.getmHandler().obtainMessage(0, "解析entityList回调消息失败").sendToTarget();
                    return;
                }
                Log.d("yjm trace", entityLocation.toString());
                showRealtimeTrack(entityLocation);
            }

            @Override
            public void onReceiveLocation(TraceLocation location) {
                // TODO Auto-generated method stub
                Log.d("yjm trace", "onReceiveLocation " + location.toString());
                showRealtimeTrack(location);
            }

        };
    }

}
