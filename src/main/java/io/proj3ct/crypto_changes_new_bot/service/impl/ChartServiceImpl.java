package io.proj3ct.crypto_changes_new_bot.service.impl;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import io.proj3ct.crypto_changes_new_bot.client.CurrencyClient;
import io.proj3ct.crypto_changes_new_bot.exeption.ServiceException;
import io.proj3ct.crypto_changes_new_bot.service.ChartService;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ChartServiceImpl implements ChartService {

    private static final Logger LOG = LoggerFactory.getLogger(ChartServiceImpl.class);

    private static final String USD_XPATH = "/ValCurs//Valute[@ID='R01235']/Value";
    private static final String EUR_XPATH = "/ValCurs//Valute[@ID='R01239']/Value";
    private static final String GBP_XPATH = "/ValCurs//Valute[@ID='R01035']/Value";
    private static final String JPY_XPATH = "/ValCurs//Valute[@ID='R01820']/Value";
    private static final String CNY_XPATH = "/ValCurs//Valute[@ID='R01375']/Value";

    @Autowired
    private CurrencyClient currencyClient;

    @Override
    public String getUSDExchangeRate() throws ServiceException {
        var xmlOptional = currencyClient.getCurrencyRatesXML();
        String xml = xmlOptional.orElseThrow(
                () -> new ServiceException("Не удалось получить XML")
        );
        return extractCurrencyValueFromXML(xml, USD_XPATH);
    }

    @Override
    public String getEURExchangeRate() throws ServiceException {
        var xmlOptional = currencyClient.getCurrencyRatesXML();
        String xml = xmlOptional.orElseThrow(
                () -> new ServiceException("Не удалось получить XML")
        );
        return extractCurrencyValueFromXML(xml, EUR_XPATH);
    }

    @Override
    public String getGBPExchangeRate() throws ServiceException {
        var xmlOptional = currencyClient.getCurrencyRatesXML();
        String xml = xmlOptional.orElseThrow(
                () -> new ServiceException("Не удалось получить XML")
        );
        return extractCurrencyValueFromXML(xml, GBP_XPATH);
    }

    @Override
    public String getJPYExchangeRate() throws ServiceException {
        var xmlOptional = currencyClient.getCurrencyRatesXML();
        String xml = xmlOptional.orElseThrow(
                () -> new ServiceException("Не удалось получить XML")
        );
        return extractCurrencyValueFromXML(xml, JPY_XPATH);
    }

    @Override
    public String getCNYExchangeRate() throws ServiceException {
        var xmlOptional = currencyClient.getCurrencyRatesXML();
        String xml = xmlOptional.orElseThrow(
                () -> new ServiceException("Не удалось получить XML")
        );
        String cnyRate = extractCurrencyValueFromXML(xml, CNY_XPATH);
        // Замена запятой на точку
        cnyRate = cnyRate.replace(",", ".");
        // Умножаем курс на 10, так как ЦБ РФ предоставляет курс за 10 юаней
        return String.format("%.4f", Double.parseDouble(cnyRate) * 10);
    }

    private static String extractCurrencyValueFromXML(String xml, String xpathExpression)
            throws ServiceException {
        var source = new InputSource(new StringReader(xml));
        try {
            var xpath = XPathFactory.newInstance().newXPath();
            var document = (Document) xpath.evaluate("/", source, XPathConstants.NODE);
            return xpath.evaluate(xpathExpression, document);
        } catch (XPathExpressionException e) {
            throw new ServiceException("Не удалось распарсить XML", e);
        }
    }

    @Override
    public File createChart(String symbol, String interval, String outputsize) throws ServiceException, IOException {
        String data = currencyClient.getHistoricalData(symbol, interval, outputsize);
        LOG.info("Received data for chart: {}", data); // Логирование полученных данных

        // Парсинг данных и создание датасета
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> parsedData = parseData(data);

        // Ограничение количества данных для отображения
        int maxDataPoints = 100; // Ограничим количество точек данных до 100
        int count = 0;
        for (Map.Entry<String, Double> entry : parsedData.entrySet()) {
            if (count >= maxDataPoints) break;
            dataset.addValue(entry.getValue(), symbol, entry.getKey());
            count++;
        }

        // Создание графика
        JFreeChart chart = ChartFactory.createLineChart(
                symbol + " Exchange Rate",
                "Time",
                "Rate",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Настройка осей
        CategoryPlot plot = chart.getCategoryPlot();
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // Наклон меток времени

        // Сохранение графика в файл
        File chartFile = File.createTempFile(symbol, ".png");
        LOG.info("Chart file created: {}", chartFile.getAbsolutePath()); // Логирование пути к файлу графика
        ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600); // Увеличение размера графика
        return chartFile;
    }

    @Override
    public File createSMAChart(String symbol, String interval, int timePeriod, String seriesType) throws ServiceException, IOException {
        // Получение данных для обычного графика
        String historicalData = currencyClient.getHistoricalData(symbol, interval, "compact");
        LOG.info("Received historical data for SMA chart: {}", historicalData); // Логирование полученных данных
        Map<String, Double> historicalParsedData = parseData(historicalData);

        // Получение данных для графика SMA
        String smaData = currencyClient.getSMAData(symbol, interval, timePeriod, seriesType);
        LOG.info("Received SMA data for SMA chart: {}", smaData); // Логирование полученных данных
        Map<String, Double> smaParsedData = parseSMAData(smaData);

        // Создание датасета
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Добавление данных для обычного графика
        for (Map.Entry<String, Double> entry : historicalParsedData.entrySet()) {
            if (smaParsedData.containsKey(entry.getKey())) {
                dataset.addValue(entry.getValue(), symbol, entry.getKey());
            }
        }

        // Добавление данных для графика SMA
        for (Map.Entry<String, Double> entry : smaParsedData.entrySet()) {
            dataset.addValue(entry.getValue(), "SMA", entry.getKey());
        }

        // Создание графика
        JFreeChart chart = ChartFactory.createLineChart(
                symbol + " Exchange Rate with SMA",
                "Time",
                "Rate",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Настройка осей
        CategoryPlot plot = chart.getCategoryPlot();
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // Наклон меток времени

        // Настройка цвета для графика SMA
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(1, java.awt.Color.RED); // Установка красного цвета для графика SMA

        // Сохранение графика в файл
        File chartFile = File.createTempFile(symbol, ".png");
        LOG.info("SMA Chart file created: {}", chartFile.getAbsolutePath()); // Логирование пути к файлу графика
        ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600); // Увеличение размера графика
        return chartFile;
    }

    private Map<String, Double> parseData(String data) {
        Map<String, Double> result = new TreeMap<>();
        JSONObject jsonObject = new JSONObject(data);

        // Проверка наличия ключа "Meta Data" и извлечение интервала
        if (jsonObject.has("Meta Data")) {
            JSONObject metaData = jsonObject.getJSONObject("Meta Data");
            String interval = metaData.optString("4. Interval", "5min"); // Использование optString для безопасного извлечения
            String timeSeriesKey = "Time Series (" + interval + ")";

            if (jsonObject.has(timeSeriesKey)) {
                JSONObject timeSeries = jsonObject.getJSONObject(timeSeriesKey);
                for (String key : timeSeries.keySet()) {
                    JSONObject value = timeSeries.getJSONObject(key);
                    result.put(key, value.getDouble("1. open"));
                }
            } else {
                LOG.error("Time Series key not found: {}", timeSeriesKey);
            }
        } else {
            LOG.error("Meta Data not found in JSON response");
        }

        return result;
    }

    private Map<String, Double> parseSMAData(String data) {
        Map<String, Double> result = new TreeMap<>();
        JSONObject jsonObject = new JSONObject(data);

        if (jsonObject.has("Technical Analysis: SMA")) {
            JSONObject smaData = jsonObject.getJSONObject("Technical Analysis: SMA");
            for (String key : smaData.keySet()) {
                JSONObject value = smaData.getJSONObject(key);
                result.put(key, value.getDouble("SMA"));
            }
        } else {
            LOG.error("SMA data not found in JSON response");
        }

        return result;
    }

    @Override
    public String getForecast(String currencyCode) throws ServiceException {
        return currencyClient.getForecast(currencyCode);
    }

    public String compareCurrentPriceWithSMA(String symbol) throws ServiceException, IOException {
        String historicalData = currencyClient.getHistoricalData(symbol, "5min", "compact");
        Map<String, Double> historicalParsedData = parseData(historicalData);

        String smaData = currencyClient.getSMAData(symbol, "5min", 10, "open");
        Map<String, Double> smaParsedData = parseSMAData(smaData);

        // Получаем последнюю цену из исторических данных
        double lastPrice = historicalParsedData.values().stream().reduce((first, second) -> second).orElse(0.0);

        // Получаем последнюю SMA
        double lastSMA = smaParsedData.values().stream().reduce((first, second) -> second).orElse(0.0);

        if (lastPrice > lastSMA) {
            return "Текущая цена " + symbol + " выше, чем SMA.";
        } else {
            return "Текущая цена " + symbol + " ниже, чем SMA.";
        }
    }

    @Override
    public String getRSIStatus(String symbol) throws ServiceException, IOException {
        String rsiData = currencyClient.getRSIData(symbol, "weekly", 10, "open");
        JSONObject jsonObject = new JSONObject(rsiData);

        if (jsonObject.has("Technical Analysis: RSI")) {
            JSONObject rsiDataObject = jsonObject.getJSONObject("Technical Analysis: RSI");
            double lastRSI = 0.0;
            for (String key : rsiDataObject.keySet()) {
                JSONObject value = rsiDataObject.getJSONObject(key);
                lastRSI = value.getDouble("RSI");
            }

            if (lastRSI >= 70) {
                return symbol + " находится в состоянии перекупленности " + ": " + lastRSI;
            } else if (lastRSI <= 30) {
                return symbol + " находится в зоне скидок " + ": " + lastRSI;
            } else {
                return symbol + " находится в оптимальной зоне " + ": " + lastRSI;
            }
        } else {
            LOG.error("RSI data not found in JSON response");
            throw new ServiceException("RSI data not found");
        }
    }
}
