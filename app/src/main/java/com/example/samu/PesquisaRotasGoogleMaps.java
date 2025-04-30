package com.example.samu;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PesquisaRotasGoogleMaps {
    private static final String TAG = "PesquisaGoogleMaps";
    private static final String API_KEY = "AIzaSyDdxlVIO3mbaFrf9g3PxnJWqL0WiJBG578";
    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";

    private Context context;
    private PesquisaCompleteListener listener;

    public interface PesquisaCompleteListener {
        void onPesquisaCompleta(List<ApresentarResultados.Resultado> resultados);
        void onPesquisaErro(String mensagem);
    }

    public PesquisaRotasGoogleMaps(Context context, PesquisaCompleteListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void pesquisarRotas(String origem, String destino) {
        String url = String.format("%s?origin=%s&destination=%s&key=%s&mode=transit",
                DIRECTIONS_API_URL,
                origem.replace(" ", "+"), // Formatar para URL
                destino.replace(" ", "+"), // Formatar para URL
                API_KEY
        );

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        List<ApresentarResultados.Resultado> resultados = processarResultados(response);
                        listener.onPesquisaCompleta(resultados);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Erro na requisição: " + error.getMessage());
                        listener.onPesquisaErro("Erro ao pesquisar rotas.");
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private List<ApresentarResultados.Resultado> processarResultados(JSONObject response) {
        List<ApresentarResultados.Resultado> resultados = new ArrayList<>();
        try {
            String status = response.getString("status");
            if (status.equals("OK")) {
                JSONArray routes = response.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONArray legs = route.getJSONArray("legs");
                    if (legs.length() > 0) {
                        JSONObject leg = legs.getJSONObject(0);
                        JSONArray steps = leg.getJSONArray("steps");

                        for (int i = 0; i < steps.length(); i++) {
                            JSONObject step = steps.getJSONObject(i);
                            String travelMode = step.getString("travel_mode");
                            JSONObject duration = step.getJSONObject("duration");
                            String durationText = duration.getString("text");
                            // JSONObject distance = step.getJSONObject("distance");
                            // String distanceText = distance.getString("text");

                            String tipoTransporte = "";
                            if (travelMode.equals("TRANSIT")) {
                                JSONObject transitDetails = step.getJSONObject("transit_details");
                                JSONObject line = transitDetails.getJSONObject("line");
                                String vehicleType = line.getJSONObject("vehicle").getString("type");
                                if (vehicleType.equals("BUS")) {
                                    tipoTransporte = "autocarro";
                                } else if (vehicleType.equals("SUBWAY")) {
                                    tipoTransporte = "metro";
                                } else if (vehicleType.equals("TRAIN")) {
                                    tipoTransporte = "comboio";
                                } else {
                                    tipoTransporte = vehicleType.toLowerCase(); // Outros tipos de transporte público
                                }
                                // O preço para transporte público geralmente não é fornecido diretamente nesta API
                                resultados.add(new ApresentarResultados.Resultado(tipoTransporte, 0.0, durationText)); // Preço -1 indica não disponível
                            } else if (travelMode.equals("DRIVING")) {
                                resultados.add(new ApresentarResultados.Resultado("carro", 0.0, durationText)); // Preço não disponível
                            } else if (travelMode.equals("WALKING")) {
                                resultados.add(new ApresentarResultados.Resultado("pé", 0.0, durationText));
                            }
                        }
                    }
                } else {
                    listener.onPesquisaErro("Nenhuma rota encontrada.");
                }
            } else {
                String errorMessage = response.getString("error_message");
                Log.e(TAG, "Erro na API do Google Maps: " + errorMessage);
                listener.onPesquisaErro("Erro ao obter rotas: " + errorMessage);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Erro ao processar JSON: " + e.getMessage());
            listener.onPesquisaErro("Erro ao processar os resultados da pesquisa.");
        }
        return resultados;
    }
}