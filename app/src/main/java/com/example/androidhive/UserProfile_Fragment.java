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
        View.OnClickListener, PlayerNotificationCallback, ConnectionStateCallback {

    public Explore main_activity;
    public TextView followers_tv;
    public TextView following_tv;
    public ImageButton listeners_ib;
    public String followers;
    public String following;
    public String listeners;
    public boolean following_or_nah;
    public ProgressDialog pdialog;
    public View rootview;
    private ImageButton follow_button;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.userprofile_fragment, container, false);
        main_activity = (Explore) getActivity();

        main_activity.tuning_in = true;


        android.support.v7.app.ActionBar bar = main_activity.getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));
        bar.setTitle(Html.fromHtml("<font color='#ffffff'>" + main_activity.user_profile_name + "'s Profile" + "</font>"));
        main_activity.stop_tune_in.setTitle("Tune Out " + main_activity.current_following_name);
        //main_activity.see_profile.setTitle("See " + main_activity.current_following_name + "'s Profile");
        main_activity.stop_tune_in.setVisible(true);
        //main_activity.see_profile.setVisible(true);
        following_tv = (TextView) rootview.findViewById(R.id.following);
        followers_tv = (TextView) rootview.findViewById(R.id.followers);

        follow_button = (ImageButton) rootview.findViewById(R.id.plus_button);
        follow_button.setOnClickListener(this);
        new Get_Following_Or_Nah().execute(main_activity.current_user_id, main_activity.current_following_id);


        if(following_or_nah)
        {
            follow_button.setImageResource(R.drawable.check);
        }
        else
        {
            follow_button.setImageResource(R.drawable.plus);
        }

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                main_activity.runOnUiThread(new Runnable() {


                    public void run() {
                        //username.setText(the_username);


                        if(following_or_nah)
                        {
                            follow_button.setImageResource(R.drawable.check);
                        }
                        else
                        {
                            follow_button.setImageResource(R.drawable.plus);
                        }


                    }
                });
            }
        }, 0, 1000);

        followers_tv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("Textview Click", "Detected");
                main_activity.followers_or_following = "followers";
                main_activity.fragmentTransaction = main_activity.fm.beginTransaction();

                main_activity.fr = new UserProfile_List();
                main_activity.fragmentTransaction.addToBackStack(null);
                main_activity.fragmentTransaction.replace(R.id.fragment_container, main_activity.fr, "Profiles_List").commit();
            }
        });

        following_tv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("Textview Click", "Detected");
                main_activity.followers_or_following = "following";
                main_activity.fragmentTransaction = main_activity.fm.beginTransaction();

                main_activity.fr = new UserProfile_List();
                main_activity.fragmentTransaction.addToBackStack(null);
                main_activity.fragmentTransaction.replace(R.id.fragment_container, main_activity.fr, "Profiles_List").commit();
            }
        });

        new Get_Following_Number().execute();
        new Get_Followers_Number().execute();


        return rootview;

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
                        main_activity.my_toast.cancel();
                    }catch (Exception e)
                    {

                    }
                    main_activity.my_toast = Toast.makeText(getActivity(), "Followed " + main_activity.current_following_name,
                            Toast.LENGTH_SHORT);
                    main_activity.my_toast.setGravity(Gravity.CENTER, 0, 0);
                    main_activity.my_toast.show();
                    main_activity.following_or_nah = true;
                    follow_button.setImageResource(R.drawable.check);
                }
                else
                {
                    new UnfollowUser().execute(main_activity.current_following_id, main_activity.current_user_id);
                    try {
                        main_activity.my_toast.cancel();
                    }catch (Exception e)
                    {

                    }
                    main_activity.my_toast = Toast.makeText(getActivity(), "Unfollowed " + main_activity.current_following_name,
                            Toast.LENGTH_SHORT);
                    main_activity.my_toast.setGravity(Gravity.CENTER, 0, 0);
                    main_activity.my_toast.show();
                    main_activity.following_or_nah = false;
                    follow_button.setImageResource(R.drawable.plus);
                }
                break;


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
            params.add(new BasicNameValuePair("current_following_id", main_activity.user_profile_id));



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
                        following_or_nah = true;
                    }
                    else
                    {
                        following_or_nah = false;
                    }

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



    class Get_Following_Number extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... args) {


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_profile_id", main_activity.user_profile_id));



            // getting product details by making HTTP request
            // Note that product details url will use GET request
            JSONObject json = main_activity.jsonParser.makeHttpRequest(
                    main_activity.url_get_following_number, "GET", params);
            try {

                int success = json.getInt(main_activity.TAG_SUCCESS);
                if (success == 1) {
                    // successfully received product details
                    JSONArray productObj = json
                            .getJSONArray("user"); // JSON Array

                    // get first product object from JSON Array
                    JSONObject product = productObj.getJSONObject(0);
                    following = product.getString("following_number");


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
            following_tv.setText(following + " Following");


        }
    }

    class Get_Followers_Number extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... args) {


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_profile_id", main_activity.user_profile_id));



            // getting product details by making HTTP request
            // Note that product details url will use GET request
            JSONObject json = main_activity.jsonParser.makeHttpRequest(
                    main_activity.url_get_followers_number, "GET", params);
            try {

                int success = json.getInt(main_activity.TAG_SUCCESS);
                if (success == 1) {
                    // successfully received product details
                    JSONArray productObj = json
                            .getJSONArray("user"); // JSON Array

                    // get first product object from JSON Array
                    JSONObject product = productObj.getJSONObject(0);
                    followers = product.getString("followers_number");


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
            followers_tv.setText(followers + " Followers");


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
