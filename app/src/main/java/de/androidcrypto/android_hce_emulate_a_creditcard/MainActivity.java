package de.androidcrypto.android_hce_emulate_a_creditcard;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity {

    private TextView tvHceServiceLog;
    private String hceServiceLog = "";

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Created", "----: ");

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewMainActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvHceServiceLog = findViewById(R.id.tvHceServiceLog);
        Logger.initialize(tvHceServiceLog);

        /**
         * Note on this setting: Do NOT remove this setting or the app will fail when the screen goes
         * from portrait to landscape and vice versa. The app will then run the onDestroy() method, but
         * the BroadcastReceiver is not registered yet. The only way to solve this issue would be to
         * use a STATIC BroadcastReceiver.
         */
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.registerReceiver(mMessageReceiver, new IntentFilter("HceCcEmulatorService"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                    mMessageReceiver, new IntentFilter("HceCcEmulatorService"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mMessageReceiver);
    }

    private void appendMessageToLog(String message, String data) {
        hceServiceLog += message + " | " + data + "\n";
        Log.d("Appended--", "----: ");

        //tvHceServiceLog.setText(hceServiceLog);
        Logger.log("MainActivity", message + " | " + data);

    }

    private void showAToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d("Received--", "----: ");

            String message = intent.getStringExtra("Message");
            String data = intent.getStringExtra("Data");
            appendMessageToLog(message, data);
        }
    };

}