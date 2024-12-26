package com.example.weather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "af26e61196184faaa3b81503240711";
    private static final String BASE_URL = "https://api.weatherapi.com/v1/";
    private FusedLocationProviderClient fusedLocationClient;

    private EditText etLocation;
    private TextView tvTemperature, tvCondition, tvFeelsLike, tvHumidity, tvWind, tvSunrise, tvSunset, tvForecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация элементов
        etLocation = findViewById(R.id.etLocation);
        Button btnGetWeather = findViewById(R.id.btnGetWeather);
        RadioGroup radioGroupTemperature = findViewById(R.id.radioGroupTemperature);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCondition = findViewById(R.id.tvCondition);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvSunrise = findViewById(R.id.tvSunrise);
        tvSunset = findViewById(R.id.tvSunset);
        tvForecast = findViewById(R.id.tvForecast);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        btnGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = etLocation.getText().toString();
                if (!location.isEmpty()) {
                    getWeather(location);
                } else {
                    Toast.makeText(MainActivity.this, "Введите город", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            getWeather(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
    }

    private void getWeather(String location) {
        String url = BASE_URL + "current.json?key=" + API_KEY + "&q=" + location;
        fetchWeatherData(url);
    }

    private void getWeather(double latitude, double longitude) {
        String url = BASE_URL + "current.json?key=" + API_KEY + "&q=" + latitude + "," + longitude;
        fetchWeatherData(url);
    }

    private void fetchWeatherData(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject current = response.getJSONObject("current");
                            double temperatureC = current.getDouble("temp_c");
                            double temperatureF = current.getDouble("temp_f");

                            // Получение выбранной единицы измерения
                            int selectedId = ((RadioGroup) findViewById(R.id.radioGroupTemperature)).getCheckedRadioButtonId();
                            String temperature;
                            if (selectedId == R.id.radioCelsius) {
                                temperature = temperatureC + " °C";
                            } else {
                                temperature = temperatureF + " °F";
                            }

                            tvTemperature.setText(temperature);
                            tvCondition.setText(current.getJSONObject("condition").getString("text"));
                            tvFeelsLike.setText("Ощущается как: " + current.getDouble("feelslike_c") + " °C");
                            tvHumidity.setText("Влажность: " + current.getInt("humidity") + "%");
                            tvWind.setText("Ветер: " + current.getDouble("wind_kph") + " км/ч");
                            tvSunrise.setText("Восход: " + response.getJSONObject("location").getString("localtime"));
                            tvSunset.setText("Закат: " + response.getJSONObject("location").getString("localtime"));
                            // Добавьте код для прогноза, если необходимо
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Ошибка получения данных", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonObjectRequest);
    }

    private void getForecast(String location) {
        String url = BASE_URL + "forecast.json?key=" + API_KEY + "&q=" + location + "&days=7";
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray forecastDays = response.getJSONObject("forecast").getJSONArray("forecastday");
                            StringBuilder forecastBuilder = new StringBuilder("Прогноз на 7 дней:\n");
                            for (int i = 0; i < forecastDays.length(); i++) {
                                JSONObject day = forecastDays.getJSONObject(i);
                                String date = day.getString("date");
                                JSONObject dayInfo = day.getJSONObject("day");
                                String maxTemp = dayInfo.getString("maxtemp_c") + " °C";
                                String minTemp = dayInfo.getString("mintemp_c") + " °C";
                                String condition = dayInfo.getJSONObject("condition").getString("text");
                                forecastBuilder.append(date).append(": ").append(maxTemp).append(" / ").append(minTemp).append(" - ").append(condition).append("\n");
                            }
                            tvForecast.setText(forecastBuilder.toString());
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "Ошибка парсинга прогноза", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Разрешение на доступ к местоположению необходимо для работы приложения", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
