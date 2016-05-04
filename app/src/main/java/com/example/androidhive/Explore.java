package com.example.androidhive;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;

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


public class Explore extends AppCompatActivity {

    // Progress Dialog

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> productsList;
    HashMap<String, List<String>> listDataChild;
    ArrayList<String> listDataHeader;
    List<String> user_options;
    ArrayList<String> pid_list;
    private String is_playing;
    private boolean playing;

    private MyBroadcastReceiver receiver;

    private boolean receiver_exists = false;


    ExpandableListView lv;
    ExpandableListAdapter exp_adapter;


    private static String ip = "52.38.141.152";

    //Explore List Stuff
    private ListView mExploreList;
    private LinearLayout mExploreListviewElement;
    List<String> image_locations;

    // Drawer List Stuff
    private LinearLayout mDrawerLayout;
    private ListView mDrawerList;
    private TextView drawerSwitchStatus;
    private ArrayAdapter<String> mAdapter;


    // JSON Node names

    private static final String TAG_PRODUCTS = "users";
    public static final String TAG_PID = "id";
    private static final String TAG_NAME = "name";

    // products JSONArray
    JSONArray products = null;


    // Progress Dialog
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    // url to create new product

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    private String id;
    private Button broad_button;


    public final static String EXTRA_MESSAGE = "com.mycompany.app.MESSAGE";
    public AuthenticationResponse response;
    private static final int REQUEST_CODE = 1337;
    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "57b61875218e4e1f8a8d0cdb57a7259b";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "sync-me-up://callback";


    //Runs "LoadAllProducts" and begins the Spotify login
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        Intent intent = getIntent();
        id = intent.getStringExtra(MainScreenActivity.TAG_PID);
        Log.d("Explore ID", id);

        //Explore Stuff
        //mExploreList = (ListView) findViewById(R.id.explore_list);
        //mExploreListviewElement = (LinearLayout) findViewById(R.id.explore_list_element);
        ArrayList<ExploreElement> explore_element_array= new ArrayList<ExploreElement>();
        explore_element_array.add(new ExploreElement("guppy", "guppy"));
        explore_element_array.add(new ExploreElement("guppy", "guppy"));
        explore_element_array.add(new ExploreElement("guppy", "guppy"));
        explore_element_array.add(new ExploreElement("guppy", "guppy"));

        mExploreList = (ListView) findViewById(R.id.explore_list);
        ExploreAdapter adapter = new ExploreAdapter(this, explore_element_array);
        mExploreList.setAdapter(adapter);

        //Set ListView for Drawer
        mDrawerLayout = (LinearLayout) findViewById(R.id.drawer_linear_layout);
        //mDrawerSwitch = (Switch) findViewById(R.id.drawer_switch);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        addDrawerItems();

        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));
        bar.setTitle(Html.fromHtml("<font color='#ffffff'>Explore Popular Streams</font>"));

        broad_button = (ToggleButton) findViewById(R.id.toggBtn);
        broad_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(((ToggleButton) v).isChecked()) {
                    Intent in = new Intent(getApplicationContext(),
                            Menu_Activity.class);
                    in.putExtra(TAG_PID, id);
                    in.putExtra(EXTRA_MESSAGE, true);
                    startActivity(in);
                }
                else {
                    Intent in = new Intent(getApplicationContext(),
                            Menu_Activity.class);
                    in.putExtra(TAG_PID, id);
                    in.putExtra(EXTRA_MESSAGE, false);
                    startActivity(in);
                }
            }
        });

    }

    /*

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
                    id = userPrivate.id.toString();

                    new CreateNewProduct().execute(name, id);

                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("User Failure", error.toString());
                }
            });
        }
    }

*/

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


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class ExploreAdapter extends ArrayAdapter<ExploreElement>
    {

        public ExploreAdapter(Context context, ArrayList<ExploreElement> elements)
        {
            super (context, 0, elements);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ExploreElement mExploreElement = getItem(position);
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.explore_listview_item, parent, false);
            }

            ImageButton picture1 = (ImageButton) convertView.findViewById(R.id.picture_button1);
            ImageButton picture2 = (ImageButton) convertView.findViewById(R.id.picture_button2);

            int resId1 = getResources().getIdentifier(mExploreElement.pic1, "drawable", getPackageName());
            picture1.setImageResource(resId1);

            int resId2 = getResources().getIdentifier(mExploreElement.pic2, "drawable", getPackageName());
            picture2.setImageResource(resId2);

            return convertView;

        }

    }



}
