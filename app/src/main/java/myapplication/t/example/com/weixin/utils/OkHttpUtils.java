package myapplication.t.example.com.weixin.utils;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
/**
 * Created by zsl on 2018/4/22.
 */

public class OkHttpUtils {
    private OkHttpClient mOkHttpClient;
    private static OkHttpUtils mOkHttpUtils;

    public OkHttpUtils(){
        mOkHttpClient = new OkHttpClient();
    }

    public static OkHttpUtils getInstance(){
        if (mOkHttpUtils == null){
            synchronized (OkHttpUtils.class){
                if (mOkHttpUtils == null){
                    mOkHttpUtils = new OkHttpUtils();
                }
            }
        }
        return mOkHttpUtils;
    }

    public Call get(String url){
        Request request = new Request.Builder().url(url).build();
        return mOkHttpClient.newCall(request);
    }

    public com.squareup.okhttp.Call post(String url, RequestBody formBody){
        Request request = new Request.Builder().url(url).post(formBody).build();
        return mOkHttpClient.newCall(request);
    }

}
