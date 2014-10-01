package com.tealeaf.plugin.plugins;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.*;
import com.tealeaf.plugin.IPlugin;
import com.tealeaf.event.*;
import com.tealeaf.EventQueue;


import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.pm.PackageManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GoogleServicesPlugin implements IPlugin {
	
	 private Context _ctx;
	 private HashMap<String, String> manifestKeyMap = new HashMap<String,String>();
	 private HashMap<String, GService> g_services = new HashMap<String, GService>();
	 Boolean dev_mode = false;
	 
	 public GoogleServicesPlugin() {
		 dev_mode = false;
	 }
	 
	 public static final String md5(final String s) {
		    try {
		        // Create MD5 Hash
		        MessageDigest digest = java.security.MessageDigest
		                .getInstance("MD5");
		        digest.update(s.getBytes());
		        byte messageDigest[] = digest.digest();

		        // Create Hex String
		        StringBuffer hexString = new StringBuffer();
		        for (int i = 0; i < messageDigest.length; i++) {
		            String h = Integer.toHexString(0xFF & messageDigest[i]);
		            while (h.length() < 2)
		                h = "0" + h;
		            hexString.append(h);
		        }
		        return hexString.toString();

		    } catch (NoSuchAlgorithmException e) {
		        Debug.log("Problem in md5 function: " + e.getMessage());
		    }
		    return "";
		}
	 
	 private void loadManifestKeys(Activity activity){
		 PackageManager manager = activity.getBaseContext().getPackageManager();
		 // We are not searching for GooglePLayID because it is used directly 
		 // on manifest.xml && manifest.xsl
		 String[] keys = {"GServicesDebug", "useAdmob", "admobUnitID", 
				 		  "testDeviceID", "useGooglePlay", "admobType" };
	        try {
	            Bundle meta = manager.getApplicationInfo(activity.getApplicationContext().getPackageName(),
	                    PackageManager.GET_META_DATA).metaData;
	            
	            for (String k : keys) {
	                if (meta.containsKey(k)) {
	                	//Log.d("GoogleServices", "Extrayendo '" + k + "' con valor " + meta.get(k).toString());
	                    manifestKeyMap.put(k, meta.get(k).toString());
	                }
	            }
	        } catch (Exception e) {
	            Debug.error("Exception while loading manifest keys:" + e.getMessage());
	        }
	        
	 }
	 
	 private void checkGoogleAdmob(){
		 	if(manifestKeyMap.get("useAdmob").equals("true")) {
		 		try {
		 		GoogleAdmob g_admob = new GoogleAdmob();
		 		
		 		// Banner or interstitial
		 		if(manifestKeyMap.get("admobType").equals("banner")) {
		 			g_admob.setBannerType();
		 		} else {
		 			g_admob.setInterstitialType();
		 		}
		 		
		 		if(dev_mode){
		 			g_admob.setDevelopmentMode(true);
		 			String deviceID = manifestKeyMap.get("testDeviceID");
		 			g_admob.setTestDeviceID(deviceID);
		 		}
		 		
		 		String unitID = manifestKeyMap.get("admobUnitID");
		 		g_admob.setUnitId(unitID);
		 		
		 		g_services.put("GoogleAdmob", g_admob);
		 		} catch (Exception e) {
		 			Debug.error("Error inside checkGoogleAdmob because " + e.toString() +
		 							". Printing stacktrace", e);
		 		}
		 	}
	 }
	 
	 public void checkGoogleGames() {
		 
		 if(manifestKeyMap.get("useGooglePlay").equals("true")) {
		 		GooglePlayGames g_games = new GooglePlayGames();
				/*if(dev_mode){
		 			g_games.setDebugMode();
		 		}*/
		 		g_services.put("GooglePlayGames", g_games);
		 	}
	 }
	 
	 public void printDeviceID(Activity activity) {
		// Obtaining deviceID
	     String android_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
	     String deviceId = md5(android_id).toUpperCase();
	   	 Debug.log("Printing DeviceID: " + deviceId);
	 }
	 
	 public void onCreate(Activity activity, Bundle savedInstanceState) {

		 	loadManifestKeys(activity);

		 	
		 	// Setting the development mode in debug.
		 	if(manifestKeyMap.get("GServicesDebug").equals("true")) {
		 		dev_mode = true;
		 		Debug.setDevelopmentMode(true);		 		
		 		// UNCOMMENT TO GET THE DEVICE ID
		 		// printDeviceID(activity);
		 	}
		 	
	        
	   	 	
	   	 	// checking for google services activated in manifest.json
	   	 	checkGoogleAdmob();
	   	 	checkGoogleGames();
		 	
	   	 	//g_services.put("GooglePlayGames", new GooglePlayGames(activity, _ctx));
	   	 	
	   	 	Iterator<String> it = g_services.keySet().iterator();
	    	while(it.hasNext()){
	    	  String key = it.next();
	    	  g_services.get(key).onCreate(activity, savedInstanceState);
	    	  g_services.get(key).setApplicationContext(_ctx);
	    	}
	        
	    }
	 
    public void onCreateApplication(Context applicationContext) {
    	_ctx = applicationContext;
    	
    	// In this event, the g_services HashMap is not created
    	
    }
    
    public void onResume() {
    	Iterator<String> it = g_services.keySet().iterator();
    	while(it.hasNext()){
    	  String key = it.next();
    	  g_services.get(key).onResume();
    	}
    }
    
    public void onStart() {
    	Iterator<String> it = g_services.keySet().iterator();
    	while(it.hasNext()){
    	  String key = it.next();
    	  g_services.get(key).onStart();
    	}
    }
    
    public void onPause() {
    	Iterator<String> it = g_services.keySet().iterator();
    	while(it.hasNext()){
    	  String key = it.next();
    	  g_services.get(key).onPause();
    	}
    }
    
    public void onStop() {
    	Iterator<String> it = g_services.keySet().iterator();
    	while(it.hasNext()){
    	  String key = it.next();
    	  g_services.get(key).onStop();
    	}
    }
    
    public void onDestroy() {
    	Iterator<String> it = g_services.keySet().iterator();
    	while(it.hasNext()){
    	  String key = it.next();
    	  g_services.get(key).onDestroy();
    	}
    }
    
    public void onNewIntent(Intent intent) {
    	Iterator<String> it = g_services.keySet().iterator();
    	while(it.hasNext()){
    	  String key = it.next();
    	  g_services.get(key).onNewIntent(intent);
    	}
    }
    
    public void onActivityResult(Integer request, Integer result, Intent data) {
    	Iterator<String> it = g_services.keySet().iterator();
    	while(it.hasNext()){
    	  String key = it.next();
    	  g_services.get(key).onActivityResult(request, result, data);
    	}
    }
    
    public void setInstallReferrer(String referrer) {
    	Iterator<String> it = g_services.keySet().iterator();
    	while(it.hasNext()){
    	  String key = it.next();
    	  g_services.get(key).setInstallReferrer(referrer);
    	}
    }
    
    public boolean consumeOnBackPressed() {
        return true;
      }

      public void onBackPressed() {
      }

    
    public void callMethod (String jsonData) {

    	try {
    		JSONObject data = new JSONObject(jsonData);
    		
    		String service = data.optString("service");
    		String method = data.optString("method");  
            
            g_services.get(service).dispatchMethod(method, data);
            
    		
    	} catch (Exception e){
    		Debug.error("There was a problem calling method at GoogleServicesPlugin: " +
    					e.getMessage() + ". Printing stacktrace",e);
    	}
    	    	
    	 
    }
}

