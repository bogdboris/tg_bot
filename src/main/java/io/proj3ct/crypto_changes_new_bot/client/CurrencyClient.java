package io.proj3ct.crypto_changes_new_bot.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.proj3ct.crypto_changes_new_bot.exeption.ServiceException;

import java.io.IOException;
import java.util.Optional;

@Component
public class CurrencyClient {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyClient.class);

    @Autowired
    private OkHttpClient client;

    @Value("${cbr.currency.rates.xml.url}")
    private String cbrCurrencyRatesXmlUrl;

    @Value("${api.key}")
    private String apiKey;

    private static final String RAMBLER_URL_TEMPLATE = "https://finance.rambler.ru/currencies/consensus/%s/";
    private static final String BANKI_URL = "https://www.banki.ru/products/currency/usd/";

    public Optional<String> getCurrencyRatesXML() throws ServiceException {
        var request = new Request.Builder()
                .url(cbrCurrencyRatesXmlUrl)
                .build();

        try (var response = client.newCall(request).execute()) {
            var body = response.body();
            return body == null ? Optional.empty() : Optional.of(body.string());
        } catch (IOException e) {
            throw new ServiceException("Ошибка получения курсов валют от ЦБ РФ", e);
        }
    }

    public String getHistoricalData(String symbol, String interval, String outputsize) throws ServiceException {
        String url = String.format("https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=%s&apikey=%s&outputsize=%s",
                symbol, interval, apiKey, outputsize);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ServiceException("Ошибка получения исторических данных: " + response.message());
            }
            return response.body().string();
        } catch (IOException e) {
            throw new ServiceException("Ошибка получения исторических данных", e);
        }
    }

    public String getForecast(String currencyCode) throws ServiceException {
        String url = String.format(RAMBLER_URL_TEMPLATE, currencyCode);
        try {
            Document document = Jsoup.connect(url).get();
            LOG.info("HTML content: {}", document.html()); // Логирование HTML-кода страницы для отладки

            // Попытка найти элемент прогноза с использованием обновленного селектора
            Element forecastElement = document.selectFirst(".xPIn_5A3");
            if (forecastElement != null) {
                return forecastElement.text();
            } else {
                throw new ServiceException("Forecast element not found");
            }
        } catch (IOException e) {
            throw new ServiceException("Error fetching forecast data", e);
        }
    }

    public String getSMAData(String symbol, String interval, int timePeriod, String seriesType) throws ServiceException {
        String url = String.format("https://www.alphavantage.co/query?function=SMA&symbol=%s&interval=%s&time_period=%d&series_type=%s&apikey=%s",
                symbol, interval, timePeriod, seriesType, apiKey);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ServiceException("Ошибка получения данных SMA: " + response.message());
            }
            return response.body().string();
        } catch (IOException e) {
            throw new ServiceException("Ошибка получения данных SMA", e);
        }
    }

    public String getCurrencyChange() throws ServiceException {
        try {
            Document document = Jsoup.connect(BANKI_URL).get();
            Element changeElement = document.selectFirst(".bpFNho");
            if (changeElement != null) {
                return changeElement.text();
            } else {
                throw new ServiceException("Change element not found");
            }
        } catch (IOException e) {
            throw new ServiceException("Error fetching currency change data", e);
        }
    }

    public String getRSIData(String symbol, String interval, int timePeriod, String seriesType) throws ServiceException {
        String url = String.format("https://www.alphavantage.co/query?function=RSI&symbol=%s&interval=%s&time_period=%d&series_type=%s&apikey=%s",
                symbol, interval, timePeriod, seriesType, apiKey);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ServiceException("Ошибка получения данных RSI: " + response.message());
            }
            return response.body().string();
        } catch (IOException e) {
            throw new ServiceException("Ошибка получения данных RSI", e);
        }
    }
}
