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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import fi.metropolia.audiostoryutility.Login.LoginRequest;
import fi.metropolia.audiostoryutility.Login.LoginResponse;
import fi.metropolia.audiostoryutility.R;
import fi.metropolia.audiostoryutility.interfaces.LoginApi;
import fi.metropolia.audiostoryutility.security.Encrypter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity{

    private static final String DEBUG_TAG = "LoginActivity";

    //preference Constants
    public static final String PREF_USERNAME = "username";
    public static final String PREF_PASSWORD = "password";
    public static final String PREF_ID = "collection_id";
    private static final String PREFS_NAME = "AudioStoryUtility";
    private static final String CHECKED = "checked";


    private static final int API_KEY_LENGTH = 128;
    public static final String API_KEY = "Key";

    public LoginApi service;
    public LoginRequest loginRequest;
    public Call<LoginResponse> originalLoginResponseCall;
    public Callback<LoginResponse> loginCallback;

    //Views
    private EditText et_user, et_pass, et_id;
    private CheckBox cb_remember_me;

    private Intent mainActivityIntent;

    public String user;
    public String pass;
    public String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mainActivityIntent = new Intent(this, NfcActivity.class);

        initViews();
        initRetrofit();

        loadPreferences();
    }

    private void initViews() {
        et_user = (EditText)findViewById(R.id.username);
        et_pass = (EditText)findViewById(R.id.password);
        et_id = (EditText)findViewById(R.id.collection_id);
        cb_remember_me = (CheckBox) findViewById(R.id.remember_checkBox);
    }
    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://resourcespace.tekniikanmuseo.fi/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(LoginApi.class);
        loginRequest = new LoginRequest();

        originalLoginResponseCall = service.getApiKey(loginRequest);

        loginCallback = new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {


                LoginResponse loginResponse = response.body();
                Log.d(DEBUG_TAG, "Response code: " + response.code());
                Log.d(DEBUG_TAG, "onResponse: " + loginResponse.getApi_key());

                int length = loginResponse.getApi_key().length();
                if(length == API_KEY_LENGTH){
                    Encrypter encrypter = new Encrypter();
                    mainActivityIntent.putExtra(API_KEY, loginResponse.getApi_key());
                    mainActivityIntent.putExtra(PREF_USERNAME, encrypter.encrypt(loginRequest.getUsername()));
                    mainActivityIntent.putExtra(PREF_PASSWORD, encrypter.encrypt(loginRequest.getPassword()));
                    mainActivityIntent.putExtra(PREF_ID, id);
                    startActivity(mainActivityIntent);
                }
                else{
                    Toast.makeText(getBaseContext(), "Wrong username or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.d(DEBUG_TAG, "onFailure: " + t.getMessage());
            }
        };
    }


    public void onLoginButtonTap(View v){
        user = et_user.getText().toString().trim();
        pass = et_pass.getText().toString().trim();
        id = et_id.getText().toString().trim();

        if (isFormValid()) {

            if(cb_remember_me.isChecked()){
                savePreferences(user, pass, id, true);
            }else {
                savePreferences(null, null, null, false);
            }

            if(isNetworkAvailable()) {
                Log.d(DEBUG_TAG, "Connected to internet");

                loginRequest.setUsername(user);
                loginRequest.setPassword(pass);

                Call<LoginResponse> newLoginResponseCall = originalLoginResponseCall.clone();
                newLoginResponseCall.enqueue(loginCallback);

            }else {
                Toast.makeText(getBaseContext(), "You are not connected to internet", Toast.LENGTH_SHORT).show();
            }

        }
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
