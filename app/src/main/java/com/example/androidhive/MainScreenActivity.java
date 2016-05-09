package com.example.androidhive;

import android.app.Activity;
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
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
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


public class MainScreenActivity extends BaseActivity {

	// Progress Dialog


	ArrayList<HashMap<String, String>> productsList;
	HashMap<String, List<String>> listDataChild;
	ArrayList<String> listDataHeader;
	List<String> user_options;
	ArrayList<String> pid_list;

	ListView lv;
	ExpandableListAdapter exp_adapter;


	// JSON Node names

	private static final String TAG_PRODUCTS = "users";

	private static final String TAG_NAME = "name";

	// products JSONArray
	JSONArray products = null;

	ArrayList<Broadcast> arrayOfBroadcasts;
	BroadcastAdapter adapter;


	// Progress Dialog
	private ProgressDialog pDialog;
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
		setContentView(R.layout.main_screen);
		super.onCreateDrawer();

		listDataChild = new HashMap<String, List<String>>();
		user_options = new ArrayList<String>();

		listDataHeader = new ArrayList<String>();
		pid_list = new ArrayList<String>();

		user_options.add("Tune In");
		user_options.add("See Profile");
		//user_options.add("Gando Sux");

		arrayOfBroadcasts = new ArrayList<Broadcast>();
		new LoadAllProducts().execute();

		Intent in = getIntent();
		current_user_id = sharedPref.getString("user_id", "nothing returned");
		broadcast_status = sharedPref.getBoolean("broadcast_status", false);

		//Set ListView for Drawer


		android.support.v7.app.ActionBar bar = getSupportActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));
		bar.setTitle(Html.fromHtml("<font color='#ffffff'>Following</font>"));





		// Loading products in Background Thread
		//NEED TO READD THIS SOON!!!!
		//new LoadAllProducts().execute();

		// Get listview
		lv = (ListView) findViewById(R.id.following_list);

		/*
		ArrayList<Broadcast> arrayOfBroadcasts = new ArrayList<Broadcast>();
		arrayOfBroadcasts.add(new Broadcast("Madison Claire", 215, "Rock Your Body", "Justin Timberlake", "guppy"));

		BroadcastAdapter adapter = new BroadcastAdapter(this, arrayOfBroadcasts);
		lv.setAdapter(adapter);
		*/

		ListView imageButtonContextMenu;
		imageButtonContextMenu = (ListView) findViewById(R.id.following_list);
		registerForContextMenu(imageButtonContextMenu);

		/*lv.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if(childPosition == 0){
					String pid = pid_list.get(groupPosition);
					Intent in = new Intent(getApplicationContext(),
							TuneIn.class);
					in.putExtra(TAG_PID, pid);
					in.putExtra(EXTRA_MESSAGE, response);
					startActivity(in);
				}
				return true;
			}
		});*/

	}

	@Override
	public void onNewIntent(Intent in)
	{
		super.onNewIntent(in);
		setIntent(in);
		current_user_id = sharedPref.getString("user_id", "nothing returned");
		broadcast_status = sharedPref.getBoolean("broadcast_status", false);
		Log.d("User ID Following", current_user_id);
		Log.d("Broadcast Following", Boolean.toString(broadcast_status));


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



	//Runs a PHP script to load all users from database
	//Then expandable listview adapter displays the users in the UI
	class LoadAllProducts extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		/**
		 * getting All products from url
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(url_all_products, "GET", params);

			// Check your log cat for JSON reponse
			Log.d("All Products: ", json.toString());

			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// products found
					// Getting Array of Products
					products = json.getJSONArray(TAG_PRODUCTS);

					// looping through All Products
					for (int i = 0; i < products.length(); i++) {
						JSONObject c = products.getJSONObject(i);
						boolean exists = false;

						// Storing each json item in variable
						String id = c.getString(TAG_PID);
						String name = c.getString(TAG_NAME);
						String song = c.getString("song");
						String artist = c.getString("artist");
						String listeners = c.getString("listeners");
						int real_listeners = Integer.parseInt(listeners);


						arrayOfBroadcasts.add(new Broadcast(name, real_listeners, song, artist, "guppy"));


						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(TAG_PID, id);
						map.put(TAG_NAME, name);
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
			runOnUiThread(new Runnable() {
				public void run() {




					adapter = new BroadcastAdapter(MainScreenActivity.this, arrayOfBroadcasts);
					lv.setAdapter(adapter);
					/**
					 * Updating parsed JSON data into ListView
					 * */
/*
					ListAdapter adapter = new SimpleAdapter(
							MainScreenActivity.this, productsList,
							R.layout.list_item, new String[] {TAG_NAME, TAG_PID},
							new int[] {R.id.name ,R.id.pid});
					// updating listview
					setListAdapter(adapter);


*/

					//System.out.println(listDataHeader);
					//System.out.println(listDataChild);
					//lv.setAdapter(exp_adapter);

				}
			});

		}

	}







	public class BroadcastAdapter extends ArrayAdapter<Broadcast> {

		private final int VIEW_TYPE_SELECTED = 1;
		private final int VIEW_TYPE_NORMAL = 0;

		public BroadcastAdapter(Context context, ArrayList<Broadcast> broadcasts)
		{
			super(context, 0, broadcasts);
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
			Broadcast mBroadcast = getItem(position);
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
					layoutId = R.layout.list_item;
				//}
				convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
			}
			TextView userTextView = (TextView) convertView.findViewById(R.id.name);
			TextView listenersTextView = (TextView) convertView.findViewById(R.id.text_view_listeners);
			TextView songArtistTextView = (TextView) convertView.findViewById(R.id.song_text_view);
			ImageView profilePicImage = (ImageView) convertView.findViewById(R.id.list_item_image);

			int resId = getResources().getIdentifier(mBroadcast.image_title, "drawable", getPackageName());
			profilePicImage.setImageResource(resId);

			userTextView.setText(mBroadcast.username);
			String listeners = mBroadcast.listeners + " Listeners";
			listenersTextView.setText(listeners);
			String songAndArtist = mBroadcast.song + " - " + mBroadcast.artist;
			songArtistTextView.setText(songAndArtist);
			return convertView;
		}

	}



	/*public void showPopUp(View view) {
		Toast.makeText(this, "Hello World", Toast.LENGTH_LONG).show();
		/*MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu, menu);*/

		/*View popupView = getLayoutInflater().inflate(R.layout.popup_more, null);
		PopupWindow popupWindow = new PopupWindow(popupView);
		ListView popUpListView = (ListView) popupView.findViewById(R.id.popup_lv);
		ArrayList<String> options = new ArrayList<String>();
		options.add("Follow");
		options.add("View Profile");
		options.add("Cancel");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
		popUpListView.setAdapter(adapter);
		popupWindow.setTouchable(true);
		//popupWindow.setFocusable(true);
		popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
		//findViewById(android.R.id.content)
		Log.v("Hello", "Hello");
	}*/



}