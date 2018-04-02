package com.example.jt0930517.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jt0930517 on 2018/4/2.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
