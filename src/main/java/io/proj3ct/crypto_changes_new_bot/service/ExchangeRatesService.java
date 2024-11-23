package io.proj3ct.crypto_changes_new_bot.service;
import io.proj3ct.crypto_changes_new_bot.exeption.ServiceException;

public interface ExchangeRatesService {

    String getUSDExchangeRate() throws ServiceException;

    String getEURExchangeRate() throws ServiceException;

}
