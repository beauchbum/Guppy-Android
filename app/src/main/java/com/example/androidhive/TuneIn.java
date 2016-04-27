package com.example.androidhive;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class TuneIn extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "57b61875218e4e1f8a8d0cdb57a7259b";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "sync-me-up://callback";

    private Player mPlayer;
    private TextView song;
    private TextView album;
    private TextView artist;
    private String old_uri = "No Track Yet";
    private String new_uri;
    private String the_song;
    private String the_album;
    private String the_artist;
    private boolean playing;
    private String pid;
    private static final String TAG_PRODUCTS = "users";

    private static final String TAG_SUCCESS = "success";

    private static String ip = "10.0.0.26";
    JSONParser jsonParser = new JSONParser();
    private static String url_get_uri = "http://" + ip + "/android_connect/get_uri.php";


    private static final int REQUEST_CODE = 1337;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune_in);
        song = (TextView) findViewById(R.id.song);
        album = (TextView) findViewById(R.id.album);
        artist = (TextView) findViewById(R.id.artist);

        Intent intent = getIntent();

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        song.setText(the_song);
                        album.setText(the_album);
                        artist.setText(the_artist);
                    }
                });
            }
        }, 0, 1000);

        AuthenticationResponse response = intent.getParcelableExtra(MainScreenActivity.EXTRA_MESSAGE);
        pid = intent.getStringExtra(MainScreenActivity.TAG_PID);


        //if (response.getType() == AuthenticationResponse.Type.TOKEN) {
        Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
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
                        params.add(new BasicNameValuePair("id", pid));
                        JSONObject json = jsonParser.makeHttpRequest(
                                url_get_uri, "GET", params);
                        
                        try {

                            int success = json.getInt(TAG_SUCCESS);
                            if (success == 1) {
                                // successfully received product details
                                JSONArray productObj = json
                                        .getJSONArray("user"); // JSON Array

                                // get first product object from JSON Array
                                JSONObject product = productObj.getJSONObject(0);
                                new_uri = product.getString("uri");
                                the_song = product.getString("song");
                                the_album = product.getString("album");
                                the_artist = product.getString("artist");
                                playing = product.getBoolean("playing");
                                Log.d("URI", new_uri);

                            }else{
                                // product with pid not found
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("New URI", new_uri);
                        if (new_uri.equals(old_uri) == false)
                        {
                            mPlayer.play(new_uri);
                            old_uri = new_uri;
                        }
                    }
                }, 5, 2, TimeUnit.SECONDS);

            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
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
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("Main2Activity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("Main2Activity", "Playback error received: " + errorType.name());
    }


    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);


        super.onDestroy();
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
            params.add(new BasicNameValuePair("id", pid));

            Log.d("PID", pid);

            // getting product details by making HTTP request
            // Note that product details url will use GET request
            JSONObject json = jsonParser.makeHttpRequest(
                    url_get_uri, "GET", params);
            try {

                int success = json.getInt(TAG_SUCCESS);
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



}
