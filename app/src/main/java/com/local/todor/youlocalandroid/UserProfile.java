package com.local.todor.youlocalandroid;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class UserProfile extends Activity {

    String avatar, username, about;
    Animation anim;
    String user,pass,error;
    RelativeLayout relativeLayout;
    CircularImageView userAvatar;
    TextView txtViewUsername;
    TextView txtViewAboutMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user_profile);
        relativeLayout = (RelativeLayout)findViewById(R.id.relativeLayout);
        userAvatar = (CircularImageView)findViewById(R.id.user_avatar);
        txtViewUsername = (TextView)findViewById(R.id.txtViewUsername);
        txtViewAboutMe = (TextView)findViewById(R.id.txtAboutMe);
        anim = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        relativeLayout.startAnimation(anim);

        user = getIntent().getStringExtra("user");
        pass = getIntent().getStringExtra("pass");
        ServerConnection serverConnection = new ServerConnection(getApplicationContext(), user, pass);

            try {
                serverConnection.execute().get();
                YouLocalAndroid youLocalAndroid = (YouLocalAndroid)getApplicationContext();
                JSONObject jArray = youLocalAndroid.jsonObject;
                userAvatar.setImageBitmap(youLocalAndroid.userAvatar);
                txtViewUsername.setText(youLocalAndroid.username);
                txtViewAboutMe.setText(youLocalAndroid.about);
            } catch (InterruptedException e) {
                e.printStackTrace();
                error = getResources().getString(R.string.wrongUsrPass);
                Toast.makeText(this,error,Toast.LENGTH_LONG).show();
                this.finish();
                return;
            } catch (ExecutionException e) {
                e.printStackTrace();
                error = getResources().getString(R.string.wrongUsrPass);
                Toast.makeText(this,error,Toast.LENGTH_LONG).show();
                this.finish();
                return;
            }
            catch (Exception e) {
                e.printStackTrace();
                error = getResources().getString(R.string.wrongUsrPass);
                Toast.makeText(this,error,Toast.LENGTH_LONG).show();
                this.finish();
                return;
            }
    }
}

 class ServerConnection extends AsyncTask<String,Void,String> {

     Context mContext;
     String username,pass;
     HttpsURLConnection connection;


    public ServerConnection(Context mContext, String username, String pass) {
        this.mContext = mContext;
        this.username = username;
        this.pass = pass;
    }

     private ProgressDialog dialog;

     @Override
     protected void onPreExecute() {
         super.onPreExecute();
         try {
             dialog = new ProgressDialog(mContext);
             String message = mContext.getResources().getString(R.string.pleaseWait);
             dialog.setMessage(message);
             dialog.show();
             dialog.setCancelable(false);
             dialog.setCanceledOnTouchOutside(false);
         } catch(Exception e){
             // WindowManager$BadTokenException will be caught and the app would not display
             // the 'Force Close' message
         }
     }

    protected String doInBackground(String... urls) {
        final String LOG_TAG = "getJSON";
        InputStream is = null;
        String result = "";
        JSONObject jArray = null;

            try {
                URL url = new URL("https://www.youlocalapp.com/oauth2/2.0/signin");
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                String urlParameters = "email=" + username + "&password=" + pass;
                //String urlParameters = "email=androidtestuser@youloc.al&password=android";
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                connection.connect();
                is = connection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                } finally {
                    is.close();
                }

                result = sb.toString();
                jArray = new JSONObject(result);
            } catch (UnsupportedEncodingException e) {
                if (e != null)
                    Log.e(LOG_TAG, "Error converting result " + e.toString());
            } catch (JSONException e) {
                if (e != null)
                    Log.e(LOG_TAG, "Error parsing data " + e.toString());
            } catch (IOException e) {
                if (e != null)
                    Log.e(LOG_TAG, "Error in http connection " + e.toString());
            }

        YouLocalAndroid youLocalAndroid = (YouLocalAndroid) mContext;
        youLocalAndroid.jsonObject = jArray;

        try {
            youLocalAndroid.username = jArray.getString("fullname");
            youLocalAndroid.about = jArray.getString("aboutMe");
            URL imageUrl = new URL(jArray.getString("avatarOriginal"));
            connection = (HttpsURLConnection) imageUrl.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            youLocalAndroid.userAvatar = BitmapFactory.decodeStream(input);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onPostExecute(String error) {
        super.onPostExecute(error);
        dialog.dismiss();
    }
}
