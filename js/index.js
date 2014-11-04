function pluginSend(evt, params) {
    NATIVE && NATIVE.plugins.sendEvent("GoogleServicesPlugin", evt,
                JSON.stringify(params || {}));
}

function pluginOn(evt, next) {
    NATIVE && NATIVE.events.registerHandler(evt, next);
}

function invokeCallbacks(list, clear) {
    // Pop off the first two arguments and keep the rest
    var args = Array.prototype.slice.call(arguments);
    args.shift();
    args.shift();

    // For each callback,
    for (var ii = 0; ii < list.length; ++ii) {
        var next = list[ii];

        // If callback was actually specified,
        if (next) {
            // Run it
            next.apply(null, args);
        }
    }

    // If asked to clear the list too,
    if (clear) {
        list.length = 0;
    }
}


var GooglePlay = Class(function () {

  var loginCB = [];
  var loginFailCB = [];
  var firstLoginCB = [];
  var calledFirstLogin = false;
  this.init = function(opts) {
    logger.log("{GoogleServices} {GooglePlayGames} Registering for events on startup");

    pluginOn("gameplayLogin", function(evt) {
      logger.log("{gameplay} State updated:", evt.state);

	  if (evt.state == "open") {
      invokeCallbacks(loginCB, true, evt.state === "open", evt);
      if(!calledFirstLogin){
      	invokeCallbacks(firstLoginCB, true, evt.state === "open", evt);
      	calledFirstLogin = true;
      }
    } else {
    	invokeCallbacks(loginFailCB, false, evt.state === "close", evt);
    }
    });
  };
  
  this.onFirstLogin = function(callback) {
  	firstLoginCB.push(callback);
  };
  
  this.onLoginFailed = function(callback) {
  	logger.log("{GoogleServices} {GooglePlayGames} Login failed");
  	loginFailCB.push(callback);
  };
  
  this.sendToPlugin = function(method, data) {
  	 var obj = {service: "GooglePlayGames",
  	 			method: method,
  	 			parameters: data || {}};
  	 pluginSend("callMethod", obj);
  };

  this.sendAchievement = function(achievementID, percentSolved) {
    logger.log("{GoogleServices} {GooglePlayGames} Sending of achievement");

    var param = {"achievement_id":achievementID,"percent_solved":percentSolved};

    this.sendToPlugin("sendAchievement",param);
  };

  this.sendScore = function(leaderBoardID, score) {
    logger.log("{GoogleServices} {GooglePlayGames} Sending of Score to leaderboard");

    var param = {"leaderboard_id":leaderBoardID,"score":score};

    this.sendToPlugin("sendScore",param);
  };

  this.setNumber = function(name, val) {
    return;
  };

  this.initSync = function(param_name) {
    return;
  };

  this.logout = function() {
    logger.log("{GoogleServices} {GooglePlayGames} Logging Out a user");
    this.sendToPlugin("explicitSignOut");
  };

  this.login = function(next) {
    logger.log("{GoogleServices} {GooglePlayGames} Logging in a user");
    loginCB.push(next);
    this.sendToPlugin("beginUserInitiatedSignIn");
  };

  this.showLeaderBoard = function() {
    logger.log("{GoogleServices} {GooglePlayGames} Showing Leaderboard");
    
    this.sendToPlugin("showLeaderBoard");
  };

  this.showAchievements = function() {
    logger.log("{GoogleServices} {GooglePlayGames} Showing Achievements");
    this.sendToPlugin("showAchievements");
  };
  
  this.rateApplication = function() {
  	logger.log("{GoogleServices} {GooglePlayGames} Opening Google Play app page");
  	this.sendToPlugin("openGooglePlayAppPage");
  };
});

var AdMob = Class(function () {
	
	this.sendToPlugin = function(method, data) {
	  	 var obj = {service: "GoogleAdmob",
	  	 			method: method,
	  	 			parameters: data || {}};

	  	 pluginSend("callMethod", obj);
  	};
  
  	this.loadBanner = function () {
		logger.log("{GoogleServices} {GoogleAdMob} Load banner.");
		this.sendToPlugin("loadBanner");
	};
	
	this.displayBanner = function () {
		logger.log("{GoogleServices} {GoogleAdMob} Display banner.");
		this.sendToPlugin("displayBanner");
	};
	
	this.hideBanner = function () {
		logger.log("{GoogleServices} {GoogleAdMob} Hide banner.");
		this.sendToPlugin("hideBanner");
	};
	
	// INTERSTITIAL FUNCTIONS
	
	this.loadInterstitial = function () {
		logger.log("{GoogleServices} {GoogleAdMob} Load interstitial.");
		this.sendToPlugin("loadInterstitial");
	};
	
	this.displayInterstitial = function () {
		logger.log("{GoogleServices} {GoogleAdMob} Display interstitial.");
		this.sendToPlugin("displayInterstitial");
	};
});

var GoogleServices = Class(function () {
	
    this.google_play = new GooglePlay();
    this.admob = new AdMob();

    this.init = function(opts) {
    
    };
	
});

exports = new GoogleServices();