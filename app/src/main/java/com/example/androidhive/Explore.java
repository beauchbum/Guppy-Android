package com.example.androidhive;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.TextView;

import android.widget.ExpandableListView;

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


public class Explore extends BaseActivity {



    //Explore List Stuff
    private ListView mExploreList;
    private LinearLayout mExploreListviewElement;
    List<String> image_locations;




    private static final String TAG_NAME = "name";




    // products JSONArray
    JSONArray products = null;



    // Progress Dialog


    private ProgressDialog pdialog;
    // url to create new product

    // JSON Node names
    private static final String TAG_SUCCESS = "success";


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
        super.onCreateDrawer();
        pdialog = new ProgressDialog(Explore.this);
        pdialog.setMessage("Loading profile. Please wait...");
        pdialog.setIndeterminate(false);
        pdialog.setCancelable(false);
        pdialog.show();



        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);


        //Explore Stuff
        //mExploreList = (ListView) findViewById(R.id.explore_list);
        //mExploreListviewElement = (LinearLayout) findViewById(R.id.explore_list_element);
        ArrayList<ExploreElement> explore_element_array = new ArrayList<ExploreElement>();
        explore_element_array.add(new ExploreElement("guppy", "guppy"));
        explore_element_array.add(new ExploreElement("guppy", "guppy"));
        explore_element_array.add(new ExploreElement("guppy", "guppy"));
        explore_element_array.add(new ExploreElement("guppy", "guppy"));

        mExploreList = (ListView) findViewById(R.id.explore_list);
        ExploreAdapter adapter = new ExploreAdapter(this, explore_element_array);
        mExploreList.setAdapter(adapter);


        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));
        bar.setTitle(Html.fromHtml("<font color='#ffffff'>Explore Popular Streams</font>"));


    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        // = getIntent();
        setIntent(intent);
        broadcast_status = sharedPref.getBoolean("broadcast_status", false);
        current_user_id = sharedPref.getString("user_id", "nothing returned");
        Log.d("Broadcast Explore", Boolean.toString(broadcast_status));
        //try {
            Log.d("User Id Explore", current_user_id);
       // } catch (NullPointerException e)
        {

        }
        //Log.d("User Id Explore", current_user_id);

        if(broadcast_status == true)
        {
            broad_button.setChecked(true);
        }
        else
        {
            broad_button.setChecked(false);
        }

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
                    editor.putString("user_id", current_user_id);
                    editor.commit();
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


        public class ExploreAdapter extends ArrayAdapter<ExploreElement> {

            public ExploreAdapter(Context context, ArrayList<ExploreElement> elements) {
                super(context, 0, elements);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ExploreElement mExploreElement = getItem(position);
                if (convertView == null) {
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

        @Override
        public void onDestroy ()
        {
            new StopBroadcast().execute();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPref.edit().clear().commit();
            super.onDestroy();
        }

    }

