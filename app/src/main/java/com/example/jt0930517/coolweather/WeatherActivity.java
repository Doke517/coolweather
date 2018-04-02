package com.example.jt0930517.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jt0930517.coolweather.gson.Forecast;
import com.example.jt0930517.coolweather.gson.Weather;
import com.example.jt0930517.coolweather.service.AutoUpdateService;
import com.example.jt0930517.coolweather.util.HttpUtil;
import com.example.jt0930517.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView mTitleCity;

    private TextView mTtleUpdateTime;

    private TextView mTextDegree;

    private TextView mTextWeatherInfo;

    private LinearLayout mLayoutForecast;

    private TextView mTextAqi;

    private TextView mTextPM25;

    private TextView mTextComfort;

    private TextView mTextCarWash;

    private TextView mTextSport;

    private ImageView mImgBingPic;

    public SwipeRefreshLayout swipeRefresh;

    public DrawerLayout mDrawerLayout;

    private Button mBtnNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        // initialize
        weatherLayout = (ScrollView) findViewById(R.id.layout_weather);
        mTitleCity = (TextView)findViewById(R.id.text_title_city);
        mTtleUpdateTime = (TextView)findViewById(R.id.text_update_time);
        mTextDegree = (TextView)findViewById(R.id.text_degree);
        mTextWeatherInfo = (TextView)findViewById(R.id.text_weather_info);
        mLayoutForecast = (LinearLayout)findViewById(R.id.layout_forecast);
        mTextAqi = (TextView)findViewById(R.id.text_aqi);
        mTextPM25 = (TextView)findViewById(R.id.text_pm25);
        mTextComfort = (TextView)findViewById(R.id.text_comfort);
        mTextCarWash = (TextView)findViewById(R.id.text_car_wash);
        mTextSport = (TextView)findViewById(R.id.text_sport);
        mImgBingPic = (ImageView)findViewById(R.id.img_bing_pic);
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.layout_drawer);
        mBtnNav = (Button)findViewById(R.id.nav_btn);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        final String weatherId;
        if(weatherString != null){
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handlerWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            // 无缓存时向服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        mBtnNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(mImgBingPic);
        }else{
            loadBingPic();
        }
    }

    /**
     * 根据天气ID请求城市天气数据
     */
    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=9cd1a7d77597410580966f56dd72e475";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handlerWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            Intent intent = new Intent(WeatherActivity.this, AutoUpdateService.class);
                            startService(intent);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic(){
        String requestBingpic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingpic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic =  response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(mImgBingPic);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        mTitleCity.setText(cityName);
        mTtleUpdateTime.setText(updateTime);
        mTextDegree.setText(degree);
        mTextWeatherInfo.setText(weatherInfo);
        mLayoutForecast.removeAllViews();
        for(Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,mLayoutForecast,false);
            TextView dateText = (TextView)view.findViewById(R.id.text_date);
            TextView infoText = (TextView)view.findViewById(R.id.text_info);
            TextView maxText = (TextView)view.findViewById(R.id.text_max);
            TextView minText = (TextView)view.findViewById(R.id.text_min);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            mLayoutForecast.addView(view);
        }

        if(weather.aqi != null){
            mTextAqi.setText(weather.aqi.city.aqi);
            mTextPM25.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        mTextComfort.setText(comfort);
        mTextCarWash.setText(carWash);
        mTextSport.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
