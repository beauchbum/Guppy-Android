package com.example.androidhive;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.content.Intent;
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

public class TuneIn extends AppCompatActivity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "57b61875218e4e1f8a8d0cdb57a7259b";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "sync-me-up://callback";

    private Player mPlayer;
    private TextView username;
    private TextView song;
    private TextView album;
    private TextView artist;
    private String old_uri = "No Track Yet";
    private String new_uri;
    private String the_username = "Loading Username";
    private String the_song = "Loading Song";
    private String the_album = "Loading Album";
    private String the_artist = "Loading Artist";
    private boolean broadcaster_playing = false;
    private boolean currently_playing = false;
    private String pid;
    private static final String TAG_PRODUCTS = "users";

    private static final String TAG_SUCCESS = "success";

    private static String ip = "52.38.141.152";
    JSONParser jsonParser = new JSONParser();
    private static String url_get_uri = "http://" + ip + "/get_uri.php";

    private ImageView imageViewRound;


    private static final int REQUEST_CODE = 1337;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune_in);

        //username = (TextView) findViewById(R.id.username);
        song = (TextView) findViewById(R.id.song);
        album = (TextView) findViewById(R.id.album);
        artist = (TextView) findViewById(R.id.artist);
        imageViewRound=(ImageView)findViewById(R.id.imageView_round);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Infinity.ttf");
        //username.setTypeface(custom_font, Typeface.BOLD);
        song.setTypeface(custom_font, Typeface.BOLD);
        album.setTypeface(custom_font, Typeface.BOLD);
        artist.setTypeface(custom_font, Typeface.BOLD);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.guppy);
        imageViewRound.setImageBitmap(icon);

        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));
        bar.setTitle(Html.fromHtml("<font color='#ffffff'>Michael Jordan</font>"));




        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //username.setText(the_username);

                        song.setText(the_song);
                        album.setText(the_album);
                        artist.setText(the_artist);
                    }
                });
            }
        }, 0, 1000);


        Intent intent = getIntent();
        AuthenticationResponse response = intent.getParcelableExtra(Explore.EXTRA_MESSAGE);
        pid = intent.getStringExtra(Explore.TAG_PID);



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
                                the_username = product.getString("name");
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

    private boolean convert_string(String original)
    {
        if (original.contains("1")){
            return true;
        }
        else{
            return false;
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
