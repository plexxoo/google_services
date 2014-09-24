package com.tealeaf.plugin.plugins;

import android.util.Log;

// Singleton class for output logs
class Debug {
	
	   private static final String TAG = "GoogleServices";
	   private static Boolean development_mode = false;
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
	   
	   public static void log(String content) {
		   if (development_mode) {
			   Log.d(TAG, content);
		   }
	   }
	   
	   public static void log(String content, Throwable tw){
		   if (development_mode) {
			   Log.d(TAG, content, tw);
		   }
	   }
	   
	   public static void warning(String content){
		   Log.w(TAG, content);
	   }
	   
	   public static void warning(String content, Throwable tw){
		   Log.w(TAG, content, tw);
	   }
	   
	   public static void error(String content){
		   Log.e(TAG, content);
	   }
	   
	   public static void error(String content, Throwable tw){
		   Log.e(TAG, content, tw);
	   }
	   
	   public static void setDevelopmentMode(Boolean state) {
		   development_mode = state;
	   }
}
