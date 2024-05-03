package com.bank.light.services;

import com.bank.light.exceptions.GatewayException;
import com.bank.light.interfaces.GatewayService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class GatewayServiceImpl implements GatewayService {

    private final Map<String, Double> lastCurrencies = new ConcurrentHashMap<>();

    // hour of last currencies request
    private int requestHour = -1;

    public Map<String, Double> getCurrencies() {
        int hour = LocalTime.now().getHour();
        if (hour != requestHour) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request =
                    HttpRequest.newBuilder(URI.create("https://www.cbr-xml-daily.ru/daily_json.js")).build();
            HttpResponse<String> response;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new GatewayException(e.getMessage());
            }
            String body = response.body();
            JSONObject currencies = new JSONObject(body).getJSONObject("Valute");
            lastCurrencies.put("eur", currencies.getJSONObject("EUR").getDouble("Value"));
            lastCurrencies.put("usd", currencies.getJSONObject("USD").getDouble("Value"));
            requestHour = hour;
        }
        return lastCurrencies;
    }
}
