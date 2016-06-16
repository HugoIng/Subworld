package com.deepred.subworld;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by aplicaty on 09/03/16.
 */
public class HtmlHelper {

    private static final String TAG = HtmlHelper.class.getSimpleName();
    private static final Object lock = new Object();
    private static volatile HtmlHelper INSTANCE = null;
    private String html;
    private String mime = "text/html";
    private String encoding = "utf-8";
    private String key;

    private String NORMAL_ZOOM = "1";
    private String TILTED_ZOOM = "1.5";
    private String NORMAL_PERSPECTIVE = "0";
    private String TILTED_PERSPECTIVE = "1500";
    private String NORMAL_Y_TRANSLATE = "0";
    private String TILTED_Y_TRANSLATE = "-60";


    private HtmlHelper(Context ctx) {
        key = ctx.getString(R.string.google_maps_key_web);

        InputStream is = null;
        int size = 0;

        try {
            is = ctx.getAssets().open("map.html");

            size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            html = new String(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HtmlHelper getInstance(Context ctx) {
        if(INSTANCE == null) {
            INSTANCE = new HtmlHelper(ctx);
        }
        return INSTANCE;
    }

    public String getHtml() {
        return html;
    }
    public String getEncoding() {
        return encoding;
    }
    public  String getMime() {
        return mime;
    }
}




/*google.maps.event.addListenerOnce(map, 'tilesloaded', function(){
    //this part runs when the mapobject is created and rendered
    google.maps.event.addListenerOnce(map, 'tilesloaded', function(){
        //this part runs when the mapobject shown for the first time
    });
});*/


/*// Bounds for North America
   var strictBounds = new google.maps.LatLngBounds(
     new google.maps.LatLng(28.70, -127.50),
     new google.maps.LatLng(48.85, -55.90)
   );

   // Listen for the dragend event
   google.maps.event.addListener(map, 'dragend', function() {
     if (strictBounds.contains(map.getCenter())) return;

     // We're out of bounds - Move the map back within the bounds

     var c = map.getCenter(),
         x = c.lng(),
         y = c.lat(),
         maxX = strictBounds.getNorthEast().lng(),
         maxY = strictBounds.getNorthEast().lat(),
         minX = strictBounds.getSouthWest().lng(),
         minY = strictBounds.getSouthWest().lat();

     if (x < minX) x = minX;
     if (x > maxX) x = maxX;
     if (y < minY) y = minY;
     if (y > maxY) y = maxY;

     map.setCenter(new google.maps.LatLng(y, x));
   });
*/
