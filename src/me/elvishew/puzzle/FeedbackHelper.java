package me.elvishew.puzzle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FeedbackHelper {

    private FeedbackHelper() {
    }

    public static String readErrorLogs() {
        // Need android.permission.READ_LOGS.
        /*StringBuilder sb = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(
                    "logcat -d -s AndroidRuntime:E");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()), 1024);
            String line = bufferedReader.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            try {
                Runtime.getRuntime().exec("logcat -c");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return sb.toString();*/
        return null;
    }
}
