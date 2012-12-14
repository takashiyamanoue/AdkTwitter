package org.yamalab.android.AdkTwitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TwitterController extends AccessoryController
implements OnClickListener  {
	static final String TAG = "TwitterController";
		
	public String CONSUMER_KEY = "*****************"; // dev.twitter.com で
	public String CONSUMER_SECRET = "*****************************"; // dev.twitter.com で
	public String CALLBACK_URL = "http://www.yama-lab.org" ; // "myapp://oauth";
	
	private String nechatterStatus;
	private AdkTwitterActivity activity;
	TwitterController mTwitterController;
	private Button loginbutton;
	private Button logoutbutton;
	public RequestToken requestToken = null;
	public Twitter twitter = null;
//	public OAuthAuthorization twitterOauth;
	private boolean accessingWeb=false;
    /** Called when the activity is first created. */
    
    TwitterController(AdkTwitterActivity hostActivity) {
        super(hostActivity);
		Log.d(TAG,"TwitterController");
        activity=hostActivity;
//        mTwitterController=activity.mTwitterController;
        //まぁ一応書いときます
	    SharedPreferences pref = activity.getSharedPreferences("Twitter_setting", activity.MODE_PRIVATE);
	    //これは必要だから消さないでね
		nechatterStatus  = pref.getString("status", "");

		loginbutton = (Button) findViewById(R.id.tweetlogin_button);
        loginbutton.setOnClickListener(this);
        logoutbutton = (Button) findViewById(R.id.logout_button);
        logoutbutton.setOnClickListener(this);
      
    }
    

    private void connectTwitter() throws TwitterException{
		//参考:http://groups.google.com/group/twitter4j/browse_thread/thread/d18c179ba0d85351
		//英語だけど読んでね！
    	setAccessingWeb(true);
		ConfigurationBuilder confbuilder  = new ConfigurationBuilder(); 

		confbuilder.setOAuthConsumerKey(CONSUMER_KEY).setOAuthConsumerSecret(CONSUMER_SECRET); 
		confbuilder.setGZIPEnabled(false);
        if(activity==null) {
        	Log.d(TAG,"connectTwitter activity==null");
        	return;
        }
//        Configuration configuration = confbuilder.build();
		twitter = new TwitterFactory(confbuilder.build()).getInstance();
//        twitterOauth = new OAuthAuthorization(configuration);

		// requestTokenもクラス変数。
		try {
			requestToken = twitter.getOAuthRequestToken(CALLBACK_URL);
//			requestToken = twitterOauth.getOAuthRequestToken(CALLBACK_URL);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"connectTwitter-twitter.getOAuthError "+e.toString());
			e.printStackTrace();
		}catch (Exception e){
			Log.d(TAG,"connectTwitter-twitter.getOAuthError "+e.toString());
			e.printStackTrace();
		}
        if(requestToken==null) {
        	Log.d(TAG,"connectTwitter requestToken==null");
//        	return;
        }

		  // 認証用URLをインテントにセット。
		  // TwitterLoginはActivityのクラス名。
//		  Intent intent = new Intent(this.activity, TwitterLoginController.class);
//		  intent.putExtra("auth_url", activity.requestToken.getAuthorizationURL());
/*
		  // アクティビティを起動
//		  activity.startActivityForResult(intent, 1);
		activity.mTwitterLoginController.loadUrl(requestToken.getAuthorizationURL());
//        activity.mTwitterLoginController.loadUrl("https://api.twitter.com/oauth/request_token");
//        activity.mTwitterLoginController.loadUrl("https://api.twitter.com/oauth/authorize");
//		activity.mTwitterLoginController.loadUrl("https://api.twitter.com/oauth/access_token");
//		activity.mTwitterLoginController.loadUrl("https://api.twitter.com/1/");
		activity.showTabContents(R.id.main_login_label);
		*/
	}

    
	private class connectTwitterTask extends AsyncTask<Void, Void, String> {
		 @Override
	     protected String doInBackground(Void... params ) {
	    	Log.d(TAG, "connectTwitterTask.doInBackground - " );
	    	try{
	    	   connectTwitter();
	    	}
	    	catch(Exception e){
	    		setAccessingWeb(false);
	    		Log.d(TAG,"tweetTask error:"+e.toString());
				e.printStackTrace();
				return "";
			}
    		setAccessingWeb(false);
    		String rtn=requestToken.getAuthorizationURL();
	    	return rtn;
	     }
	     @Override
	     protected void onPostExecute(String result) {
	         super.onPostExecute(result);
	         if (result != null) {
		       activity.showTabContents(R.id.main_login_label);
	     		activity.mTwitterLoginController.loadUrl(result);
	         } else {
	                Log.d(TAG,"");
	         }
	    }
	}
	
    final private boolean isConnected(String nechatterStatus){
		if(nechatterStatus != null && nechatterStatus.equals("available")){
			return true;
		}else{
			return false;
		}
	}
    
    
    public void disconnectTwitter(){
		
        SharedPreferences pref=activity.getSharedPreferences("Twitter_setting",activity.MODE_PRIVATE);

        SharedPreferences.Editor editor=pref.edit();
        editor.remove("oauth_token");
        editor.remove("oauth_token_secret");
        editor.remove("status");

        editor.commit();
        
        //finish();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		//もう設定されているか？
	  if(v==loginbutton){
		if(isConnected(nechatterStatus)){
			
			//disconnectTwitter();
			/*
			Intent intent2=new Intent();
			intent2.setClassName("org.yamalab.android.AdkTwitter","org.yamalab.android.AdkTwitter.Tweet");
			intent2.setAction(Intent.ACTION_VIEW);
			activity.startActivityForResult(intent2,0);
			*/
			this.activity.showTabContents(R.id.main_tweet_label);
		}else{
			
			try {
//				connectTwitter();
				new connectTwitterTask().execute();
			} catch (Exception e) {
				//showToast(R.string.nechatter_connect_error);
			}
		}
	  }
	  else
	  if(v==logoutbutton){
  		disconnectTwitter();
		try {
//			connectTwitter();
			new connectTwitterTask().execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
		
	}		

	@Override
	protected void onAccesssoryAttached() {
		// TODO Auto-generated method stub
		
	}

	protected boolean parseCommand(String x, StringMsg m){
		String subcmd=Util.skipSpace(x);
		String [] rest=new String[1];
		String [] match=new String[1];
		Log.d(TAG,"parseCommand("+x+","+m.getValue()+")");
		int [] intv = new int[1];
		String tweetMessage="";
		if(m!=null)
		   tweetMessage=m.getValue();
		if(Util.parseKeyWord(subcmd,"tweet",rest)){
		   	String subsub=Util.skipSpace(rest[0]);
		   	if(this.activity==null) return false;
		   	if(this.activity.mTweet==null) return false;
		    SharedPreferences pref = activity.getSharedPreferences("Twitter_setting", activity.MODE_PRIVATE);
			nechatterStatus  = pref.getString("status", "");
			if(accessingWeb) return true;
			accessingWeb=true;
		   	if(isConnected(nechatterStatus)){
		   		if(tweetMessage==null) return false;
		   	    activity.mTweet.tweet(tweetMessage);
		   	}
		   	else{
		   		/* */
//					new connectTwitterTask().execute("");
		   		/* */
		   	}
		   	return true;
		}

		return false;
	}
	public void setAccessingWeb(boolean x){
		accessingWeb=x;
	}

}
