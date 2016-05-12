package com.example.androidhive;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by Ryan on 5/9/2016.
 */
public class TuneIn_Fragment extends Fragment implements
        PlayerNotificationCallback, ConnectionStateCallback {

    public Explore main_activity;

    private String the_song = "Loading Song";
    private String the_album = "Loading Album";
    private String the_artist = "Loading Artist";
    private TextView song;
    private TextView album;
    private TextView artist;
    private Player mPlayer;
    private String old_uri = "No Track Yet";
    private String new_uri;
    private boolean broadcaster_playing = false;
    private boolean currently_playing = false;

    private ImageView imageViewRound;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.tunein_fragment, container, false);
        main_activity = (Explore) getActivity();
        //username = (TextView) findViewById(R.id.username);
        song = (TextView) rootview.findViewById(R.id.song);
        album = (TextView) rootview.findViewById(R.id.album);
        artist = (TextView) rootview.findViewById(R.id.artist);
        imageViewRound=(ImageView) rootview.findViewById(R.id.imageView_round);
        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Infinity.ttf");
        //username.setTypeface(custom_font, Typeface.BOLD);
        song.setTypeface(custom_font, Typeface.BOLD);
        album.setTypeface(custom_font, Typeface.BOLD);
        artist.setTypeface(custom_font, Typeface.BOLD);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.guppy);
        imageViewRound.setImageBitmap(icon);

        android.support.v7.app.ActionBar bar = main_activity.getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));
        bar.setTitle(Html.fromHtml("<font color='#ffffff'>Michael Jordan</font>"));




        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                main_activity.runOnUiThread(new Runnable() {
                    public void run() {
                        //username.setText(the_username);

                        song.setText(the_song);
                        album.setText(the_album);
                        artist.setText(the_artist);
                    }
                });
            }
        }, 0, 1000);



        //if (response.getType() == AuthenticationResponse.Type.TOKEN) {
        Config playerConfig = new Config(getActivity().getApplicationContext(), main_activity.response.getAccessToken(), main_activity.CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                mPlayer = player;

                //Non UI Thread that runs continuosly to check for new song URI/play/pause
                ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                exec.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {

                        //JSON making the HTTP request
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("id", main_activity.current_following_id));
                        JSONObject json = main_activity.jsonParser.makeHttpRequest(
                                main_activity.url_get_uri, "GET", params);

                        try {

                            int success = json.getInt(main_activity.TAG_SUCCESS);
                            if (success == 1) {
                                // successfully received product details
                                JSONArray productObj = json
                                        .getJSONArray("user"); // JSON Array

                                // get first product object from JSON Array
                                JSONObject product = productObj.getJSONObject(0);
                                new_uri = product.getString("uri");
                                //the_username = product.getString("name");
                                the_song = product.getString("song");
                                the_album = product.getString("album");
                                the_artist = product.getString("artist");
                                broadcaster_playing = convert_string(product.getString("playing"));
                                Log.d("Received Value", product.getString("playing"));
                                Log.d("URI", new_uri);
                                Log.d("Playing", String.valueOf(broadcaster_playing));
                                //String am_i_playing = String.valueOf(broadcaster_playing);
                                //Log.d("broadcaster playing", am_i_playing);

                            }else{
                                // product with pid not found
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (new_uri.equals(old_uri) == false) {
                            mPlayer.play(new_uri);
                            currently_playing = true;
                            old_uri = new_uri;
                        }
                        if (currently_playing == false && broadcaster_playing == true)
                        {
                            mPlayer.resume();
                            currently_playing = true;
                        }
                        if (currently_playing == true && broadcaster_playing == false)
                        {
                            mPlayer.pause();
                            currently_playing = false;
                        }

                    }
                }, 0, 1, TimeUnit.SECONDS);

            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
        return rootview;
    }

    @Override
    public void onLoggedIn() {
        Log.d("Main2Activity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("Main2Activity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("Main2Activity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("Main2Activity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("Main2Activity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(PlayerNotificationCallback.EventType eventType, PlayerState playerState) {
        Log.d("Main2Activity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(PlayerNotificationCallback.ErrorType errorType, String errorDetails) {
        Log.d("Main2Activity", "Playback error received: " + errorType.name());
    }


    private boolean convert_string(String original)
    {
        if (original.contains("1")){
            return true;
        }
        else{
            return false;
        }
    }

    class Decrement_Listeners extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", args[0]));



            // getting JSON Object
            // Note that create product url accepts POST method

            JSONObject json = main_activity.jsonParser.makeHttpRequest(main_activity.url_decrement_listeners,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(main_activity.TAG_SUCCESS);

                if (success == 1) {

                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done


        }
    }

    //Runs PHP script to grab the song URI from the database
    class Get_uri extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog = new ProgressDialog(TuneIn.this);
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            */
        }

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... args) {


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", main_activity.current_following_id));

            Log.d("PID", main_activity.current_following_id);

            // getting product details by making HTTP request
            // Note that product details url will use GET request
            JSONObject json = main_activity.jsonParser.makeHttpRequest(
                    main_activity.url_get_uri, "GET", params);
            try {

                int success = json.getInt(main_activity.TAG_SUCCESS);
                if (success == 1) {
                    // successfully received product details
                    JSONArray productObj = json
                            .getJSONArray("user"); // JSON Array

                    // get first product object from JSON Array
                    JSONObject product = productObj.getJSONObject(0);
                    new_uri = product.getString("uri");
                    Log.d("URI", new_uri);

                }else{
                    // product with pid not found
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details

        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Spotify.destroyPlayer(this);
        new Decrement_Listeners().execute(main_activity.current_following_id);
    }

}
