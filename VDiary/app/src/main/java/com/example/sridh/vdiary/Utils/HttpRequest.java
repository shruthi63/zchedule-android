package com.example.sridh.vdiary.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.example.sridh.vdiary.Classes.Credential;
import com.example.sridh.vdiary.Classes.Method;
import com.example.sridh.vdiary.config;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created by sid on 6/13/17.
 */

public class HttpRequest {
    private Method method;
    private String url;
    private Map<String,String> params= new HashMap<>();
    private static String herokuBaseUrl_server1 = "https://zchedule-server1.herokuapp.com";
    private static String herokuBaseUrl_server2 = "https://zchedule-server2.herokuapp.com";
    public static String[] servers = {herokuBaseUrl_server1,herokuBaseUrl_server2};
    private String authentication =null;
    private Context context;
    public HttpRequest(Context context,String url){
        this.url= config.server.link +url;
        this.context=context;
        this.method=Method.POST;
    }

    public HttpRequest(Context context){
        this.context= context;
    }

    public void getBestServer(OnResponseListener onResponseListener){
        this.url = "https://zchedule-router.herokuapp.com/allocate_server";
        this.method=Method.GET;
        this.sendRequest(onResponseListener);
    }

    public void sendLogoutRequest(OnResponseListener onResponseListener){
        this.url = "https://zchedule-router.herokuapp.com/dislocate_server";
        this.method=Method.GET;
        (new ApiExecuter(onResponseListener)).execute(true);
    }

    public void addParam(String key,String value){
        params.put(key,value);
    }

    public HttpRequest addAuthenticationHeader(String reg,String password){
        Credential creds = new Credential(reg,password);
        this.authentication= new Gson().toJson(creds);
        return this;
    }

    public interface OnResponseListener{
        void OnResponse(String response);
    }

    public HttpRequest sendRequest(OnResponseListener onResponseListener){
        (new ApiExecuter(onResponseListener)).execute(false);
        return this;
    }

    public static void getAll(Activity activity,String regno, String password, final OnResponseListener onResponseListener){
        new HttpRequest(activity,"/all")
        .addAuthenticationHeader(regno,password)
        .sendRequest(new OnResponseListener() {
            @Override
            public void OnResponse(String response) {
                onResponseListener.OnResponse(response);
            }
        });
    }

    public static void getAll(Activity activity,Credential credential, final OnResponseListener onResponseListener){
        new HttpRequest(activity,"/all")
                .addAuthenticationHeader(credential.userName,credential.password)
                .sendRequest(new OnResponseListener() {
                    @Override
                    public void OnResponse(String response) {
                        onResponseListener.OnResponse(response);
                    }
                });
    }

    private Request buildRequest(boolean isLogoutRequest){
        Request.Builder requestBuilder = null;
        if(method.equals(Method.GET)) {
            requestBuilder= new Request.Builder()
                    .url(url)
                    .get();
        }
        else{
            requestBuilder= new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(null,new byte[0]));
        }

        if(authentication!=null)
            requestBuilder.addHeader("Authorization", authentication);
        if(isLogoutRequest)
            requestBuilder.addHeader("server",config.server.name);

        return requestBuilder.build();
    }

    private class ApiExecuter extends AsyncTask<Boolean,Void,String> {

        OnResponseListener onResponseListener;
        public ApiExecuter(OnResponseListener onResponseListener){
            this.onResponseListener=onResponseListener;
        }
        @Override
        protected String doInBackground(Boolean... booleans) {
            OkHttpClient client = new OkHttpClient();

            Request request = buildRequest(booleans[0]);
            Response response = null;
            try {
                client.setConnectTimeout(60, TimeUnit.SECONDS);
                client.setReadTimeout(60,TimeUnit.SECONDS);
                client.setWriteTimeout(60,TimeUnit.SECONDS);
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(response!=null && response.isSuccessful()){
                try {
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            onResponseListener.OnResponse(s);
            super.onPostExecute(s);
        }
    }

}
