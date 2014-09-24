package com.tealeaf.plugin.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tealeaf.logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class GService {
	
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
			logger.log("Intentando llamar al metodo: " + methodName);
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
