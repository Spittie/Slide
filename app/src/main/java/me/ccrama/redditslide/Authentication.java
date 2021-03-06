package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Created by ccrama on 3/30/2015.
 */
public class Authentication {
    public static boolean isLoggedIn;
    public static RedditClient reddit;
    private static boolean isNetworkAvailable(Context ac) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ac.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean mod;

    public static String name;
    private static final String CLIENT_ID = "KI2Nl9A_ouG9Qw";
    public static SharedPreferences authentication;
    private static final String REDIRECT_URL = "http://www.ccrama.me";
    private Reddit a;

    public Authentication(Context context) {
        if(isNetworkAvailable(context)) {
            isLoggedIn = false;
            this.a = (Reddit) context;
            reddit = new RedditClient(UserAgent.of("android:me.ccrama.RedditSlide:v4.0 (by /u/ccrama)"));
            reddit.setLoggingMode(LoggingMode.ALWAYS);
                new VerifyCredentials(context).execute();

        }

    }
    public static class UpdateToken extends  AsyncTask<Void, Void, Void>{

        Context context;
        public UpdateToken(Context c){
            this.context = c;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.v("Slide", "REAUTH");
            if(isLoggedIn) {
                try {

                    final Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
                    Log.v("Slide", "REAUTH LOGGED IN");

                    OAuthHelper oAuthHelper = reddit.getOAuthHelper();

                    oAuthHelper.setRefreshToken(refresh);
                OAuthData finalData = null;
                    finalData = oAuthHelper.refreshToken(credentials);


                refresh = oAuthHelper.getRefreshToken();
                    reddit.authenticate(finalData);
                    if (reddit.isAuthenticated()) {
                        Authentication.name = reddit.me().getFullName();
                        mod = reddit.me().isMod();
                        Authentication.isLoggedIn = true;


                    }
                } catch (OAuthException e) {

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialogWrapper.Builder(context).setTitle("Uh oh, an error occured")
                                    .setMessage("Reddit could not be reached. Would you like to try again?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new UpdateToken(context).execute();
                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Reddit.forceRestart(context);
                                }
                            }).show();

                        }
                    });
                    e.printStackTrace();
                }

            } else {
                final Credentials fcreds = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
                OAuthData authData = null;
                if(reddit != null) {
                    try {

                        authData = reddit.getOAuthHelper().easyAuth(fcreds);
                        Authentication.name = "LOGGEDOUT";
                        mod = false;

                    } catch (OAuthException e) {
                        //TODO fail
                    }
                    reddit.authenticate(authData);
                }


            }
            return null;
        }
    }
    private static String refresh;

    public static ArrayList<String> modSubs;
    public class VerifyCredentials extends AsyncTask<String, Void, Void> {
        Context mContext;
        public VerifyCredentials(Context context){
            mContext = context;
        }



        @Override
        public void onPostExecute(Void voids){
            if(a.loader != null){

                            String[] strings = StartupStrings.startupStrings(mContext);
                          a.loader.loading.setText(strings[new Random().nextInt(strings.length)]);

                        }


                new SubredditStorage(mContext).execute(a);


        }

        @Override
        protected Void doInBackground(String... subs) {
            try {
                String token = authentication.getString("lasttoken", "");
                Log.v("Slide", "TOKEN IS " + token);
                if (!token.isEmpty()) {

                    final Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
                    OAuthHelper oAuthHelper = reddit.getOAuthHelper();
                    oAuthHelper.setRefreshToken(token);
                    try {
                        OAuthData finalData = oAuthHelper.refreshToken(credentials);

                         refresh = oAuthHelper.getRefreshToken();
                        reddit.authenticate(finalData);
                        if (reddit.isAuthenticated()) {
                            final String name = reddit.me().getFullName();
                            final Set<String> accounts =authentication.getStringSet("accounts", new HashSet<String>());
                            if(accounts.contains(name)){ //convert to new system
                                accounts.remove(name);
                                accounts.add(name + ":" + token);
                                Authentication.authentication.edit().putStringSet("accounts", accounts).commit(); //force commit

                            }
                            Authentication.name = name;
                            mod = reddit.me().isMod();

                            Authentication.isLoggedIn = true;


                        }
                    } catch (OAuthException e) {
                        e.printStackTrace();
                        Log.v("Slide", "RESTARTING CREDS");
                    }
                } else {
                    Log.v("Slide", "NOT LOGGED IN");

                    final Credentials fcreds = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
                    OAuthData authData = null;
                    try {
                        authData = reddit.getOAuthHelper().easyAuth(fcreds);
                        Authentication.name = "LOGGEDOUT";

                    } catch (OAuthException e) {
                      //TODO fail
                        e.printStackTrace();
                    }
                    reddit.authenticate(authData);


                }
                int inboxC;
                if (isLoggedIn)
                    inboxC = reddit.me().getInboxCount();
            } catch(Exception e){
              //TODO fail

            }
            return null;
        }

    }

}
