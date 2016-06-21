package com.example.androidhive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

import java.io.InputStream;
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
public class UserProfile_Fragment extends Fragment implements
        View.OnClickListener ,PlayerNotificationCallback, ConnectionStateCallback {

    public Explore main_activity;

    private String the_song = "Loading Song";
    private String the_album = "Loading Album";
    private String the_artist = "Loading Artist";
    private String the_art;
    private TextView song;
    private TextView album;
    private TextView artist;
    private TextView paused_textview;
    private ImageButton follow_button;
    private ImageButton pause_play;
    private String old_uri = "No Track Yet";
    private String new_uri;
    private boolean broadcaster_playing = true;
    private boolean broadcasting = true;
    private boolean currently_playing = false;
    private boolean following;
    public ProgressDialog pdialog;
    public View rootview;
    public Toast following_toast;

    private ImageView albumImageView;
    private ImageView imageViewRound;
    private String currently_following;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.tunein_fragment, container, false);
        main_activity = (Explore) getActivity();
        //username = (TextView) findViewById(R.id.username);
        song = (TextView) rootview.findViewById(R.id.song);
        album = (TextView) rootview.findViewById(R.id.album);
        artist = (TextView) rootview.findViewById(R.id.artist);
        albumImageView=(ImageView) rootview.findViewById(R.id.album_image_view);
        paused_textview=(TextView) rootview.findViewById(R.id.paused_playback);
        //Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Infinity.ttf");
        //username.setTypeface(custom_font, Typeface.BOLD);
        //song.setTypeface(custom_font, Typeface.BOLD);
        //album.setTypeface(custom_font, Typeface.BOLD);
        //artist.setTypeface(custom_font, Typeface.BOLD);



        //Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.guppy);
        //imageViewRound.setImageBitmap(icon);
        //new Get_Following_Or_Nah().execute();



        Log.d("Tune In", "Fragment Started");




        currently_following = main_activity.current_following_id;

        follow_button = (ImageButton) rootview.findViewById(R.id.plus_button);
        follow_button.setOnClickListener(this);

        pause_play = (ImageButton) rootview.findViewById(R.id.play);
        pause_play.setOnClickListener(this);


        if(main_activity.following_or_nah)
        {
            follow_button.setImageResource(R.drawable.check);
        }
        else
        {
            follow_button.setImageResource(R.drawable.plus);
        }

        if(main_activity.paused_playback)
        {
            pause_play.setImageResource(R.drawable.play);
        }
        else
        {
            pause_play.setImageResource(R.drawable.pause);
        }

        if(main_activity.tuning_in)
        {
            albumImageView.setImageBitmap(main_activity.albumArtImage);
            if(main_activity.following_or_nah)
            {
                follow_button.setImageResource(R.drawable.check);
            }
            else
            {
                follow_button.setImageResource(R.drawable.plus);
            }

            song.setText(the_song);
            album.setText(the_album);
            artist.setText(the_artist);
        }
        else
        {
            Log.d("Downloading Image", "");

            new DownloadImageTask().execute(the_art);
        }
        main_activity.tuning_in = true;



        android.support.v7.app.ActionBar bar = main_activity.getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));
        bar.setTitle(Html.fromHtml("<font color='#ffffff'>" + main_activity.current_following_name + "</font>"));
        main_activity.stop_tune_in.setTitle("Tune Out " + main_activity.current_following_name);
        main_activity.see_profile.setTitle("See " + main_activity.current_following_name + "'s Profile");
        main_activity.stop_tune_in.setVisible(true);
        main_activity.see_profile.setVisible(true);




        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                main_activity.runOnUiThread(new Runnable() {


                    public void run() {
                        //username.setText(the_username);
                        if(broadcasting == false) {
                            main_activity.onOptionsItemSelected(main_activity.my_menu.findItem(R.id.menu_stop_tune_in));
                        }

                        if(main_activity.following_or_nah)
                        {
                            follow_button.setImageResource(R.drawable.check);
                        }
                        else
                        {
                            follow_button.setImageResource(R.drawable.plus);
                        }

                        if(broadcaster_playing == false)
                        {
                            albumImageView.setAlpha(90);
                            paused_textview.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            albumImageView.setAlpha(255);
                            paused_textview.setVisibility(View.GONE);
                        }

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
                main_activity.mPlayer = player;

                //Non UI Thread that runs continuosly to check for new song URI/play/pause
                PlayMusic();

            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });

        return rootview;

    }

    public void PlayMusic()
    {
        Log.d("play music", "it running");
        main_activity.exec = new ScheduledThreadPoolExecutor(1);
        main_activity.exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(main_activity.tuning_in) {

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
                            the_art = product.getString("artwork");
                            Log.d("User Broadcasting ", product.getString("broadcasting"));
                            broadcaster_playing = convert_string(product.getString("playing"));
                            broadcasting = convert_string(product.getString("broadcasting"));
                            //String am_i_playing = String.valueOf(broadcaster_playing);
                            //Log.d("broadcaster playing", am_i_playing);

                        } else {
                            // product with pid not found
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(broadcasting == false)
                    {
                        main_activity.runOnUiThread(new Runnable() {


                            public void run() {
                                //username.setText(the_username);

                                main_activity.onOptionsItemSelected(main_activity.my_menu.findItem(R.id.menu_stop_tune_in));
                                broadcasting = true;

                            }
                        });
                    }

                    if (new_uri.equals(old_uri) == false) {
                        main_activity.mPlayer.play(new_uri);
                        currently_playing = true;
                        old_uri = new_uri;
                        new DownloadImageTask().execute(the_art);

                    }
                    if (currently_playing == false && broadcaster_playing == true) {
                        main_activity.mPlayer.resume();
                        currently_playing = true;
                    }
                    if (currently_playing == true && broadcaster_playing == false) {
                        main_activity.mPlayer.pause();
                        currently_playing = false;
                    }
                }

            }
        }, 0, 1, TimeUnit.SECONDS);
    }



    @Override
    public void onClick(View v)
    {

        switch (v.getId()) {
            case R.id.plus_button:
                if(main_activity.following_or_nah == false)
                {
                    new FollowUser().execute(main_activity.current_following_id, main_activity.current_user_id);
                    try {
                        following_toast.cancel();
                    }catch (Exception e)
                    {

                    }
                    following_toast = Toast.makeText(getActivity(), "Followed " + main_activity.current_following_name,
                            Toast.LENGTH_SHORT);
                    following_toast.setGravity(Gravity.CENTER, 0, 0);
                    following_toast.show();
                    main_activity.following_or_nah = true;
                    follow_button.setImageResource(R.drawable.check);
                }
                else
                {
                    new UnfollowUser().execute(main_activity.current_following_id, main_activity.current_user_id);
                    try {
                        following_toast.cancel();
                    }catch (Exception e)
                    {

                    }
                    following_toast = Toast.makeText(getActivity(), "Unfollowed " + main_activity.current_following_name,
                            Toast.LENGTH_SHORT);
                    following_toast.setGravity(Gravity.CENTER, 0, 0);
                    following_toast.show();
                    main_activity.following_or_nah = false;
                    follow_button.setImageResource(R.drawable.plus);
                }
                break;
            case R.id.play:
                if (main_activity.paused_playback == false && broadcaster_playing == true)
                {
                    //exec.shutdown();
                    main_activity.mPlayer.pause();
                    pause_play.setImageResource(R.drawable.play);
                    main_activity.paused_playback = true;
                }
                else
                {
                    if(broadcaster_playing == true) {
                        main_activity.mPlayer.resume();
                        pause_play.setImageResource(R.drawable.pause);
                        main_activity.paused_playback = false;
                    }
                }

        }

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
            params.add(new BasicNameValuePair("listener_id", args[1]));


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

    public void onStop()
    {
        super.onStop();
        //new Decrement_Listeners().execute(currently_following, main_activity.current_user_id);
        //exec.shutdown();

    }

    class Get_Following_Or_Nah extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdialog = new ProgressDialog(getActivity());
            pdialog.setMessage("Loading" + main_activity.current_following_name + ". Please wait...");
            pdialog.setIndeterminate(false);
            pdialog.setCancelable(false);
            pdialog.show();

        }

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... args) {


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("current_user_id", main_activity.current_user_id));
            params.add(new BasicNameValuePair("current_following_id", main_activity.current_following_id));


            Log.d("PID", main_activity.current_following_id);

            // getting product details by making HTTP request
            // Note that product details url will use GET request
            JSONObject json = main_activity.jsonParser.makeHttpRequest(
                    main_activity.url_get_following_or_nah, "GET", params);
            try {

                int success = json.getInt(main_activity.TAG_SUCCESS);
                if (success == 1) {
                    // successfully received product details
                    JSONArray productObj = json
                            .getJSONArray("user"); // JSON Array

                    // get first product object from JSON Array
                    JSONObject product = productObj.getJSONObject(0);
                    String result = product.getString("result");
                    if (result.equals("true"))
                    {
                        main_activity.following_or_nah = true;
                    }
                    else
                    {
                        main_activity.following_or_nah = false;
                    }
                    Log.d("Following Or Nah", result);

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
            pdialog.dismiss();


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
            /*
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

    class FollowUser extends AsyncTask<String, String, String> {

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
            params.add(new BasicNameValuePair("followed_id", args[0]));
            params.add(new BasicNameValuePair("follower_id", args[1]));



            // getting JSON Object
            // Note that create product url accepts POST method

            JSONObject json = main_activity.jsonParser.makeHttpRequest(main_activity.url_follow_user,
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

    class UnfollowUser extends AsyncTask<String, String, String> {

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
            params.add(new BasicNameValuePair("followed_id", args[0]));
            params.add(new BasicNameValuePair("follower_id", args[1]));



            // getting JSON Object
            // Note that create product url accepts POST method

            JSONObject json = main_activity.jsonParser.makeHttpRequest(main_activity.url_unfollow_user,
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        //ImageView bmImage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                Log.d("Art URL", urls[0]);
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", "No Text Yet");
                e.printStackTrace();
            }
            main_activity.albumArtImage = mIcon11;
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            albumImageView.setImageBitmap(result);


        }
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //Spotify.destroyPlayer(this);
        //new Decrement_Listeners().execute(currently_following, main_activity.current_user_id);
    }

}
