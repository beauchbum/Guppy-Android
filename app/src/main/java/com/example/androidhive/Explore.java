package com.example.androidhive;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.JsonObject;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ErrorDetails;
import kaaes.spotify.webapi.android.models.Followers;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.google.android.gms.gcm.GoogleCloudMessaging.INSTANCE_ID_SCOPE;


public class Explore extends AppCompatActivity{



    //Explore Fragment
    public ListView mExploreList;
    public LinearLayout mExploreListviewElement;
    List<String> image_locations;
    HashMap<String, List<String>> listDataChild;
    ArrayList<String> listDataHeader;
    List<String> user_options;
    ListView lv;

//////////////////////////////////////////////////////////////

    //Tune In Fragment
    public boolean paused_playback;
    public boolean broadcaster_playing;
    public boolean tuning_in = false;
    public String current_following_gid;
    public String current_following_sid;
    public String current_following_name;
    public boolean following_or_nah = false;
    public Player mPlayer;
    public ScheduledThreadPoolExecutor exec;

//////////////////////////////////////////////////////////////

    //Following Fragment
    public ArrayAdapter<String> mAdapter;
    ArrayList<Broadcast> arrayOfBroadcasts;
    Following_Fragment.BroadcastAdapter adapter;
    public ArrayList<String> gid_list;
    public ArrayList<String> sid_list;
    public ArrayList<String> name_list;
    public ArrayList<String> broadcasting_list;
    public String popup_current_following_gid;
    public String popup_current_following_sid;
    public String popup_current_following_name;

//////////////////////////////////////////////////////////////

    //Broadcasting
    public ToggleButton broad_button;
    public Switch broad_switch;
    public boolean broadcast_status = false;
    public boolean isBroadcaster_playing = false;
    public boolean is_playing = false;
    public boolean receiver_exists = false;
    public String current_user_id;
    public boolean user_always_broadcasting = false;
    public String spotify_id;
    public String spotify_profile_url;
    public Bitmap albumArtImage;
    public String albumArt;
    public SpotifyApi my_api;
    public String SpotifyAccessToken;
    public int SpotifyAccessTokenExpires;
    public String SpotifyRefreshToken;
    public SpotifyService spotify;
    public Boolean albumArtDone = false;
    String trackId;
    public SetURI uri_posting;
    public ChangePlayback change_playback;
    public Long broadcast_receive_time = 0L;
    public Long song_duration = 0L;
    public Timer broadcast_timer;
    public String GCM_IID = "";
    String authorizedEntity = "";
    String scope = "";
    String token = "";

//////////////////////////////////////////////////////////////

    //Navigation of Fragments
    public LinearLayout mDrawerLayout;
    public DrawerLayout Drawer;
    public ListView mDrawerList;
    public TextView drawerSwitchStatus;
    android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
    public Fragment fr;
    public Fragment tuning_in_fragment;
    public Fragment my_broadcast_fragment;
    public Toast my_toast;
    public ProgressDialog pdialog;
    private ActionBarDrawerToggle mDrawerToggle;
    public Menu my_menu;
    public MenuItem stop_tune_in;
    public MenuItem see_profile;
    public android.support.v4.app.FragmentTransaction fragmentTransaction;
    public TuneIn_Fragment my_otherfragment;
    public Explore_Fragment temp_explore_fragment;
    public Following_Fragment temp_following_fragment;
    public MyBroadcast temp_broadcasting_fragment;
    private IntentFilter song_filter = new IntentFilter(My_Broadcasting_Service.SONGCHANGE);
    private IntentFilter playback_filter = new IntentFilter(My_Broadcasting_Service.PLAYBACK);


//////////////////////////////////////////////////////////////

    //My Broadcast
    public boolean broadcast_working = false;
    String trackName = "";
    String artistName = "";
    String albumName = "";
    public BroadcastReceiver song_receiver;
    public BroadcastReceiver playback_receiver;
    public String spotify_profile_pic;







//////////////////////////////////////////////////////////////

    //General Use
    public JSONParser jsonParser = new JSONParser();
    public static final String TAG_PID = "id";

    public final static String EXTRA_MESSAGE = "com.mycompany.app.MESSAGE";
    public AuthenticationResponse response;
    public static final int REQUEST_CODE = 1337;
    public static final int REQUEST_TOKEN = 1338;
    // TODO: Replace with your client ID
    public static final String CLIENT_ID = "57b61875218e4e1f8a8d0cdb57a7259b";
    // TODO: Replace with your redirect URI
    public static final String REDIRECT_URI = "sync-me-up://callback";
    public static final String TAG_PRODUCTS = "users";
    public static final String TAG_NAME = "name";
    public static final String TAG_SUCCESS = "success";
    public static final String MyPREFERENCES = "MyPrefs" ;

//////////////////////////////////////////////////////////////

