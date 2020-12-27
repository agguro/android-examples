package be.agguro.ScanExApp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_PICTURE_CAPTURE = 1;
    private static final String TAG = "CapturePicture";
    /*  String scanFormat;
        Integer scanOrientation;
        String scanECLevel;
        byte[] scanRawBytes;
    */
    public static String timeStamp;
    public static TextView txt;
    public static File filename;
    private static String pictureFilePath;
    //public static File fd;
    // public static Bitmap bitmapImage;
    private static String output = "";
    String scanContent;
    String pictureFile;
    private ImageView image;
    private View.OnClickListener capture = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                sendTakePictureIntent();
            }
        }
    };
    private View.OnClickListener scan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                sendScanBarcodeIntent();
            }
        }
    };
    //save captured picture in gallery
    private View.OnClickListener savePicture = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            addToGallery();
            image.setVisibility(ImageView.INVISIBLE);
            uploadToServer();
        }
    };

    private static String OpenHttpConnection(String urlString) throws IOException {
        String line;
        try {
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            if (connection != null) {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + "*****");
                connection.setRequestProperty("uploaded_file", txt.getText() + "-" + filename.getName() + "\n");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

                dos.writeBytes("--" + "*****" + "\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=" + txt.getText() + "-" + filename.getName() + "\n");
                dos.writeBytes("\n");

                FileInputStream fileInputStream = new FileInputStream(filename);
                // create a buffer of  maximum size
                int bytesAvailable = fileInputStream.available();

                int bufferSize = Math.min(bytesAvailable, 1024);
                byte[] buffer = new byte[bufferSize];

                // read file and write it into form...
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, 1024);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necessary after file data...
                dos.writeBytes("\n");
                dos.writeBytes("--" + "*****" + "--" + "\n");

                //dStream.writeBytes(urlParameters);


                dos.flush();
                dos.close();
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();
                output += responseOutput.toString();
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

        image = findViewById(R.id.picture);
        txt = (TextView) findViewById(R.id.scanCodeView);

        Button captureButton = findViewById(R.id.capture);
        captureButton.setOnClickListener(capture);
        Button scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(scan);

        findViewById(R.id.save).setOnClickListener(savePicture);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            captureButton.setEnabled(false);
            scanButton.setEnabled(false);
        }

    }

    private void sendScanBarcodeIntent() {
        IntentIntegrator scanIntent = new IntentIntegrator(this);
        scanIntent.initiateScan();
    }

    private void sendTakePictureIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File pictureFile = null;
            try {
                pictureFile = getPictureFile();
            } catch (IOException ex) {
                Toast.makeText(this,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        BuildConfig.APPLICATION_ID + ".provider", pictureFile);

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);
            }
        }
    }

    private File getPictureFile() throws IOException {
        timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        pictureFile = timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, ".jfif", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICTURE_CAPTURE: {
                    File imgFile = new File(pictureFilePath);
                    if (imgFile.exists()) {
                        image.setImageURI(Uri.fromFile(imgFile));
                    }
                }
                case REQUEST_CODE: {
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    //retrieve scan result
                    if (scanResult != null) {
                        //we have a result
                        scanContent = scanResult.getContents();
                        txt.setText(scanContent);
                    }
                }
            }
        }
    }

    private void addToGallery() {
        if (pictureFilePath != null) {
            Intent galleryIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            filename = new File(pictureFilePath);

            Uri picUri = Uri.fromFile(filename);
            galleryIntent.setData(picUri);
            this.sendBroadcast(galleryIntent);

        }
    }

    private void uploadToServer() {
        UploadTask d = new UploadTask(this);
        //change website here
        d.execute("https://agguro.be/ScanEx/scanexapp.php");
    }

    private static class UploadTask extends AsyncTask<String, String, String> {

        private final WeakReference<MainActivity> activityReference;

        // only retain a weak reference to the activity
        UploadTask(MainActivity context) {
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
