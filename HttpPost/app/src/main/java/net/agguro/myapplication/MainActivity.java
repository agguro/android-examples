package net.agguro.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static String output;

    private static String OpenHttpConnection(String urlString) throws IOException {

        String line;
        try {
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            if (connection != null) {
                String urlParameters = "name=agguro&id=5845U2J&data='some data'";
                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(urlParameters);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();
                output = "Request URL: \n" + url;
                output += System.getProperty("line.separator") + "Request Parameters: \n " + urlParameters;
                output += System.getProperty("line.separator") + "Response Code: \n" + responseCode;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();
                output += System.getProperty("line.separator") + responseOutput.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new IOException("exception");
        }
        return output;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadTask d = new DownloadTask(this);
        d.execute("https://agguro.net/bin/getpostparams");

    }

    private static class DownloadTask extends AsyncTask<String, String, String> {

        private final WeakReference<MainActivity> activityReference;

        // only retain a weak reference to the activity
        DownloadTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... url) {
            String in = "";
            try {
                in = OpenHttpConnection(url[0]);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            return in;
        }

        /**
         * After completing background task
         **/
        @Override
        protected void onPostExecute(String url) {
            MainActivity activity = activityReference.get();
            TextView textOutput = activity.findViewById(R.id.textOutput);
            textOutput.setText(output);
        }
    }
}

