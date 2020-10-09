package com.cynoteck.newproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    SignInButton signInButton;
    GoogleSignInClient mGoogleApiClient;
    SignInButton googlesignInButton;
    RelativeLayout rlFB,rlGoogle;
    public  int ss=0;
    private final int RC_SIGN_IN = 7;
    String user_email="",user_password="",user_name="";
    LoginButton fb_login_button;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
//        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main);
        printHashKey();
        rlGoogle=findViewById(R.id.rlGoogle);
        rlFB=findViewById(R.id.rlFB);
        rlGoogle.setOnClickListener(this);
        rlFB.setOnClickListener(this);
        fb_login_button=findViewById(R.id.fbLoginButton);
        googlesignInButton=findViewById(R.id.button_google);

//        signInButton.setSize(SignInButton.SIZE_STANDARD);

        google();

        facebook();

    }

    private void facebook() {
        boolean loggedOut = AccessToken.getCurrentAccessToken() == null;

        if (!loggedOut) {
            Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName());

            getUserProfile(AccessToken.getCurrentAccessToken());
        }

        AccessTokenTracker fbTracker = new AccessTokenTracker() {

            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                if (accessToken2 == null) {
                    Toast.makeText(getApplicationContext(),"User Logged Out.",Toast.LENGTH_LONG).show();
                }
            }

        };

        fbTracker.startTracking();
        fb_login_button.setReadPermissions(Arrays.asList("email", "public_profile"));
        callbackManager = CallbackManager.Factory.create();

        fb_login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {


                boolean loggedOut = AccessToken.getCurrentAccessToken() == null;

                if (!loggedOut) {

                    // Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName());
                    getUserProfile(AccessToken.getCurrentAccessToken());
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException exception) {

                Toast.makeText(MainActivity.this,"FacebookException "+exception.getMessage().toString()+" "+ exception.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                Log.e("errrrrfb",exception.getMessage());
            }

        });


    }

    private void google() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);

    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.e("facebook_complete_data", object.toString() + " \n" + "other data: " + response);
                        try {

                            String first_name = object.getString("first_name");
                            String last_name = object.getString("last_name");
                            String id=object.getString("id");

                            user_name = first_name+" "+last_name;

                            if(object.has("email")){
                                user_email = object.getString("email");
                            }
                            else
                            {
                                user_email=id;
                            }
//                            Constants.login_name=user_name;
//                            Constants.userEmail=user_email;
//                            Log.e("fab",user_email);
//                            Constants.logintype="Facebook";

                            intentActivity();
                            String user_PhotoUrl = "https://graph.facebook.com/" + id + "/picture?type=normal";

                            SharedPreferences sharedPreferences =MainActivity.this.getSharedPreferences("userImage", 0);
                            SharedPreferences.Editor login_editor = sharedPreferences.edit();
                            login_editor.putString("user_image",user_PhotoUrl);

                            login_editor.commit();

                            Log.e("imagefb",user_PhotoUrl);



//                            if (name_string != null) {
//                                if (user_PhotoUrl != null) {
//                                    login_type_string = "Facebook";
//                                    user_PhotoUrl = "https://graph.facebook.com/" + email_string + "/picture?type=large";
//                                    Log.d("fbimage", "" + user_PhotoUrl);
//                                    email_string = email_string.replace(" ", "%20");
//
//                                }
//                            }

                        } catch (Exception e) {

                            Toast.makeText(MainActivity.this,"getUserProfile "+e.getMessage().toString()+" "+ e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

                            Log.e("errrfb",e.getMessage());
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void printHashKey() {

        try {
            final PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                final String hashKey = new String(Base64.encode(md.digest(), 0));

                Log.d("AppLog", "key:" + hashKey + "=");//oDySciRR44nYFXbz5+Sff+UNAYc=
            }
        }
        catch (Exception e) {
            Log.d("error", "error:", e);
        }
    }

    private void updateUI(GoogleSignInAccount account) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressLint("ApplySharedPref")
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            Log.e("working","handleSignInResult");
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            assert account != null;
            user_email = account.getEmail();
            user_name = account.getDisplayName();
            String person_pic = String.valueOf(account.getPhotoUrl());
            Log.e("person_pic",person_pic.toString());
            SharedPreferences sharedPreferences =MainActivity.this.getSharedPreferences("userImage", 0);
            SharedPreferences.Editor MainActivity_editor = sharedPreferences.edit();
            if(person_pic.equals("")) {
                MainActivity_editor.putString("user_image", "");
            }
            else {
                MainActivity_editor.putString("user_image", person_pic.toString());
            }
            MainActivity_editor.commit();

            intentActivity();


        } catch (ApiException e) {

            Toast.makeText(MainActivity.this,"handleSignInResult "+e.getMessage().toString()+" "+ e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

            Log.e("exception",e.getMessage()+" "+e.getStackTrace()+" "+e.getCause());

        }
    }

    private void intentActivity() {

        Log.e("GoogleInfo",user_email+" "+user_name);
        Toast.makeText(this, ""+user_name, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,MoveOnMap.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.rlGoogle:
                ss = 1;
                googlesignIn();
                break;

            case R.id.rlFB:
                fb_login_button.performClick();
                break;
        }
    }

    private void googlesignIn() {
        Log.e("working","googlesignIn");

        Intent signInIntent = mGoogleApiClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
}
