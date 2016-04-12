package com.example.androidhive;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;


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


public class MainScreenActivity extends ListActivity {

	// Progress Dialog

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();

	ArrayList<HashMap<String, String>> productsList;
	HashMap<String, List<String>> listDataChild;
	List<String> listDataHeader;
	List<String> user_options;

	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;


	// url to get all products list
	private static String url_all_products = "http://10.0.0.26/android_connect/get_all_products.php";

	// JSON Node names

	private static final String TAG_PRODUCTS = "products";
	private static final String TAG_PID = "pid";
	private static final String TAG_NAME = "name";

	// products JSONArray
	JSONArray products = null;




	Button btnViewProducts;
	Button btnNewProduct;

	// Progress Dialog
	private ProgressDialog pDialog;
	JSONParser jsonParser = new JSONParser();
	// url to create new product
	private static String url_create_product = "http://10.0.0.26/android_connect/create_product.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";


	public final static String EXTRA_MESSAGE = "com.mycompany.app.MESSAGE";
	public AuthenticationResponse response;
	private static final int REQUEST_CODE = 1337;
	// TODO: Replace with your client ID
	private static final String CLIENT_ID = "57b61875218e4e1f8a8d0cdb57a7259b";
	// TODO: Replace with your redirect URI
	private static final String REDIRECT_URI = "sync-me-up://callback";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);


		listDataChild = new HashMap<String, List<String>>();
		user_options = new ArrayList<String>();

		user_options.add("Tune In");
		user_options.add("See Profile");
		user_options.add("Gando Sux");



		//Spotify Authentication
		AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
				AuthenticationResponse.Type.TOKEN,
				REDIRECT_URI);
		builder.setScopes(new String[]{"user-read-private", "streaming"});
		AuthenticationRequest request = builder.build();
		AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

		//loading products
		productsList = new ArrayList<HashMap<String, String>>();

		// Loading products in Background Thread
		new LoadAllProducts().execute();

		// Get listview
		expListView = (ExpandableListView) findViewById(R.id.list);

		// on seleting single product
		// launching Edit Product Screen
		expListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				// getting values from selected ListItem
				String pid = ((TextView) view.findViewById(R.id.pid)).getText()
						.toString();

				// Starting new intent
				Intent in = new Intent(getApplicationContext(),
						EditUserActivity.class);
				// sending pid to next activity
				in.putExtra(TAG_PID, pid);

				// starting new activity and expecting some response back
				startActivityForResult(in, 100);
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
					String username = userPrivate.id.toString();
					String description = "Created Automatically by Spotify login";
					//addUser(name, username, description);

				}

				@Override
				public void failure(RetrofitError error) {
					Log.d("User Failure", error.toString());
				}
			});

		}
	}

	public void addUser(String name, String username, String description) {

		new CreateNewProduct().execute(name, username, description);
	}


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

						// Storing each json item in variable
						String id = c.getString(TAG_PID);
						String name = c.getString(TAG_NAME);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(TAG_PID, id);
						map.put(TAG_NAME, name);

						listDataHeader.add(name);
						listDataChild.put(name, user_options);

						// adding HashList to ArrayList
						productsList.add(map);
					}
				} else {
					// no products found
					// Launch Add New product Activity
					Intent i = new Intent(getApplicationContext(),
							NewUserActivity.class);
					// Closing all previous activities
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
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
					ListAdapter adapter = new SimpleAdapter(
							MainScreenActivity.this, productsList,
							R.layout.list_item, new String[] { TAG_PID},
							new int[] { R.id.pid});
					// updating listview
					setListAdapter(adapter);

					listAdapter = new ExpandableListAdapter(MainScreenActivity.this,listDataHeader, listDataChild);
					expListView.setAdapter(listAdapter);
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
			params.add(new BasicNameValuePair("name", args[0]));
			params.add(new BasicNameValuePair("price", args[1]));
			params.add(new BasicNameValuePair("description", args[2]));

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
					// successfully created product
					Intent i = new Intent(getApplicationContext(), AllUsersActivity.class);
					startActivity(i);

					// closing this screen
					finish();
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
}









