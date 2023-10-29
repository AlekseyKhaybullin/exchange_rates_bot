package khaybullin.exchange_rates_bot.service;

import khaybullin.exchange_rates_bot.exception.ServiceException;

public interface ExchangeRatesService {

    String getUSDExchangeRate() throws ServiceException;
    String getEURExchangeRate() throws ServiceException;

}
