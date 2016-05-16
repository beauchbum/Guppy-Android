package com.example.androidhive;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by Ryan on 5/9/2016.
 */
public class Following_Fragment extends Fragment {

    public Explore main_activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootview = inflater.inflate(R.layout.following_fragment, container, false);

        main_activity = (Explore) getActivity();
        ListView imageButtonContextMenu;
        imageButtonContextMenu = (ListView) rootview.findViewById(R.id.following_list);
        registerForContextMenu(imageButtonContextMenu);
        main_activity.lv = (ListView) rootview.findViewById(R.id.following_list);
        main_activity.listDataChild = new HashMap<String, List<String>>();

        android.support.v7.app.ActionBar bar = main_activity.getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));
        bar.setTitle(Html.fromHtml("<font color='#ffffff'>Following</font>"));


        new LoadAllProducts().execute();

        return rootview;

    }

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
            JSONObject json = main_activity.jsonParser.makeHttpRequest(main_activity.url_all_products, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(main_activity.TAG_SUCCESS);
                main_activity.arrayOfBroadcasts.clear();
                main_activity.pid_list.clear();

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    main_activity.products = json.getJSONArray(main_activity.TAG_PRODUCTS);


                    // looping through All Products
                    for (int i = 0; i < main_activity.products.length(); i++) {
                        JSONObject c = main_activity.products.getJSONObject(i);
                        boolean exists = false;

                        // Storing each json item in variable
                        String id = c.getString(main_activity.TAG_PID);
                        String name = c.getString(main_activity.TAG_NAME);
                        String song = c.getString("song");
                        String artist = c.getString("artist");
                        String listeners = c.getString("listeners");
                        int real_listeners = Integer.parseInt(listeners);


                        main_activity.arrayOfBroadcasts.add(new Broadcast(name, real_listeners, song, artist, "guppy"));
                        main_activity.pid_list.add(i, id);
                        Log.d("array of broadcasts", main_activity.arrayOfBroadcasts.toString());



                        System.out.println(name);

                        for (String str: main_activity.listDataHeader){
                            if(str.trim().contains(name))
                                exists = true;
                        }
                        if(!exists) {
                            main_activity.listDataHeader.add(name);
                            main_activity.listDataChild.put(name, main_activity.user_options);
                            main_activity.pid_list.add(id);
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
            main_activity.runOnUiThread(new Runnable() {
                public void run() {




                    main_activity.adapter = new BroadcastAdapter(main_activity.getApplicationContext(), main_activity.arrayOfBroadcasts);
                    main_activity.lv.setAdapter(main_activity.adapter);
                    main_activity.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            main_activity.current_following_id = main_activity.pid_list.get(position);
                            new Increment_Listeners().execute(main_activity.current_following_id);
                            main_activity.TuneIn();
                        }
                    });


                }
            });

        }

    }


    class Increment_Listeners extends AsyncTask<String, String, String> {

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



            // getting JSON Object
            // Note that create product url accepts POST method

            JSONObject json = main_activity.jsonParser.makeHttpRequest(main_activity.url_increment_listeners,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(main_activity.TAG_SUCCESS);

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

            int resId = getResources().getIdentifier(mBroadcast.image_title, "drawable", getActivity().getPackageName());
            profilePicImage.setImageResource(resId);

            userTextView.setText(mBroadcast.username);
            String listeners = mBroadcast.listeners + " Listeners";
            listenersTextView.setText(listeners);
            String songAndArtist = mBroadcast.song + " - " + mBroadcast.artist;
            songArtistTextView.setText(songAndArtist);
            return convertView;
        }

    }
}
