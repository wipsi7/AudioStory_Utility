package fi.metropolia.audiostoryutility.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import fi.metropolia.audiostoryutility.R;
import fi.metropolia.audiostoryutility.nfc.NfcController;

public class NfcWriteActivity extends AppCompatActivity {


    private final String DEBUG_TAG = "NfcWriteActivity";

    private NfcController nfcController;
    private PendingIntent pendingIntent;


    private String user, pass, collId, artifactName;

    private NdefRecord[] ndefRecords;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nfc_write);

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

    private void init() {

        nfcController = new NfcController(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        getDataFromIntent();
        ndefRecords = createNDefRecordArray();
    }

    private NdefRecord[] createNDefRecordArray() {
        NdefRecord[] ndefRecordsTemp = new NdefRecord[4];
        ndefRecordsTemp[0] = nfcController.createNdefTextRecord(user);
        ndefRecordsTemp[1] = nfcController.createNdefTextRecord(pass);
        ndefRecordsTemp[2] = nfcController.createNdefTextRecord(collId);
        ndefRecordsTemp[3] = nfcController.createNdefTextRecord(artifactName);

        return ndefRecordsTemp;
    }

    /** Acquires data from LoginActivity (API key, username, password, collection id) */
    private void getDataFromIntent(){

        Intent NfcActivityIntent = getIntent();

        user = NfcActivityIntent.getStringExtra(LoginActivity.PREF_USERNAME);
        pass = NfcActivityIntent.getStringExtra(LoginActivity.PREF_PASSWORD);
        collId = NfcActivityIntent.getStringExtra(LoginActivity.PREF_ID);
        artifactName = NfcActivityIntent.getStringExtra(NfcActivity.ARTIFACT_NAME);

    }

    public void onCancelClick(View v){
       finish();
   }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)) {

            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            nfcController.writeToTag(tagFromIntent, ndefRecords);

            finish();
        }

    }
}
