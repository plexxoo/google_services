Google Services plugin for Gameclosure
=============

This is a [Gameclosure](http://www.gameclosure.com) plugin for Google Services.

It is based on the following plugins:
  * [Gameplay](https://github.com/hashcube/gameplay) from Ramprasad Rajendran and modifications from Davide Aimone
  * [Admob](https://github.com/jpdibacco/gameclosure) from Juan Patricio Di Bacco

And why we have created yet another gameplay and admob plugin? We were having problems using them together. So we decided to put them into a single plugin.

It uses the AdMob and GooglePlay services (only android at this moment)

Google AdMob Features:
  * **Banner Ads**
  * **Interstitial Ads**
  
Google Play Features
  * **Login/Logout**
  * **Leaderboard**
  * **Achievements**
  * **Events**: onFirstLogin, onLoginFailed
  
What is comming 
  * **(Admob) AdEvents** (Loaded, failed, opened, closed & leftApplication) callbacks sended to javascript.
  * **(GooglePlay) Rate application** A method to open the application page in Google Play
  
How to Install
-------------
clone this repo to '''addons''' folder inside devkit and do following
```
$ cd google_services
$ android update project -p android/google-play-services_lib/
$ android update project -p android/BaseGameUtils/
```

Configuration in manifest file
-------------

### Development mode:

Inside the android section in **manifest.json** use GServicesDebug parameter:
```
"android": {
		"versionCode": 9,
		"icons": {
			"36": "resources/icons/android36.png",
			"48": "resources/icons/android48.png",
			"72": "resources/icons/android72.png",
			"96": "resources/icons/android96.png"
		},
		
		"GServicesDebug": "true"
	},
```



### GoogleAdmob

Inside the android section in **manifest.json** you can use these parameters parameters:

  * **"useAdmob"**: "true"
  * **"admobType"**: "banner", (banner|**"Interstitial"**)
  * **"admobUnitID"**: "ca-app-pub-andyournumber"
  * **"testDeviceID"**:"yourTestDeviceIDNumber"

Example of production manifest
```js
"android": {
		"versionCode": 9,
		"icons": {
			"36": "resources/icons/android36.png",
			"48": "resources/icons/android48.png",
			"72": "resources/icons/android72.png",
			"96": "resources/icons/android96.png"
		},
		
		"useAdmob": true,
		"admobType": "banner",
		"admobUnitID": "ca-app-pub-yournumber"
		
	},
```

Example of development manifest
```js
"android": {
		"versionCode": 9,
		"icons": {
			"36": "resources/icons/android36.png",
			"48": "resources/icons/android48.png",
			"72": "resources/icons/android72.png",
			"96": "resources/icons/android96.png"
		},
		
		"GServicesDebug": true,
		"useAdmob": true,
		"admobType": "banner",
		"admobUnitID": "ca-app-pub-yournumber",
		"testDeviceID": "yourTestDeviceID"
		
	},
```


### GooglePlay

Inside the android section in **manifest.json** you can use these parameters parameters:

  * **"useGooglePlay"**: "true"
  * **"GooglePlayID"**: "yourGooglePlayID"

Example of production manifest
```js
   "android": {
		"versionCode": 9,
		"icons": {
			"36": "resources/icons/android36.png",
			"48": "resources/icons/android48.png",
			"72": "resources/icons/android72.png",
			"96": "resources/icons/android96.png"
		},
		
		"useGooglePlay": "true",
		"GooglePlayID": "yourGooglePlayID"
		
	}
```

Admob Usage
-------------

```
import plugins.google_services.google_services as g_services;

g_services.admob.loadBanner();
g_services.admob.showBanner();


```

Google Play Usage
-------------

```
import plugins.google_services.google_services as g_services;

//Suscribing to do something on a first login:
g_services.google_play.onFirstLogin(function(){
	// do something
});

//Suscribing to do something on a login fail:

g_services.google_play.onLoginFailed(function(){
	// do something
});  

// Login
g_services.google_play.login();

// or
g_services.google_play.login(function() {

	// Sending achievement
	g_services.google_play.sendAchievement("achievement_Id", integer_percent_solved);
	
	// Send score
	g_services.google_play.sendScore("leaderscore_id", your_score);
	
	// showing leaderboards
	g_services.google_play.showLeaderBoard();
	
	// Showing achievements
	g_services.google_play.showAchievements();

});

```