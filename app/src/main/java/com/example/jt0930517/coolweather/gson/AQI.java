package com.example.jt0930517.coolweather.gson;

/**
 * Created by jt0930517 on 2018/4/2.
 */

public class AQI {
    public AQICity city;

    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
