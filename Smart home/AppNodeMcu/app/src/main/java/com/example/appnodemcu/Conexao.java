package com.example.appnodemcu;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Conexao {

    public static String getDados(String urlUser){

        OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(urlUser)
                    .build();
    try{
        Response response = client.newCall(request).execute();
        return response.body().string();
    }catch (IOException erro){

        return null;
    }



    }
}
