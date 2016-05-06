package fi.metropolia.audiostoryutility.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import fi.metropolia.audiostoryutility.R;
import fi.metropolia.audiostoryutility.nfc.NfcController;

public class NfcActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "NfcActivity";
    public static final String ARTIFACT_NAME ="artifactName";

    private NfcController nfcController;
    private PendingIntent pendingIntent;

    private EditText et_artifact_name;
    private Button btnWrite;

    private String apiKey, userName, password, collectionID = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        initViews();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(nfcController.isNfcAvailable()){
            nfcController.getNfcAdapter().enableForegroundDispatch(this, pendingIntent, nfcController.getIntentFilterArray(), nfcController.getTechListArray());
        }else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcController.getNfcAdapter().disableForegroundDispatch(this);
    }

    private void initViews() {

        et_artifact_name = (EditText)findViewById(R.id.artifact_name);
        btnWrite = (Button)findViewById(R.id.write_button);

    }

    private void init() {

        getDataFromIntent();
        nfcController = new NfcController(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
    }

    /** Acquires data from LoginActivity (API key, username, password, collection id) */
    private void getDataFromIntent(){

        Intent loginIntent = getIntent();
        apiKey = loginIntent.getStringExtra(LoginActivity.API_KEY);
        userName = loginIntent.getStringExtra(LoginActivity.PREF_USERNAME);
        password = loginIntent.getStringExtra(LoginActivity.PREF_PASSWORD);
        collectionID = loginIntent.getStringExtra(LoginActivity.PREF_ID);
    }

    /** called on "write to tag" button click*/
    public void onTagButtonClick(View view){
        String artifactName = et_artifact_name.getText().toString().trim();
        if(!artifactName.isEmpty()) {
            
            hideKeyboard();

            Intent nfcWriteIntent = new Intent(this, NfcWriteActivity.class);

            nfcWriteIntent.putExtra(LoginActivity.PREF_USERNAME, userName);
            nfcWriteIntent.putExtra(LoginActivity.PREF_PASSWORD, password);
            nfcWriteIntent.putExtra(LoginActivity.PREF_ID, collectionID);
            nfcWriteIntent.putExtra(ARTIFACT_NAME, artifactName.replaceAll("\\s", "_"));
            startActivity(nfcWriteIntent);
        }
        else {
            et_artifact_name.requestFocus();
            et_artifact_name.setError(getString(R.string.err_name_first_activity_nfc));
        }
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}