package com.hl.netplayhere;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
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
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.hl.netplayhere.activity.NavigationActivity;
import com.hl.netplayhere.adapter.SpotAdapter;
import com.hl.netplayhere.bean.HotSpot;
import com.hl.netplayhere.util.Constant;
import com.hl.netplayhere.util.Utils;

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

    public FragmentPage1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentPager3.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentPage1 newInstance(String param1, String param2) {
        FragmentPage1 fragment = new FragmentPage1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
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
}
