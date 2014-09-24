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
	 
	 //private GoogleAdmob g_admob ;
	 //private GooglePlayGames gp_games;
	 
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
		 		
		 		Debug.log("Poniendo GoogleAdmob");
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
	    	}
	        
	    }
	 
    public void onCreateApplication(Context applicationContext) {
    	_ctx = applicationContext;
    	
    	Iterator<String> it = g_services.keySet().iterator();
    	while(it.hasNext()){
    	  String key = it.next();
    	  g_services.get(key).onCreateApplication(applicationContext);
    	}
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




class SampleEvent extends com.tealeaf.event.Event {
	   
    boolean failed;
    double longitude, latitude;
    public SampleEvent(double longitude, double latitude) {
        super("myCustomEvent1"); // Choose an event name
        this.failed = false;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}

/*

class GooglePlayGames extends GService {
	private Activity _activity;
	private GameHelper mHelper;
	private Boolean DEBUG_BUILD;
	
	public GooglePlayGames (Activity activity, Context ctx ) {
		DEBUG_BUILD = true;
		
		// create game helper with all APIs (Games, Plus, AppState):
	    mHelper = new GameHelper(_activity, GameHelper.CLIENT_ALL);

	    // enable debug logs (if applicable)
	    if (DEBUG_BUILD) {
	        mHelper.enableDebugLog(true);
	    }

	    GameHelper.GameHelperListener listener = new GameHelper.GameHelperListener() {
	        @Override
	        public void onSignInSucceeded() {
	            // handle sign-in succeess
	        }
	        @Override
	        public void onSignInFailed() {
	            // handle sign-in failure (e.g. show Sign In button)
	        }
	        
	    };
	    
	    mHelper.setup(listener);
	}

	public void login(){
		mHelper.beginUserInitiatedSignIn();
	}
	
	public void explicitSignOut() {
		mHelper.signOut();
		//mClient.disconnect();
	}
	

	
	public void unlockAchievement(String achievement_id) {
		if(mHelper.isSignedIn()) {
			Games.Achievements.unlock(mHelper.getApiClient(), achievement_id);
		}
	}
	
	public void showAchievements() {
		_activity.startActivityForResult(Games.Achievements.getAchievementsIntent(mHelper.getApiClient()), 1);
	}
	

	
	public void sendScore(String leaderboard_id, String score )
	{
		Games.Leaderboards.submitScore(mHelper.getApiClient(), leaderboard_id, Long.parseLong(score));
	}
	
	public void showLeaderboard(String leaderboard_id) {
		_activity.startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mHelper.getApiClient(), leaderboard_id), 1);
	}
	
	@Override
	public void onStart() { 
	    mHelper.onStart(_activity);
	}
	
	@Override
	public void onStop() {
		mHelper.onStop();
	}

	@Override
    public void onActivityResult(int request, int response, Intent data) {	 
	    mHelper.onActivityResult(request, response, data);
    }
}

class GService {
	
	private String parameter_field_name = "parameters";
	
	protected JSONArray extractJSONArrayParameter(JSONObject obj, String parameterName) {
		try {
			return obj.getJSONObject(parameter_field_name).getJSONArray(parameterName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	} 
	
	
	protected JSONObject extractJSONObjectParameter(JSONObject obj, String parameterName) {
		try {
			return obj.getJSONObject(parameter_field_name).getJSONObject(parameterName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	} 
	
	protected Double extractDoubleParameter(JSONObject obj, String parameterName) {
		try {
			return obj.getJSONObject(parameter_field_name).getDouble(parameterName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected Long extractLongParameter(JSONObject obj, String parameterName) {
		try{
			return obj.getJSONObject(parameter_field_name).getLong(parameterName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected Integer extractIntParameter(JSONObject obj, String parameterName) {
		try {
			return obj.getJSONObject(parameter_field_name).getInt(parameterName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected Boolean extractBooleanParameter(JSONObject obj, String parameterName) {
		try {
			return obj.getJSONObject(parameter_field_name).getBoolean(parameterName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected String extractStringParameter(JSONObject obj, String parameterName) {
		try {
			return obj.getJSONObject(parameter_field_name).getString(parameterName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
    protected void dispatchMethod(String methodName, JSONObject obj) {

        Method myMethod;
		try {
			myMethod = this.getClass().getDeclaredMethod(methodName, new Class[] { JSONObject.class });
			myMethod.invoke(this, obj);
			
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
    }
    
    public void onCreate(Activity activity, Bundle savedInstanceState){};
    public void onCreateApplication(Context applicationContext){};
    public void onResume(){};
    public void onStart(){};
    public void onPause(){};
    public void onStop(){};
    public void onDestroy(){};
    public void onNewIntent(Intent intent){};
    public void onActivityResult(int request, int response, Intent data) {}
    public void setInstallReferrer(String referrer){};
    
}


//Singleton class for output logs
class Debug {
	
	   private static Boolean development_mode;
	   private static Debug instance = null;
	   
	   protected Debug() {
	      // Exists only to defeat instantiation.
	   }
	   
	   public static Debug getInstance() {
	      if(instance == null) {
	         instance = new Debug ();
	      }
	      return instance;
	   }
	   
	   public static void log(String Tag, String content) {
		   if (development_mode) {
			   logger.log("{GoogleServices} {", Tag, "} ", content);
		   }
	   }
	   
	   public static void log(String content) {
		   if (development_mode) {
			   logger.log("{GoogleServices} ", content);
		   }
	   }
	   
	   public static void setDevelopmentMode(Boolean state) {
		   development_mode = state;
	   }
}

class GoogleAdmob extends GService {
	
	private AdView adView;
	private Boolean development_mode = false;
	private String AD_UNIT_ID;
	private String DEVICE_ID;
	private AdRequest adRequest;
	private InterstitialAd interstitial;
	private Activity _activity;
	private Boolean is_banner;
	private RelativeLayout layout;
	private RelativeLayout.LayoutParams lp;
	
	public GoogleAdmob (Activity activity, Context ctx, Boolean banner) {
		Debug.log("GoogleAdmob", "Loading GoogleAdmob");
		
		_activity = activity;
		
		is_banner = banner;
		
		AdListener adListener = new AdListener() {
			@Override
			public void onAdLoaded(){
				Debug.log("GoogleAdmob", "onAdLoaded");
			};
			@Override
			public void onAdFailedToLoad(int errorCode){
				String message = String.format("onAdFailedToLoad (%s)", getErrorReason(errorCode));
				Debug.log("GoogleAdmob", "onAdFailedTo load " + errorCode);
			};
			@Override
			public void onAdOpened(){
				Debug.log("GoogleAdmob", "onAdOpened");
			};
			@Override
			public void onAdClosed(){
				Debug.log("GoogleAdmob", "onAdClosed");
			};
			@Override
			public void onAdLeftApplication(){
				Debug.log("GoogleAdmob", "onAdLeftApplication");
			};
		};
		
		if (development_mode) {
			adRequest = new AdRequest.Builder()
		    .addTestDevice(DEVICE_ID)
		    .build();
		} else {
			adRequest = new AdRequest.Builder().build();
		}
		
		try {
			
			if(!is_banner) {
				Debug.log("GoogleAdmob", "Interstitial Ad");
				
				// Create the interstitial.
			    interstitial = new InterstitialAd(activity);
			    interstitial.setAdUnitId(AD_UNIT_ID);
				interstitial.setAdListener(adListener);
				interstitial.loadAd(adRequest);
				
			} else {
				Debug.log("GoogleAdmob", "Banner Ad");
				
				adView = new AdView(_activity);
		        adView.setAdSize(AdSize.BANNER);
		        adView.setAdUnitId(AD_UNIT_ID);
		        lp = new RelativeLayout.LayoutParams(
		                 RelativeLayout.LayoutParams.MATCH_PARENT,
		                 RelativeLayout.LayoutParams.WRAP_CONTENT);
		        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		        adView.setAdListener(adListener);
		        adView.setLayoutParams(lp);
		        layout = new RelativeLayout(_activity);

				// int adViewId = ctx.getResources().getIdentifier("adView", "id", ctx.getPackageName());
				// adView = (AdView)activity.findViewById(adViewId);
				// adView.setAdListener(adListener);
			}
		
		}
		catch (Exception e){
			Debug.log("GoogleAdmob", "error en adlistener " + e);
			Log.e("{GoogleServices} {GoogleAdmob}", "STACKTRACE");
		    Log.e("{GoogleServices} {GoogleAdmob}", Log.getStackTraceString(e));
			
		}
		
	}
	
	
	
	public void setUnitId(String unitID) {
		this.AD_UNIT_ID = unitID;
	}
	
	public void setTestDeviceID (String deviceID) {
		this.DEVICE_ID = deviceID;
	}
	
	
	
	public void showInterstitial() {
		
		_activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
					Debug.log("GoogleAdmob", "interstitialAd.isLoaded(): " + interstitial.isLoaded());
					
					if (interstitial.isLoaded()) {								
						Debug.log("GoogleAdmob", "insterstitialAd show()");
						interstitial.show();
					} else {
						interstitial.loadAd(adRequest);	
					}
			}
		});
	}
	

	public void showBanner() {
		// adView.loadAd(adRequest);
		_activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Debug.log("GoogleAdmob", "Banner Ad adView.loadAd");
				
				adView.loadAd(adRequest);
		        adView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
		        adView.setVisibility(View.VISIBLE);
		        _activity.addContentView(layout, lp);
		        layout.addView(adView);
			}
		});
		
	}
	
	public void hideBanner() {
		adView.destroy();
		adView.setVisibility(View.GONE);
	}

	public void testMethod() {
		// Start loading the ad in the background.
		try {
				//adView.loadAd(adRequest);
			
				_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(is_banner){
							
							Debug.log("GoogleAdmob", "Banner Ad adView.loadAd");
							
							 adView.loadAd(adRequest);
					         adView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
					         _activity.addContentView(layout, lp);
					         layout.addView(adView);
						} else {
							Debug.log("GoogleAdmob", "interstitialAd.isLoaded(): " + interstitial.isLoaded());
							
							if (interstitial.isLoaded()) {								
								Debug.log("GoogleAdmob", "insterstitialAd show()");
								interstitial.show();
							} else {
								interstitial.loadAd(adRequest);
						}
					}
					}
				});
			
			

		} 
		catch (Exception e){
			logger.log("{GoogleServices} {GoogleAdmob} error al lanzar loadAd", e);
			Log.e("{GoogleServices} {GoogleAdmob}", "STACKTRACE");
		    Log.e("{GoogleServices} {GoogleAdmob}", Log.getStackTraceString(e));
		}
		
	}
	
	//  Gets a string error reason from an error code. 
	private String getErrorReason(int errorCode) {
		String errorReason = "";
		switch(errorCode) {
			case AdRequest.ERROR_CODE_INTERNAL_ERROR:
				errorReason = "Internal error";
				break;
			case AdRequest.ERROR_CODE_INVALID_REQUEST:
				errorReason = "Invalid request";
				break;
			case AdRequest.ERROR_CODE_NETWORK_ERROR:
				errorReason = "Network Error";
				break;
			case AdRequest.ERROR_CODE_NO_FILL:
				errorReason = "No fill";
				break;
		}
		return errorReason;
	}



	
	class AdLoadedEvent extends com.tealeaf.event.Event {
		   
	    boolean failed;
	    public AdLoadedEvent() {
	        super("adLoaded"); // Choose an event name
	        this.failed = false;
	    }
	}
	
	class AdOpenedEvent extends com.tealeaf.event.Event {
		   
	    boolean failed;
	    public AdOpenedEvent() {
	        super("adOpened"); // Choose an event name
	        this.failed = false;
	    }
	}
	
	class AdClosedEvent extends com.tealeaf.event.Event {
		   
	    boolean failed;
	    public AdClosedEvent() {
	        super("adClosed"); // Choose an event name
	        this.failed = false;
	    }
	}
	
	class AdLeftApplicationEvent extends com.tealeaf.event.Event {
		   
	    boolean failed;
	    public AdLeftApplicationEvent() {
	        super("adLeftApplication"); // Choose an event name
	        this.failed = false;
	    }
	}
	
	
	class AdFailedToLoadEvent extends com.tealeaf.event.Event {
		   
	    boolean failed;
	    int errorCode;
	    String reason;
	    
	    public AdFailedToLoadEvent(int errorCode, String errorReason) {
	        super("adFailedToLoad"); // Choose an event name
	        this.failed = true;
	        this.errorCode = errorCode;
	        this.reason = errorReason;
	    }
	}
}*/