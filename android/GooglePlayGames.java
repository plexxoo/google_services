package com.tealeaf.plugin.plugins;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.WindowManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.GameHelper;
import com.tealeaf.EventQueue;

import com.tealeaf.plugin.IPlugin;

/**
 * See http://developer.android.com/reference/android/app/Activity.html
 * to understand lifecycle of an Activity
 */
public class GooglePlayGames extends GService implements GameHelper.GameHelperListener,RealTimeMessageReceivedListener,
RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener 
{
	/** used for gameplayLogin event */
	public static final String EVENT_LOGIN = "gameplayLogin";
	/** used to cjeck if user is signed in */
	public static final String EVENT_SIGNEDIN = "gameplaySignedIn";
	/** used to cjeck if user is signed in */
	public static final String EVENT_MESSAGE = "gameplayMsgReceived";
	/** used to cjeck if user is signed in */
	public static final String EVENT_ROOMCONNECTED = "gameplayRoomConnected";
	/** used to send if this device is game master or not */
	public static final String EVENT_ISMASTER = "gameplayIsMaster";
	/** used when map definition is received */
	public static final String EVENT_MAPRECEIVED = "gameplayMapReceived";
	/** used when all acks are received*/
	public static final String EVENT_ALLACKRECEIVED = "gameplayAllAckReceived";
	/** sent when the game can start*/
	public static final String EVENT_STARTGAME = "gameplayStartGame";
	/** sent when the game can start*/
	public static final String EVENT_MOVEDONE = "gameplayMoveDone";
	/** sent when a player leave the game*/
	public static final String EVENT_PLAYERLEAVE = "gameplayPlayerLeave";
	/** sent when user cancel action*/
	public static final String EVENT_CANCEL = "gameplayCancel";
	/** sent when an error has occured*/
	public static final String EVENT_ERROR = "gameplayError";
	
	
	/** used to identifies msg that contains map definition*/
	public static final String MSG_MAP = "#MAP#";
	/** used to identifies msg that are ack*/
	public static final String MSG_ACK = "#ACK#";
	/** used to identifies msg to start the game*/
	public static final String MSG_STARTGAME = "#STG#";
	/** used to identifies msg to start the game*/
	public static final String MSG_MOVE = "#MOV#";
	
	
	/** at least 2 players required for our game */
	final static int MIN_PLAYERS = 2;
	/** max 4 players required for our game */
	final static int MAX_PLAYERS = 4;
	
	Context _context;
	Activity _activity;

	// The game helper object. This class is mainly a wrapper around this object.
	protected GameHelper mHelper;

	// We expose these constants here because we don't want users of this class
	// to have to know about GameHelper at all.
	public static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;
	public static final int CLIENT_APPSTATE = GameHelper.CLIENT_APPSTATE;
	public static final int CLIENT_PLUS = GameHelper.CLIENT_PLUS;
	public static final int CLIENT_ALL = GameHelper.CLIENT_ALL;

	// Requested clients. By default, that's just the games client.
	protected int mRequestedClients = CLIENT_GAMES;

	// stores any additional scopes.
	private String[] mAdditionalScopes;

	protected String mDebugTag = "BaseGameActivity";
	protected boolean mDebugLog = false;

	// Room ID where the currently active game is taking place; null if we're not playing.
	protected String mRoomId = null;
	
	// The participants in the currently active game
	protected ArrayList<Participant> mParticipants = null;
    
    // My participant ID in the currently active game
	protected String mMyId = null;
    
    // Message buffer for sending messages
	protected byte[] mMsgBuf = new byte[2];

	// request code for the "select players" UI
	// can be any number as long as it's unique
	protected final int RC_SELECT_PLAYERS = 10000;

	// arbitrary request code for the waiting room UI.
	// This can be any integer that's unique in your Activity.
	protected final static int RC_WAITING_ROOM = 10002;

	// are we already playing?
	protected boolean mPlaying = false;
	
	//list of received ack from other players
	protected LinkedList<String> mLstAcks = new LinkedList<String>();
	
	

	public class GPStateEvent extends com.tealeaf.event.Event {
		String state;

		public GPStateEvent(String name, String state) {
			super(name);
			this.state = state;
		}
	}

	/** Constructs a BaseGameActivity with default client (GamesClient). */
	public GooglePlayGames() {
		//nothing to do...
		//setRequestedClients(CLIENT_ALL);
	}

	/**
	 * Constructs a BaseGameActivity with the requested clients.
	 * @param requestedClients The requested clients (a combination of CLIENT_GAMES,
	 *         CLIENT_PLUS and CLIENT_APPSTATE).
	 */
	public GooglePlayGames(int requestedClients) {
		setRequestedClients(requestedClients);
	}

	/**
	 * Sets the requested clients. The preferred way to set the requested clients is
	 * via the constructor, but this method is available if for some reason your code
	 * cannot do this in the constructor. This must be called before onCreate in order to
	 * have any effect. If called after onCreate, this method is a no-op.
	 *
	 * @param requestedClients A combination of the flags CLIENT_GAMES, CLIENT_PLUS
	 *         and CLIENT_APPSTATE, or CLIENT_ALL to request all available clients.
	 * @param additionalScopes.  Scopes that should also be requested when the auth
	 *         request is made.
	 */
	protected void setRequestedClients(int requestedClients, String ... additionalScopes) {
		mRequestedClients = requestedClients;
		mAdditionalScopes = additionalScopes;
	}

	public void onCreateApplication(Context applicationContext) {
		_context = applicationContext;
	}

	public void onCreate(Activity activity, Bundle savedInstanceState) {
		_activity = activity;
	}

	/**
	 * Starts game helper. 
	 */
	public void initGameHelper()
	{
		// create game helper with defined APIs (can be Games, Plus, AppState)
		mHelper = new GameHelper(_activity, mRequestedClients);

		// enable debug logs (if applicable)
		
		if (mDebugLog) {
			mHelper.enableDebugLog(true);
		}

		mHelper.setup(this);
		mHelper.setMaxAutoSignInAttempts(0);
	}

	/**
	 * Activity lifecycle.
	 * Called as first methon on Activity after its creation
	 * @param savedInstanceState not used
	 */
	public void onCreate(Bundle savedInstanceState) {
		initGameHelper();
	}

	/**
	 * Activity lifecycle.
	 * Called after onPause() or onStart(). When returns, the activity is running
	 */
	public void onResume() {
		if(mHelper == null)
			initGameHelper();

		if (mDebugLog) {
			enableDebugLog(mDebugLog, mDebugTag);
		}
	}

	/**
	 * Activity lifecycle.
	 */
	public void onStart() {
		mHelper.onStart(_activity);
	}

	/**
	 * Activity lifecycle.
	 */
	public void onPause() {
		//nothing to do...
	}

	/**
	 * Activity lifecycle.
	 */
	public void onStop() {
		
		// if we're in a room, leave it.
        leaveRoom();
        
		mHelper.onStop();
	}

	/**
	 * Activity lifecycle.
	 */
	public void onDestroy() {
		//nothing to do...
	}

	@Override
	public void onSignInFailed(){
		// Sign in has failed. So show the user the sign-in button.
		EventQueue.pushEvent(new GPStateEvent(EVENT_LOGIN,"close"));
	}

	@Override
	public void onSignInSucceeded(){
		// show sign-out button, hide the sign-in button
		
		EventQueue.pushEvent(new GPStateEvent(EVENT_LOGIN,"open"));
	}

	public void onNewIntent(Intent intent) {
		//TODO
	}

	public void setInstallReferrer(String referrer) {
		//TODO
	}

	/**
	 * Called when an activity you launched exits, giving you the requestCode you started it with,
	 * the resultCode it returned, and any additional data from it. 
	 *  
	 * The resultCode will be RESULT_CANCELED if the activity explicitly returned that,
	 * didn't return any result, or crashed during its operation.
	 *  
	 * You will receive this call immediately before onResume() when your activity is re-starting.
	 * @param request The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
	 * @param response The integer result code returned by the child activity through its setResult().
	 * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	//@Override
	public void onActivityResult(int request, int response, Intent data) {
		//int request = iRequest.intValue();
		//int response = iResponse.intValue();

		//process request from select_players
		if (request == RC_SELECT_PLAYERS) 
		{
			// user canceled
			if (response != Activity.RESULT_OK)	{
				EventQueue.pushEvent(new GPStateEvent(EVENT_CANCEL, null));
				return;
			}

			// get the invitee list
			Bundle extras = data.getExtras();
			final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

			// get auto-match criteria
			Bundle autoMatchCriteria = null;
			int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
			int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

			if (minAutoMatchPlayers > 0) 
			{
				autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
						minAutoMatchPlayers, maxAutoMatchPlayers, 0);
			} 
			else 
			{
				autoMatchCriteria = null;
			}

			// create the room and specify a variant if appropriate
			RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
			roomConfigBuilder.addPlayersToInvite(invitees);
			if (autoMatchCriteria != null) {
				roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
			}
			RoomConfig roomConfig = roomConfigBuilder.build();
			Games.RealTimeMultiplayer.create(getApiClient(), roomConfig);

			// prevent screen from sleeping during handshake
			_activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		else if (request == RC_WAITING_ROOM) 
		{
			if (response == Activity.RESULT_OK) {
				// (start game)
			}
			else if (response == Activity.RESULT_CANCELED) {
				// Waiting room was dismissed with the back button. The meaning of this
				// action is up to the game. You may choose to leave the room and cancel the
				// match, or do something else like minimize the waiting room and
				// continue to connect in the background.

				// in this example, we take the simple approach and just leave the room:
				leaveRoom();
				
				EventQueue.pushEvent(new GPStateEvent(EVENT_CANCEL, null));
			}
			else if (response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
				// player wants to leave the room.
				leaveRoom();
			}
		}
		else
			mHelper.onActivityResult(request, response, data);

	}

	// create a RoomConfigBuilder that's appropriate for your implementation
	private RoomConfig.Builder makeBasicRoomConfigBuilder() {
		return RoomConfig.builder(this)
				.setMessageReceivedListener(this)
				.setRoomStatusUpdateListener(this);
	}

	public boolean consumeOnBackPressed() {
		return true;
	}

	public void onBackPressed() {
		logError("test");
	}

	protected GoogleApiClient getApiClient() {
		return mHelper.getApiClient();
	}
	
	/**
	 * Propagates result of isSignedIn() to GC bus
	 * @param sDummyParam
	 */
	public void isSignedIn(String sDummyParam) {
		EventQueue.pushEvent(new GPStateEvent(EVENT_SIGNEDIN,isSignedIn() ? "true" : "false"));
	}

	/**
	 * @return if the user is signed in or not into services
	 */
	protected boolean isSignedIn() {
		return mHelper.isSignedIn();
	}

	/**
	 * Starts user login process
	 * @param dummyParam
	 */
	public void beginUserInitiatedSignIn(JSONObject data) {
	//public void beginUserInitiatedSignIn(String dummyParam) {
		mHelper.beginUserInitiatedSignIn();
	}

	/**
	 * Sign out user
	 * @param dummyParam
	 */
	public void signOut(String dummyParam) {
		mHelper.signOut();
	}

	protected void showAlert(String title, String message) {
		showAlert(title, message);
	}

	protected void showAlert(String message) {
		showAlert(message);
	}

	protected void enableDebugLog(boolean enabled, String tag) {
		mDebugLog = true;
		mDebugTag = tag;
		if (mHelper != null) {
			enableDebugLog(enabled, tag);
		}
	}

	protected String getInvitationId() {
		return mHelper.getInvitationId();
	}

	protected void reconnectClients(int whichClients) {
		reconnectClients(whichClients);
	}

	protected String getScopes() {
		return getScopes();
	}

	protected String[] getScopesArray() {
		return getScopesArray();
	}

	protected boolean hasSignInError() {
		return mHelper.hasSignInError();
	}

	protected GameHelper.SignInFailureReason getSignInError() {
		return mHelper.getSignInError();
	}

	public void sendAchievement(JSONObject param)
	//public void sendAchievement(String param)
	{
		if(!(mHelper.isSignedIn())){
			Debug.log("{gameplay-native} not signed in");
			return;
		}

		Debug.log("{gameplay-native} Inside sendAchievement");
		final Bundle params = new Bundle();
		String achievementID = extractStringParameter(param, "achievement_id");
		Long percentSolved = extractDoubleParameter(param, "percent_solved").longValue();
		Games.Achievements.unlock(getApiClient(), achievementID);
		/*try {
			JSONObject ldrData = new JSONObject(param);
			Iterator<?> keys = ldrData.keys();
			while( keys.hasNext() ){
				String key = (String)keys.next();
				Object o = ldrData.get(key);
				if(key.equals("achievementID")){
					achievementID = (String) o;
					continue;
				}
				if(key.equals("percentSolved")){
					percentSolved = new Float(o.toString());
					continue;
				}
				params.putString(key, (String) o);
			}
			
		} catch(JSONException e) {
			logger.log("{gameplay-native} Error in Params of sendAchievement because "+ e.getMessage());
		}*/
	}

	public void showLeaderBoard(JSONObject data)
	//public void showLeaderBoard(String dummyParam)
	{
		if(!(mHelper.isSignedIn())){
			Debug.log("{gameplay-native} not signed in");
			return;
		}
		//TODO: getlLeaderboardsIndent accepts id as parameter to show a specific leaderboard.
		_activity.startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()), 1);
	}

	public void showAchievements(JSONObject data)
	//public void showAchievements(String dummyParam)
	{
		if(!(mHelper.isSignedIn())){
			Debug.log("{gameplay-native} not signed in");
			return;
		}
		_activity.startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), 1);
	}

	public void sendScore(JSONObject data)
	{
		if(!(mHelper.isSignedIn())){
			Debug.log("{gameplay-native} not signed in");
			return;
		}

		final Bundle params = new Bundle();
		String leaderBoardID = extractStringParameter(data, "leaderboard_id");
		Long score = extractDoubleParameter(data, "score").longValue();
		
		Games.Leaderboards.submitScore(getApiClient(), leaderBoardID, score);
		/*try {
			
			JSONObject ldrData = new JSONObject(param);
			Iterator<?> keys = ldrData.keys();

			while( keys.hasNext() ){
				String key = (String)keys.next();
				Object o = ldrData.get(key);
				if(key.equals("leaderBoardID")){
					leaderBoardID = (String) o;
					continue;
				}
				if(key.equals("score")){
					score =  Long.parseLong(o.toString());
					continue;
				}
				params.putString(key, (String) o);
			}
			
		} catch(JSONException e) {
			logger.log("{gameplay-native} Error in Params of sendScore because "+ e.getMessage());
		}*/
	}

	public void logError(String errorDesc) {
		Debug.error("{gameplay-native} logError "+ errorDesc);
	}

	@Override
	public void onInvitationReceived(Invitation arg0) {
		logError("test");

	}

	@Override
	public void onInvitationRemoved(String arg0) {
		logError("test");

	}

	/**
	 * Called when the user join a room. Display waiting room 
	 */
	@Override
	public void onJoinedRoom(int statusCode, Room room) 
	{
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			// let screen go to sleep
			_activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			// show error message, return to main screen.
			logError("failed onJoinedRoom:" + statusCode);
			EventQueue.pushEvent(new GPStateEvent(EVENT_ERROR, String.valueOf(statusCode)));
		}
		else
		{
			// get waiting room intent
			Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(getApiClient(), room, Integer.MAX_VALUE);
			_activity.startActivityForResult(i, RC_WAITING_ROOM);
		}
	}

	@Override
	public void onLeftRoom(int arg0, String arg1) {
		logError("test");

	}

	/**
	 * Called when room is fully connected.
	 */
	@Override
	public void onRoomConnected(int statusCode, Room room) 
	{
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			// let screen go to sleep
			_activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			// show error message, return to main screen.
			logError("failed onRoomConnected:" + statusCode);
			EventQueue.pushEvent(new GPStateEvent(EVENT_ERROR, String.valueOf(statusCode)));
		}
		else
		{
			updateRoom(room);
			
			EventQueue.pushEvent(new GPStateEvent(EVENT_ROOMCONNECTED,String.valueOf(room.getParticipants().size())));
		}
	}
	
	/**
	 * Update partecipants list
	 * @param room
	 */
	void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
    }

	@Override
	public void onRoomCreated(int statusCode, Room room)
	{
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			// let screen go to sleep
			_activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			// show error message, return to main screen.
			logError("failed onRoomCreated:" + statusCode);
			EventQueue.pushEvent(new GPStateEvent(EVENT_ERROR, String.valueOf(statusCode)));
		}
		else
		{
			// get waiting room intent
			Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(getApiClient(), room, Integer.MAX_VALUE);
			_activity.startActivityForResult(i, RC_WAITING_ROOM);
		}
	}

	// Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
	// is connected yet).
	@Override
	public void onConnectedToRoom(Room room)
	{
		// get room ID, participants and my ID:
		mRoomId = room.getRoomId();
		mParticipants = room.getParticipants();
		mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(getApiClient()));
	}

	@Override
	public void onDisconnectedFromRoom(Room arg0) {
		logError("test");

	}
	
	// Leave the room.
    void leaveRoom() {
    	logError("Leaving room.");
    	mPlaying = false;

    	if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
            mRoomId = null;
        } 
    }

	@Override
	public void onP2PDisconnected(String arg0) {
		logError("test");

	}
	
	@Override
	public void onP2PConnected(String arg0) {
		logError("test");

	}

	@Override
	public void onPeerInvitedToRoom(Room room, List<String> arg1) {
		updateRoom(room);

	}

	@Override
	public void onPeerJoined(Room room, List<String> arg1) {
		updateRoom(room);

	}

	@Override
	public void onRoomAutoMatching(Room room) {
		updateRoom(room);

	}

	@Override
	public void onRoomConnecting(Room room) {
		updateRoom(room);

	}

	// returns whether there are enough players to start the game
	boolean shouldStartGame(Room room) {
		int connectedPlayers = 0;
		for (Participant p : room.getParticipants()) {
			if (p.isConnectedToRoom()) ++connectedPlayers;
		}
		return connectedPlayers >= MIN_PLAYERS;
	}

	// Returns whether the room is in a state where the game should be canceled.
	boolean shouldCancelGame(Room room) {
		// TODO: Your game-specific cancellation logic here. For example, you might decide to
		// cancel the game if enough people have declined the invitation or left the room.
		// You can check a participant's status with Participant.getStatus().
		// (Also, your UI should have a Cancel button that cancels the game too)
		return true;
		
		/*
		if(room.getParticipants().size() > 1)
			return false;
		else
			return true;
		*/
	}

	@Override
	public void onPeersConnected(Room room, List<String> peers) {
		updateRoom(room);
		
		if (mPlaying) {
			// add new player to an ongoing game
		}
		else if (shouldStartGame(room)) {
			// start game!
		}
	}

	@Override
	public void onPeersDisconnected(Room room, List<String> peers) {
		updateRoom(room);
		
		if (mPlaying) {
			// do game-specific handling of this -- remove player's avatar
			// from the screen, etc. If not enough players are left for
			// the game to go on, end the game and leave the room.
		}
		
		if (shouldCancelGame(room)) {
			// cancel the game
			Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
			_activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			//send event to GC
			EventQueue.pushEvent(new GPStateEvent(EVENT_PLAYERLEAVE,null));
		}
	}

	@Override
	public void onPeerLeft(Room room, List<String> peers) {
		// peer left -- see if game should be canceled
		if (!mPlaying && shouldCancelGame(room)) {
			Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
			_activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		updateRoom(room);
	}

	/**
	 * We treat most of the room update callbacks in the same way: we update our list of
     * participants and update the display. In a real game we would also have to check if that
     * change requires some action like removing the corresponding player avatar from the screen,
     * etc.
	 */
	@Override
	public void onPeerDeclined(Room room, List<String> peers) {
		// peer declined invitation -- see if game should be canceled
		if (!mPlaying && shouldCancelGame(room)) {
			Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
			_activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		updateRoom(room);
	}
	
	/**
	 * Call this method to show a player picker UI that prompts the initiating player to select friends
	 * to invite to a real-time game session or select a number of random players for auto-matching.
	 * @param dummyParam not used but due
	 */
	public void invitePlayers(String dummyParam) {
		// launch the player selection screen minimum: 1 other player; maximum: 3 other players
		Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(getApiClient(), MIN_PLAYERS-1, MAX_PLAYERS-1);
		_activity.startActivityForResult(intent, RC_SELECT_PLAYERS);
    }
	
	/**
	 * Send reliable message to all participant except the player itself
	 * @param dummyParam
	 * @param message
	 */
	public void sendReliableMessage(String message){
		
		try {
			for (Participant p : mParticipants) {
				if (!p.getParticipantId().equals(mMyId)) {

					Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(), null, message.getBytes("UTF-8"),
							mRoomId, p.getParticipantId());
				}
			}
		} 
		catch (UnsupportedEncodingException e) {
			logError(e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param message
	 */
	public void sendMap(String message){

        // Add a prefix to indicate that this is the map definition
        sendReliableMessage(MSG_MAP + message);
	}
	
	/**
	 * Called when we receive a real-time message from the network.
	 * Only forward to GC bus
	 */
	@Override
	public void onRealTimeMessageReceived(RealTimeMessage rtm) {
	    // get real-time message
	    byte[] b = rtm.getMessageData();
	    
	    try {
			String msg = new String(b, "UTF-8");
			
			//topic of the message, fixed length
			String topic = msg.substring(0,5);
			//message
			String payload =  msg.substring(5);
			
			// map received
			if(topic.equals(MSG_MAP))
			{
				EventQueue.pushEvent(new GPStateEvent(EVENT_MAPRECEIVED,payload));
			}
			//ack received
			else if(topic.equals(MSG_ACK))
			{
				//add to list if it's not already present
				if(!mLstAcks.contains(payload))
					mLstAcks.add(payload);
				
				//if we have received all acks we can start the game
				if(mLstAcks.size() == mParticipants.size() -1)
				{
					mLstAcks.clear();
					EventQueue.pushEvent(new GPStateEvent(EVENT_ALLACKRECEIVED,null));
				}
			}
			//start game received
			else if(topic.equals(MSG_STARTGAME))
			{
				mPlaying = true;
				EventQueue.pushEvent(new GPStateEvent(EVENT_STARTGAME,payload));
			}
			else if(topic.equals(MSG_MOVE))
			{
				EventQueue.pushEvent(new GPStateEvent(EVENT_MOVEDONE,payload));
			}
			else
				EventQueue.pushEvent(new GPStateEvent(EVENT_MESSAGE,msg));
		} 
	    catch (UnsupportedEncodingException e) {
			logError(e.getMessage());
		}
	}
	
	/**
	 * Based on highest partecipantID defines who is the master for the game. This can be useful when you need to defines some
	 * particular logic in the game that needs a unique source of data (ie player order, map definition, etc)
	 * 
	 * Note: The player ID is not the same as the participant ID. 
	 * A player ID is a permanent identifier for a particular user and is consistent from game to game.
	 * A participant ID is a temporary identifier that is valid only for a particular room. 
	 * Invitees from a player's circles have both a player ID and a participant ID. 
	 * However, to preserve anonymity, players who joined the room via auto-match will only have a participant ID (and not a player ID).
	 * @param dummyParam
	 */
	public void isMaster(String dummyParam)
	{
		Participant master = null;
		for (Participant p : mParticipants) {
			if(master == null)
				master = p;
			else
			{
				if(p.getParticipantId().compareTo(master.getParticipantId())>0)
					master = p;
			}
		}
		
	    if (master.getParticipantId().equals(mMyId)) 
	    	EventQueue.pushEvent(new GPStateEvent(EVENT_ISMASTER,String.valueOf(true)));
	    else
	    	EventQueue.pushEvent(new GPStateEvent(EVENT_ISMASTER,String.valueOf(false)));
	}
	
	/** 
	 * Sends a generic ack message
	 */
	public void sendAck(String dummyParam)
	{
		sendReliableMessage(MSG_ACK + mMyId);
	}
	
	/**
	 * Sends a message to start the game. this should be used when all clients are sync and ready to start.
	 * Sends also the player number to each participant
	 */
	public void sendStartGame(String dummyParam)
	{
		mPlaying = true;
		
		try{
			int index = 2; //master is always the player 1
			for (Participant p : mParticipants) {
				if (!p.getParticipantId().equals(mMyId)) 
				{
					String msg = MSG_STARTGAME + index;
					Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(), null, msg.getBytes("UTF-8"),
							mRoomId, p.getParticipantId());
					index++;
				}
			}
		}
		catch (UnsupportedEncodingException e) {
			logError(e.getMessage());
		}
	}
	
	/**
	 * This method must be called when the player exits the game
	 */
	public void leaveGame(String dummyParam)
	{
        leaveRoom();
	}
	
	/**
	 * This method should be used when a move is done to send move detail to other opponents
	 * @param data
	 */
	public void sendMove(String data)
	{
		sendReliableMessage(MSG_MOVE + data);
	}
	
	public void openGooglePlayAppPage(JSONObject data){
		
		Uri uri = Uri.parse("market://details?id=" + _context.getPackageName());
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
		  _activity.startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			_activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + _context.getPackageName())));
		}
	}
}
