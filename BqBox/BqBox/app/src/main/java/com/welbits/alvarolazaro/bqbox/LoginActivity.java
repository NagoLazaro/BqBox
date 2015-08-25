package com.welbits.alvarolazaro.bqbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AppKeyPair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "DBRoulette";

    ///////////////////////////////////////////////////////////////////////////
    //                      Your app-specific settings.                      //
    ///////////////////////////////////////////////////////////////////////////

    // Replace this with your app key and secret assigned by Dropbox.
    // Note that this is a really insecure way to do this, and you shouldn't
    // ship code which contains your key & secret in such an obvious way.
    // Obfuscation is good.
    private static final String APP_KEY = "82869ggkejw2li1";
    private static final String APP_SECRET = "9i4ly1wiknw703v";

    ///////////////////////////////////////////////////////////////////////////
    //                      End app-specific settings.                       //
    ///////////////////////////////////////////////////////////////////////////

    // You don't need to change these, leave them alone.
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    // Item comparators
    private static final Comparator<DropboxAPI.Entry> SORT_BY_NAME_COMPARATOR = new Comparator<DropboxAPI.Entry>() {
        @Override
        public int compare(DropboxAPI.Entry lhs, DropboxAPI.Entry rhs) {
            return lhs.fileName().toLowerCase().compareTo(rhs.fileName().toLowerCase());
        }
    };
    private static final Comparator<DropboxAPI.Entry> SORT_BY_DATE_COMPARATOR = new Comparator<DropboxAPI.Entry>() {
        final SimpleDateFormat dropboxDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.getDefault());

        private Date parse(String dateString) {
            try {
                return dropboxDateFormat.parse(dateString);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int compare(DropboxAPI.Entry lhs, DropboxAPI.Entry rhs) {
            return parse(lhs.clientMtime).compareTo(parse(rhs.clientMtime));
        }
    };

    // Android widgets
    @Bind(R.id.list)
    RecyclerView list;
    @Bind(R.id.auth_button)
    Button authButton;
    @Bind(R.id.change_list_mode)
    Button changeListModeButton;
    @Bind(R.id.change_sort_mode)
    Button changeSortModeButton;

    private DropboxAPI<AndroidAuthSession> mApi;
    private boolean mLoggedIn, modeList = true, sortByName = true;
    private ListAdapter adapter;

    private static String format(DropboxAPI.Entry entry) {
        return "Filename: " + entry.fileName() +
                "\n\nModifiedDate: " + entry.modified +
                "\n\nCreationDate: " + entry.clientMtime +
                "\n\nPath: " + entry.path +
                "\n\nReadOnly: " + entry.readOnly +
                "\n\nSize: " + entry.size;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<>(session);

        // Basic Android widgets
        setContentView(R.layout.main);
        ButterKnife.bind(this);

        checkAppKeySetup();

        // RecyclerView
        adapter = new ListAdapter(new OnItemClicked() {
            @Override
            public void onItemClicked(DropboxAPI.Entry entry, int position) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Epub details")
                        .setMessage(format(entry))
                        .show();
            }
        });
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        // Display the proper UI state if logged in or not
        setLoggedIn(mApi.getSession().isLinked());
    }

    @OnClick(R.id.change_list_mode)
    void changeListMode(Button button) {
        modeList = !modeList;
        list.setLayoutManager(modeList ? new LinearLayoutManager(this) : new GridLayoutManager(this, 2));
        button.setText(modeList ? "Grid" : "List");
    }

    @OnClick(R.id.change_sort_mode)
    void changeSortMode(Button button) {
        sortByName = !sortByName;
        adapter.sort(sortByName ? SORT_BY_NAME_COMPARATOR : SORT_BY_DATE_COMPARATOR);
        button.setText(sortByName ? "Sort by date" : "Sort by name");
    }

    @OnClick(R.id.auth_button)
    void onAuthButtonClicked() {
        // This logs you out if you're logged in, or vice versa
        if (mLoggedIn) {
            logOut();
        } else {
            // Start the remote authentication
            mApi.getSession().startOAuth2Authentication(LoginActivity.this);
        }
    }

    private void loadData() {
        new ListFiles(mApi, "/", new OnLoadCompleted() {
            @Override
            public void onLoadCompleted(List<DropboxAPI.Entry> items) {
                adapter.addAll(items);
                adapter.sort(SORT_BY_NAME_COMPARATOR);
            }
        }).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = mApi.getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                storeAuth(session);
                setLoggedIn(true);
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i(TAG, "Error authenticating", e);
            }
        }
    }

    private void logOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        setLoggedIn(false);
    }

    /**
     * Convenience function to change UI state based on being logged in
     */
    private void setLoggedIn(boolean loggedIn) {
        mLoggedIn = loggedIn;
        if (loggedIn) {
            authButton.setText("Unlink from Dropbox");
            changeListModeButton.setVisibility(View.VISIBLE);
            changeSortModeButton.setVisibility(View.VISIBLE);
            loadData();
        } else {
            authButton.setText("Link with Dropbox");
            changeListModeButton.setVisibility(View.GONE);
            changeSortModeButton.setVisibility(View.GONE);
            adapter.clear();
        }
    }

    private void checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
            finish();
            return;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            finish();
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        }
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.apply();
        }
    }

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.apply();
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }
}

