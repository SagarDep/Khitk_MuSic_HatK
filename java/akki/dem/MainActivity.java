package akki.dem;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.lang.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;
import akki.dem.MusicService.MusicBinder;
import android.widget.MediaController.MediaPlayerControl;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;



public class MainActivity extends AppCompatActivity implements MediaPlayerControl {

    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController controller;
    private boolean paused=false, playbackPaused=false;
    Button artist,songs,album,year;
    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    //Declarations for automatic background vhange
    private static final long GET_DATA_INTERVAL = 10000;
    int images[] = {R.drawable.a,R.drawable.c,R.drawable.main_bg,R.drawable.e};
    int index = 0;
    ImageView img;
    Handler hand = new Handler();

    ImageView imageView;
    private static final int PICK_IMAGE=1000;
    Uri imageUri;

    private ShareActionProvider mShareActionProvider;


@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        getSongList();

        //automatic background change
        img = (ImageView) findViewById(R.id.image2);
        hand.postDelayed(run, GET_DATA_INTERVAL);


    //ActionBarLogo
        ActionBar ab = getSupportActionBar();
        ab.setLogo(R.drawable.logo);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        final SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
        setController();


        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        artist = (Button) findViewById(R.id.button);
        songs = (Button) findViewById(R.id.button1);
        album = (Button) findViewById(R.id.button2);
        year = (Button) findViewById(R.id.button3);



        artist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this,"Sort by Artist",Toast.LENGTH_SHORT).show();
                Collections.sort(songList, new Comparator<Song>(){
                    public int compare(Song a, Song b){
                        return a.getArtist().compareTo(b.getArtist());
                    }
                });
                songAdt.notifyDataSetChanged();
            }
        });
        songAdt.notifyDataSetChanged();


        songs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Sort by SongTitle",Toast.LENGTH_SHORT).show();
                Collections.sort(songList, new Comparator<Song>(){
                    public int compare(Song a, Song b){
                        return a.getTitle().compareTo(b.getTitle());
                    }
                });
                songAdt.notifyDataSetChanged();
            }
        });
        songAdt.notifyDataSetChanged();

        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this,"Sort by Album",Toast.LENGTH_SHORT).show();
                Collections.sort(songList, new Comparator<Song>(){
                    public int compare(Song a, Song b){
                        return a.getAlbum().compareTo(b.getAlbum());
                    }
                });
                songAdt.notifyDataSetChanged();
            }
        });
        songAdt.notifyDataSetChanged();

        year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this,"Sort by Year",Toast.LENGTH_SHORT).show();
                Collections.sort(songList, new Comparator<Song>(){
                    public int compare(Song a, Song b){
                       int x= a.getYear().compareTo(b.getYear());
                        if(x==-1)
                            return 1;
                        else if(x==1)
                           return -1;
                           else return 0;

                    }
                });
                songAdt.notifyDataSetChanged();
            }
        });
        songAdt.notifyDataSetChanged();





        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @java.lang.Override
            public void onShake(int count) {
                handleShakeEvent();
            }


        });


    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            img.setBackgroundDrawable(getResources().getDrawable(images[index++]));
            if (index == images.length)
                index = 0;
            hand.postDelayed(run, GET_DATA_INTERVAL);
        }
    };


    public void handleShakeEvent(){
        playNext();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }



    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM);
            int yearColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.YEAR);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                Integer thisYear = musicCursor.getInt(yearColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist , thisAlbum , thisYear ));
            }
            while (musicCursor.moveToNext());
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }


    private void setController(){
        //set the controller up
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }


    //play next
    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    //play previous
    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    @Override
    protected void onPause(){
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    public void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv = null;
                System.exit(0);
                break;
            case R.id.about:
                Intent intent = new Intent(this, About_Activity.class);
                startActivity(intent);
                break;
            case R.id.features:
                Intent intet = new Intent(this, FeaturesActivity.class);
                startActivity(intet);
                break;
            case R.id.action_bg:
                imageView = (ImageView) findViewById(R.id.image);
                openGallery();
                break;
            case R.id.action_share:
                Intent i= new Intent("android.intent.action.SEND");
                i.setType("text/plain");
                i.putExtra("android.intent.extra.TEXT","Khitk -  Music Hatke");
                startActivity(i);
                return true;
            case R.id.action_lyrics:
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.musixmatch.android.lyrify");
                if (launchIntent != null) {
                    startActivity(launchIntent);//null pointer check in case package name was not found
                }
        }

        return super.onOptionsItemSelected(item);
    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery,PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode ,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK && requestCode==PICK_IMAGE){
            imageUri=data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }



    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
        return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


}
