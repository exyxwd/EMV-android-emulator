package de.androidcrypto.android_hce_emulate_a_creditcard;

import android.widget.TextView;

public class Logger {

    private static StringBuilder logBuilder = new StringBuilder();
    private static TextView logTextView;

    public static void initialize(TextView textView) {
        logTextView = textView;
    }

    public static void log(String tag, String message) {
        String logMessage = tag + ": " + message + "\n";
        logBuilder.append(logMessage);

        if (logTextView != null) {
            logTextView.post(() -> logTextView.setText(logBuilder.toString()));
        }
    }

    public static void clearLog() {
        logBuilder.setLength(0);
        if (logTextView != null) {
            logTextView.post(() -> logTextView.setText(""));
        }
    }
}
