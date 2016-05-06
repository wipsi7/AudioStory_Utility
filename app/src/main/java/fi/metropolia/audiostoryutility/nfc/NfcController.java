package fi.metropolia.audiostoryutility.nfc;

import android.content.Context;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

public class NfcController {

    private static final String NFC_TAG = "nfcTag";

    private Context context;
    private IntentFilter nDef, tech;
    private IntentFilter[] intentFilterArray;
    private String[][] techListArray;

    private Locale locale;
    private Ndef ndef;
    private NfcAdapter nfcAdapter;

    public NfcController(Context context){
        this.context = context;

        init();
    }

    public IntentFilter[] getIntentFilterArray() {
        return intentFilterArray;
    }

    public String[][] getTechListArray(){
        return techListArray;
    }

    public NfcAdapter getNfcAdapter(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(context.getApplicationContext());

        return nfcAdapter;
    }

    private void init() {
        locale = context.getResources().getConfiguration().locale;
        nfcAdapter = NfcAdapter.getDefaultAdapter(context.getApplicationContext());

        nDef = createNdefIntentFilter();
        tech = createTechIntentFilter();
        intentFilterArray = new IntentFilter[]{nDef, tech};

        techListArray = new String[][]{new String[] { Ndef.class.getName()}};
    }

    private IntentFilter createTechIntentFilter() {
        IntentFilter techTemp = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            techTemp.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(NFC_TAG, "error: Malformed data type");
        }

        return techTemp;
    }

    private IntentFilter createNdefIntentFilter() {
        IntentFilter nDefTemp = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            nDefTemp.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(NFC_TAG, "error: Malformed data type");
        }

        return nDefTemp;
    }

    public NdefRecord createNdefTextRecord(String payload) {
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


    public void writeToTag(Tag tagFromIntent, NdefRecord[] ndefRecords) {
        NdefMessage ndefMessage = new NdefMessage(ndefRecords);

        ndef = Ndef.get(tagFromIntent);
/*        Toast.makeText(this, "NDef max size: "+ ndef.getMaxSize(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Message max size: "+ ndefMessage.getByteArrayLength(), Toast.LENGTH_SHORT).show();*/

        if(ndefMessage.getByteArrayLength() <= ndef.getMaxSize()) {
            try {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(context.getApplicationContext(), "Tag is not writable!", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();

                Toast.makeText(context.getApplicationContext(), "Write complete!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(NFC_TAG, "Error: tag connection fail");
                e.getMessage();
                Toast.makeText(context.getApplicationContext(), "Write failed!", Toast.LENGTH_SHORT).show();
            } catch (FormatException e) {
                Log.e(NFC_TAG, "Error: malformed NDEF message");
                e.getMessage();
                Toast.makeText(context.getApplicationContext(), "Write failed!", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(context.getApplicationContext(), "Not enough space", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isNfcAvailable() {
        NfcAdapter nfcAdapterTemp = getNfcAdapter();

        if(nfcAdapterTemp != null && nfcAdapterTemp.isEnabled()){
            return true;
        }else {
            Toast.makeText(context.getApplicationContext(),"NFC not available", Toast.LENGTH_SHORT).show();
            return false;
        }


    }

}
