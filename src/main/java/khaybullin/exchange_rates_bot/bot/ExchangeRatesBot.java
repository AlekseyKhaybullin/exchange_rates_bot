package khaybullin.exchange_rates_bot.bot;

import khaybullin.exchange_rates_bot.exception.ServiceException;
import khaybullin.exchange_rates_bot.service.ExchangeRatesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;


@Component
public class ExchangeRatesBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRatesBot.class);

    private static final String START = "/start";
    private static final String HELP = "/help";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";

    @Autowired
    private ExchangeRatesService exchangeRatesService;

    public ExchangeRatesBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        var message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();
        switch (message) {
            case START -> {
                String userName = update.getMessage().getChat().getUserName();
                startCommand(chatId, userName);
            }
            case USD -> usdCommand(chatId);
            case EUR -> eurCommand(chatId);
            case HELP -> helpCommand(chatId);
            default -> unknownCommand(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return "khay_change_bot";
    }

    private void startCommand(Long chatId, String userName) {
        var text = """
                Добро пожаловать в мой самый лучший бот, %s!
                Здесь ты сможешь узнать курс валюты на сегодня!
                                
                Для этого воспользуйся командами:
                                
                /usd - курс доллара
                /eur - курс евро
                                
                Дополнительные команды:
                /help - помощь
                """;

        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void usdCommand(Long chatId) {
        String formattedText;
        try {
            var usd = exchangeRatesService.getUSDExchangeRate();
            var text = "Курс доллара на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), usd);
        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса доллара", e);
            formattedText = "Не удалось получить текущий курс доллара. Попробуй позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void eurCommand(Long chatId) {
        String formattedText;
        try {
            var eur = exchangeRatesService.getEURExchangeRate();
            var text = "Курс евро на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), eur);
        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса евро", e);
            formattedText = "Не удалось получить текущий курс евро. Попробуй позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void helpCommand(Long chatId) {
        var text = """
                Справочная информация по боту
                                
                Для получения текущих курсов валют воспользуйтесь командами:
                /usd - курс доллара
                /eur - курс евро
                """;
        sendMessage(chatId, text);
    }

    public void unknownCommand(Long chatId){
        var text = "Не удалось распознать команду";
        sendMessage(chatId, text);
    }

    private void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }
}
