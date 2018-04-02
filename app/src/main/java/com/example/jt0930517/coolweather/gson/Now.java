package com.example.jt0930517.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jt0930517 on 2018/4/2.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;
    }
}
