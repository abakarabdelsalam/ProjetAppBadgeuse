package com.project.pointage.ui.login;

import android.Manifest;
import android.app.Activity;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.project.location.Work_Place;

import com.project.pointage.R;
import com.project.pointage.*;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    private Database database = new Database(LoginActivity.this);
    private Authentification authentification = new Authentification(LoginActivity.this);
    private String username = null;
    private String password = null;
    private boolean isCheckUser = false;
    private Message messenger = new Message();

    private Work_Place verif;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkPermission();

        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        usernameEditText.setText("");
        passwordEditText.setText("");


        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }

            }

        });

             loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {

                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);
                usernameEditText.setText("");
                passwordEditText.setText("");
                //Complete and destroy login activity once successful
                //finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();

                isCheckUser = authentification.checkUser(username,password,database);
               Log.i("debug","is User valid LogActivity: "+isCheckUser);
               if(isCheckUser){
                    loginViewModel.login(username,password);
                }
               else{
                   loadingProgressBar.setVisibility(View.INVISIBLE);
                   Log.i("debug","Login failed");

                   messenger.message(LoginActivity.this,"Login failed","Le mot de passe et/ou l'identifiant est incorrect",0);
               }
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {


        //SharedPReferences

       // String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
       // Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();

        Intent intent = null ;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int type = pref.getInt("fonction",-1);
        Log.i("debug","Valeur de la fonction: "+type);
        if(type == 1){
            intent = new Intent(this,Employer.class);
        }
        else if(type == 0){
            intent = new Intent(this,Employeur.class);
        }
        intent.putExtra("user",pref.getString("user",null));
        startActivity(intent);
    }


    private void showLoginFailed(@StringRes Integer errorString) {
       // Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
        messenger.message(LoginActivity.this,"Error",""+errorString,0);
    }

    @Override
    public void onBackPressed() {

        Toast.makeText(LoginActivity.this,"Hello",Toast.LENGTH_LONG).show();
        Log.i("debug","Quiter ");
        //super.onBackPressed();
    }




    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Ask permission to the user
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            verif = new Work_Place(this);
            // Faire ce qui est à faire quand on a accès à la localisation
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                verif = new Work_Place(this);
                // Faire ce qui est à faire quand on a accès à la localisation
            }
            else {
                Toast.makeText(LoginActivity.this, "Vous devez autoriser l'accès à la localisation", Toast.LENGTH_LONG).show();
            }
        }
    }
}