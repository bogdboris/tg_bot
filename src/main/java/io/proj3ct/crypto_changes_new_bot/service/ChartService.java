package io.proj3ct.crypto_changes_new_bot.service;

import io.proj3ct.crypto_changes_new_bot.exeption.ServiceException;

import java.io.File;
import java.io.IOException;

public interface ChartService {
    String getUSDExchangeRate() throws ServiceException;
    String getEURExchangeRate() throws ServiceException;
    String getGBPExchangeRate() throws ServiceException;
    String getJPYExchangeRate() throws ServiceException;
    String getCNYExchangeRate() throws ServiceException;
    File createChart(String symbol, String interval, String outputsize) throws ServiceException, IOException;
    String getForecast(String currencyCode) throws ServiceException;
    File createSMAChart(String symbol, String interval, int timePeriod, String seriesType) throws ServiceException, IOException;
}
