package com.example.speech;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.flashbar.Flashbar;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;
import com.kwabenaberko.openweathermaplib.constants.Lang;
import com.kwabenaberko.openweathermaplib.constants.Units;
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper;
import com.kwabenaberko.openweathermaplib.implementation.callbacks.CurrentWeatherCallback;
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class MainActivity extends AppCompatActivity implements LocationListener, RecognitionListener {

    private String[] permissions = {Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.VIBRATE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private SpeechRecognizer speechRecognizer;

    private String LOG_TAG = "TRIGGER";
    private edu.cmu.pocketsphinx.SpeechRecognizer mRecognizer;
    private Vibrator mVibrator;
    private static int sensibility  = 40;
    private static final String WAKEWORD_SEARCH = "TRIGGER_SEARCH";
    private static final String KEYWORD_SEARCH = "hey app";


    private LocationManager locationManager;
    private double latitude, longitude;
    private OpenWeatherMapHelper weather;


    private Button btnListen;
    private RecognitionProgressView recognitionProgressView;

    private List<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        requestPermission();

        init();

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

        weather = new OpenWeatherMapHelper(getString(R.string.OPEN_WEATHER_MAP_API_KEY));
        weather.setUnits(Units.METRIC);
        weather.setLang(Lang.VIETNAMESE);


        onLocationChanged(location);

    }


    /**
     * set init app
     */
    private void init(){
        int[] colors = {
                ContextCompat.getColor(this, R.color.color1),
                ContextCompat.getColor(this, R.color.color2),
                ContextCompat.getColor(this, R.color.color3),
                ContextCompat.getColor(this, R.color.color4),
                ContextCompat.getColor(this, R.color.color5)
        };

        int[] heights = { 30, 24, 18, 23, 16 };

        btnListen = findViewById(R.id.btnListen);
        recognitionProgressView = (RecognitionProgressView) findViewById(R.id.recognition_view);

        contacts = new ArrayList<>();
        GetContactsIntoArrayList();

        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recognitionProgressView.play();
                startRecognition();

                btnListen.setText("Recording");
                btnListen.setEnabled(false);
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);


        recognitionProgressView.setSpeechRecognizer(speechRecognizer);
        recognitionProgressView.setRecognitionListener(new RecognitionListenerAdapter() {
            @Override
            public void onResults(Bundle results) {

                processing_text(results);
                recognitionProgressView.stop();
                recognitionProgressView.play();

                btnListen.setText("Start");
                btnListen.setEnabled(true);
            }
        });
        recognitionProgressView.setColors(colors);
        recognitionProgressView.setBarMaxHeightsInDp(heights);
        recognitionProgressView.setCircleRadiusInDp(6); // kich thuoc cham tron
        recognitionProgressView.setSpacingInDp(2); // khoang cach giua cac cham tron
        recognitionProgressView.setIdleStateAmplitudeInDp(4); // bien do dao dong cua cham tron
        recognitionProgressView.setRotationRadiusInDp(30); // kich thuoc vong quay cua cham tron
        recognitionProgressView.play();

    }


    /**
     * check permission
     */
    private void requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> remainingPermissions = new ArrayList<>();
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    remainingPermissions.add(permission);
                }
            }
            if(remainingPermissions.size() > 0){
                requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), 101);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101){
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){

                }
            }
            //all is good, continue flow
        }
    }

    public void GetContactsIntoArrayList(){

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);

        String name;
        String phonenumber;

        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            name = name.toLowerCase();

            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            Log.d("CONTACT",name + " "  + ":" + " " + phonenumber);

            contacts.add(new Contact(name, phonenumber));
        }

        cursor.close();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupTrigger();
    }

    /**
     * Stop the recognizer.
     * Since cancel() does trigger an onResult() call,
     * we cancel the recognizer rather then stopping it.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mRecognizer != null) {
            mRecognizer.removeListener(this);
            mRecognizer.cancel();
            mRecognizer.shutdown();
            Log.d(LOG_TAG, "PocketSphinx Recognizer was shutdown");
        }
    }

    // Trigger Recognition
    /**
     * Setup the Recognizer with a sensitivity value in the range [1..100]
     * Where 1 means no false alarms but many true matches might be missed.
     * and 100 most of the words will be correctly detected, but you will have many false alarms.
     */
    private void setupTrigger() {
        try {
            final Assets assets = new Assets(MainActivity.this);
            final File assetDir = assets.syncAssets();
            mRecognizer = SpeechRecognizerSetup.defaultSetup()
                    .setAcousticModel(new File(assetDir, "models/en-us-ptm"))
                    .setDictionary(new File(assetDir, "models/lm/words.dic"))
                    .setKeywordThreshold(Float.valueOf("1.e-" + 2 * sensibility))
                    .getRecognizer();
            mRecognizer.addKeyphraseSearch(WAKEWORD_SEARCH, KEYWORD_SEARCH);
            mRecognizer.addListener(this);
            mRecognizer.startListening(WAKEWORD_SEARCH);
            Log.d(LOG_TAG, "... listening");
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    //
    // RecognitionListener Implementation
    //

    @Override
    public void onBeginningOfSpeech() {
        Log.d(LOG_TAG, "Beginning Of Speech");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle("~ ~ ~");
        }
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(LOG_TAG, "End Of Speech");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle("");
        }
    }

    @Override
    public void onPartialResult(final Hypothesis hypothesis) {
        if (hypothesis != null) {
            final String text = hypothesis.getHypstr();
            Log.d(LOG_TAG, "on partial: " + text);
            if (text.equals(KEYWORD_SEARCH)) {
                mVibrator.vibrate(100);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle("");
                }
                Log.d(LOG_TAG, "onPartialResult: " + "SUCCESSFUL");
            }
        }
    }

    @Override
    public void onResult(final Hypothesis hypothesis) {
        if (hypothesis != null) {
            Log.d(LOG_TAG, "on Result: " + hypothesis.getHypstr() + " : " + hypothesis.getBestScore());
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("");
            }
        }
    }

    @Override
    public void onError(final Exception e) {
        Log.e(LOG_TAG, "on Error: " + e);
    }

    @Override
    public void onTimeout() {
        Log.d(LOG_TAG, "on Timeout");
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {

            speechRecognizer.destroy();

        }
        super.onDestroy();
    }

    private void startRecognition() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi");

        speechRecognizer.startListening(intent);

    }


    /**
     * Processing text after recognition speech
     * @param results: text after recognition speech
     */
    private void processing_text(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        String text = matches.get(0);
        text = text.toLowerCase();


        if (text.contains("gọi")){

            if (text.contains("cho")){
                String string_start = "cho";

                int start = text.indexOf(string_start) + string_start.length()+1;
                int end = text.length();

                String name = text.substring(start, end);

                Log.d("name: ", name);

                ArrayList<String> nameCall = new ArrayList<>();
                final ArrayList<String> phoneCall = new ArrayList<>();

                for (Contact contact : contacts){
                    Log.d("contact_name: ", contact.getName());
                    if (contact.getName().contains(name) || name.contains(contact.getName())){
                        nameCall.add(contact.getName());
                        phoneCall.add(contact.getPhone());
                    }
                }
                if (nameCall.size() > 1){

                    ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.contact, nameCall);

                    final Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.list_contact);
                    ListView listView = dialog.findViewById(R.id.contact_list);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            phone_call(phoneCall.get(position));
                        }
                    });

                    Button cancel = dialog.findViewById(R.id.cancel);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.setCancelable(false);
                    dialog.show();
                }
                else if(nameCall.size() == 1){
                    phone_call(phoneCall.get(0));
                }else{
                    show_alert(this, "Lỗi", "Người liên lạc không được tìm thấy ");
                }

            }else{
                String[] t = matches.get(0).split(" ");
                String number = "";
                for (String i : t){

                    Pattern pattern = Pattern.compile("\\d*");
                    Matcher matcher = pattern.matcher(i);
                    Log.d("Text: ", i);
                    if (matcher.matches()) {
                        number += i;
                    }
                }

                phone_call(number);
            }

        }
        else if (text.contains("tìm")){

            if(text.contains("đường")){

                String string_start = "đến";

                int start = text.indexOf(string_start) + string_start.length();
                int end = text.length();

                String location = text.substring(start, end);
                navigation(location);

                Toast.makeText(this, "Tim duong", Toast.LENGTH_SHORT).show();

            }else if (text.contains("gần")){

                String string_start = "tìm";
                String string_end = "gần";

                int start = text.indexOf(string_start) + string_start.length();
                int end = text.indexOf(string_end);

                String location = text.substring(start, end);
                search_location(location);

                Toast.makeText(this, "Tim gan nhat", Toast.LENGTH_LONG).show();

            }else{

                String string_start = "tìm";

                int start = text.indexOf(string_start) + string_start.length();
                int end = text.length();

                String key = text.substring(start, end);

                search_google(key);
            }
        }
        else if(text.contains("mở")){

            if (text.contains("chụp") || text.contains("camera")){
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(intent);

            }
            else if (text.contains("ảnh")){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
            else if (text.contains("youtube")){
                lauch_application("com.google.android.youtube");
            }
            else if (text.contains("mp3") || text.contains("nhạc")){
                lauch_application("com.zing.mp3");
            }
            else if (text.contains("face")){
                lauch_application("com.facebook.katana");
            }
            else if (text.contains("messenger")){
                lauch_application("com.facebook.orca");
            }
            else if (text.contains("instagram")){
                lauch_application("com.instagram.android");
            }
            else if(text.contains("map")){
                lauch_application("com.google.android.apps.maps");
            }else{
                show_alert(MainActivity.this, "", "Yêu cầu của bạn hiện tại đang được phát triển.");
            }


        }
        else if (text.contains("thời tiết")){
            weather();
        }
        else{
            search_google(text);
        }

    }

    /**
     * Show alert
     * @param activity
     * @param title
     * @param message
     */
    public static void show_alert(Activity activity, String title, String message) {

        // https://github.com/aritraroy/Flashbar
        new Flashbar.Builder(activity)
                .gravity(Flashbar.Gravity.BOTTOM)
                .duration(5000)
                .title(title)
                .titleSizeInPx(50f)
                .message(message)
                .backgroundColorRes(R.color.color1)
                .build()
                .show();
    }


    /**
     * Call phone of numbers
     * @param number: phonenumber
     */
    private void phone_call(String number){

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+number));
        startActivity(callIntent);

    }


    /**
     * Search to the google by key search
     * @param key: key search
     */
    private void search_google(String key){

        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);

        intent.putExtra(SearchManager.QUERY, key);

        startActivity(intent);
    }


    /**
     * Search for the nearest your location
     * @param location: address to find
     */
    private void search_location(String location){
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    /**
     * Seach for navigation from your location to {location}
     * @param location: address to find
     */
    private void navigation(String location){
        location = location.replace(" ", "+");

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void lauch_application(String idApp){

        Toast.makeText(getApplicationContext(), idApp, Toast.LENGTH_SHORT).show();

        Intent lauch = getPackageManager().getLaunchIntentForPackage(idApp);

        if (lauch != null){
            startActivity(lauch);
        }else{
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=" + idApp)));
        }
    }

    private void weather(){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please waiting....");
        progressDialog.show();

        weather.getCurrentWeatherByGeoCoordinates(latitude, longitude, new CurrentWeatherCallback() {
            @Override
            public void onSuccess(CurrentWeather currentWeather) {

                progressDialog.dismiss();

                Date timeSunrise = new Date(currentWeather.getSys().getSunrise()*1000);
                Date timeSunset = new Date(currentWeather.getSys().getSunset()*1000);

                DateFormat dateFormat = new SimpleDateFormat("hh:mm a");

                String sunrise = dateFormat.format(timeSunrise);
                String sunset = dateFormat.format(timeSunset);


                Log.v("Weather", "Coordinates: " + currentWeather.getCoord().getLat() + ", "+currentWeather.getCoord().getLon() +"\n"
                        +"Weather Description: " + currentWeather.getWeather().get(0).getDescription() + "\n"
                        +"Temperature: " + currentWeather.getMain().getTempMax()+"\n"
                        +"Wind Speed: " + currentWeather.getWind().getSpeed() + "\n"
                        +"City, Country: " + currentWeather.getName() + ", " + currentWeather.getSys().getCountry() + "\n"
                        +"Time sunrise: " + sunrise +"\n"
                        +"Time sunset: " + sunset +"\n"
                );

                String location = currentWeather.getName() + ", " + currentWeather.getSys().getCountry();
                String description = currentWeather.getWeather().get(0).getDescription();
                String wind = String.valueOf(currentWeather.getWind().getSpeed());
                String tempMax = String.valueOf(currentWeather.getMain().getTempMax());
                String tempMin = String.valueOf(currentWeather.getMain().getTempMin());
                String humidity = String.valueOf(currentWeather.getMain().getHumidity());

                show_weather(location, description, tempMax, tempMin, wind, humidity,  sunrise, sunset);
            }

            @Override
            public void onFailure(Throwable throwable) {
                progressDialog.dismiss();
                show_alert(MainActivity.this, "Lỗi", "Chức năng đang bị lỗi!");
                Log.v("Weather", throwable.getMessage());
            }
        });

    }

    private void show_weather(String location, String description,  String tempMax, String tempMin, String wind,String humidity, String sunrise, String sunset){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.weather);

        dialog.setContentView(R.layout.weather);

        TextView tvLocation, tvDescription, tvTempMax, tvTempMin, tvWind, tvHumidity, tvSunrise, tvSunset;

        tvLocation = dialog.findViewById(R.id.tvLocation);
        tvDescription = dialog.findViewById(R.id.tvDescription);
        tvTempMax = dialog.findViewById(R.id.tvTempMax);
        tvTempMin = dialog.findViewById(R.id.tvTempMin);
        tvWind = dialog.findViewById(R.id.tvWind);
        tvHumidity = dialog.findViewById(R.id.tvHumidity);
        tvSunrise = dialog.findViewById(R.id.tvSunrise);
        tvSunset = dialog.findViewById(R.id.tvSunset);


        tvLocation.setText(location);
        tvDescription.setText(description);
        tvTempMax.setText(tempMax);
        tvTempMin.setText(tempMin);
        tvWind.setText(wind);
        tvHumidity.setText(humidity);
        tvSunrise.setText(sunrise);
        tvSunset.setText(sunset);

        dialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Log.d("onLocationChanged", String.valueOf(latitude) + " " + String.valueOf(longitude));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
