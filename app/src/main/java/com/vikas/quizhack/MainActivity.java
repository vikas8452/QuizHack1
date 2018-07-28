package com.vikas.quizhack;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vikas.quizhack.databinding.ActivityMainBinding;

import java.io.File;

public class MainActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener{
    public static MediaProjection sMediaProjection;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 5566;
    private static final int REQUEST_CODE = 55566;
    private ActivityMainBinding binding;
    private MediaProjectionManager mProjectionManager;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    Button tutorial;
    private BroadcastReceiver mReceiver = null;
    private Spinner spinner;
   public static String engine="Google";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        checkDrawOverlayPermission();
        checkWritePermission();

        MobileAds.initialize(this, "ca-app-pub-2934083584902567~7620178762");
        mAdView =findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        tutorial=findViewById(R.id.tutorial);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("videoId");


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-2934083584902567/1045772375");

        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        spinner =findViewById(R.id.spinner);



        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.engine, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver(this);
        registerReceiver(mReceiver, filter);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }


        });

        mInterstitialAd.show();

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        binding.test.setOnClickListener(view -> {

            mInterstitialAd.show();
            if(!checkDrawOverlayPermission()){
                checkDrawOverlayPermission();
                return;
            }
            if(!checkWritePermission()){
                checkWritePermission();
                return;
            }
            startMediaProjection();
        });

        if (sMediaProjection == null) {
            binding.test.setVisibility(View.VISIBLE);
        }

        tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] value = new String[1];
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                       value[0] = dataSnapshot.getValue(String.class);
                      //  Log.d(TAG, "Value is: " + value);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                     //   Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });
                watchYoutubeVideo(value[0]);
            }
        });


    }



    private boolean checkWritePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            return false;
        }
        return true;
    }

    private void startMediaProjection() {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("kanna","get write permission");
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            if (sMediaProjection == null)
            {
                Log.d("kanna", "not get permission of media projection");
                Toast.makeText(this, "Need MediaProjection", Toast.LENGTH_LONG).show();
                startMediaProjection();
            }
            else {
                startBubble();
            }
        }
    }

    @Override
    protected void onResume(){
        mInterstitialAd.show();
        super.onResume();
        if (sMediaProjection == null)
        {
            Intent intent=new Intent(this,BubbleService.class);
            stopService(intent);
            binding.test.setVisibility(View.VISIBLE);
        }
    }

    private void startBubble() {
        Log.d("kanna","start bubble");
        binding.test.setVisibility(View.GONE);
        Intent intent = new Intent(this, BubbleService.class);
        stopService(intent);
        startService(intent);
    }

    @Override
    public void onBackPressed() {
        mInterstitialAd.show();
        super.onBackPressed();

    }

    @Override
    protected void onDestroy()
    {
        Intent intent=new Intent(this,BubbleService.class);
        stopService(intent);
        super.onDestroy();
    }

    public  void watchYoutubeVideo(String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    public void onPause(){
        super.onPause();

        // Check if WebView is null first since onPause can occasionally get called before all your views are initialized

    }

    @Override
    protected void onRestart() {
        if (sMediaProjection == null) {

            Intent intent=new Intent(this,BubbleService.class);
            stopService(intent);
            binding.test.setVisibility(View.VISIBLE);
        }
        super.onRestart();
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        engine=adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
