package com.example.mobile_weatherapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView currentWeather;
    private TextView forecast1, forecast2, forecast3, forecast4, forecast5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentWeather = findViewById(R.id.currentWeather);
        forecast1 = findViewById(R.id.forecast1);
        forecast2 = findViewById(R.id.forecast2);
        forecast3 = findViewById(R.id.forecast3);
        forecast4 = findViewById(R.id.forecast4);
        forecast5 = findViewById(R.id.forecast5);

        double latitude = 37.5665; // Seoul latitude
        double longitude = 126.9780; // Seoul longitude

        new FetchWeatherTask().execute(latitude, longitude);
        new FetchForecastTask().execute(latitude, longitude);
    }

    private class FetchWeatherTask extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... params) {
            double lat = params[0];
            double lon = params[1];
            try {
                return lookUpWeather(lat, lon);
            } catch (IOException | JSONException e) {
                Log.e("FetchWeatherTask", "Error fetching weather data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String weather) {
            if (weather != null) {
                currentWeather.setText("현재 날씨 : " + weather);
            } else {
                currentWeather.setText("날씨 데이터를 불러올 수 없습니다.");
            }
        }

        private String lookUpWeather(double lat, double lon) throws IOException, JSONException {
            String apiKey = "be30d0417182e4188259fd1a1bf395fe"; // Replace with your actual API key
            String urlStr = "https://api.openweathermap.org/data/3.0/onecall?lat=" + lat + "&lon=" + lon + "&exclude=minutely,hourly,daily,alerts&appid=" + apiKey + "&units=metric&lang=kr";
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            Log.d("FetchWeatherTask", "Response Code: " + responseCode);

            if (responseCode == 200) { // HTTP OK
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                urlConnection.disconnect();
                Log.d("FetchWeatherTask", "Weather data: " + content.toString());
                JSONObject jsonObject = new JSONObject(content.toString());
                return jsonObject.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("description");
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                urlConnection.disconnect();
                Log.e("FetchWeatherTask", "Error response: " + content.toString());
                return null;
            }
        }
    }

    private class FetchForecastTask extends AsyncTask<Double, Void, List<ForecastModel>> {
        @Override
        protected List<ForecastModel> doInBackground(Double... params) {
            double lat = params[0];
            double lon = params[1];
            try {
                return lookUpForecast(lat, lon);
            } catch (IOException | JSONException e) {
                Log.e("FetchForecastTask", "Error fetching forecast data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<ForecastModel> forecastList) {
            if (forecastList != null && forecastList.size() >= 5) {
                forecast1.setText(formatForecast(forecastList.get(0)));
                forecast2.setText(formatForecast(forecastList.get(1)));
                forecast3.setText(formatForecast(forecastList.get(2)));
                forecast4.setText(formatForecast(forecastList.get(3)));
                forecast5.setText(formatForecast(forecastList.get(4)));
            } else {
                forecast1.setText("예보 데이터를 불러올 수 없습니다.");
                forecast2.setText("");
                forecast3.setText("");
                forecast4.setText("");
                forecast5.setText("");
            }
        }

        private List<ForecastModel> lookUpForecast(double lat, double lon) throws IOException, JSONException {
            String apiKey = "be30d0417182e4188259fd1a1bf395fe"; // Replace with your actual API key
            String urlStr = "https://api.openweathermap.org/data/3.0/onecall?lat=" + lat + "&lon=" + lon + "&exclude=current,minutely,hourly,alerts&appid=" + apiKey + "&units=metric&lang=kr";
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            Log.d("FetchForecastTask", "Response Code: " + responseCode);

            if (responseCode == 200) { // HTTP OK
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                urlConnection.disconnect();
                Log.d("FetchForecastTask", "Forecast data: " + content.toString());
                JSONObject jsonObject = new JSONObject(content.toString());
                List<ForecastModel> forecastList = new ArrayList<>();
                JSONArray daily = jsonObject.getJSONArray("daily");
                for (int i = 0; i < daily.length(); i++) {
                    JSONObject day = daily.getJSONObject(i);
                    ForecastModel forecast = new ForecastModel();
                    forecast.setDate(day.getString("dt"));
                    forecast.setTemperature(day.getJSONObject("temp").getDouble("day"));
                    forecast.setDescription(day.getJSONArray("weather").getJSONObject(0).getString("description"));
                    forecastList.add(forecast);
                }
                return forecastList;
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                urlConnection.disconnect();
                Log.e("FetchForecastTask", "Error response: " + content.toString());
                return null;
            }
        }

        private String formatForecast(ForecastModel forecast) {
            return forecast.getDate() + " - " + forecast.getTemperature() + "°C, " + forecast.getDescription();
        }
    }

    private class ForecastModel {
        private String date;
        private double temperature;
        private String description;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