    // PHP Script URLs
    public static String ip = "52.38.141.152";
    public static String url_get_following = "http://" + ip + "/get_following.php";
    public static String url_search_users = "http://" + ip + "/search_users.php";
    public static final String url_start_broadcast = "http://" + ip + "/start_broadcast.php";
    public static final String url_stop_broadcast = "http://" + ip + "/stop_broadcast.php";
    public static final String url_always_broadcasting = "http://" + ip + "/always_broadcasting.php";
    public static final String url_not_always_broadcasting = "http://" + ip + "/not_always_broadcasting.php";
    public static String url_create_product = "http://" + ip + "/create_product.php";
    public static String url_post_uri = "http://" + ip + "/post_uri.php";
    public static String url_play_playback = "http://" + ip + "/play_playback.php";
    public static String url_pause_playback = "http://" + ip + "/pause_playback.php";
    public static String url_get_uri = "http://" + ip + "/get_uri.php";
    public static String url_increment_listeners = "http://" + ip + "/increment_listeners.php";
    public static String url_decrement_listeners = "http://" + ip + "/decrement_listeners.php";
    public static String url_follow_user = "http://" + ip + "/follow_user.php";
    public static String url_unfollow_user = "http://" + ip + "/unfollow_user.php";
    public static String url_get_following_or_nah = "http://" + ip + "/get_following_or_not.php";
    public static String url_get_following_number = "http://" + ip + "/get_following_number.php";
    public static String url_get_followers_number = "http://" + ip + "/get_followers_number.php";
    public static String url_get_followers_profiles = "http://" + ip + "/get_followers_profiles.php";
    public static String url_get_following_profiles = "http://" + ip + "/get_following_profiles.php";
    public static String url_get_guppy_id = "http://" + ip + "/get_guppy_id.php";
    public static String url_get_spotify_token = "http://" + ip + "/get_spotify_token.php";



//////////////////////////////////////////////////////////////

    // products JSONArray
    JSONArray products = null;

