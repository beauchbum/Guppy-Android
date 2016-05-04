package com.example.androidhive;

import android.app.Activity;
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


public class MainScreenActivity extends AppCompatActivity {

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


	ListView lv;
	ExpandableListAdapter exp_adapter;



	private static String ip = "52.38.141.152";
	// url to get all products list
	private static String url_all_products = "http://" + ip + "/get_all_products.php";
	private static final String url_start_broadcast = "http://" + ip + "/start_broadcast.php";
	private static final String url_stop_broadcast = "http://" + ip + "/stop_broadcast.php";
	private static String url_create_product = "http://" + ip + "/create_product.php";
	private static String url_post_uri = "http://" + ip + "/post_uri.php";
	private static String url_play_playback = "http://" + ip + "/play_playback.php";
	private static String url_pause_playback = "http://" + ip + "/pause_playback.php";

	// Drawer List Stuff
	private LinearLayout mDrawerLayout;
	private ListView mDrawerList;
	private Switch mDrawerSwitch;
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

	private String current_user_id;
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

		//android:background="#209CF2"

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);

		listDataChild = new HashMap<String, List<String>>();
		user_options = new ArrayList<String>();

		listDataHeader = new ArrayList<String>();
		pid_list = new ArrayList<String>();

		user_options.add("Tune In");
		user_options.add("See Profile");
		//user_options.add("Gando Sux");

		//Set ListView for Drawer
		mDrawerLayout = (LinearLayout) findViewById(R.id.drawer_linear_layout);
		//mDrawerSwitch = (Switch) findViewById(R.id.drawer_switch);
		mDrawerList = (ListView) findViewById(R.id.drawer_list);
		addDrawerItems();



		//Spotify Authentication
		AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
				AuthenticationResponse.Type.TOKEN,
				REDIRECT_URI);
		builder.setScopes(new String[]{"user-read-private", "streaming"});
		AuthenticationRequest request = builder.build();
		AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

		android.support.v7.app.ActionBar bar = getSupportActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));
		bar.setTitle(Html.fromHtml("<font color='#ffffff'>Following</font>"));

		//loading products
		productsList = new ArrayList<HashMap<String, String>>();


		broad_button = (ToggleButton) findViewById(R.id.toggBtn);
		broad_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(((ToggleButton) v).isChecked()) {
					new StartBroadcast().execute();
				}
				else {
					new StopBroadcast().execute();
				}
			}
		});

		// Loading products in Background Thread
		//NEED TO READD THIS SOON!!!!
		//new LoadAllProducts().execute();

		// Get listview
		lv = (ListView) findViewById(R.id.following_list);

		ArrayList<Broadcast> arrayOfBroadcasts = new ArrayList<Broadcast>();
		arrayOfBroadcasts.add(new Broadcast("Madison Claire", 215, "Rock Your Body", "Justin Timberlake", "guppy"));
		arrayOfBroadcasts.add(new Broadcast("Joe Sauer", 74, "Stayin' Alive", "Bee Gees", "guppy"));
		arrayOfBroadcasts.add(new Broadcast("Jack Lansa", 336, "This Love", "Maroon 5", "guppy"));
		arrayOfBroadcasts.add(new Broadcast("Anthony Sacco", 329, "Hey Ya!", "OutKast", "guppy"));
		arrayOfBroadcasts.add(new Broadcast("Tom Leary", 383, "Work It", "Missy Elliott", "guppy"));
		arrayOfBroadcasts.add(new Broadcast("Allison Nicolai", 221, "It's My Life", "No Doubt", "guppy"));
		arrayOfBroadcasts.add(new Broadcast("Mark Musial", 386, "Calling All Angels", "Train", "guppy"));
		arrayOfBroadcasts.add(new Broadcast("Jessica Mindrum", 296, "Cheap Thrills", "Sia", "guppy"));
		arrayOfBroadcasts.add(new Broadcast("Zac Bricta", 133, "Take It Easy", "Eagles", "guppy"));
		arrayOfBroadcasts.add(new Broadcast("Ryan Beauchamp", 1, "Teenage Dream", "Katy Perry", "guppy"));
		BroadcastAdapter adapter = new BroadcastAdapter(this, arrayOfBroadcasts);
		lv.setAdapter(adapter);

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


	//Receives a "success" code from Spotify login attempt
	//Runs "CreateNewProduct" to add user to database
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

	private void addDrawerItems(){
		// More Drawer Stuff
		String[] mDrawerTitles = {"Home", "Following", "Settings", "Logout"};
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDrawerTitles) {
			@Override
			public View getView(int position, View convertView,
								ViewGroup parent) {
				View view =super.getView(position, convertView, parent);

				TextView textView=(TextView) view.findViewById(android.R.id.text1);

            /*YOUR CHOICE OF COLOR*/
				textView.setTextColor(Color.WHITE);

				return view;
			}
		};
		mDrawerList.setAdapter(mAdapter);

		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(MainScreenActivity.this, "Click", Toast.LENGTH_SHORT).show();
				if(position == 1)
				{
					Intent in = new Intent(getApplicationContext(),
							Explore.class);
					in.putExtra(TAG_PID, current_user_id);
					startActivity(in);
				}
			}
		});

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
			pDialog = new ProgressDialog(MainScreenActivity.this);
			pDialog.setMessage("Loading users. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting All products from url
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);

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


						// adding HashList to ArrayList
						productsList.add(map);
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
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
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
					exp_adapter = new ExpandableListAdapter(MainScreenActivity.this, listDataHeader, listDataChild);
					//lv.setAdapter(exp_adapter);

				}
			});

		}

	}

	//Runs a PHP script to add a user to the database as long as they do not already exist
	class CreateNewProduct extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainScreenActivity.this);
			pDialog.setMessage("Creating Product..");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
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
			pDialog.dismiss();
		}
	}

	//Registers Broadcast Receiver which will listen for changes in Spotify playback
	//Then sets the broadcasting variable to "1"
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
				int success = json.getInt(TAG_SUCCESS);

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
			pDialog.dismiss();
		}
	}

	//Runs PHP script to change broadcasting variable to "0"
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
				int success = json.getInt(TAG_SUCCESS);

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
			pDialog.dismiss();
		}
	}

	//Runs PHP script to set the new song URI
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

			Log.d("Setting URI", params.toString());


			// sending modified data through http request
			// Notice that update product url accepts POST method
			JSONObject json = jsonParser.makeHttpRequest(url_post_uri,
					"POST", params);

			// check json success tag
			try {
				int success = json.getInt(TAG_SUCCESS);

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
			pDialog.dismiss();
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
				int success = json.getInt(TAG_SUCCESS);

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
			pDialog.dismiss();
		}
	}

	public void openExplore(View view) {

		Intent exploreIntent = new Intent (this, Explore.class);
		startActivity(exploreIntent);

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