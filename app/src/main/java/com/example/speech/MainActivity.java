package com.example.speech;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Toast;

import com.andrognito.flashbar.Flashbar;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private String[] permissions = {Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.CALL_PHONE,
                                    Manifest.permission.READ_CONTACTS};

    private SpeechRecognizer speechRecognizer;

    private Button btnListen;
    private RecognitionProgressView recognitionProgressView;

    private List<Contact> contacts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnListen = findViewById(R.id.btnListen);
        recognitionProgressView = (RecognitionProgressView) findViewById(R.id.recognition_view);

        requestPermission();

        init();

        contacts = new ArrayList<>();
        GetContactsIntoArrayList();

        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recognitionProgressView.play();
                startRecognition();

            }
        });

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

        int[] heights = { 20, 24, 18, 23, 16 };

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);


        recognitionProgressView.setSpeechRecognizer(speechRecognizer);
        recognitionProgressView.setRecognitionListener(new RecognitionListenerAdapter() {
            @Override
            public void onResults(Bundle results) {

                processing_text(results);
                recognitionProgressView.stop();
                recognitionProgressView.play();
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
            else if (text.contains("instagram")){
                lauch_application("com.instagram.android");
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
}
