package fi.metropolia.audiostoryutility.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import fi.metropolia.audiostoryutility.R;

public class NfcWriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_write);
    }

   public void onCancelClick(View v){
       finish();
   }
}
