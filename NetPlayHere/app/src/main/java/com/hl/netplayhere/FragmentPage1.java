package com.hl.netplayhere;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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
import com.baidu.mapapi.model.LatLngBounds;
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
import com.baidu.trace.OnTrackListener;
import com.baidu.trace.TraceLocation;
import com.hl.netplayhere.activity.NavigationActivity;
import com.hl.netplayhere.adapter.SpotAdapter;
import com.hl.netplayhere.bean.HotSpot;
import com.hl.netplayhere.util.Constant;
import com.hl.netplayhere.util.GsonService;
import com.hl.netplayhere.util.HistoryTrackData;
import com.hl.netplayhere.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
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
    LinearLayout mFunctionILl;
    PopupWindow popupWindow;

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

    private static List<LatLng> pointList = new ArrayList<>();

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

    private View rootView;// 缓存Fragment view

    protected static OnTrackListener trackListener = null;

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
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_page1, null);
        }
        // 缓存的rootView需要判断是否已经被加过parent，如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null)
        {
            parent.removeView(rootView);
        }
        mMapView = (MapView) rootView.findViewById(R.id.bmapView);
        mListView = (ListView) rootView.findViewById(R.id.hotSpotLv);
        mFunctionILl = (LinearLayout) rootView.findViewById(R.id.functionll);
        mFunctionILl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });
        return rootView;
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
        query.findObjects(getContext(), new FindListener<HotSpot>() {
            @Override
            public void onSuccess(List<HotSpot> list) {
                    Log.d("yjm", "load hotspot : " + list.size());
                    mSpotAdapter = new SpotAdapter(getContext(), list);
                    mListView.setAdapter(mSpotAdapter);
            }

            @Override
            public void onError(int i, String s) {
                Log.d("yjm", "error " + s);
            }

        });
        initNavi();

        trackApp = (MyApplication) getActivity().getApplication();
        trackApp.initBmap(mMapView);
        initOnEntityListener();
        initOnTrackListener();
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
                Log.d("navi init", "initSuccess");
            }

            @Override
            public void initFailed() {
                Log.d("navi init", "initFailed");
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

    private void drawRealtimePoint(LatLng point) {

        if (null != overlay) {
            overlay.remove();
        }

        MapStatus mMapStatus = new MapStatus.Builder().target(point).zoom(19).build();

        msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

//        if (null == realtimeBitmap) {
//            realtimeBitmap = BitmapDescriptorFactory
//                    .fromResource(R.drawable.icon_geo);
//        }

        overlayOptions = new MarkerOptions().position(point)
                /*.icon(realtimeBitmap)*/.zIndex(9).draggable(true);

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
     * 查询历史轨迹
     */
    public void queryHistoryTrack(int processed, String processOption) {
        int startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        int endTime = (int) (System.currentTimeMillis() / 1000);
        // entity标识
        String entityName = trackApp.getEntityName();
        // 是否返回精简的结果（0 : 否，1 : 是）
        int simpleReturn = 0;
        // 是否返回纠偏后轨迹（0 : 否，1 : 是）
        int isProcessed = processed;
        // 分页大小
        int pageSize = 1000;
        // 分页索引
        int pageIndex = 1;

        trackApp.getClient().queryHistoryTrack(trackApp.getServiceId(), entityName, simpleReturn,
                isProcessed, processOption,
                startTime, endTime,
                pageSize,
                pageIndex,
                trackListener);
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

    /**
     * 初始化OnTrackListener
     */
    private void initOnTrackListener() {
        trackListener = new OnTrackListener() {
            // 请求失败回调接口
            @Override
            public void onRequestFailedCallback(String arg0) {
                // TODO Auto-generated method stub
                trackApp.getmHandler().obtainMessage(0, "track请求失败回调接口消息 : " + arg0).sendToTarget();
            }

            // 查询历史轨迹回调接口
            @Override
            public void onQueryHistoryTrackCallback(String arg0) {
                // TODO Auto-generated method stub
                super.onQueryHistoryTrackCallback(arg0);
                showHistoryTrack(arg0);
            }

            @Override
            public void onQueryDistanceCallback(String arg0) {
                // TODO Auto-generated method stub
                try {
                    JSONObject dataJson = new JSONObject(arg0);
                    if (null != dataJson && dataJson.has("status") && dataJson.getInt("status") == 0) {
                        double distance = dataJson.getDouble("distance");
                        DecimalFormat df = new DecimalFormat("#.0");
                        //trackApp.getmHandler().obtainMessage(0, "里程 : " + df.format(distance) + "米").sendToTarget();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    trackApp.getmHandler().obtainMessage(0, "queryDistance回调消息 : " + arg0).sendToTarget();
                }
            }

            @Override
            public Map<String, String> onTrackAttrCallback() {
                // TODO Auto-generated method stub
                System.out.println("onTrackAttrCallback");
                return null;
            }

        };
    }

    /**
     * 显示历史轨迹
     *
     * @param historyTrack
     */
    private void showHistoryTrack(String historyTrack) {

        HistoryTrackData historyTrackData = GsonService.parseJson(historyTrack,
                HistoryTrackData.class);

        List<LatLng> latLngList = new ArrayList<LatLng>();
        if (historyTrackData != null && historyTrackData.getStatus() == 0) {
            if (historyTrackData.getListPoints() != null) {
                latLngList.addAll(historyTrackData.getListPoints());
            }
            // 绘制历史轨迹
            drawHistoryTrack(latLngList, historyTrackData.distance);
        }

    }

    /**
     * 绘制历史轨迹
     *
     * @param points
     */
    private void drawHistoryTrack(final List<LatLng> points, final double distance) {
        // 绘制新覆盖物前，清空之前的覆盖物
        trackApp.getmBaiduMap().clear();

        if (points.size() == 1) {
            points.add(points.get(0));
        }

        if (points == null || points.size() == 0) {
            trackApp.getmHandler().obtainMessage(0, "当前查询无轨迹点").sendToTarget();
            //resetMarker();
        } else if (points.size() > 1) {

            LatLng llC = points.get(0);
            LatLng llD = points.get(points.size() - 1);
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(llC).include(llD).build();

            msUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);

//            bmStart = BitmapDescriptorFactory.fromResource(R.mipmap.icon_start);
//            bmEnd = BitmapDescriptorFactory.fromResource(R.mipmap.icon_end);

            // 添加起点图标
//            startMarker = new MarkerOptions()
//                    .position(points.get(points.size() - 1)).icon(bmStart)
//                    .zIndex(9).draggable(true);

            // 添加终点图标
//            endMarker = new MarkerOptions().position(points.get(0))
//                    .icon(bmEnd).zIndex(9).draggable(true);

            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.BLUE).points(points);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.flat(true);
            markerOptions.anchor(0.5f, 0.5f);
            markerOptions.icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_geo));
            markerOptions.position(points.get(points.size() - 1));

            addMarker();

            trackApp.getmHandler().obtainMessage(0, "当前轨迹里程为 : " + (int) distance + "米").sendToTarget();

        }

    }

    private void showPopupWindow(View view){
        if(popupWindow == null){
            View contentView = LayoutInflater.from(getContext()).inflate(
                    R.layout.pop_window, null);
            popupWindow = new PopupWindow(contentView,
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            popupWindow.setTouchInterceptor(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                    // 这里如果返回true的话，touch事件将被拦截
                    // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
                }
            });
            LinearLayout item1 = (LinearLayout) contentView.findViewById(R.id.pop_item1);
            item1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Constant.isMapNeedReload){
                        mLocClient.registerLocationListener(new MyLocationListener());
                        LocationClientOption option = new LocationClientOption();
                        option.setOpenGps(true);// 打开gps
                        option.setCoorType("bd09ll"); // 设置坐标类型
                        option.setScanSpan(1000);
                        mLocClient.setLocOption(option);
                        mLocClient.start();
                    }
                    startRefreshThread(false);
                    popupWindow.dismiss();
                }
            });
            LinearLayout item2 = (LinearLayout) contentView.findViewById(R.id.pop_item2);
            item2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLocClient.stop();
                    mLocClient.unRegisterLocationListener(new MyLocationListener());
                    Log.d("yjm", "定位是否开启：" + mLocClient.isStarted());
                    Constant.isMapNeedReload = true;
                    startRefreshThread(true);
                    queryHistoryTrack(0, null);
                    popupWindow.dismiss();
                }
            });
            LinearLayout item3 = (LinearLayout) contentView.findViewById(R.id.pop_item3);
            item3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLocClient.stop();
                    mLocClient.unRegisterLocationListener(new MyLocationListener());
                    Constant.isMapNeedReload = true;
                    startRefreshThread(false);
                    searchNearby();
                    popupWindow.dismiss();
                }
            });

        }

        if(!popupWindow.isShowing()){
            popupWindow.showAsDropDown(view);
        } else{
            popupWindow.dismiss();
        }

    }

}
