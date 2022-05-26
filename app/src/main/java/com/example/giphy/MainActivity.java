package com.example.giphy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DataAdapter.OnItemClickListener {

    RecyclerView rView;
    ArrayList<DataModel> dataModelArrayList = new ArrayList<>();
    DataAdapter dataAdapter;
    public static final String API_KEY = "HKQnKjJxaX3TIK8xFonkXMGbHlLaPLQW";
    public static final String BASE_URL = "https://api.giphy.com/v1/gifs/trending?api_key=";
    String url = BASE_URL + API_KEY;
    boolean connected = false;
    Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rView = findViewById(R.id.recyclerView);

        rView.setLayoutManager(new GridLayoutManager(this, 2));
        rView.addItemDecoration(new SpaceItemDecoration(10));
        rView.setHasFixedSize(true);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();


        if (checkingNetwork()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (checkingNetwork()) {
                        Glide.get(MainActivity.this).clearDiskCache();
                        editor.clear().commit();
                    }
                }
            }).start();
            //we are connected to a network
            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        JSONArray dataArray = response.getJSONArray("data");
                        int size = dataArray.length();
                        editor.putInt("array_size", size);
                        editor.apply();
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject obj = dataArray.getJSONObject(i);
                            JSONObject obj1 = obj.getJSONObject("images");
                            JSONObject obj2 = obj1.getJSONObject("downsized_medium");

                            String sourceUrl = obj2.getString("url");
                            dataModelArrayList.add(new DataModel(sourceUrl));
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("url_" + i, sourceUrl);
                            edit.commit();

                        }

                        dataAdapter = new DataAdapter(MainActivity.this, dataModelArrayList);
                        rView.setAdapter(dataAdapter);
                        dataAdapter.setOnItemClickListener(MainActivity.this::onItemClick);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "We have error! " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            MySingleton.getInstance(this).addToRequestQueue(objectRequest);
        } else {
            //Toast.makeText(MainActivity.this, " ", Toast.LENGTH_SHORT).show();
            int size = pref.getInt("array_size", 0);
            for (int i = 0; i < size; i++) {
                pref.getString("url_" + i, null);
                String sourceUrl = pref.getString("url_" + i, null);
                dataModelArrayList.add(new DataModel(sourceUrl));
            }
            dataAdapter = new DataAdapter(MainActivity.this, dataModelArrayList);
            rView.setAdapter(dataAdapter);
            dataAdapter.setOnItemClickListener(MainActivity.this::onItemClick);
        }
    }
    //Getting the data


    //Add data to request queue


    @Override
    public void onItemClick(int pos) {
        Intent fullView = new Intent(this, FullActivity.class);
        DataModel clickedItem = dataModelArrayList.get(pos);

        fullView.putExtra("imageUrl", clickedItem.getImageUrl());
        startActivity(fullView);
    }

    public boolean checkingNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        } else {
            connected = false;
        }
        return connected;
    }


}