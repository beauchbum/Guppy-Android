package com.example.androidhive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.spotify.sdk.android.authentication.AuthenticationClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class BaseActivity extends AppCompatActivity {

    public String current_user_id;
    public JSONParser jsonParser = new JSONParser();
    public BroadcastReceiver receiver;
    public boolean playing;
    public String is_playing;
    public boolean receiver_exists = false;
    public LinearLayout mDrawerLayout;
    public ListView mDrawerList;
    public TextView drawerSwitchStatus;
    public ArrayAdapter<String> mAdapter;
    public ToggleButton broad_button;
    public boolean currently_broadcasting = false;
    public boolean broadcast_status = false;
    public static final String TAG_PID = "id";


    public static String ip = "52.38.141.152";

    // url to get all products list
    public static String url_all_products = "http://" + ip + "/get_all_products.php";
    public static final String url_start_broadcast = "http://" + ip + "/start_broadcast.php";
    public static final String url_stop_broadcast = "http://" + ip + "/stop_broadcast.php";
    public static String url_create_product = "http://" + ip + "/create_product.php";
    public static String url_post_uri = "http://" + ip + "/post_uri.php";
    public static String url_play_playback = "http://" + ip + "/play_playback.php";
    public static String url_pause_playback = "http://" + ip + "/pause_playback.php";

    public SharedPreferences.Editor editor;
    public SharedPreferences sharedPref;





    protected void onCreateDrawer() {

        //Set ListView for Drawer
        mDrawerLayout = (LinearLayout) findViewById(R.id.drawer_linear_layout);
        //mDrawerSwitch = (Switch) findViewById(R.id.drawer_switch);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        addDrawerItems();
        broad_button = (ToggleButton) findViewById(R.id.toggBtn);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPref.edit();




        broad_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(((ToggleButton) v).isChecked()) {
                    currently_broadcasting = true;
                    //editor.clear();
                    editor.putBoolean("broadcast_status", true);
                    editor.commit();
                    new StartBroadcast().execute();
                }
                else {
                    currently_broadcasting = false;
                    //editor.clear();
                    editor.putBoolean("broadcast_status", false);
                    editor.commit();
                    new StopBroadcast().execute();
                }
            }
        });

    }

    private void addDrawerItems() {
        // More Drawer Stuff
        String[] mDrawerTitles = {"Home", "Following", "Settings", "Logout"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDrawerTitles) {
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView textView = (TextView) view.findViewById(android.R.id.text1);

            /*YOUR CHOICE OF COLOR*/
                textView.setTextColor(Color.WHITE);

                return view;
            }
        };

        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                {
                    Intent in = new Intent(getApplicationContext(),
                            Explore.class);
                    //in.putExtra(TAG_PID, current_user_id);
                    startActivity(in);
                }
                if (position == 1)
                {
                    Intent in = new Intent(getApplicationContext(),
                            MainScreenActivity.class);
                    //in.putExtra(TAG_PID, current_user_id);
                    startActivity(in);
                }
                if(position == 2)
                {
                    Intent in = new Intent(getApplicationContext(),
                            SettingsActivity.class);
                    startActivity(in);
                }
                if (position == 3) {
                    //AuthenticationClient.logout(getApplicationContext());
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(i);
                }
            }
        });
    }





    public class StartBroadcast extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Saving product
         * */
        protected String doInBackground(String... args) {

			/*
			// getting updated data from EditTexts
			String name = txtName.getText().toString();
			String price = txtPrice.getText().toString();
			String description = txtDesc.getText().toString();
			*/

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", current_user_id));

            Log.d("Broadcast", "Started");


            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_start_broadcast,
                    "POST", params);



            // check json success tag
            try {
                int success = json.getInt("success");

                if (success == 1) {
                    // successfully updated
                    Intent i = getIntent();
                    // send result code 100 to notify about product update
                    setResult(100, i);

                } else {
                    // failed to update product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            IntentFilter filter = new IntentFilter();
            filter.addAction("com.spotify.music.playbackstatechanged");
            filter.addAction("com.spotify.music.metadatachanged");
            filter.addAction("com.spotify.music.queuechanged");



            receiver = new MyBroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long timeSentInMs = intent.getLongExtra("timeSent", 0L);
                    String action = intent.getAction();

                    String trackId = intent.getStringExtra("id");
                    String artistName = intent.getStringExtra("artist");
                    String albumName = intent.getStringExtra("album");
                    String trackName = intent.getStringExtra("track");
                    Log.d("trackId", trackId);
                    int trackLengthInSec = intent.getIntExtra("length", 0);
                    if (action.equals(BroadcastTypes.METADATA_CHANGED)){
                        new SetURI().execute(trackId, trackName, albumName, artistName);
                    }
                    if (action.equals(BroadcastTypes.PLAYBACK_STATE_CHANGED)) {
                        playing = intent.getBooleanExtra("playing", false);
                        //int positionInMs = intent.getIntExtra("playbackPosition", 0);
                        if (playing){
                            is_playing = "true";
                        }
                        else{
                            is_playing = "false";
                        }
                        new ChangePlayback().execute(is_playing);
                    }


                }
            };
            registerReceiver(receiver, filter);
            receiver_exists = true;

            return null;
        }


        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product uupdated
            //pDialog.dismiss();
        }
    }

    class StopBroadcast extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Saving product
         * */
        protected String doInBackground(String... args) {

            // Building Parameters
            if(receiver_exists){
                unregisterReceiver(receiver);
            }
            receiver_exists = false;
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", current_user_id));

            Log.d("Broadcast", "Stopped");


            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_stop_broadcast,
                    "POST", params);

            // check json success tag
            try {
                int success = json.getInt("success");

                if (success == 1) {
                    // successfully updated
                    Intent i = getIntent();
                    // send result code 100 to notify about product update
                    setResult(100, i);

                } else {
                    // failed to update product
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
            // dismiss the dialog once product uupdated
            //pDialog.dismiss();
        }
    }

    class ChangePlayback extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Saving product
         * */
        protected String doInBackground(String... args) {

			/*
			// getting updated data from EditTexts
			String name = txtName.getText().toString();
			String price = txtPrice.getText().toString();
			String description = txtDesc.getText().toString();
			*/


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", current_user_id));


            Log.d("Setting URI", params.toString());
            JSONObject json;


            // sending modified data through http request
            // Notice that update product url accepts POST method
            if(args[0].equals("true")){
                json = jsonParser.makeHttpRequest(url_play_playback,
                        "POST", params);
            }
            else{
                json = jsonParser.makeHttpRequest(url_pause_playback,
                        "POST", params);
            }


            // check json success tag
            try {
                int success = json.getInt("success");

                if (success == 1) {
                    // successfully updated
                    Intent i = getIntent();
                    // send result code 100 to notify about product update
                    setResult(100, i);

                } else {
                    // failed to update product
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
            // dismiss the dialog once product uupdated
            //pDialog.dismiss();
        }
    }

    class SetURI extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Saving product
         * */
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", current_user_id));
            params.add(new BasicNameValuePair("uri", args[0]));
            params.add(new BasicNameValuePair("song", args[1]));
            params.add(new BasicNameValuePair("album", args[2]));
            params.add(new BasicNameValuePair("artist", args[3]));

            Log.d("Setting URI", params.toString());


            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_post_uri,
                    "POST", params);

            // check json success tag
            try {
                int success = json.getInt("success");

                if (success == 1) {
                    // successfully updated
                    Intent i = getIntent();
                    // send result code 100 to notify about product update
                    setResult(100, i);

                } else {
                    // failed to update product
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
            // dismiss the dialog once product uupdated
            //pDialog.dismiss();
        }
    }
}
