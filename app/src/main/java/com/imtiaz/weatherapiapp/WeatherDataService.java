package com.imtiaz.weatherapiapp;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class WeatherDataService {

    public static final String QUERY_FOR_CITY_ID = "https://www.metaweather.com/api/location/search/?query=";
    public static final String QUERY_FOR_WEATHER_BY_ID = "https://www.metaweather.com/api/location/";
    Context context;
    String cityId;


    public WeatherDataService(Context context) {
        this.context = context;
    }


    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(String cityId);
    }


    public void getCityId(String cityName, VolleyResponseListener volleyResponseListener) {

        String url = QUERY_FOR_CITY_ID + cityName;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                cityId = "";
                try {
                    JSONObject cityInfo = response.getJSONObject(0);
                    cityId = cityInfo.getString("woeid");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                volleyResponseListener.onResponse(cityId);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError("Something Error");
            }
        });
        // Add a request in RequestQueue.
        MySingleton.getInstance(context).addToRequestQueue(request);
    }


    public interface ForCstByIdResponse {
        void onError(String message);

        void onResponse(List<WeatherReportModel> weatherReportModel);
    }

    public void getCityForecastById(String cityId, ForCstByIdResponse forCstByIdResponse) {
        List<WeatherReportModel> weatherReportModels = new ArrayList<>();

        String url = QUERY_FOR_WEATHER_BY_ID + cityId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray consolidated_weather_list = response.getJSONArray("consolidated_weather");
                    for (int i = 0; i < consolidated_weather_list.length(); i++) {

                        WeatherReportModel one_day = new WeatherReportModel();
                        JSONObject first_day_from_api = (JSONObject) consolidated_weather_list.get(i);

                        one_day.setId(first_day_from_api.getInt("id"));
                        one_day.setWeather_state_name(first_day_from_api.getString("weather_state_name"));
                        one_day.setWeather_state_abbr(first_day_from_api.getString("weather_state_abbr"));
                        one_day.setWind_direction_compass(first_day_from_api.getString("wind_direction_compass"));
                        one_day.setCreated(first_day_from_api.getString("created"));
                        one_day.setApplicable_date(first_day_from_api.getString("applicable_date"));
                        one_day.setMin_temp(first_day_from_api.getString("min_temp"));
                        one_day.setMax_temp(first_day_from_api.getString("max_temp"));
                        one_day.setThe_temp(first_day_from_api.getString("the_temp"));
                        one_day.setWind_speed(first_day_from_api.getString("wind_speed"));
                        one_day.setWind_direction(first_day_from_api.getString("wind_direction"));
                        one_day.setAir_pressure(first_day_from_api.getString("air_pressure"));
                        one_day.setHumidity(first_day_from_api.getString("humidity"));
                        one_day.setVisibility(first_day_from_api.getString("visibility"));
                        one_day.setPredictability(first_day_from_api.getString("predictability"));
                        weatherReportModels.add(one_day);
                    }
                    forCstByIdResponse.onResponse(weatherReportModels);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
        // Add a request in RequestQueue.
        MySingleton.getInstance(context).addToRequestQueue(request);
    }


    public interface GetCityForCastByNameCallback {
        void onError(String message);

        void onResponse(List<WeatherReportModel> weatherReportModel);
    }

    public void getCityForecastByName(String cityName, GetCityForCastByNameCallback getCityForCastByNameCallback) {

        // fetch the city id given the city name
        getCityId(cityName, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String cityId) {
                getCityForecastById(cityId, new ForCstByIdResponse() {
                    @Override
                    public void onError(String message) {
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(List<WeatherReportModel> weatherReportModel) {
                        getCityForCastByNameCallback.onResponse(weatherReportModel);
                    }
                });
            }
        });
    }


}
