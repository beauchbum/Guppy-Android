package com.example.androidhive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class Menu_Activity extends Activity {

    private String id;
    JSONParser jsonParser = new JSONParser();

    private boolean stop_start;


    private static String ip = "52.38.141.152";
    // url to get all products list
    private static String url_all_products = "http://" + ip + "/get_all_products.php";
    private static final String url_start_broadcast = "http://" + ip + "/start_broadcast.php";
    private static final String url_stop_broadcast = "http://" + ip + "/stop_broadcast.php";
    private static String url_create_product = "http://" + ip + "/create_product.php";
    private static String url_post_uri = "http://" + ip + "/post_uri.php";
    private static String url_play_playback = "http://" + ip + "/play_playback.php";
    private static String url_pause_playback = "http://" + ip + "/pause_playback.php";

    private MyBroadcastReceiver receiver;
    private boolean receiver_exists = false;

    private String is_playing;
    private boolean playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        id = intent.getStringExtra(Explore.TAG_PID);
        Log.d("ID", id);
        stop_start = intent.getBooleanExtra(Explore.EXTRA_MESSAGE, false);
        if(stop_start)
        {
            new StartBroadcast().execute();
        }
        else
        {
            new StopBroadcast().execute();
        }
        finish();


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
            params.add(new BasicNameValuePair("id", id));

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
            params.add(new BasicNameValuePair("id", id));

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
            params.add(new BasicNameValuePair("id", id));


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
            params.add(new BasicNameValuePair("id", id));
            params.add(new BasicNameValuePair("uri", args[0]));

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

    @Override
    public void onDestroy(){
        new StopBroadcast().execute();
        super.onDestroy();
    }
}
