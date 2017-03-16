package com.example.androidhive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static android.R.attr.filter;
import static com.example.androidhive.R.id.album;
import static com.example.androidhive.R.id.artist;
import static com.example.androidhive.R.id.play;
import static com.example.androidhive.R.id.song;


public class MyBroadcast extends Fragment {

    public View rootview;
    public Explore main_activity;
    public BroadcastReceiver song_receiver;
    public BroadcastReceiver playback_receiver;
    private TextView song;
    private TextView album;
    private TextView artist;
    private TextView paused_textview;
    private ImageView albumImageView;
    private IntentFilter song_filter = new IntentFilter(My_Broadcasting_Service.SONGCHANGE);
    private IntentFilter playback_filter = new IntentFilter(My_Broadcasting_Service.PLAYBACK);



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.tunein_fragment, container, false);
        main_activity = (Explore) getActivity();
        android.support.v7.app.ActionBar bar = main_activity.getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));
        bar.setTitle(Html.fromHtml("<font color='#ffffff'>Your Broadcast</font>"));
        song = (TextView) rootview.findViewById(R.id.song);
        album = (TextView) rootview.findViewById(R.id.album);
        artist = (TextView) rootview.findViewById(R.id.artist);
        paused_textview=(TextView) rootview.findViewById(R.id.paused_playback);
        albumImageView=(ImageView) rootview.findViewById(R.id.album_image_view);



        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        alertDialogBuilder.setTitle("No Broadcast Yet!");
        alertDialogBuilder.setMessage("Please select a new song to begin");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FragmentManager fm = getFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                }else{
                    Log.d("RYAN", "Back pressed, but no stack");
                }
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();

        song.setText(main_activity.trackName);
        album.setText(main_activity.albumName);
        artist.setText(main_activity.artistName);

        if (main_activity.broadcast_working == false) {
            alertDialog.show();
        }




        song_receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                Log.d("RYAN", "BROADCAST RECEIVED");
                song.setText(intent.getStringExtra("song"));
                album.setText(intent.getStringExtra("album"));
                artist.setText(intent.getStringExtra("artist"));
                alertDialog.hide();

            }
        };

        playback_receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                if(intent.getBooleanExtra("playback", false) == false)
                {
                    albumImageView.setAlpha(90);
                    paused_textview.setVisibility(View.VISIBLE);
                }
                else
                {
                    albumImageView.setAlpha(255);
                    paused_textview.setVisibility(View.GONE);
                }

            }
        };

        return rootview;
    }




    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(song_receiver, song_filter);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(playback_receiver, playback_filter);


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy(){
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(song_receiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(playback_receiver);
        super.onDestroy();

    }


}