    //Runs "LoadAllProducts" and begins the Spotify login
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base);
        // URL-safe characters up to a maximum of 1000, or
        // you can also leave it blank.


        uri_posting = new SetURI();
        change_playback = new ChangePlayback();

        new GCM_Token().execute();

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

        my_toast = Toast.makeText(getApplicationContext(), "User Not Broadcasting", Toast.LENGTH_SHORT);
        my_toast.setGravity(Gravity.CENTER, 0, 0);
        song_duration = 0L;
        broadcast_receive_time = 0L;

        mDrawerLayout = (LinearLayout) findViewById(R.id.drawer_linear_layout);
        //mDrawerSwitch = (Switch) findViewById(R.id.drawer_switch);
        Drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        String[] mDrawerTitles = {"Home", "Following", "My Broadcast", "Settings", "Logout"};
        addDrawerItems(mDrawerTitles);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                Drawer,         /* DrawerLayout object */
                //R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //().setTitle(mDrawerTitle);
            }
        };

        Drawer.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);




        broad_button = (ToggleButton) findViewById(R.id.toggBtn);

        broad_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((ToggleButton) v).isChecked()) {
                    broadcast_status = true;
                    new StartBroadcast().execute();
                } else {
                    broadcast_status = false;
                    new StopBroadcast().execute("true");
                }

            }
        });

        broad_switch = (Switch) findViewById(R.id.broad_switch);

        broad_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true){
                    new AlwaysBroadcasting().execute("true");
                    broadcast_status = true;
                    broad_button.setChecked(true);
                    broad_button.setEnabled(false);
                    new StartBroadcast().execute();
                }
                else {
                    new AlwaysBroadcasting().execute("false");
                    broad_button.setEnabled(true);
                }
            }
        });






        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.CODE,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);





        listDataHeader = new ArrayList<String>();
        gid_list = new ArrayList<String>();
        sid_list = new ArrayList<String>();
        name_list = new ArrayList<String>();
        broadcasting_list = new ArrayList<String>();
        arrayOfBroadcasts = new ArrayList<Broadcast>();
        //new LoadAllProducts().execute();


        playback_receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                is_playing = intent.getBooleanExtra("playback", false);
            }
        };


        song_receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                trackName = intent.getStringExtra("song");
                albumName = intent.getStringExtra("album");
                artistName = intent.getStringExtra("artist");
                broadcast_working = true;

            }
        };
    }

    @Override
    public void onStart()
    {
        LocalBroadcastManager.getInstance(this).registerReceiver(song_receiver, song_filter);
        LocalBroadcastManager.getInstance(this).registerReceiver(playback_receiver, playback_filter);
        super.onStart();
    }

    public void TuneOut()
    {
        new Decrement_Listeners().execute(current_following_gid, current_user_id);
        current_following_gid = null;
        current_following_sid = null;
        current_following_name = null;
        tuning_in = false;
        Log.d("Progress", "After Tuning IN");
        mPlayer.pause();
        Log.d("Progress", "After Pausing");

        paused_playback = false;
        broadcaster_playing = false;
        broad_button.setEnabled(true);
        Log.d("Progress", "Changing Button");

        fragmentTransaction = fm.beginTransaction();

        my_otherfragment = (TuneIn_Fragment) getSupportFragmentManager().findFragmentByTag("Tune In");
        if (my_otherfragment != null && my_otherfragment.isVisible())
        {
            Log.d("Removing Fragment", "tune in");
            fragmentTransaction.remove(tuning_in_fragment);
            fr = new Following_Fragment();
            fragmentTransaction.add(R.id.fragment_container, fr, "Following").commit();
        }
        else
        {
            Log.d("Removing Fragment", "Other");

            fragmentTransaction.remove(tuning_in_fragment).commit();
        }
        see_profile.setVisible(false);
        stop_tune_in.setVisible(false);
        String[] mDrawerTitles = {"Home", "Following", "My Broadcast", "Settings", "Logout"};
        addDrawerItems(mDrawerTitles);

    }

    public void TuneIn(){
        android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
        new StopBroadcast().execute("true");
        broad_button.setChecked(false);
        broad_button.setEnabled(false);
        tuning_in_fragment = new TuneIn_Fragment();
        fragmentTransaction.replace(R.id.fragment_container, tuning_in_fragment, "Tune In");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    public void SeeProfile(){
        spotify_profile_url = "https://open.spotify.com/user/";
        String menu_view_profile_url = spotify_profile_url.concat(current_following_sid.toString());
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(menu_view_profile_url));
        Log.d("SPOTIFY PROFILE", menu_view_profile_url);
        startActivity(browserIntent);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
            switch (item.getItemId()) {
                case R.id.menu_stop_tune_in:
                    TuneOut();
                    break;
                case R.id.menu_view_profile:
                    SeeProfile();
                    break;
            }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }


    public void addDrawerItems(final String[] mDrawerTitles) {
        // More Drawer Stuff
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
                switch (mDrawerTitles[position]){
                    case "Home":
                        temp_explore_fragment = (Explore_Fragment) getSupportFragmentManager().findFragmentByTag("Explore");
                        if (temp_explore_fragment != null && temp_explore_fragment.isVisible())
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
                        break;
                    case "Following":
                        temp_following_fragment = (Following_Fragment)getSupportFragmentManager().findFragmentByTag("Following");
                        if (temp_following_fragment != null && temp_following_fragment.isVisible())
                        {

                        }
                        else
                        {
                            fr = new Following_Fragment();
                            android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, fr, "Following").addToBackStack(null);
                            fragmentTransaction.commit();
                        }

                        Drawer.closeDrawers();
                        break;
                    case "My Broadcast":
                        if (broadcast_status)
                        {
                            temp_broadcasting_fragment = (MyBroadcast)getSupportFragmentManager().findFragmentByTag("MyBroadcast");
                            if (temp_broadcasting_fragment != null && temp_broadcasting_fragment.isVisible())
                            {

                            }
                            else {
                                android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
                                fr = new MyBroadcast();
                                fragmentTransaction.replace(R.id.fragment_container, fr, "MyBroadcast").addToBackStack(null);
                                fragmentTransaction.commit();

                            }
                            Drawer.closeDrawers();
                            break;
                        }
                        else
                        {
                            my_toast = Toast.makeText(getApplicationContext(), "You aren't broadcasting!", Toast.LENGTH_SHORT);
                            my_toast.setGravity(Gravity.CENTER, 0, 0);
                            my_toast.show();
                            break;
                        }

                    case "Settings":
                        Drawer.closeDrawers();
                        break;
                    case "Logout":
                        AuthenticationClient.logout(getApplicationContext());
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        startActivity(i);
                        break;
                    default:
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, tuning_in_fragment, "Tune In").addToBackStack(null);
                        fragmentTransaction.commit();
                        Drawer.closeDrawers();

                }

            }
        });
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            response = AuthenticationClient.getResponse(resultCode, intent);
            String code = response.getCode();
            Log.d("RYAN - CODE", code);
            new GetSpotifyToken().execute(code);
        }



    }

    class GetSpotifyToken extends AsyncTask<String, String, String> {

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
            JSONObject json = jsonParser.makeHttpRequest(url_get_spotify_token, "GET", params);

            try {

                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // successfully received product details
                    JSONArray productObj = json.getJSONArray("users"); // JSON Array

                    // get first product object from JSON Array
                    JSONObject product = productObj.getJSONObject(0);
                    SpotifyAccessToken = product.getString("access_token");
                    SpotifyAccessTokenExpires = product.getInt("expires_in");
                    SpotifyRefreshToken = product.getString("refresh_token");

                    Log.d("RYAN - ACCESS TOKEN", SpotifyAccessToken);





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
            my_api = new SpotifyApi();


            my_api.setAccessToken(SpotifyAccessToken);
            spotify = my_api.getService();

            spotify.getMe(new Callback<UserPrivate>() {
                @Override
                public void success(UserPrivate userPrivate, Response response) {
                    String name = userPrivate.display_name.toString();
                    spotify_id = userPrivate.id.toString();
                    Map spotify_external_urls =  userPrivate.external_urls;
                    Image spotify_images = userPrivate.images.get(0);
                    Object spotify_profile = spotify_external_urls.get("spotify");
                    spotify_profile_url = spotify_profile.toString();
                    spotify_profile_pic = spotify_images.url;


                    new CreateNewProduct().execute(name, spotify_id, spotify_profile_url, spotify_profile_pic);


                }

                @Override
                public void failure(RetrofitError error) {
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
                params.add(new BasicNameValuePair("name", args[0]));
                params.add(new BasicNameValuePair("id", args[1]));
                params.add(new BasicNameValuePair("prof_pic", args[2]));


                // getting JSON Object
                // Note that create product url accepts POST method

                JSONObject json = jsonParser.makeHttpRequest(url_create_product,
                        "POST", params);

                // check log cat fro response


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
                new Get_guppy_id().execute();

            }
        }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        my_menu = menu;
        see_profile = my_menu.findItem(R.id.menu_view_profile);
        stop_tune_in = my_menu.findItem(R.id.menu_stop_tune_in);
        see_profile.setVisible(false);
        stop_tune_in.setVisible(false);
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


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", current_user_id));

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


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", current_user_id));



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
        }
    }

    class AlwaysBroadcasting extends AsyncTask<String, String, String> {

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
            JSONObject json;


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", current_user_id));
            if (args[0] == "true"){
                json = jsonParser.makeHttpRequest(url_always_broadcasting,
                        "POST", params);
            }
            else {
                json = jsonParser.makeHttpRequest(url_not_always_broadcasting,
                        "POST", params);
            }


            // sending modified data through http request
            // Notice that update product url accepts POST method


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
            params.add(new BasicNameValuePair("id", current_user_id));


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
            Log.d("Posting URL", "It worked");
        }
    }

    class Get_guppy_id extends AsyncTask<String, String, String> {

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
            params.add(new BasicNameValuePair("spotify_id", spotify_id));



            // getting product details by making HTTP request
            // Note that product details url will use GET request
            JSONObject json = jsonParser.makeHttpRequest(
                    url_get_guppy_id, "GET", params);

            String temp = "Empty";
            try {

                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // successfully received product details
                    JSONArray productObj = json
                            .getJSONArray("user"); // JSON Array

                    // get first product object from JSON Array
                    JSONObject product = productObj.getJSONObject(0);
                    current_user_id = product.getString("guppy_id");
                    temp = product.getString("always_broadcasting");





                }else{
                    // product with pid not found
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return temp;
        }


        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String temp) {
            // dismiss the dialog once got all details
            Log.d("RYAN TAG", temp);

            if (temp.equals("1"))
            {
                user_always_broadcasting = true;
                broad_button.setChecked(true);
                broad_button.setEnabled(false);
                broad_switch.setChecked(true);
            }
            Log.d("RYAN TAG", "" + user_always_broadcasting);
            Intent i = new Intent(getApplicationContext(), My_Broadcasting_Service.class);


            SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("gid", current_user_id);
            editor.putString("token", SpotifyAccessToken);
            editor.putString("refresh", SpotifyRefreshToken);
            editor.putInt("token_time", SpotifyAccessTokenExpires);
            editor.commit();



            startService(i);


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

            JSONObject json = jsonParser.makeHttpRequest(url_decrement_listeners,
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


        }
    }

    class GCM_Token extends AsyncTask<String, String, String> {

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
            try {
                InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
                String token = instanceID.getToken("818475639900", GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.d("RYAN", token);
            }
            catch (Exception exception){

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

    @Override
    public void onDestroy()
    {

        new StopBroadcast().execute("true");
        new ChangePlayback().execute("false");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(song_receiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playback_receiver);
        super.onDestroy();
        new SetURI().execute(null, null, null, null, null);
    }


}

