package com.example.androidhive;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.ToggleButton;


import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class Explore extends AppCompatActivity{


    public String current_user_id;
    public String current_following_id;
    public JSONParser jsonParser = new JSONParser();
    public BroadcastReceiver receiver;
    public boolean playing;
    public String is_playing;
    public boolean receiver_exists = false;
    public LinearLayout mDrawerLayout;
    public DrawerLayout Drawer;
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
    public static String url_get_uri = "http://" + ip + "/get_uri.php";
    public static String url_increment_listeners = "http://" + ip + "/increment_listeners.php";
    public static String url_decrement_listeners = "http://" + ip + "/decrement_listeners.php";



    //Explore List Stuff
    public ListView mExploreList;
    public LinearLayout mExploreListviewElement;
    List<String> image_locations;
    HashMap<String, List<String>> listDataChild;
    ArrayList<String> listDataHeader;
    List<String> user_options;

    ListView lv;

    ArrayList<Broadcast> arrayOfBroadcasts;
    Following_Fragment.BroadcastAdapter adapter;
    public ArrayList<String> pid_list;



    public static final String TAG_PRODUCTS = "users";
    public static final String TAG_NAME = "name";
    public static final String TAG_SUCCESS = "success";





    // products JSONArray
    JSONArray products = null;



    // Progress Dialog


    private ProgressDialog pdialog;
    // url to create new product

    android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
    public Fragment fr;




    public final static String EXTRA_MESSAGE = "com.mycompany.app.MESSAGE";
    public AuthenticationResponse response;
    public static final int REQUEST_CODE = 1337;
    // TODO: Replace with your client ID
    public static final String CLIENT_ID = "57b61875218e4e1f8a8d0cdb57a7259b";
    // TODO: Replace with your redirect URI
    public static final String REDIRECT_URI = "sync-me-up://callback";


    //Runs "LoadAllProducts" and begins the Spotify login
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            Explore_Fragment firstFragment = new Explore_Fragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment, "Explore").commit();
        }

        pdialog = new ProgressDialog(Explore.this);
        pdialog.setMessage("Loading profile. Please wait...");
        pdialog.setIndeterminate(false);
        pdialog.setCancelable(false);
        pdialog.show();


        //Set ListView for Drawer
        mDrawerLayout = (LinearLayout) findViewById(R.id.drawer_linear_layout);
        //mDrawerSwitch = (Switch) findViewById(R.id.drawer_switch);
        Drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        addDrawerItems();

        broad_button = (ToggleButton) findViewById(R.id.toggBtn);

        broad_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(((ToggleButton) v).isChecked()) {
                    currently_broadcasting = true;
                    new StartBroadcast().execute();
                }
                else {
                    currently_broadcasting = false;
                    new StopBroadcast().execute();
                }
            }
        });



        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);





        listDataHeader = new ArrayList<String>();
        pid_list = new ArrayList<String>();
        pid_list = new ArrayList<String>();


        arrayOfBroadcasts = new ArrayList<Broadcast>();
        //new LoadAllProducts().execute();





    }

    public void TuneIn(){
        android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fr = new TuneIn_Fragment();
        fragmentTransaction.replace(R.id.fragment_container, fr);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

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
                if (position == 0) {
                    Explore_Fragment my_otherfragment = (Explore_Fragment) getSupportFragmentManager().findFragmentByTag("Explore");
                    if (my_otherfragment != null && my_otherfragment.isVisible())
                    {

                    }
                    else
                    {
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fr = new Explore_Fragment();
                        fragmentTransaction.replace(R.id.fragment_container, fr, "Explore");
                        fragmentTransaction.commit();
                    }
                    Drawer.closeDrawers();
                }
                if (position == 1) {
                    Following_Fragment my_otherfragment = (Following_Fragment)getSupportFragmentManager().findFragmentByTag("Following");
                    if (my_otherfragment != null && my_otherfragment.isVisible())
                    {

                    }
                    else
                    {
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fr = new Following_Fragment();
                        fragmentTransaction.replace(R.id.fragment_container, fr, "Following");
                        Explore_Fragment myFragment = (Explore_Fragment) getSupportFragmentManager().findFragmentByTag("Explore");
                        if (myFragment != null && myFragment.isVisible()) {
                            fragmentTransaction.addToBackStack(null);
                        }
                        fragmentTransaction.commit();
                    }

                    Drawer.closeDrawers();
                }
                if (position == 2) {

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



    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            response = AuthenticationClient.getResponse(resultCode, intent);

            SpotifyApi my_api = new SpotifyApi();
            my_api.setAccessToken(response.getAccessToken());
            SpotifyService spotify = my_api.getService();

            spotify.getMe(new Callback<UserPrivate>() {
                @Override
                public void success(UserPrivate userPrivate, Response response) {
                    Log.d("User Success", userPrivate.id);
                    String name = userPrivate.display_name.toString();
                    current_user_id = userPrivate.id.toString();
                    new CreateNewProduct().execute(name, current_user_id);

                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("User Failure", error.toString());
                }
            });
        }
    }





        class CreateNewProduct extends AsyncTask<String, String, String> {

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
                params.add(new BasicNameValuePair("id", args[1]));
                params.add(new BasicNameValuePair("name", args[0]));


                // getting JSON Object
                // Note that create product url accepts POST method
                Log.d("URL", url_create_product);
                JSONObject json = jsonParser.makeHttpRequest(url_create_product,
                        "POST", params);

                // check log cat fro response
                Log.d("Create Response", json.toString());

                // check for success tag
                try {
                    int success = json.getInt(TAG_SUCCESS);

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
                pdialog.dismiss();

            }
        }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        menu.setHeaderTitle("More Info");
        menu.setHeaderIcon(R.drawable.guppy);
        inflater.inflate(R.menu.menu, menu);
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        new StopBroadcast().execute();
    }
}

