package de.androidcrypto.android_hce_emulate_a_creditcard;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private TextView tvHceServiceLog;
    private TextView tvInformation;
    private String hceServiceLog = "";
    private static final int CARD_EMULATOR_PORT = 52345; // Port for receiving data

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewMainActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvHceServiceLog = findViewById(R.id.tvHceServiceLog);
        tvInformation = findViewById(R.id.tvInformation);
        Logger.initialize(tvHceServiceLog);

        Button btnClearLog = findViewById(R.id.btnClearLog);
        btnClearLog.setOnClickListener(v -> {
            Logger.clearLog();
        });

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Display the device's IP address
        tvInformation.setText(getLocalIpAddress() + " | " + "PORT: " + CARD_EMULATOR_PORT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().contains(".")) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return "Unable to get IP Address";
    }

//    private void startServer() {
//        new Thread(() -> {
//            try (ServerSocket serverSocket = new ServerSocket(CARD_EMULATOR_PORT)) {
//                runOnUiThread(() -> tvHceServiceLog.append("Listening on port " + CARD_EMULATOR_PORT + "\n"));
//                while (true) {
//                    try (Socket clientSocket = serverSocket.accept();
//                         BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
//
//                        StringBuilder receivedData = new StringBuilder();
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            receivedData.append(line).append("\n");
//                        }
//                        // Update the UI with the received data
//                        runOnUiThread(() -> tvHceServiceLog.append(receivedData.toString()));
//                    } catch (Exception clientException) {
//                        runOnUiThread(() -> Toast.makeText(this, "Error handling client: " + clientException.getMessage(), Toast.LENGTH_SHORT).show());
//                    }
//                }
//            } catch (Exception serverException) {
//                runOnUiThread(() -> tvHceServiceLog.setText("Error: " + serverException.getMessage()));
//            }
//        }).start();
//    }
}