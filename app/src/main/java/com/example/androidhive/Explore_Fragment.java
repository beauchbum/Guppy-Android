package com.example.androidhive;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.ViewGroupCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ryan on 5/9/2016.
 */
public class Explore_Fragment extends Fragment {

    private ListView mExploreList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootview = inflater.inflate(R.layout.explore_fragment, container, false);
        ArrayList<ExploreElement> explore_element_array = new ArrayList<ExploreElement>();
        explore_element_array.add(new ExploreElement("guppy", "guppy"));
        explore_element_array.add(new ExploreElement("guppy", "guppy"));
        explore_element_array.add(new ExploreElement("guppy", "guppy"));
        explore_element_array.add(new ExploreElement("guppy", "guppy"));

        mExploreList = (ListView) rootview.findViewById(R.id.explore_list);
        Explore_Fragment.ExploreAdapter adapter = new Explore_Fragment.ExploreAdapter(getActivity().getApplicationContext(), explore_element_array);
        mExploreList.setAdapter(adapter);

        return rootview;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);


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

            int resId1 = getResources().getIdentifier(mExploreElement.pic1, "drawable", getActivity().getPackageName());
            picture1.setImageResource(resId1);

            int resId2 = getResources().getIdentifier(mExploreElement.pic2, "drawable", getActivity().getPackageName());
            picture2.setImageResource(resId2);

            return convertView;

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
