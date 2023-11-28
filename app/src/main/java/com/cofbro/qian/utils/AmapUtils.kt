package com.cofbro.qian.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps2d.model.LatLng
import com.cofbro.qian.mapsetting.util.ToastUtil
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.regex.Matcher
import java.util.regex.Pattern


object AmapUtils {
    //要申请的权限
    private val mPermissions =
        Manifest.permission.ACCESS_FINE_LOCATION

    /**
     * 判断是否缺少权限
     */
    private fun lacksPermission(mContexts: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(mContexts, permission) ==
                PackageManager.PERMISSION_DENIED
    }
    /*
    请求精确定位
     */
    fun checkLocationPermission(activity:AppCompatActivity){
        if (lacksPermission(activity.applicationContext, mPermissions)){
            /*
            缺少精确定位，提醒开启精确定位
             */
            ToastUtil.show(activity.applicationContext,"请选择精确位置")
            getLocationPermission(activity)
        }else{
            //权限开启

        }
    }
    private fun getLocationPermission(activity:AppCompatActivity){
        val locationPermissionRequest = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                   ToastUtil.show(activity.applicationContext,"请选择精确位置")
                } else -> {
                // No location access granted.
            }
            }
        }
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    /*
    获取自己的定位信息
     */
     fun getCurrentLocationLatLng(applicationContext:Context, onSuccess:(LatLng,String)->Unit = { latLng: LatLng, s: String -> }, onError:(String)->Unit = {}) {
        AMapLocationClient.updatePrivacyAgree(applicationContext, true)
        AMapLocationClient.updatePrivacyShow(applicationContext, true, true)
        //初始化定位
       val mLocationClient = AMapLocationClient(applicationContext)
        var mLocationOption: AMapLocationClientOption? = null
        //设置定位回调监听
        mLocationClient.setLocationListener { amapLocation ->
            if (amapLocation != null) {
                if (amapLocation.errorCode == 0) {
    //                    amapLocation.locationType //获取当前定位结果来源，如网络定位结果，详见定位类型表
                        amapLocation.latitude //获取纬度
                        amapLocation.longitude //获取经度
                        amapLocation.accuracy //获取精度信息
                        amapLocation.address //地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                        amapLocation.country //国家信息
                        amapLocation.province //省信息
                        amapLocation.city //城市信息
                        amapLocation.district //城区信息
                        amapLocation.street //街道信息
                        amapLocation.streetNum //街道门牌号信息
    //                    amapLocation.cityCode //城市编码
    //                    amapLocation.adCode //地区编码
    //                    amapLocation.aoiName //获取当前定位点的AOI信息
    //                    amapLocation.buildingId //获取当前室内定位的建筑物Id
    //                    amapLocation.floor //获取当前室内定位的楼层
    //                    amapLocation.gpsAccuracyStatus //获取GPS的当前状态
                    val address = urlEncodeChinese(amapLocation.country + " " + amapLocation.address)
                    onSuccess(LatLng(amapLocation.latitude,amapLocation.longitude),address)
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    onError("location Error, ErrCode:" + amapLocation.errorCode + ", errInfo:" + amapLocation.errorInfo)
                }
            }
        }
        //初始化AMapLocationClientOption对象
        mLocationOption = AMapLocationClientOption()

        mLocationOption.locationMode =
            AMapLocationClientOption.AMapLocationMode.Hight_Accuracy

        // 设置为单次定位  : 默认为false
        mLocationOption.isOnceLocation = false
        mLocationOption.httpTimeOut = 20000
        mLocationOption.isLocationCacheEnable = false
        mLocationClient.setLocationOption(mLocationOption)
        //启动定位
        mLocationClient.startLocation()
    }
    private fun urlEncodeChinese(urlString: String): String {
        var url = urlString
        try {
            val matcher: Matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(url)
            var tmp = ""
            while (matcher.find()) {
                tmp = matcher.group()
                url = url.replace(tmp.toRegex(), URLEncoder.encode(tmp, "UTF-8"))
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return url.replace(" ", "%20")
    }

}