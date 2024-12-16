package io.proj3ct.crypto_changes_new_bot.bot;

import io.proj3ct.crypto_changes_new_bot.client.CurrencyClient;
import io.proj3ct.crypto_changes_new_bot.exeption.ServiceException;
import io.proj3ct.crypto_changes_new_bot.service.ChartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@Component
public class ExchangeRatesBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRatesBot.class);

    private static final String START = "/start";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";
    private static final String GBP = "/gbp";
    private static final String JPY = "/jpy";
    private static final String CNY = "/cny";
    private static final String HELP = "/help";

    @Autowired
    private ChartService chartService;

    @Autowired
    private CurrencyClient currencyClient;

    @Value("${bot.token}")
    private String botToken;

    @Value("${chat.id}")
    private Long chatId;

    public ExchangeRatesBot(@Value("${bot.token}") String botToken) {
        super(botToken);
        this.botToken = botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        switch (message) {
            case START -> {
                String userName = update.getMessage().getChat().getUserName();
                startCommand(chatId, userName);
            }
            case USD -> usdCommand(chatId);
            case EUR -> eurCommand(chatId);
            case GBP -> gbpCommand(chatId);
            case JPY -> jpyCommand(chatId);
            case CNY -> cnyCommand(chatId);
            case HELP -> helpCommand(chatId);
            default -> unknownCommand(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return "crypto_changes_new_bot";
    }

    private void startCommand(Long chatId, String userName) {
        var text = """
                 Здесь можно узнать курсы валют на сегодня.
                 Команды:
                 /usd - курс доллара
                 /eur - курс евро
                 /gbp - курс фунта стерлингов
                 /jpy - курс иены
                 /cny - курс юаня
                 /help - получение справки
                 """;
        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void usdCommand(Long chatId) {
        String formattedText;
        try {
            var usd = chartService.getUSDExchangeRate();
            var text = "Курс доллара на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), usd);
            sendMessage(chatId, formattedText);

            File chartFile = chartService.createChart("USD", "5min", "compact");
            sendPhoto(chatId, chartFile);

            String comparisonResult = chartService.compareCurrentPriceWithSMA("USD");
            sendMessage(chatId, comparisonResult);

            String rsiStatus = chartService.getRSIStatus("USD");
            sendMessage(chatId, rsiStatus);

            try {
                String forecast = chartService.getForecast("USD");
                sendMessage(chatId, "Прогноз курса доллара: " + forecast);
            } catch (ServiceException e) {
                LOG.error("Ошибка получения прогноза курса доллара", e);
                sendMessage(chatId, "Не удалось получить прогноз курса доллара. Попробуйте позже.");
            }
        } catch (ServiceException | IOException e) {
            LOG.error("Ошибка получения курса доллара или создания графика", e);
            formattedText = "Не удалось получить текущий курс доллара или создать график. Попробуйте позже.";
            sendMessage(chatId, formattedText);
        }
    }

    private void eurCommand(Long chatId) {
        String formattedText;
        try {
            var eur = chartService.getEURExchangeRate();
            var text = "Курс евро на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), eur);
            sendMessage(chatId, formattedText);

            File chartFile = chartService.createChart("EUR", "5min", "compact");
            sendPhoto(chatId, chartFile);

            String comparisonResult = chartService.compareCurrentPriceWithSMA("EUR");
            sendMessage(chatId, comparisonResult);

            String rsiStatus = chartService.getRSIStatus("EUR");
            sendMessage(chatId, rsiStatus);

            try {
                String forecast = chartService.getForecast("EUR");
                sendMessage(chatId, "Прогноз курса евро: " + forecast);
            } catch (ServiceException e) {
                LOG.error("Ошибка получения прогноза курса евро", e);
                sendMessage(chatId, "Не удалось получить прогноз курса евро. Попробуйте позже.");
            }
        } catch (ServiceException | IOException e) {
            LOG.error("Ошибка получения курса евро или создания графика", e);
            formattedText = "Не удалось получить текущий курс евро или создать график. Попробуйте позже.";
            sendMessage(chatId, formattedText);
        }
    }

    private void gbpCommand(Long chatId) {
        String formattedText;
        try {
            var gbp = chartService.getGBPExchangeRate();
            var text = "Курс фунта стерлингов на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), gbp);
            sendMessage(chatId, formattedText);

            File chartFile = chartService.createChart("GBP", "5min", "compact");
            sendPhoto(chatId, chartFile);

            String comparisonResult = chartService.compareCurrentPriceWithSMA("GBP");
            sendMessage(chatId, comparisonResult);

            String rsiStatus = chartService.getRSIStatus("GBP");
            sendMessage(chatId, rsiStatus);

            try {
                String forecast = chartService.getForecast("GBP");
                sendMessage(chatId, "Прогноз курса фунта стерлингов: " + forecast);
            } catch (ServiceException e) {
                LOG.error("Ошибка получения прогноза курса фунта стерлингов", e);
                sendMessage(chatId, "Не удалось получить прогноз курса фунта стерлингов. Попробуйте позже.");
            }
        } catch (ServiceException | IOException e) {
            LOG.error("Ошибка получения курса фунта стерлингов или создания графика", e);
            formattedText = "Не удалось получить текущий курс фунта стерлингов или создать график. Попробуйте позже.";
            sendMessage(chatId, formattedText);
        }
    }

    private void jpyCommand(Long chatId) {
        String formattedText;
        try {
            var jpy = chartService.getJPYExchangeRate();
            var text = "Курс иены на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), jpy);
            sendMessage(chatId, formattedText);

            File chartFile = chartService.createChart("JPY", "5min", "compact");
            sendPhoto(chatId, chartFile);

            String comparisonResult = chartService.compareCurrentPriceWithSMA("JPY");
            sendMessage(chatId, comparisonResult);

            String rsiStatus = chartService.getRSIStatus("JPY");
            sendMessage(chatId, rsiStatus);

            try {
                String forecast = chartService.getForecast("JPY");
                sendMessage(chatId, "Прогноз курса иены: " + forecast);
            } catch (ServiceException e) {
                LOG.error("Ошибка получения прогноза курса иены", e);
                sendMessage(chatId, "Не удалось получить прогноз курса иены. Попробуйте позже.");
            }
        } catch (ServiceException | IOException e) {
            LOG.error("Ошибка получения курса иены или создания графика", e);
            formattedText = "Не удалось получить текущий курс иены или создать график. Попробуйте позже.";
            sendMessage(chatId, formattedText);
        }
    }

    private void cnyCommand(Long chatId) {
        String formattedText;
        try {
            var cny = chartService.getCNYExchangeRate();
            var text = "Курс юаня на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), cny);
            sendMessage(chatId, formattedText);

            File chartFile = chartService.createChart("CNY", "5min", "compact");
            sendPhoto(chatId, chartFile);

            String comparisonResult = chartService.compareCurrentPriceWithSMA("CNY");
            sendMessage(chatId, comparisonResult);

            String rsiStatus = chartService.getRSIStatus("CNY");
            sendMessage(chatId, rsiStatus);

            try {
                String forecast = chartService.getForecast("CNY");
                sendMessage(chatId, "Прогноз курса юаня: " + forecast);
            } catch (ServiceException e) {
                LOG.error("Ошибка получения прогноза курса юаня", e);
                sendMessage(chatId, "Не удалось получить прогноз курса юаня. Попробуйте позже.");
            }
        } catch (ServiceException | IOException e) {
            LOG.error("Ошибка получения курса юаня или создания графика", e);
            formattedText = "Не удалось получить текущий курс юаня или создать график. Попробуйте позже.";
            sendMessage(chatId, formattedText);
        }
    }

    private void helpCommand(Long chatId) {
        var text = """
                 Используйте команды:
                 /usd - курс доллара
                 /eur - курс евро
                 /gbp - курс фунта стерлингов
                 /jpy - курс иены
                 /cny - курс юаня
                 """;
        sendMessage(chatId, text);
    }

    private void unknownCommand(Long chatId) {
        var text = "Не удалось распознать команду!";
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

    private void sendPhoto(Long chatId, File file) {
        var chatIdStr = String.valueOf(chatId);
        var sendPhoto = new SendPhoto(chatIdStr, new InputFile(file));
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки фото", e);
        }
    }

    @Scheduled(cron = "0 15 2 * *", zone = "Asia/Yekaterinburg")
    public void sendDailyCurrencyChange() {
        try {
            String currencyChange = currencyClient.getCurrencyChange();
            String message = "Изменение курса доллара за последний день: " + currencyChange;
            sendMessage(chatId, message);
        } catch (ServiceException e) {
            LOG.error("Ошибка получения изменения курса доллара", e);
        }
    }
}
