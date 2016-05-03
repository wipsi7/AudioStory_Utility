package fi.metropolia.audiostoryutility.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import fi.metropolia.audiostoryutility.NfcActivity;
import fi.metropolia.audiostoryutility.R;
import fi.metropolia.audiostoryutility.interfaces.AsyncResponse;
import fi.metropolia.audiostoryutility.server.ServerConnection;
import fi.metropolia.audiostoryutility.tasks.LoginTask;

public class LoginActivity extends AppCompatActivity{



    public static final String PREF_USERNAME = "username";
    public static final String PREF_PASSWORD = "password";
    public static final String PREF_ID = "collection_id";

    private static final String PREFS_NAME = "remember_prefs";
    private static final String CHECKED = "checked";

    private static final String DEBUG_TAG = "LoginActivity";
    private static final int API_KEY_LENGTH = 128;

    public static final String API_KEY = "Key";


    private EditText et_user, et_pass, et_id;
    private CheckBox cb_remember_me;
    private Button btn_login;
    private Intent mainActivityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        loadPreferences();

    }

    private void init() {

        mainActivityIntent = new Intent(this, NfcActivity.class);

        et_user = (EditText)findViewById(R.id.username);
        et_pass = (EditText)findViewById(R.id.password);
        et_id = (EditText)findViewById(R.id.collection_id);
        btn_login = (Button)findViewById(R.id.enter_button);
        cb_remember_me = (CheckBox) findViewById(R.id.remember_checkBox);


        // button click listener
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user = et_user.getText().toString();
                final String pass = et_pass.getText().toString();
                final String id = et_id.getText().toString();

                if (isFormValid()) {

                    if(cb_remember_me.isChecked()){
                        savePreferences(user, pass, id, true);
                    }else {
                        savePreferences(null, null, null, false);
                    }

                    if(isNetworkAvailable()) {
                        Log.d(DEBUG_TAG, "Connected to internet");
                        LoginTask loginTask = new LoginTask();
                        loginTask.setOnLoginResult(new AsyncResponse() {

                            @Override
                            public void onProcessFinish(ServerConnection result) {
                                int length = result.getApiKey().length();
                                if(length == API_KEY_LENGTH){
                                    mainActivityIntent.putExtra(API_KEY, result.getApiKey());
                                    mainActivityIntent.putExtra(PREF_USERNAME, user);
                                    mainActivityIntent.putExtra(PREF_PASSWORD, pass);
                                    mainActivityIntent.putExtra(PREF_ID, id);
                                    startActivity(mainActivityIntent);
                                }
                                else{
                                    Toast.makeText(getBaseContext(), "Wrong username or password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        loginTask.execute(user, pass);

                    }else {
                        Toast.makeText(getBaseContext(), "You are not connected to internet", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    private void savePreferences(String user, String pass, String id,  boolean remember_checkbox){
        getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                .edit()
                .putString(PREF_USERNAME, user)
                .putString(PREF_PASSWORD, pass)
                .putString(PREF_ID,id)
                .putBoolean(CHECKED, remember_checkbox)
                .commit();
    }


    private void loadPreferences() {
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        String username = pref.getString(PREF_USERNAME, null);
        String password = pref.getString(PREF_PASSWORD, null);
        String id = pref.getString(PREF_ID, null);
        boolean checked = pref.getBoolean(CHECKED, false);

        if(checked){
            et_user.setText(username);
            et_pass.setText(password);
            et_id.setText(id);
            cb_remember_me.setChecked(true);
        }
    }


    private boolean isFormValid(){
        boolean isUserEmpty = et_user.getText().toString().isEmpty();
        boolean isPassEmpty = et_pass.getText().toString().isEmpty();
        boolean isCollectionIdEmpty = et_id.getText().toString().isEmpty();
        if(isUserEmpty){
            et_user.setError(getString(R.string.emptyfiled_activity_login));
        }
        if(isPassEmpty){
            et_pass.setError(getString(R.string.emptyfiled_activity_login));
        }
        if(isCollectionIdEmpty){
            et_id.setError(getString(R.string.emptyfiled_activity_login));
        }

        return !(isPassEmpty || isUserEmpty || isCollectionIdEmpty);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
