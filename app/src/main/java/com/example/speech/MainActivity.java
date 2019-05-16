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
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.kwabenaberko.openweathermaplib.models.common.Sys;
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private String[] permissions = {Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.VIBRATE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SET_ALARM};

    private SpeechRecognizer speechRecognizer;

    private String LOG_TAG = "TRIGGER";

    private LocationManager locationManager;
    private double latitude, longitude;
    private OpenWeatherMapHelper weather;

    private RecyclerView mMessageRecycler;
    private List<Message> messageList;
    private MessageListAdapter mMessageAdapter;
    private ImageButton btnListen, btnKeyBoard;
    private EditText edittext_chatbox;
    private LinearLayout layout_chatbox;
    private FrameLayout layout_speech;

    private TextToSpeech textToSpeech;

    private boolean isRecognitionSpeech;

    private RecognitionProgressView recognitionProgressView;

    private List<Contact> contacts;
    private List<Application> apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        init();


    }


    /**
     * setup init app
     */
    private void init(){
        contacts = GetContactsIntoArrayList();
        apps = getAllApplicationInPhone();


        // Get phone's location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        onLocationChanged(location);


        //setup weather
        weather = new OpenWeatherMapHelper(getString(R.string.OPEN_WEATHER_MAP_API_KEY));
        weather.setUnits(Units.METRIC);
        weather.setLang(Lang.VIETNAMESE);


        // setup UI Message
        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);

        layout_chatbox = findViewById(R.id.layout_chatbox);
        layout_speech = findViewById(R.id.layout_speech);


        btnKeyBoard = findViewById(R.id.btnKeyBoard);
        btnListen = findViewById(R.id.btnListen);
        recognitionProgressView = (RecognitionProgressView) findViewById(R.id.recognition_view);


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                }
            }
        });



        messageList = new ArrayList<>();

        mMessageAdapter = new MessageListAdapter(this, messageList);

        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));

        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();

        readCsvMessage();

        sendMessage("Chào bạn, Tôi có thể giúp gì cho bạn!", false);

        isRecognitionSpeech = true;

        layout_chatbox.setVisibility(View.INVISIBLE);


        edittext_chatbox = (EditText) findViewById(R.id.edittext_chatbox);

        findViewById(R.id.button_chatbox_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edittext_chatbox.getText().toString();
                edittext_chatbox.setText("");
                sendMessage(text, true);

                processing_text(text);
            }
        });

        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startRecognition();

            }
        });

        btnKeyBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechToKeyboard();
            }
        });

        findViewById(R.id.btnListesInChatbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardToSpeech();
                startRecognition();
            }
        });

        setUiRecognition();


    }

    /**
     * Setup UI Recognition Progress View
     */
    private void setUiRecognition(){
        // setup Speech Recognition
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        recognitionProgressView.setSpeechRecognizer(speechRecognizer);
        recognitionProgressView.setRecognitionListener(new RecognitionListenerAdapter() {
            @Override
            public void onResults(Bundle results) {

                finishRecognition();

                ArrayList<String> matches = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                String text = matches.get(0);
                Log.d(LOG_TAG, "onResults: "+ text);

                sendMessage(text, true);

                processing_text(text);

            }
        });
        recognitionProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finishRecognition();
                speechRecognizer.stopListening();

            }
        });

        int[] colors = {
                ContextCompat.getColor(this, R.color.color1),
                ContextCompat.getColor(this, R.color.color2),
                ContextCompat.getColor(this, R.color.color3),
                ContextCompat.getColor(this, R.color.color4),
                ContextCompat.getColor(this, R.color.color5)
        };

        int[] heights = {60, 76, 58, 80, 55};

        recognitionProgressView.setColors(colors);
        recognitionProgressView.setBarMaxHeightsInDp(heights);
        recognitionProgressView.setCircleRadiusInDp(6); // kich thuoc cham tron
        recognitionProgressView.setSpacingInDp(2); // khoang cach giua cac cham tron
        recognitionProgressView.setIdleStateAmplitudeInDp(8); // bien do dao dong cua cham tron
        recognitionProgressView.setRotationRadiusInDp(40); // kich thuoc vong quay cua cham tron
        recognitionProgressView.play();

    }


    /**
     * Add Message
     * @param text
     * @param isUser
     */
    private void sendMessage(String text, boolean isUser) {

        messageList.add(new Message(text, isUser, System.currentTimeMillis()));
        mMessageAdapter.notifyDataSetChanged();
        writeCsvMessage();
    }


    /**
     * Check permission
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

    /**
     * get list contact in phone
     */
    public List<Contact> GetContactsIntoArrayList(){

        List<Contact> contacts = new ArrayList<>();

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);

        String name;
        String phonenumber;
        String nameNumber;

        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            nameNumber = name;
            name = name.toLowerCase();

            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            Log.d("CONTACT",name + " "  + ":" + " " + phonenumber);

            contacts.add(new Contact(name, phonenumber, nameNumber));
        }

        cursor.close();

        return contacts;

    }

    /**
     * get All Application in Phone
     */
    private List<Application> getAllApplicationInPhone (){

        List<Application> apps = new ArrayList<>();

        apps.add(new Application("facebook", "com.facebook.katana"));
        apps.add(new Application("youtube", "com.google.android.youtube"));
        apps.add(new Application("instagram", "com.instagram.android"));
        apps.add(new Application("nhạc", "com.zing.mp3"));
        apps.add(new Application("messenger", "com.facebook.orca"));
        apps.add(new Application("map", "com.google.android.apps.maps"));
        apps.add(new Application("bản đồ", "com.google.android.apps.maps"));



        PackageManager manager = getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for (ResolveInfo resolveInfo : availableActivities){

            String name = (String) resolveInfo.loadLabel(manager);
            name = name.toLowerCase();

            String packageName = resolveInfo.activityInfo.packageName;

            Application app = new Application(name, packageName);

            apps.add(app);
        }

        return apps;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "stop TRIGGER");
        //Start service
        Intent intent = new Intent(this, Trigger.class);
        stopService(intent);

        if(isRecognitionSpeech){
            //start Recognition Speech
            startRecognition();
        }

    }

    /**
     * Stop the recognizer.
     * Since cancel() does trigger an onResult() call,
     * we cancel the recognizer rather then stopping it.
     */
    @Override
    protected void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "start TRIGGER");

        finishRecognition();
        speechRecognizer.stopListening();

        //Start service
        Intent intent = new Intent(this, Trigger.class);
        startService(intent);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (speechRecognizer != null) {

            speechRecognizer.destroy();

        }

        //Start service
         Intent intent = new Intent(this, Trigger.class);
         stopService(intent);

    }


    /**
     * Write data to file database csv
     */
    private void writeCsvMessage(){
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SpeechApplication");
        if(!folder.exists()){
            folder.mkdirs();
        }

        File csv = new File(folder, "message.csv");
        if(!csv.exists()){
            try {
                csv.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        String data = "";
        for (Message m : messageList ){
            data += m.getMessage() + ";" + m.getCreatedAt() + ";" + String.valueOf(m.isSender()) + "\n";
        }
        Log.d("writeCsvMessage: ", data);

        FileWriter fw = null;
        try {

            fw = new FileWriter(csv.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(data);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read data from file database csv
     */
    private void readCsvMessage(){

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SpeechApplication").getAbsoluteFile();

        if(folder.exists()){

            File csv = new File(folder, "message.csv");

            if(csv.exists()){

                BufferedReader br = null;
                try {
                    String m;
                    br = new BufferedReader(new FileReader(csv));
                    while ((m = br.readLine()) != null) {

                        String[] ms = m.split(";");
                        if(ms.length == 3){
                            String message = ms[0];
                            long time = Long.parseLong(ms[1]);
                            boolean isUser = Boolean.valueOf(ms[2]);

                            if(!message.equals("Chào bạn, Tôi có thể giúp gì cho bạn!")){
                                Log.d("readCsvMessage: ", message + " " + String.valueOf(isUser) + " " + String.valueOf(time));
                                messageList.add(new Message(message, isUser, time));
                            }
                        }
                    }
                    mMessageAdapter.notifyDataSetChanged();


                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (br != null)br.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

            }


        }

    }

    /**
     * convert theme Keyboard to Speech
     */
    private void KeyboardToSpeech(){
        layout_chatbox.setVisibility(View.INVISIBLE);
        layout_speech.setVisibility(View.VISIBLE);

        closeKeyboard();
    }

    /**
     * convert theme Speech to Keyboard
     */
    private void SpeechToKeyboard(){

        finishRecognition();

        showKeyboard();
        isRecognitionSpeech = false;

        layout_chatbox.setVisibility(View.VISIBLE);
        layout_speech.setVisibility(View.INVISIBLE);

    }

    /**
     * Start Google API recognition
     */
    private void startRecognition() {

        btnListen.setVisibility(View.GONE);
        btnKeyBoard.setVisibility(View.GONE);

        recognitionProgressView.play();
        recognitionProgressView.setVisibility(View.VISIBLE);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi");

        speechRecognizer.startListening(intent);
    }

    private void finishRecognition(){

        btnListen.setVisibility(View.VISIBLE);
        btnKeyBoard.setVisibility(View.VISIBLE);

        recognitionProgressView.stop();
        recognitionProgressView.play();

        recognitionProgressView.setVisibility(View.GONE);
    }

    public void showKeyboard(){
        edittext_chatbox.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edittext_chatbox, InputMethodManager.SHOW_IMPLICIT);
    }

    public void closeKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edittext_chatbox.getWindowToken(), 0);
    }

    /**
     * Processing text after recognition speech
     * @param text: text after recognition speech
     */
    private void processing_text(String text) {

        text = text.toLowerCase();


        if (text.contains("gọi")){

            call(text);

        }
        else if (text.contains("tìm")){

            search(text);

        }
        else if(text.contains("mở")){

            app(text);
        }
        else if (text.contains("thời tiết")){
            weather();
        }
        else if (text.contains("báo thức")){
            alarm(text);
        }
        else if ( text.contains("đếm ngược")){
            timer(text);
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
     * set alarm
     * @param text
     */
    private void alarm(String text){

        if (text.contains(":")){

            String[] ls = text.split(" ");
            String[] lstemp = ls[ls.length-1].split(":");
            int hour = Integer.parseInt(lstemp[0]);
            int minutes = Integer.parseInt(lstemp[1]);

            String message = "Báo thức bằng Speech";
            createAlarm(message, hour, minutes);
        }
        else {
            String[] ls = text.split(" ");
            int hour = Integer.parseInt(ls[ls.length-1]);
            int minutes = 0;
            String message = "Báo thức bằng Speech";
            createAlarm(message, hour, minutes);
        }
    }

    /**
     * create Alarm
     * @param message
     * @param hour
     * @param minutes
     */
    private void createAlarm(String message, int hour, int minutes) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes);
        if (intent.resolveActivity(getPackageManager()) != null) {

            sendMessage("Đã đạt báo thức lúc "+ String.valueOf(hour) + ":" + String.valueOf(minutes), false);
            startActivity(intent);
        }
    }


    /**
     * set Timer
     * @param text
     */
    private void timer(String text){

        String[] t = text.split(" ");
        String number = "";
        for (String i : t){

            Pattern pattern = Pattern.compile("\\d*");
            Matcher matcher = pattern.matcher(i);
            Log.d("Text: ", i);
            if (matcher.matches()) {
                number += i;
            }
        }

        startTimer("Đếm ngược bằng Speech", Integer.valueOf(number)*60);

    }

    /**
     * Create timer
     * @param message
     * @param seconds
     */
    public void startTimer(String message, int seconds) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                .putExtra(AlarmClock.EXTRA_LENGTH, seconds);
        if (intent.resolveActivity(getPackageManager()) != null) {
            sendMessage("Đã đặt đếm ngược: " + String.valueOf(seconds /60), false);
            startActivity(intent);
        }
    }



    /**
     * processing text call phone  number
     * @param text
     */
    private void call(String text){

        if (text.contains("cho")){
            String string_start = "cho";

            int start = text.indexOf(string_start) + string_start.length()+1;
            int end = text.length();

            String name = text.substring(start, end);

            Log.d("name: ", name);

            final ArrayList<String> nameCall = new ArrayList<>();
            final ArrayList<String> phoneCall = new ArrayList<>();

            for (Contact contact : contacts){
                Log.d("contact_name: ", contact.getName());
                if (contact.getName().contains(name) || name.contains(contact.getName())){
                    nameCall.add(contact.getNameNumber());
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
                        dialog.dismiss();

                        sendMessage("Call:" + nameCall.get(position), false);

                        phone_call_number(phoneCall.get(position));
                    }
                });

                dialog.setCancelable(false);
                dialog.show();
            }
            else if(nameCall.size() == 1){
                phone_call_number(phoneCall.get(0));
            }else{

                sendMessage("Không tìm thấy tên người liên hệ trong danh bạ.", false);

            }

        }
        else if (text.contains("taxi")){

            phone_call_number("024 3232 3232");
            sendMessage("Gọi taxi G7", false);
        }
        else{
            String[] t = text.split(" ");
            String number = "";
            for (String i : t){

                Pattern pattern = Pattern.compile("\\d*");
                Matcher matcher = pattern.matcher(i);
                Log.d("Text: ", i);
                if (matcher.matches()) {
                    number += i;
                }
            }

            sendMessage("Call: "+number, false);

            phone_call_number(number);
        }

    }

    /**
     * Call phone of numbers
     * @param number: phonenumber
     */
    private void phone_call_number(String number){

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+number));
        startActivity(callIntent);

    }

    private void search(String text){
        if(text.contains("đường")){

            String string_start = "đến";

            int start = text.indexOf(string_start) + string_start.length();
            int end = text.length();

            String location = text.substring(start, end);
            navigation(location);

            sendMessage("Tìm đường đi đến " + location, false);

        }else if (text.contains("gần")){

            String string_start = "tìm";
            String string_end = "gần";

            int start = text.indexOf(string_start) + string_start.length();
            int end = text.indexOf(string_end);

            String location = text.substring(start, end);
            search_location(location);

            sendMessage("Tìm " + location + "gần nhất", false);
        }else{

            String string_start = "tìm";

            int start = text.indexOf(string_start) + string_start.length();
            int end = text.length();

            String key = text.substring(start, end);

            sendMessage("Search: " + key, false);

            search_google(key);
        }
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


    /**
     * Check exist application in phone
     * @param name
     * @return
     */
    private String existApplication(String name){

        for(int i =0; i < apps.size(); i++){
            if (name.contains(apps.get(i).getName())){
                return apps.get(i).getPackageName();
            }
        }

        return null;
    }

    /**
     * Lauch application
     * @param text
     */
    private void app(String text){
        if (text.contains("chụp") || text.contains("camera")){
            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivity(intent);
            sendMessage("Ok", false);
        }
        else if (text.contains("ảnh")){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_VIEW);

            startActivity(intent);


            sendMessage("Ok", false);

        }else{

            String pachageName = existApplication(text);
            if(pachageName != null){
                lauch_application(pachageName);

                sendMessage("Ok", false);
            }else{

                sendMessage("Không tìm thấy ứng dụng trên điện thoại của bạn", false);

            }
        }


    }

    /**
     * Lauching application
     * @param idApp: id Application
     */
    private void lauch_application(String idApp){

        Intent lauch = getPackageManager().getLaunchIntentForPackage(idApp);

        if (lauch != null){
            startActivity(lauch);
        }else{
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=" + idApp)));
        }
    }


    /**
     * Get Weather in phone's location
     */
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
                String humidity = String.valueOf(currentWeather.getMain().getHumidity());

                show_weather(location, description, tempMax, wind, humidity,  sunrise, sunset);
            }

            @Override
            public void onFailure(Throwable throwable) {
                progressDialog.dismiss();
                sendMessage("Lỗi, Không thế tìm kiếm thời tiết tại vị trí của bạn.", false);
                Log.v("Weather", throwable.getMessage());
            }
        });

    }


    /**
     * Show dialog weather in location
     * @param location
     * @param description
     * @param tempMax
     * @param wind
     * @param humidity
     * @param sunrise
     * @param sunset
     */
    private void show_weather(String location, String description,  String tempMax, String wind,String humidity, String sunrise, String sunset){

        String w = "Thời tiết " + location + " : "+description + " " + tempMax + "\u2103";

        sendMessage(w, false);

        textToSpeech.speak(w, TextToSpeech.QUEUE_FLUSH, null);


        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.weather);

        dialog.setContentView(R.layout.weather);

        TextView tvLocation, tvDescription, tvTempMax, tvWind, tvHumidity, tvSunrise, tvSunset;

        tvLocation = dialog.findViewById(R.id.tvLocation);
        tvDescription = dialog.findViewById(R.id.tvDescription);
        tvTempMax = dialog.findViewById(R.id.tvTempMax);
        tvWind = dialog.findViewById(R.id.tvWind);
        tvHumidity = dialog.findViewById(R.id.tvHumidity);
        tvSunrise = dialog.findViewById(R.id.tvSunrise);
        tvSunset = dialog.findViewById(R.id.tvSunset);


        tvLocation.setText(location);
        tvDescription.setText(description);
        tvTempMax.setText(tempMax);
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