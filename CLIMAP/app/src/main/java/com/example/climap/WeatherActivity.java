package com.example.climap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import org.json.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import okhttp3.*;

public class WeatherActivity extends AppCompatActivity {
    // UI
    private EditText editTextCity;
    private Button buttonSearch;
    private TextView textViewCity, textViewTemperature, textViewDate2;
    private ImageView imageViewWeatherIcon;
    private TextView textViewTemp, textViewHumidity, textViewWind,
            textViewVisibility, textViewPrecip, textViewPressure;
    // Forecast
    private ImageView[] fIcons = new ImageView[5];
    private TextView[] fDates = new TextView[5];
    private TextView[] fMins  = new TextView[5];
    private TextView[] fMaxs  = new TextView[5];

    private final String API_KEY = "a5b25377df0ba403528c0f0016840fcd";
    private OkHttpClient client;
    private Handler mainHandler;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // bind current weather
        editTextCity         = findViewById(R.id.editTextCity);
        buttonSearch         = findViewById(R.id.buttonSearch);
        textViewCity         = findViewById(R.id.textViewCity);
        textViewTemperature  = findViewById(R.id.textViewTemperature);
        textViewDate2        = findViewById(R.id.textViewDate2);
        imageViewWeatherIcon = findViewById(R.id.imageViewWeatherIcon);
        textViewTemp         = findViewById(R.id.textViewTemp);
        textViewHumidity     = findViewById(R.id.textViewHumidity);
        textViewWind         = findViewById(R.id.textViewWind);
        textViewVisibility   = findViewById(R.id.textViewVisibility);
        textViewPrecip       = findViewById(R.id.textViewPrecip);
        textViewPressure     = findViewById(R.id.textViewPressure);

        // bind forecast views by ID
        for (int i = 0; i < 5; i++) {
            fIcons[i] = findViewById(getResources()
                    .getIdentifier("forecast_icon"+(i+1), "id", getPackageName()));
            fDates[i] = findViewById(getResources()
                    .getIdentifier("forecast_date"+(i+1), "id", getPackageName()));
            fMins[i]  = findViewById(getResources()
                    .getIdentifier("forecast_min"+(i+1), "id", getPackageName()));
            fMaxs[i]  = findViewById(getResources()
                    .getIdentifier("forecast_max"+(i+1), "id", getPackageName()));
        }

        client = new OkHttpClient();
        mainHandler = new Handler(Looper.getMainLooper());

        buttonSearch.setOnClickListener(v -> {
            String city = editTextCity.getText().toString().trim();
            if (TextUtils.isEmpty(city)) {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchCurrentWeather(city);
            fetch5DayForecast(city);
        });
    }

    // 1) CURRENT WEATHER
    private void fetchCurrentWeather(String city) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + city + "&units=metric&appid=" + API_KEY;
        Request req = new Request.Builder().url(url).build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> Toast.makeText(
                        WeatherActivity.this, "Current weather fetch failed", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response res) throws IOException {
                String s = res.body().string();
                if (!res.isSuccessful()) {
                    mainHandler.post(() -> Toast.makeText(
                            WeatherActivity.this, "City not found", Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    JSONObject o = new JSONObject(s);
                    String name = o.getString("name");
                    JSONObject m = o.getJSONObject("main");
                    double temp = m.getDouble("temp");
                    int hum    = m.getInt("humidity");
                    double pres= m.getDouble("pressure");
                    JSONObject w= o.getJSONArray("weather").getJSONObject(0);
                    String icon= w.getString("icon");
                    JSONObject wind=o.getJSONObject("wind");
                    double ws  = wind.getDouble("speed");
                    int vis    = o.optInt("visibility", 0);

                    mainHandler.post(() -> {
                        textViewCity.setText("📍 " + name);
                        textViewDate2.setText(
                                new SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(new Date()));
                        textViewTemperature.setText(String.format(Locale.getDefault(),
                                "%.1f °C", temp));
                        textViewTemp.setText(String.format(Locale.getDefault(),
                                "🌡️ Temperature: %.1f °C", temp));
                        textViewHumidity.setText("💧 Humidity: " + hum + " %");
                        textViewPressure.setText("🌬️ Pressure: " + pres + " hPa");
                        textViewWind.setText(String.format(Locale.getDefault(),
                                "💨 Wind Speed: %.1f m/s", ws));
                        textViewVisibility.setText(String.format(Locale.getDefault(),
                                "👁️ Visibility: %.1f km", vis/1000.0));
                        textViewPrecip.setText("🌧️ Precipitation: N/A");

                        String iconUrl = "https://openweathermap.org/img/wn/"+icon+"@2x.png";
                        Picasso.get().load(iconUrl).into(imageViewWeatherIcon);
                    });
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // 2) 5-DAY FORECAST
    private void fetch5DayForecast(String city) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?q="
                + city + "&units=metric&appid=" + API_KEY;
        Request req = new Request.Builder().url(url).build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> Toast.makeText(
                        WeatherActivity.this, "Forecast fetch failed", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response res) throws IOException {
                String s = res.body().string();
                if (!res.isSuccessful()) return;

                try {
                    JSONObject root = new JSONObject(s);
                    JSONArray list = root.getJSONArray("list");
                    // we want one forecast per day at ~12:00:00
                    Map<String, JSONObject> daily = new LinkedHashMap<>();
                    SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    SimpleDateFormat outDay = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject entry = list.getJSONObject(i);
                        String dtTxt = entry.getString("dt_txt");
                        Date d = inFmt.parse(dtTxt);
                        String dayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(d);
                        String hh = new SimpleDateFormat("HH", Locale.US).format(d);
                        if (hh.equals("12") && daily.size() < 5) {
                            daily.put(dayKey, entry);
                        }
                    }

                    // Convert to arrays
                    List<JSONObject> days = new ArrayList<>(daily.values());
                    mainHandler.post(() -> {
                        for (int i = 0; i < days.size() && i < 5; i++) {
                            try {
                                JSONObject e = days.get(i);
                                JSONObject m = e.getJSONObject("main");
                                double min = m.getDouble("temp_min"),
                                        max = m.getDouble("temp_max");
                                String dtTxt = e.getString("dt_txt");
                                Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(dtTxt);
                                String dayStr = outDay.format(d);
                                JSONObject w = e.getJSONArray("weather").getJSONObject(0);
                                String icon = w.getString("icon");

                                fDates[i].setText(dayStr);
                                fMins[i].setText("Min: "+Math.round(min)+"°C");
                                fMaxs[i].setText("Max: "+Math.round(max)+"°C");
                                String iconUrl = "https://openweathermap.org/img/wn/"+icon+"@2x.png";
                                Picasso.get().load(iconUrl).into(fIcons[i]);
                            } catch (Exception ex) { ex.printStackTrace(); }
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
