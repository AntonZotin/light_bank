package com.bank.light.services;

import com.bank.light.exceptions.GatewayException;
import com.bank.light.interfaces.GatewayService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GatewayServiceImpl implements GatewayService {

    private final Map<String, Double> lastCurrencies = new ConcurrentHashMap<>();

    public GatewayServiceImpl() {
        this.updateCurrenciesAsync();
    }

    @Async
    @Scheduled(cron = "0 0 * * * *")
    public void updateCurrenciesAsync() {
        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder(URI.create("https://www.cbr-xml-daily.ru/daily_json.js")).build();
        final HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new GatewayException(e.getMessage());
        }
        final JSONObject currencies = new JSONObject(response.body()).getJSONObject("Valute");
        lastCurrencies.put("eur", currencies.getJSONObject("EUR").getDouble("Value"));
        lastCurrencies.put("usd", currencies.getJSONObject("USD").getDouble("Value"));
    }

    @Override
    public Map<String, Double> getCurrencies() {
        return this.lastCurrencies;
    }
}
