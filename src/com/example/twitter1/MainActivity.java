package com.example.twitter1;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	TextView timelineTV;
	
	static final String CONSUMER_KEY = "from site";
	static final String CONSUMER_SECRET = "from site";

	static final String CALLBACK_URL = "from site";

	static final String AUTHORIZATION_URL = "https://api.twitter.com/oauth/authorize";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	SharedPreferences mSharedPreferences;

	private static Twitter twitter;
	private static RequestToken requestToken;
	static final String PREF_KEY_OAUTH_TOKEN = "OAUTH_TOKEN";
	static final String PREF_KEY_OAUTH_SECRET = "OAUTH_SECRET";
	static final String PREF_KEY_TWITTER_LOGIN = "TWITTER_LOGIN";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		timelineTV = (TextView)findViewById(R.id.textViewAllTweets);

		Intent i = getIntent();
		Uri u = i.getData();
		mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
		new validateLogin().execute(u);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void loginClick(View v) {
		Log.d("TWEET", "Login Click");
		new doLoginTask().execute();
	}

	public void updateClick(View v) {
		Log.d("TWEET", "Update Click");
		EditText ed = (EditText) findViewById(R.id.updateTextEdit);
		String message = ed.getText().toString();
		if(!message.equalsIgnoreCase("")){
			new updateMessage().execute(message);
		}
		
	}

	public void gettweetsClick(View v) {
		Log.d("TWEET", "Get All Tweets Click");
		timelineTV.setText("");
		new timeLineGet().execute();
	}

	public void logoutClick(View v) {
		Log.d("TWEET", "Logout Click");
		logoutFromTwitter();
	}

	public class doLoginTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(CONSUMER_KEY);
			builder.setOAuthConsumerSecret(CONSUMER_SECRET);
			Configuration configuration = builder.build();

			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();

			try {
				requestToken = twitter.getOAuthRequestToken(CALLBACK_URL);
				MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse(requestToken.getAuthenticationURL())));
			} catch (TwitterException e) {
				Log.e("TWEET", "TwitterException", e);
				e.printStackTrace();
			}

			return null;
		}

	}

	public class validateLogin extends AsyncTask<Uri, String, Boolean> {
		@Override
		protected Boolean doInBackground(Uri... params) {
			if (params[0] != null) {
				Log.d("TWEET", "Uri is: " + params[0].toString());
				String verifier = params[0]
						.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

				try {

					AccessToken accessToken = twitter.getOAuthAccessToken(
							requestToken, verifier);

					// Shared Preferences
					Editor e = mSharedPreferences.edit();

					// After getting access token, access token secret
					// store them in application preferences
					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					e.putString(PREF_KEY_OAUTH_SECRET,
							accessToken.getTokenSecret());
					// Store login status - true
					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
					e.commit(); // save changes

					return true;
				} catch (Exception e) {
					Log.e("TWEET", "ExceptionOccured", e);
					return false;
				} finally {
					Log.d("TWEET", "Thank god this is over");
				}

			} else {
				Log.d("TWEET", "Uri is NULL ");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result == null) {
				Toast.makeText(MainActivity.this, "No Uri recieved",
						Toast.LENGTH_LONG).show();
				Button b = (Button) findViewById(R.id.login_button);
				b.setVisibility(Button.VISIBLE);

			} else {
				if (result == true) {
					Toast.makeText(MainActivity.this,
							"Stored OAuth Values successfully.",
							Toast.LENGTH_LONG).show();
					Button b = (Button) findViewById(R.id.login_button);
					b.setVisibility(Button.GONE);
				}
				if (result == false) {
					Toast.makeText(MainActivity.this,
							"Stored OAuth Values Failed.", Toast.LENGTH_LONG)
							.show();
					Button b = (Button) findViewById(R.id.login_button);
					b.setVisibility(Button.VISIBLE);
				}
			}
			super.onPostExecute(result);
		}
	}
	
	public class updateMessage extends AsyncTask<String, Void, Boolean>{
		@Override
		protected Boolean doInBackground(String... params) {
			Log.d("Tweet Text", "> " + params[0]);
			String status = params[0];
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(CONSUMER_KEY);
				builder.setOAuthConsumerSecret(CONSUMER_SECRET);

				// Access Token
				String access_token = mSharedPreferences.getString(
						PREF_KEY_OAUTH_TOKEN, "");
				// Access Token Secret
				String access_token_secret = mSharedPreferences.getString(
						PREF_KEY_OAUTH_SECRET, "");

				AccessToken accessToken = new AccessToken(access_token,
						access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build())
						.getInstance(accessToken);

				// Update status
				twitter4j.Status response = twitter.updateStatus(status);

				Log.d("Status", "> " + response.getText());
				return true;
			} catch (TwitterException e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
				return false;
			}

		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result == true){
				Toast.makeText(MainActivity.this, "Your update is posted successfully", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(MainActivity.this, "could not post your update", Toast.LENGTH_LONG).show();
			}
			
			super.onPostExecute(result);
		}
		
	}
	
	public class timeLineGet extends AsyncTask<Void, Void, StringBuilder>{
		StringBuilder sbr = new StringBuilder();
		@Override
		protected StringBuilder doInBackground(Void... params) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(CONSUMER_KEY);
			builder.setOAuthConsumerSecret(CONSUMER_SECRET);

			// Access Token
			String access_token = mSharedPreferences.getString(
					PREF_KEY_OAUTH_TOKEN, "");
			// Access Token Secret
			String access_token_secret = mSharedPreferences.getString(
					PREF_KEY_OAUTH_SECRET, "");

			AccessToken accessToken = new AccessToken(access_token,
					access_token_secret);
			Twitter twitter = new TwitterFactory(builder.build())
					.getInstance(accessToken);
		    ResponseList<twitter4j.Status> statuses;
			try {
				statuses = twitter.getHomeTimeline();
				System.out.println("Showing home timeline.");
				for (twitter4j.Status status : statuses) {
					System.out.println(status.getUser().getName() + ":" +
							status.getText());
					
					sbr.append(status.getUser().getName() + ":" +
							status.getText()+"\n");
				}
				return sbr;
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//getHomeTimeline();
			return null;
		}
		
		@Override
		protected void onPostExecute(StringBuilder result) {
			//Toast.makeText(MainActivity.this, sbr.toString(), Toast.LENGTH_LONG).show();
			timelineTV.setText(sbr.toString());
			super.onPostExecute(result);
		}
	}
	
	private void logoutFromTwitter() {
		
		Editor e = mSharedPreferences.edit();
		e.remove(PREF_KEY_OAUTH_TOKEN);
		e.remove(PREF_KEY_OAUTH_SECRET);
		e.remove(PREF_KEY_TWITTER_LOGIN);
		e.commit();

	
	
	}


}
