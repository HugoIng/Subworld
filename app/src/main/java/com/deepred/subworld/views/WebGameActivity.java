package com.deepred.subworld.views;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.deepred.subworld.engine.GameManager;
import com.deepred.subworld.utils.HtmlHelper;
import com.deepred.subworld.utils.IMarkersListener;
import com.deepred.subworld.utils.MyUserManager;
import com.deepred.subworld.R;
import com.deepred.subworld.model.User;
import com.google.android.gms.maps.model.LatLng;

/**
 *
 */
public class WebGameActivity extends AppCompatActivity implements IMarkersListener {

    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout drawer;
    WebView webview;
    private String TITLES[] = {"Backpack", "Hidden", "Thefts", "Lost"};
    private int ICONS[] = {android.R.drawable.ic_media_play, android.R.drawable.ic_media_play, android.R.drawable.ic_media_play, android.R.drawable.ic_media_play};
    private String NAME = "";
    private String EMAIL = "";
    private HtmlHelper html;
    private String TAG = "WebGameActivity";
    private GameManager gm;
    private boolean isGps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("WebGameActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setTitle("Subworld");
        setContentView(R.layout.activity_web);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        // Menu data
        User u = MyUserManager.getInstance().getUser();
        NAME = u.getName();
        EMAIL = u.getEmail();

        int imgId = -1;
        if (u.getChrType() == ICommon.CHRS_ARCHEOLOGIST)
            imgId = R.drawable.c1;
        else if (u.getChrType() == ICommon.CHRS_FORT_TELLER)
            imgId = R.drawable.c2;
        else if (u.getChrType() == ICommon.CHRS_SPY)
            imgId = R.drawable.c3;
        else if (u.getChrType() == ICommon.CHRS_THIEF)
            imgId = R.drawable.c4;

        mAdapter = new MyAdapter(TITLES, ICONS, NAME, EMAIL, imgId);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView
        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager
        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager
        drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view

        html = HtmlHelper.getInstance(this);
        webview = (WebView) findViewById(R.id.webView1);
        webview.setBackgroundColor(Color.parseColor("#000000"));
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient());
        webview.loadDataWithBaseURL("com.deepred.webmaptest", html.getHtml(), "text/html", "UTF-8", null);

        webview.addJavascriptInterface(new JavaScriptInterface(), "Android");

        webview.getSettings().setUseWideViewPort(false);
        webview.getSettings().setSupportZoom(false);
        webview.getSettings().setBuiltInZoomControls(false);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setSupportZoom(true);

        RadioButton tiltButton = (RadioButton) findViewById(R.id.tiltButton);
        tiltButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webview.loadUrl("javascript:togglePerpective()");
            }
        });

        gm = GameManager.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gm.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gm.unregisterListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            //case R.id.action_settings:
            //    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void updateMarker(String uid, LatLng latLng) {
        Log.d("WEB", "updateMarker" + latLng.latitude + "," + latLng.longitude + ", uid:" + uid);
        webview.loadUrl("javascript:updateMarker('" + uid + "'," + latLng.latitude + "," + latLng.longitude + ")");
    }

    @Override
    public void updateMyMarker(Location loc) {
        Log.d("WEB", "updateMyMarker: " + loc.getLatitude() + "," + loc.getLongitude() + ", bearing:" + loc.getBearing() + ", provider:" + loc.getProvider());
        webview.loadUrl("javascript:updateMyMarker(" + loc.getLatitude() + "," + loc.getLongitude() + "," + loc.getBearing() + ")");
    }

    @Override
    public void removeMarker(String uid) {
        webview.loadUrl("javascript:removeMarker(" + uid + ")");
    }

    @Override
    public void providerChanged(boolean GpsEnabled) {
        Log.d(TAG, "Provider changed: gps enabled:" + GpsEnabled);
        if (isGps != GpsEnabled) {
            isGps = GpsEnabled;
            final ImageView i = (ImageView) findViewById(R.id.gps_state);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isGps) {
                        i.setImageResource(R.drawable.gps);
                    } else {
                        i.setImageResource(R.drawable.wifi);
                    }
                }
            });
        }
    }

    @Override
    public void setZoom(float zoom) {
        Log.d("WEB", "setZoom" + zoom);
        webview.loadUrl("javascript:setZoom(" + zoom + ")");
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public void clicked(String uid) {
            Log.d("WebGameActivity", "clicked: " + uid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webview.playSoundEffect(SoundEffectConstants.CLICK);
                }
            });
        }
    }

    /*public void onMenuClick(View v) {
        Log.d(TAG, "click");
    }*/
}
