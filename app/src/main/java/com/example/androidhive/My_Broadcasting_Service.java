package com.example.androidhive;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.content.BroadcastReceiver;


import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ErrorDetails;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.R.id.message;
import static com.example.androidhive.Explore.MyPREFERENCES;
import static com.example.androidhive.Explore.TAG_SUCCESS;

public class My_Broadcasting_Service extends Service {

    public static String ip = "52.38.141.152";
    public static String url_post_uri = "http://" + ip + "/post_uri.php";
    public static String url_play_playback = "http://" + ip + "/play_playback.php";
    public static String url_pause_playback = "http://" + ip + "/pause_playback.php";
    public static String url_spotify_refresh_token = "http://" + ip + "/spotify_refresh_token.php";
    public BroadcastReceiver receiver;
    public String gid = "";
    public Long broadcast_receive_time = 0L;
    public Long song_duration = 0L;
    public JSONParser jsonParser = new JSONParser();
    String trackId;
    String artistName;
    String albumName;
    String trackName;
    public Boolean albumArtDone = false;
    public SpotifyService spotify;
    public SpotifyApi my_api;
    public SetURI uri_posting;
    public String albumArt;
    public ChangePlayback change_playback;
    public String is_playing;
    IntentFilter filter;
    public boolean broadcaster_playing;
    public String spotify_access_token;
    public String spotify_refresh_token;
    public int spotify_access_token_time;
    public static final String MyPREFERENCES = "MyPrefs" ;
    public Timer timer;
    public static String SONGCHANGE = "songchange";
    public static String PLAYBACK = "playback";
    public LocalBroadcastManager broadcaster;
    private Intent intent;











    public My_Broadcasting_Service() {



        receiver = new MyBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getLongExtra("timeSent", 0L) >= broadcast_receive_time)
                {
                    Long broadcast_receive_time = intent.getLongExtra("timeSent", 0L);
                }

                String action = intent.getAction();



                int trackLengthInSec = intent.getIntExtra("length", 0);
                if (action.equals(BroadcastTypes.METADATA_CHANGED)) {
                    //GetAlbumArt running_task = new GetAlbumArt();
                    //running_task.execute(trackId);
                    //change_playback.cancel(true);


                    try {

                        trackId = intent.getStringExtra("id").replace("'", "''");
                        artistName = intent.getStringExtra("artist").replace("'", "''");
                        albumName = intent.getStringExtra("album").replace("'", "''");
                        trackName = intent.getStringExtra("track").replace("'", "''");
                        int temp = (intent.getIntExtra("length", 0));
                        temp = temp / 1000;
                        song_duration = new Long(temp);


                        Log.d("RYAN - GID", gid);
                        Log.d("RYAN - Broadcast", trackName + " " + albumName + " " + artistName + " " + song_duration.toString());

                        albumArtDone = false;
                        String newTrackId = trackId.substring(14);

                        spotify.getTrack(newTrackId, new Callback<Track>() {
                            @Override
                            public void success(Track track, Response response) {
                                albumArt = track.album.images.get(0).url;
                                //song_duration = track.duration_ms;
                                uri_posting = new SetURI();
                                uri_posting.execute(trackId, trackName, albumName, artistName, albumArt);
                                change_playback = new ChangePlayback();
                                change_playback.execute("true");


                            }

                            @Override
                            public void failure(RetrofitError error) {
                            }
                        });
                    }
                    catch (Exception e){
                        Log.d("RYAN", e.toString());
                    }
                }

