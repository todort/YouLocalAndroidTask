package com.local.todor.youlocalandroid;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends Activity implements View.OnClickListener {

    ImageView imgViewLogo;
    EditText editTextUser;
    EditText editTextPass;
    Button btnLogin;
    TextView txtViewPass;
    Animation logoAnim,otherAnim,translateAnim,revTranslateAnim;
    Intent i;
    String pattern;
    Matcher matcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        imgViewLogo = (ImageView)findViewById(R.id.logo);
        editTextUser = (EditText)findViewById(R.id.txtUsername);
        editTextPass = (EditText)findViewById(R.id.txtPass);
        btnLogin = (Button)findViewById(R.id.loginBtn);
        txtViewPass = (TextView)findViewById(R.id.txtFrgtPass);

        logoAnim = AnimationUtils.loadAnimation(this,R.anim.logo_anim);
        otherAnim = AnimationUtils.loadAnimation(this,R.anim.other_anim);
        translateAnim = AnimationUtils.loadAnimation(this,R.anim.translate_anim);
        revTranslateAnim = AnimationUtils.loadAnimation(this,R.anim.rev_translate_anim);

        i = new Intent(this,UserProfile.class);

        txtViewPass.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        imgViewLogo.startAnimation(logoAnim);
        editTextUser.startAnimation(otherAnim);
        editTextPass.startAnimation(otherAnim);
        btnLogin.startAnimation(otherAnim);
        txtViewPass.startAnimation(otherAnim);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.loginBtn:

                if(editTextUser.getText().toString().isEmpty() || editTextPass.getText().toString().isEmpty()){
                    Toast.makeText(this,getResources().getString(R.string.emptyEditText),Toast.LENGTH_LONG).show();
                    break;
                }

                i.putExtra("user",editTextUser.getText().toString());
                i.putExtra("pass", editTextPass.getText().toString());
                pattern = "^[a-zA-Z0-9]{1,20}@[a-zA-Z0-9]{1,20}.[a-zA-Z]{2,3}$";
                Pattern p = Pattern.compile(pattern);
                matcher = p.matcher(editTextUser.getText().toString());
                if(!matcher.find()) {
                    Toast.makeText(this,getResources().getString(R.string.wrongEmail),Toast.LENGTH_LONG).show();
                    break;
                }

                if (isOnline()) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        int cx = btnLogin.getWidth() / 2;
                        int cy = btnLogin.getHeight() / 2;
                        float finalRadius = (float) Math.hypot(cx, cy);
                        Animator anim = ViewAnimationUtils.createCircularReveal(btnLogin, cx, cy, 0, finalRadius);
                        anim.start();
                        startActivity(i);
                    } else {
                        startActivity(i);
                    }
                }else{
                    String error = getResources().getString(R.string.noConnection);
                    Toast.makeText(this,error,Toast.LENGTH_LONG).show();
                    break;
                }
                break;

            case R.id.txtFrgtPass:
                if(txtViewPass.getText()==getResources().getString(R.string.frgtPass)) {
                    editTextPass.setVisibility(View.INVISIBLE);
                    btnLogin.setText(R.string.reset);
                    txtViewPass.setText(R.string.back_to);
                    btnLogin.startAnimation(translateAnim);
                }else{
                    btnLogin.setText(R.string.login);
                    txtViewPass.setText(R.string.frgtPass);
                    editTextPass.setVisibility(View.VISIBLE);
                    btnLogin.startAnimation(revTranslateAnim);
                }
                break;
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting() && netInfo.isAvailable()) {
            return true;
        } else
            return false;
    }
}
