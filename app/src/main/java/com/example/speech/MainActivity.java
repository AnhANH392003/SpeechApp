package com.example.speech;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.andrognito.flashbar.Flashbar;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION_CODE = 1;

    private SpeechRecognizer speechRecognizer;

    private Button btnListen;
    private RecognitionProgressView progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnListen = findViewById(R.id.btnListen);
        progressView = findViewById(R.id.recognition_view);

        requestPermission();

        int[] colors = {
                ContextCompat.getColor(this, R.color.color1),
                ContextCompat.getColor(this, R.color.color2),
                ContextCompat.getColor(this, R.color.color3),
                ContextCompat.getColor(this, R.color.color4),
                ContextCompat.getColor(this, R.color.color5)
        };

        int[] heights = { 20, 24, 18, 23, 16 };

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);


        final RecognitionProgressView recognitionProgressView = (RecognitionProgressView) findViewById(R.id.recognition_view);
        recognitionProgressView.setSpeechRecognizer(speechRecognizer);
        recognitionProgressView.setRecognitionListener(new RecognitionListenerAdapter() {
            @Override
            public void onResults(Bundle results) {

                showResults(results);
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


        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recognitionProgressView.play();
                startRecognition();

            }
        });

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

    private void showResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        String text = matches.get(0);
        text = text.toLowerCase();


        if (text.contains("gọi")){

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

            Toast.makeText(this, number, Toast.LENGTH_LONG).show();

        }//else if (text.contains("mở")){
//            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
//
//
//        }
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
        }else{
            search_google(text);
        }

    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            show_alert(MainActivity.this, "ERROR Permission", "Requires RECORD_AUDIO permission");
            btnListen.setEnabled(false);


        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    REQUEST_RECORD_AUDIO_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_RECORD_AUDIO_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            btnListen.setEnabled(true);


        }else{
            show_alert(MainActivity.this, "ERROR Permission", "Requires RECORD_AUDIO permission");
            btnListen.setEnabled(false);
        }
    }

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


    private void phone_call(String number){

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+number));
        startActivity(callIntent);

    }


    private void search_google(String key){

        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);

        intent.putExtra(SearchManager.QUERY, key);

        startActivity(intent);
    }

    private void search_location(String location){
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
    private void navigation(String location){
        location = location.replace(" ", "+");

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}
