
package org.yamalab.android.AdkTwitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Tweet extends AccessoryController
implements OnClickListener
{
	static final String TAG = "Tweet";
	
	private String defoTEXT = null;
	private String mainTEXT = null;
	private Button mTweetButton;
	final EditText edit;
	TwitterController mTwitterController;
	private AdkTwitterActivity activity;
	
	Tweet(AdkTwitterActivity hostActivity, TwitterController tc) {
		super(hostActivity);
		Log.d(TAG,"Twitter");
		activity=hostActivity;
//        setContentView(R.layout.tweet);
		mTwitterController=tc;
        
        //ハッシュタグ追加！
        defoTEXT = " #testTwitter ";
        
        edit = (EditText) findViewById(R.id.edittext1);
        edit.setText(defoTEXT);
        edit.setMaxLines(3); 
        edit.setSelection(0);
        
        mTweetButton = (Button) findViewById(R.id.tweet_tweet);
        mTweetButton.setOnClickListener(this);

        
	}
	
	
	public void tweet() throws TwitterException {
		// 記録していた設定ファイルを読み込む。
		Log.d(TAG,"tweet");

		SharedPreferences pref = activity.getSharedPreferences("Twitter_setting",activity.MODE_PRIVATE);

		// 設定ファイルからoauth_tokenとoauth_token_secretを取得。
		String oauthToken  = pref.getString("oauth_token", "");
		String oauthTokenSecret = pref.getString("oauth_token_secret", "");

		ConfigurationBuilder confbuilder  = new ConfigurationBuilder(); 

		confbuilder.setOAuthAccessToken(oauthToken) 
		.setOAuthAccessTokenSecret(oauthTokenSecret) 
		.setOAuthConsumerKey(mTwitterController.CONSUMER_KEY) 
		.setOAuthConsumerSecret(mTwitterController.CONSUMER_SECRET); 

		Twitter twitter=null;
		try{
		 twitter= new TwitterFactory(confbuilder.build()).getInstance(); 
		}
		catch(Exception e){
			Log.d(TAG,"tweet "+e.toString());
			e.printStackTrace();
			
		}

		// 任意の文字列を引数にして、ツイート。
		if(twitter!=null)
		twitter.updateStatus(mainTEXT);
		
//		Toast.makeText(this, "つぶやきました", Toast.LENGTH_SHORT).show();
	}

	String xwork;
    public void tweet(String x){
    	xwork=x;
        ((Activity)mHostActivity).runOnUiThread(new Runnable() {
        	@Override
        	public void run(){
                edit.setText(xwork+" "+defoTEXT); 
                edit.setSelection(0);
        	}
        });
		new tweetTask().execute(x);
    }
	private class tweetTask extends AsyncTask<String, Integer, Long> {
	     protected synchronized Long doInBackground(String... params ) {
	    	Log.d(TAG, "doInBackground - " + params[0]);
	    	mTwitterController.setAccessingWeb(true);
        	SpannableStringBuilder sb = (SpannableStringBuilder)edit.getText();
            mainTEXT = sb.toString();
	    	try{
		   	  tweet();
	    	}
	    	catch(Exception e){
	    		Log.d(TAG,"tweetTask error:"+e.toString());
				e.printStackTrace();
	    	}
	    	mTwitterController.setAccessingWeb(false);	    	
		   	return 1L;	
		 }

	}
	
	@Override
	protected void onAccesssoryAttached() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
    public void onClick(View v) {
            // クリック時の処理
		if(v==mTweetButton){
        	SpannableStringBuilder sb = (SpannableStringBuilder)edit.getText();
            mainTEXT = sb.toString();                        
			try{
				tweet();
			}
			catch(Exception e){
	    		Log.d(TAG,"tweetTask error:"+e.toString());
				e.printStackTrace();				
			}
		}
    }

}

