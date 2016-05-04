package fi.metropolia.audiostoryutility;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import fi.metropolia.audiostoryutility.activities.LoginActivity;

public class NfcActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private Locale locale;
    private PendingIntent pendingIntent;
    private IntentFilter nDef, tech;
    private IntentFilter[] intentFilterArray;
    private String[][] techListArray;
    private NdefMessage ndefMessage;
    private NdefRecord ndefRecord;
    private Ndef ndef;
    private boolean tagWriteEnabled = false;


    private EditText et_artifact_name;
    private Button btnWrite;



    private String apiKey = null;
    private String userName = null;
    private String password = null;
    private String collectionID = null;


    private static final String NFC_TAG = "nfcTag";
    private static final String DEBUG_TAG = "NfcActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        initViews();
        init();
        createNdefMessage();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isNfcAvailable()){
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilterArray, techListArray);
        }else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcAdapter.disableForegroundDispatch(this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)) {

            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (tagWriteEnabled) {
                writeToTag(tagFromIntent);
                tagWriteEnabled = false;
            }
        }

    }

    private void initViews() {

        et_artifact_name = (EditText)findViewById(R.id.artifact_name);
        btnWrite = (Button)findViewById(R.id.write_button);

    }

    private void init() {

        getDataFromIntent();

        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        createNdefFilter();
        createTechFilter();

        intentFilterArray = new IntentFilter[]{nDef, tech};
        techListArray = new String[][]{new String[] { Ndef.class.getName()}};

        locale = getResources().getConfiguration().locale;


    }






    /** Acquires data from LoginActivity (API key, username, password, collection id) */
    private void getDataFromIntent(){

        Intent loginIntent = getIntent();
        apiKey = loginIntent.getStringExtra(LoginActivity.API_KEY);
        userName = loginIntent.getStringExtra(LoginActivity.PREF_USERNAME);
        password = loginIntent.getStringExtra(LoginActivity.PREF_PASSWORD);
        collectionID = loginIntent.getStringExtra(LoginActivity.PREF_ID);

        Log.d(DEBUG_TAG, "User: " + userName);
        Log.d(DEBUG_TAG, "pass: " + password);
        Log.d(DEBUG_TAG, "ID: " + collectionID);
    }

    private void createNdefMessage() {
        ndefRecord = createTextRecord(apiKey, locale);
        ndefMessage = new NdefMessage(ndefRecord);
    }

    private void createTechFilter() {
        tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            tech.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(NFC_TAG, "error: Malformed data type");
        }
    }

    private void createNdefFilter() {
        nDef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            nDef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(NFC_TAG, "error: Malformed data type");
        }
    }

    public NdefRecord createTextRecord(String payload, Locale locale ) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = Charset.forName("UTF-8");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = 0;
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
    }

    private boolean isNfcAvailable() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter != null && nfcAdapter.isEnabled()){
            return true;
        }else {
            Toast.makeText(this,"NFC not available", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    private void writeToTag(Tag tagFromIntent) {
        ndef = Ndef.get(tagFromIntent);
/*        Toast.makeText(this, "NDef max size: "+ ndef.getMaxSize(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Message max size: "+ ndefMessage.getByteArrayLength(), Toast.LENGTH_SHORT).show();*/
        if(ndefMessage.getByteArrayLength() <= ndef.getMaxSize()) {
            try {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(this, "Tag is not writable!", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                Toast.makeText(this, "Write complete!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(NFC_TAG, "Error: tag connection fail");
                e.getMessage();
                Toast.makeText(this, "Write failed!", Toast.LENGTH_SHORT).show();
            } catch (FormatException e) {
                Log.e(NFC_TAG, "Error: malformed NDEF message");
                e.getMessage();
                Toast.makeText(this, "Write failed!", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "Not enough space", Toast.LENGTH_SHORT).show();
        }
    }

    public void writeToTag(View view){
        if(!et_artifact_name.getText().toString().isEmpty()) {
            tagWriteEnabled = true;
            hideKeyboard();
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

