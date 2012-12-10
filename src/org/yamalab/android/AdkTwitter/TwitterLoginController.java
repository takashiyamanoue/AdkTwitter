
package org.yamalab.android.AdkTwitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class TwitterLoginController extends AccessoryController {
	static final String TAG = "TwitterLoginController";

    AdkTwitterActivity activity;
    WebView webView;
    TwitterController mTwitterController;
    Twitter mTwitter;
    RequestToken requestToken;
	TwitterLoginController(AdkTwitterActivity hostActivity, TwitterController tc) {
        super(hostActivity);
		Log.d(TAG,"TwitterLoginController");
        activity=hostActivity;
        mTwitterController=tc;
//        activity.setContentView(R.layout.tweetlogin);
		webView = (WebView)findViewById(R.id.twitterlogin);
    }
    
	public void loadUrl(String x){
		WebSettings webSettings = webView.getSettings();
		//これで別のユーザーとしてサインインする。が実行できる
		webSettings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient(){

			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

				String CALLBACK_URL = "http://yama-linux.cc.kagoshima-u.ac.jp";
//				String CALLBACK_URL = "";
				if(url != null && url.startsWith(CALLBACK_URL )){
					String[] urlParameters = url.split("\\?")[1].split("&");

					String oauthToken = "";
					String oauthVerifier = "";

					if(urlParameters[0].startsWith("oauth_token")){
						oauthToken = urlParameters[0].split("=")[1];
					}else if(urlParameters[1].startsWith("oauth_token")){
						oauthToken = urlParameters[1].split("=")[1];
					}

					if(urlParameters[0].startsWith("oauth_verifier")){
						oauthVerifier = urlParameters[0].split("=")[1];
					}else if(urlParameters[1].startsWith("oauth_verifier")){
						oauthVerifier = urlParameters[1].split("=")[1];
					}

					/*
					Intent intent = getIntent();
					intent.putExtra("oauth_token", oauthToken);
					intent.putExtra("oauth_verifier", oauthVerifier);

					setResult(Activity.RESULT_OK, intent);
					*/
					setOAuth(oauthToken, oauthVerifier);
//					finish();
				}
			}
		});


//		webView.loadUrl(activity.requestToken.getAuthorizationURL());
		
		webView.loadUrl(x);
		webView.setFocusable(true);
	}
	
	@Override
	protected void onAccesssoryAttached() {
		// TODO Auto-generated method stub
		
	}
	/* */
    protected void setOAuth(String oauthToken, String oauthVerifier) {
		Log.d(TAG,"setOAuth token="+oauthToken+" verifier="+oauthVerifier);

        mTwitter=mTwitterController.twitter;
        requestToken=mTwitterController.requestToken;
			AccessToken accessToken = null;
	        if(mTwitter==null) {
	        	Log.d(TAG,"setOAuth activity.twitter==null");
	        	return;
	        }

			try {
				accessToken = mTwitter.getOAuthAccessToken(
						requestToken, oauthVerifier);

		        SharedPreferences pref=activity.getSharedPreferences(
		        		  "Twitter_setting",activity.MODE_PRIVATE);

		        SharedPreferences.Editor editor=pref.edit();
		        editor.putString("oauth_token",accessToken.getToken());
		        editor.putString("oauth_token_secret",accessToken.getTokenSecret());
		        editor.putString("status","available");

		        editor.commit();
		        
		        //つぶやくページへGO
//		        Intent intent2 = new Intent(this, Tweet.class);
//				startActivityForResult(intent2, 0);
		        activity.showTabContents(R.id.main_tweet_label);
		        //finish();
			} catch (TwitterException e) {
				//showToast(R.string.nechatter_connect_error);
			}
    }
}

