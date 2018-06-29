package com.georgeg10499.flickster;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.georgeg10499.flickster.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MovieListActivity extends AppCompatActivity {

    //contains
    //the base URL for the API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    //the parameter nme for the API key
    public static final String API_KEY_PARAM = "api_key";
    //tag for logging from this activity
    public static final String TAG = "MovieListActivity";


    //instance fields
    AsyncHttpClient client;
    // the base url for loading images
    String imageBaseUrl;
    //the poster size to when fetching images, part of the url
    String posterSize;
    //the List of playing movies
    ArrayList<Movie> movies;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        //initialize
        client = new AsyncHttpClient();
        //initialize the list of movies
        movies = new ArrayList<>();
        // get the configuration on appCreation
        getConfiguration();
        //get the now playing movie list
        getNowPlaying();

    }
    //get the list of currently playing
    private void getNowPlaying(){
        //create the url
        String url = API_BASE_URL + "/movie/now_playing";
        // set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));//API key, always required
        //execute a GET request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //load the results into movies list
                try {
                    JSONArray results = response.getJSONArray("results");
                    //iterate trough result set to movies
                    for(int i = 0; i<results.length(); i++){
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                        Log.i(TAG,String.format("Loaded movies", results.length()));
                    }
                } catch (JSONException e) {
                    logError("Failed to get data from now playing movies", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            logError("Failed to data from now_playing endpoint", throwable,true);
            }
        });

    }

    //get the configuration from API
    private void getConfiguration() {
        //create the url
        String url = API_BASE_URL + "/configuration";
        // set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));//API key, always required
        //execute a GET request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    JSONObject images = response.getJSONObject("images");
                    //get the image url
                    imageBaseUrl = images.getString("secure_base_url");
                    //get the poster size
                    JSONArray posterSizeOptions = images.getJSONArray("poster_sizes");
                    //use the option at index 3 w342 as a fallback
                    posterSize = posterSizeOptions.optString(3,"w342");
                    Log.i(TAG, String.format("Loaded configuration with imageBaseUrl %s and posterSize %s", imageBaseUrl, posterSize));

                } catch (JSONException e) {
                    logError("Failed parsing configuration", e, true);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
               logError("Failed getting configuration", throwable, true);
            }
        });
    }

    // handle errors, log and alert user
    private void logError(String message, Throwable error, boolean alerUser){
        //always log error
        Log.e(TAG, message, error);
        //alert the user to avoid silent errors
        if (alerUser){
            //show a long toast with the error message
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}