                if (action.equals(BroadcastTypes.PLAYBACK_STATE_CHANGED)) {
                    boolean temp_broadcaster_playing = intent.getBooleanExtra("playing", false);
                    Log.d("RYAN - Playback", Boolean.toString(temp_broadcaster_playing));
                    //int positionInMs = intent.getIntExtra("playbackPosition", 0);
                    if (broadcaster_playing != temp_broadcaster_playing) {

                        if (temp_broadcaster_playing) {
                            is_playing = "true";
                        } else {
                            is_playing = "false";
                        }
                        change_playback = new ChangePlayback();
                        change_playback.execute(is_playing);
                        broadcaster_playing = temp_broadcaster_playing;
                    }
                }

            }

        };
    }



    void handleCommand(Intent intent) {
        SharedPreferences preferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        gid = preferences.getString("gid", "");
        spotify_access_token = preferences.getString("token", "");
        spotify_access_token_time = preferences.getInt("token_time", 0);
        spotify_refresh_token = preferences.getString("refresh", "");



        //gid = intent.getStringExtra("gid");
        //spotify_access_token = intent.getStringExtra("token");
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        handleCommand(intent);
        Log.d("RYAN", spotify_access_token);
        Log.d("RYAN", Integer.toString(spotify_access_token_time));
        Log.d("RYAN - Refresh", spotify_refresh_token);

        timer = new Timer();
        timer.schedule(new RefreshToken(), spotify_access_token_time*1000);



        my_api = new SpotifyApi();
        my_api.setAccessToken(spotify_access_token);
        spotify = my_api.getService();

        try {
            registerReceiver(receiver, filter);
            Log.d("RYAN", "Receiver was registered");
        }
        catch (Exception e){
            Log.d("RYAN", e.toString());
        }
        return START_STICKY;
    }

    public class RefreshToken extends TimerTask {
        @Override
        public void run() {
            new RefreshSpotifyToken().execute(spotify_refresh_token);
            Log.d("RYAN", "TIMER RUN");
        }
    }



    class RefreshSpotifyToken extends AsyncTask<String, String, String> {

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
            params.add(new BasicNameValuePair("auth_code", args[0]));



            // getting product details by making HTTP request
            // Note that product details url will use GET request
            JSONObject json = jsonParser.makeHttpRequest(url_spotify_refresh_token, "GET", params);

            try {

                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // successfully received product details
                    JSONArray productObj = json.getJSONArray("users"); // JSON Array

                    // get first product object from JSON Array
                    JSONObject product = productObj.getJSONObject(0);
                    spotify_access_token = product.getString("access_token");
                    spotify_access_token_time = product.getInt("expires_in");





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
        protected void onPostExecute(String temp) {
            // dismiss the dialog once got all details

            timer = new Timer();
            timer.schedule(new RefreshToken(), spotify_access_token_time*1000);
            my_api = new SpotifyApi();
            my_api.setAccessToken(spotify_access_token);
            spotify = my_api.getService();

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

            Log.d("Changing Playback",args[0]);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", gid));


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
                    //Intent i = getIntent();
                    // send result code 100 to notify about product update
                    //setResult(100, i);

                } else {
                    // failed to update product
                }
            } catch (Exception e) {
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
            intent = new Intent(PLAYBACK);
            if (is_playing.equals("true")){
                intent.putExtra("playback", true);
            }
            else{
                intent.putExtra("playback", false);
            }
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
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

            Log.d("RYAN - GID", gid);
            Log.d("RYAN - URI", args[0]);
            Log.d("RYAN - SONG", args[1]);
            Log.d("RYAN - ALBUM", args[2]);
            Log.d("RYAN - ARTIST", args[3]);
            Log.d("RYAN - ALBUM ART", args[4]);
            Log.d("RYAN - SONG DURATION", song_duration.toString());




            params.add(new BasicNameValuePair("id", gid));
            params.add(new BasicNameValuePair("uri", args[0]));
            params.add(new BasicNameValuePair("song", args[1]));
            params.add(new BasicNameValuePair("album", args[2]));
            params.add(new BasicNameValuePair("artist", args[3]));
            params.add(new BasicNameValuePair("album_art_large", args[4]));
            params.add(new BasicNameValuePair("song_duration", song_duration.toString()));




            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_post_uri,
                    "POST", params);


            // check json success tag
            try {
                int success = json.getInt("success");

                if (success == 1) {
                    // successfully updated
                    //Intent i = getIntent();
                    // send result code 100 to notify about product update
                    //setResult(100, i);
                    Log.d("RYAN - Post URI", "Success");

                } else {
                    // failed to update product
                    Log.d("RYAN - Post URI", "Failure");
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
            Log.d("RYAN - Posting URL", "It worked");
            intent = new Intent(SONGCHANGE);
            intent.putExtra("song", trackName);
            intent.putExtra("album", albumName);
            intent.putExtra("artist", artistName);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }



    @Override
    public void onCreate(){
        super.onCreate();
        filter = new IntentFilter();
        filter.addAction("com.spotify.music.playbackstatechanged");
        filter.addAction("com.spotify.music.metadatachanged");
        filter.addAction("com.spotify.music.queuechanged");
        broadcaster = LocalBroadcastManager.getInstance(this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
