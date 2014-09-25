package com.tealeaf.plugin.plugins;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.tealeaf.EventQueue;


public class GoogleAdmob extends GService {
	
	private AdView adView;
	private Boolean development_mode = false;
	private String AD_UNIT_ID;
	private String DEVICE_ID;
	private AdRequest adRequest;
	private InterstitialAd interstitial;
	private Activity _activity;
	private Boolean is_banner = false;
	private RelativeLayout layout;
	private RelativeLayout.LayoutParams lp;
	
	// 
	private Boolean can_load_banner = true;
	private Boolean loading_banner = false;
	private Boolean banner_loaded = false;
	
	public GoogleAdmob () {
		Debug.log("Loading GoogleAdmob");
	}
	
	@Override
	public void onCreate(Activity activity, Bundle savedInstanceState) {
		
		_activity = activity;
		// Creating Listeners
		
		AdListener adListener = new AdListener() {
			@Override
			public void onAdLoaded(){
				Debug.log("GoogleAdmob: onAdLoaded");
				EventQueue.pushEvent(new AdLoadedEvent());
				
				if(is_banner){
					loading_banner = false;
					banner_loaded = true;
				}
			};
			
			@Override
			public void onAdFailedToLoad(int errorCode){
				String message = String.format("onAdFailedToLoad (%s)", getErrorReason(errorCode));
				Debug.log("GoogleAdmob: onAdFailedTo load " + errorCode + " " + message);
				EventQueue.pushEvent(new AdFailedToLoadEvent(errorCode, message));
				if(is_banner){
					loading_banner = false;
					banner_loaded = false;
				}
			};
			
			@Override
			public void onAdOpened(){
				Debug.log("GoogleAdmob: onAdOpened");
				EventQueue.pushEvent(new AdOpenedEvent());
			};
			@Override
			public void onAdClosed(){
				Debug.log("GoogleAdmob: onAdClosed");
				EventQueue.pushEvent(new AdClosedEvent());
			};
			@Override
			public void onAdLeftApplication(){
				Debug.log("GoogleAdmob: onAdLeftApplication");
				EventQueue.pushEvent(new AdLeftApplicationEvent());
			};
		};
		
		
		
		try {
			if(!is_banner) {
				Debug.log("GoogleAdmob: Interstitial Ad");
				
				// Create the interstitial.
			    interstitial = new InterstitialAd(activity);
			    interstitial.setAdUnitId(AD_UNIT_ID);
				interstitial.setAdListener(adListener);
				
				
			} else {
				Debug.log("GoogleAdmob: Banner Ad");
				
				adView = new AdView(_activity);
		        adView.setAdSize(AdSize.SMART_BANNER);
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
			Debug.error("{GoogleAdmob} onCreate failed because: " + e.getMessage() 
					+ ". Printing Stacktrace", e);
			
		}
	}
	
	public void setDevelopmentMode(Boolean dev_mode){
		development_mode = dev_mode;
	}
	
	public void setBannerType() {
		is_banner = true;
	}
	
	public void setInterstitialType(){
		is_banner = false;
	}
	
	public void setUnitId(String unitID) {
		this.AD_UNIT_ID = unitID;
	}
	
	public void setTestDeviceID (String deviceID) {
		this.DEVICE_ID = deviceID;
	}
	
	private void reloadInterstitial(){
		if (development_mode) {
			adRequest = new AdRequest.Builder()
		    .addTestDevice(DEVICE_ID)
		    .build();
		} else {
			adRequest = new AdRequest.Builder().build();
		}
		interstitial.loadAd(adRequest);
	}
	
	public void loadInterstitial(JSONObject data) {
		Debug.log("GoogleAdmob: Loading interstitial");
		try {
			
			_activity.runOnUiThread(new Runnable() {				
				@Override
				public void run() {
						Debug.log("GoogleAdmob: interstitial.isLoaded(): " + interstitial.isLoaded());
							if (!interstitial.isLoaded()) {	
								reloadInterstitial();
							}
				}
			});
			
			interstitial.loadAd(adRequest);
		} catch (Exception e) {
			Debug.error("{GoogleAdmob}  Loading Interstitial failed because: " + e.getMessage() 
					+ ". Printing Stacktrace", e);
		}
		
	}
	public void displayInterstitial(JSONObject data) {
		Debug.log("GoogleAdmob: Display interstitial");
		try {
			_activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
						Debug.log("GoogleAdmob: interstitial.isLoaded(): " + interstitial.isLoaded());
			
							if (interstitial.isLoaded()) {								
								Debug.log("GoogleAdmob: insterstitialAd show()");
								interstitial.show();
							} else {
								if (development_mode) {
									adRequest = new AdRequest.Builder()
								    .addTestDevice(DEVICE_ID)
								    .build();
								} else {
									adRequest = new AdRequest.Builder().build();
								}
								interstitial.loadAd(adRequest);	
							}
						
				}
			});
		} catch (Exception e) {
			Debug.error("{GoogleAdmob}  Displaying failed because: " + e.getMessage() 
					+ ". Printing Stacktrace", e);
		}
	}
	
	private void _loadBanner() {
		loading_banner = true;
		banner_loaded = false;
		/*if (development_mode) {
			adRequest = new AdRequest.Builder()
		    .addTestDevice(DEVICE_ID)
		    .build();
		} else {
			adRequest = new AdRequest.Builder().build();
		}*/
		adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}
	
	public void loadBanner(JSONObject data){
		Debug.log("GoogleAdmob: Loading Banner");
		
		if (loading_banner) {
			Debug.log("GoogleAdmob: A banner is already being loaded");
			return;
		}
		
		try {
		_activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(banner_loaded) {
					Debug.log("GoogleAdmob: Reloading Banner");
					_loadBanner();
				} else {
					Debug.log("GoogleAdmob: Banner first loading");
					_loadBanner();
					adView.loadAd(adRequest);
			        adView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
			        adView.setVisibility(View.GONE);
			        _activity.addContentView(layout, lp);
			        layout.addView(adView);
				}
			}
		});
		} catch (Exception e) {
			Debug.error("{GoogleAdmob}  Loading Banner failed because: " + e.getMessage() 
							+ ". Printing Stacktrace", e);
		}
	}

	public void displayBanner(JSONObject data) {
		Debug.log("GoogleAdmob: Displaying Banner");
		try {
		// adView.loadAd(adRequest);
			_activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					adView.setVisibility(View.VISIBLE);
				}
			});
		
		} catch (Exception e) {
			Debug.error("{GoogleAdmob}  Displaying Banner failed because: " + e.getMessage() 
					+ ". Printing Stacktrace", e);
		}
	}
	
	
	public void hideBanner(JSONObject data) {
		try {
			_activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					adView.setVisibility(View.GONE);
				}
			});
		} catch (Exception e) {
			Debug.error("{GoogleAdmob}  Hiding Banner failed because: " + e.getMessage() 
					+ ". Printing Stacktrace", e);
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
}