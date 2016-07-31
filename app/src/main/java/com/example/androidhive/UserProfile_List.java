package com.example.androidhive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by Ryan on 5/9/2016.
 */
public class UserProfile_List extends Fragment implements
        View.OnClickListener ,PlayerNotificationCallback, ConnectionStateCallback {

    public Explore main_activity;
    public TextView followers_tv;
    public TextView following_tv;
    public TextView listeners_tv;
    public String followers;
    public String following;
    public String listeners;
    public boolean following_or_nah;
    public ProgressDialog pdialog;
    public View rootview;
    ArrayList<HashMap<String, String>> productsList;
    ArrayList<UserProfile> arrayOfUserProfiles;
    public ArrayList<String> pid_list;
    public ArrayList<String> name_list;
    public ArrayList<String> broadcasting_list;
    public ListView userprofiles_lv;
    ArrayList<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    List<String> user_options;







    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.userprofile_followers, container, false);
        main_activity = (Explore) getActivity();

        main_activity.tuning_in = true;


        android.support.v7.app.ActionBar bar = main_activity.getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));

        if(main_activity.followers_or_following == "following")
        {
            bar.setTitle(Html.fromHtml("<font color='#ffffff'>" + main_activity.user_profile_name + " Following" + "</font>"));
        }
        else
        {
            bar.setTitle(Html.fromHtml("<font color='#ffffff'>" + main_activity.user_profile_name + " Followers" + "</font>"));

        }
        main_activity.stop_tune_in.setTitle("Tune Out " + main_activity.current_following_name);
        //main_activity.see_profile.setTitle("See " + main_activity.current_following_name + "'s Profile");
        main_activity.stop_tune_in.setVisible(true);
        //main_activity.see_profile.setVisible(true);

        arrayOfUserProfiles = new ArrayList<UserProfile>();
        pid_list = new ArrayList<String>();
        name_list = new ArrayList<String>();
        userprofiles_lv = (ListView) rootview.findViewById(R.id.followers_lv);
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();





        new LoadProfiles().execute();





        return rootview;

    }

    class LoadProfiles extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

                pdialog = new ProgressDialog(getActivity());
                pdialog.setMessage("Loading Followers. Please wait...");
                pdialog.setIndeterminate(false);
                pdialog.setCancelable(false);
                pdialog.show();


        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json;
            params.add(new BasicNameValuePair("user_profile_id", main_activity.user_profile_id));
            Log.d("User Profile Id", main_activity.user_profile_id);
            if (main_activity.followers_or_following == "followers") {
                json = main_activity.jsonParser.makeHttpRequest(main_activity.url_get_followers_profiles, "GET", params);
            }
            else
            {
                json = main_activity.jsonParser.makeHttpRequest(main_activity.url_get_following_profiles, "GET", params);
            }

            // Check your log cat for JSON reponse

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(main_activity.TAG_SUCCESS);


                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    main_activity.products = json.getJSONArray(main_activity.TAG_PRODUCTS);


                    // looping through All Products
                    for (int i = 0; i < main_activity.products.length(); i++) {
                        JSONObject c = main_activity.products.getJSONObject(i);
                        boolean exists = false;

                        // Storing each json item in variable
                        String id = c.getString("guppy_id");
                        String name = c.getString("name");



                        arrayOfUserProfiles.add(new UserProfile(name, "guppy"));
                        pid_list.add(i, id);
                        name_list.add(i, name);




                        System.out.println(name);

                        for (String str: listDataHeader){
                            if(str.trim().contains(name))
                                exists = true;
                        }
                        if(!exists) {
                            listDataHeader.add(name);
                            listDataChild.put(name, user_options);
                            pid_list.add(id);
                        }



                    }
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
            // dismiss the dialog after getting all products

            // updating UI from Background Thread
            main_activity.runOnUiThread(new Runnable() {
                public void run() {




                    UserProfileAdapter adapter = new UserProfileAdapter(main_activity.getApplicationContext(), arrayOfUserProfiles);
                    userprofiles_lv.setAdapter(adapter);


                    userprofiles_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            main_activity.SeeProfile(pid_list.get(position), name_list.get(position));
                        }
                    });




                }
            });
            pdialog.cancel();


        }

    }

    public class UserProfileAdapter extends ArrayAdapter<UserProfile> {

        private final int VIEW_TYPE_SELECTED = 1;
        private final int VIEW_TYPE_NORMAL = 0;

        public UserProfileAdapter(Context context, ArrayList<UserProfile> userProfiles)
        {
            super(context, 0, userProfiles);
        }

        @Override
        public int getViewTypeCount()
        {
            return 2;
        }

        @Override
        public int getItemViewType(int position)
        {
            return (position == 1) ? VIEW_TYPE_SELECTED : VIEW_TYPE_NORMAL;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            UserProfile mUserProfile = getItem(position);
            int viewType = getItemViewType(position);
            int layoutId = -1;
            if (convertView == null)
            {
                //if (viewType == VIEW_TYPE_SELECTED)
                //{
                //	layoutId = R.layout.list_item_selected;
                //}
                //else
                //{
                layoutId = R.layout.userprofile_list_item;
                //}
                convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            }
            TextView userTextView = (TextView) convertView.findViewById(R.id.name);
            ImageView profilePicImage = (ImageView) convertView.findViewById(R.id.list_item_image);

            int resId = getResources().getIdentifier(mUserProfile.image_title, "drawable", getActivity().getPackageName());
            profilePicImage.setImageResource(resId);
            userTextView.setText(mUserProfile.username);


            return convertView;
        }

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.followers:


            case R.id.following:

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



    class Get_Following_Number extends AsyncTask<String, String, String> {

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
            pdialog.dismiss();

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
