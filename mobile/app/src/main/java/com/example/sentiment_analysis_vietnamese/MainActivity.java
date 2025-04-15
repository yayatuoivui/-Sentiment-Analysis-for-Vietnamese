package com.example.sentiment_analysis_vietnamese;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText sentenceInput;
    private TextView resultText;
    private ImageView emotionIcon;

    // Local API URL for Android emulator
    private static final String API_URL = "http://10.0.2.2:8000/predict";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sentenceInput = findViewById(R.id.sentenceInput);
        resultText = findViewById(R.id.resultText);
        emotionIcon = findViewById(R.id.emotionIcon);
        Button classifyButton = findViewById(R.id.classifyButton);

        classifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sentence = sentenceInput.getText().toString().trim();
                if (!sentence.isEmpty()) {
                    new SentimentAnalysisTask().execute(sentence);
                } else {
                    resultText.setText("Vui lòng nhập câu.");
                    emotionIcon.setImageResource(0);
                }
            }
        });
    }

    private class SentimentAnalysisTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String sentence = params[0];
            OkHttpClient client = new OkHttpClient();

            // Create JSON payload for PhoBERT API
            String jsonBody = "{\"text\": \"" + sentence + "\"}";

            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    return json.getString("sentiment").toLowerCase();
                } else {
                    android.util.Log.e("SentimentAnalysis", "API error: HTTP " + response.code());
                    return "error: HTTP " + response.code();
                }
            } catch (IOException e) {
                android.util.Log.e("SentimentAnalysis", "Network error: " + e.getMessage());
                return "error: " + e.getMessage();
            } catch (Exception e) {
                android.util.Log.e("SentimentAnalysis", "Unexpected error: " + e.getMessage());
                return "error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.startsWith("error")) {
                resultText.setText("Lỗi phân loại: " + result);
                emotionIcon.setImageResource(0);
            } else {
                resultText.setText(result);
                // Display corresponding icon
                switch (result) {
                    case "positive":
                        emotionIcon.setImageResource(R.drawable.ic_positive);
                        break;
                    case "negative":
                        emotionIcon.setImageResource(R.drawable.ic_negative);
                        break;
                    case "neutral":
                        emotionIcon.setImageResource(R.drawable.ic_neutral);
                        break;
                    default:
                        emotionIcon.setImageResource(0);
                        break;
                }
            }
        }
    }
}