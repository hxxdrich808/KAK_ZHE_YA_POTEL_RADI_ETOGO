package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
    String NameID;
    String idPomesheniya;
    String name;
    String Time;
    String StartArenda;
    int forDateAndTime;
    final String botName;
    final String botToken;
    SendMessage messageToUser = new SendMessage();
    SendPhoto photoToUser = new SendPhoto();
    public Bot(String botName, String botToken) {
        this.botName = botName;
        this.botToken = botToken;
    }
    @Override
    public String getBotUsername() {
        return this.botName;
    }
    @Override
    public String getBotToken() {
        return this.botToken;
    }
    public void sendImageFromFileId(String fileId, String chatId) {
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(chatId);
        sendPhotoRequest.setPhoto(new InputFile(fileId));
        try {
            execute(sendPhotoRequest);
        } catch (TelegramApiException e) {}
    }
    public void setButtons(SendMessage sendMessage) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Каталог");
        inlineKeyboardButton1.setCallbackData("catalog");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        rowList.add(keyboardButtonsRow1);

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("Фильтры");
        inlineKeyboardButton2.setCallbackData("filters");
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow2.add(inlineKeyboardButton2);
        rowList.add(keyboardButtonsRow2);

        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        inlineKeyboardButton3.setText("Помощь");
        inlineKeyboardButton3.setCallbackData("help");
        List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();
        keyboardButtonsRow3.add(inlineKeyboardButton3);
        rowList.add(keyboardButtonsRow3);

        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton4.setText("Заказы");
        inlineKeyboardButton4.setCallbackData("orders");
        List<InlineKeyboardButton> keyboardButtonsRow4 = new ArrayList<>();
        keyboardButtonsRow4.add(inlineKeyboardButton4);
        rowList.add(keyboardButtonsRow4);

        inlineKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
    }
    Connection connection = null;
    @Override
    public void onUpdateReceived(Update update) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                try {
                    connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/arendapomesheniy", "postgres", "postgres");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                name = update.getMessage().getFrom().getFirstName();
                NameID = String.valueOf(update.getMessage().getFrom().getId());
                String chatId = update.getMessage().getChatId().toString();
                String message_text = update.getMessage().getText();
                messageToUser.setChatId(chatId);
                try{
                    switch (message_text){
                        case "/start" -> {
                            setButtons(messageToUser);
                            messageToUser.setText("Здравствуйте, что бы вы хотели сделать?");
                            execute(messageToUser);
                        }
                        case "Мудрость" ->{
                            messageToUser.setText("Если вы называете Java \"Явой\", то и слушаете вы не джаз, а язь.");
                            execute(messageToUser);
                        }
                        default -> {
                            messageToUser.setText("Нормально общайся, чумба-хрюк!");
                            execute(messageToUser);
                        }
                    }
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            try{
                if (update.hasCallbackQuery()){
                    String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
                    CallbackQuery callbackQuery = update.getCallbackQuery();
                    String callbackData = callbackQuery.getData();
                    switch (callbackData){
                        case ("catalog")->{
                            replyKey(callbackQuery);
                            queryy("SELECT * FROM \"houses\" ", chatId);
                        }
                        case ("filters")->{
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                            List<InlineKeyboardButton> row1 = new ArrayList<>();
                            row1.add(InlineKeyboardButton.builder()
                                    .text("Цена")
                                    .callbackData("Price")
                                    .build());
                            row1.add(InlineKeyboardButton.builder()
                                    .text("Срок")
                                    .callbackData("Srok")
                                    .build());
                            keyboard.add(row1);
                            inlineKeyboardMarkup.setKeyboard(keyboard);
                            messageToUser.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));
                            messageToUser.setReplyMarkup(inlineKeyboardMarkup);
                            messageToUser.setText("Выберите параметр для фильтра");
                            try {
                                execute(messageToUser);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                        case ("orders")->{
                            String sql = "SELECT * FROM \"orders\" WHERE \"clientid\" = ?";
                            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                                statement.setInt(1, Integer.parseInt(NameID));
                                ResultSet resultSet = statement.executeQuery();
                                replyKey(callbackQuery);
                                boolean hasOrders = false;
                                while (resultSet.next()) {
                                    hasOrders = true;
                                    int clientId = resultSet.getInt("clientid");
                                    int orderId = resultSet.getInt("orderid");
                                    int pomId = resultSet.getInt("houseid");
                                    int dayCountId = resultSet.getInt("rent_duration");
                                    String dayId = String.valueOf(resultSet.getDate("rent_date"));
                                    Time timeId = resultSet.getTime("rent_start_time");
                                    String message ="Имя: " + name + "\nid клиента: " + clientId + "\nid заказа: " + orderId + "\nid помещения: " + idPomesheniya + "\nКоличество дней: " + dayCountId + "\nДень начала аренды: " + dayId + "\nВремя начала аренды: " + timeId;
                                    messageToUser.setText(message);
                                    execute(messageToUser);
                                    sql = "SELECT * FROM \"houses\" WHERE \"id\" = ?";
                                    try (PreparedStatement statement2 = connection.prepareStatement(sql)) {
                                        statement2.setInt(1, Integer.parseInt(idPomesheniya));
                                        ResultSet resultSet2 = statement2.executeQuery();
                                        if (resultSet2.next()){
                                            String name = resultSet2.getString("Наименование");
                                            String description = resultSet2.getString("Описание");
                                            String Srok = resultSet2.getString("Срок");
                                            String price = resultSet2.getString("Цена");
                                            message =  "ID Помещения: " + idPomesheniya + "\nНазвание: " + name + "\nОписание: " + description + "\nСрок: " + Srok + "\nЦена: " + price;
                                            messageToUser.setText(message);
                                            setButtons(messageToUser);
                                            execute(messageToUser);
                                        }
                                    }
                                }
                                if (!hasOrders) {
                                    messageToUser.setText("Заказов не найдено(");
                                    execute(messageToUser);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        case ("help")->{
                            replyKey(callbackQuery);
                            messageToUser.setText("По всем вопросам обращаться сюда @averageseregafan");
                            execute(messageToUser);
                        }
                        case ("Srok")->{
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                            List<InlineKeyboardButton> row1 = new ArrayList<>();
                            row1.add(InlineKeyboardButton.builder()
                                    .text("Посуточно")
                                    .callbackData("Sutki")
                                    .build());
                            List<InlineKeyboardButton> row2 = new ArrayList<>();
                            row2.add(InlineKeyboardButton.builder()
                                    .text("На месяц")
                                    .callbackData("Month")
                                    .build());
                            List<InlineKeyboardButton> row3 = new ArrayList<>();
                            row2.add(InlineKeyboardButton.builder()
                                    .text("Почасовая аренда")
                                    .callbackData("Hour")
                                    .build());
                            keyboard.add(row1);
                            keyboard.add(row2);
                            keyboard.add(row3);
                            inlineKeyboardMarkup.setKeyboard(keyboard);
                            messageToUser.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));
                            messageToUser.setReplyMarkup(inlineKeyboardMarkup);
                            messageToUser.setText("Выберите срок интересующий срок аренды");
                            try {
                                execute(messageToUser);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                        case ("Sutki")->{
                            replyKey(callbackQuery);
                            queryy("SELECT * FROM houses WHERE Срок = 'Посуточно';",chatId);
                        }
                        case ("Month")->{
                            replyKey(callbackQuery);
                            queryy("SELECT * FROM houses WHERE Срок = 'На месяц';",chatId);
                        }
                        case ("Hour")->{
                            replyKey(callbackQuery);
                            queryy("SELECT * FROM houses WHERE Срок = 'Почасовая аренда';",chatId);
                        }
                        case ("Price")->{
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                            List<InlineKeyboardButton> row1 = new ArrayList<>();
                            row1.add(InlineKeyboardButton.builder()
                                    .text("До 10000")
                                    .callbackData("do10k")
                                    .build());
                            List<InlineKeyboardButton> row2 = new ArrayList<>();
                            row2.add(InlineKeyboardButton.builder()
                                    .text("До 20000")
                                    .callbackData("do20k")
                                    .build());
                            List<InlineKeyboardButton> row3 = new ArrayList<>();
                            row2.add(InlineKeyboardButton.builder()
                                    .text("До 30000")
                                    .callbackData("do30k")
                                    .build());
                            keyboard.add(row1);
                            keyboard.add(row2);
                            keyboard.add(row3);
                            inlineKeyboardMarkup.setKeyboard(keyboard);
                            messageToUser.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));
                            messageToUser.setReplyMarkup(inlineKeyboardMarkup);
                            messageToUser.setText("Выберите подходящий ценовой сегмент");
                            try {
                                execute(messageToUser);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                        case("do10k")->{
                            replyKey(callbackQuery);
                            queryy("SELECT * FROM houses WHERE Цена < 10000;",chatId);
                        }
                        case("do20k")->{
                            replyKey(callbackQuery);
                            queryy("SELECT * FROM houses WHERE Цена < 20000;",chatId);
                        }
                        case("do30k")->{
                            replyKey(callbackQuery);
                            queryy("SELECT * FROM houses WHERE Цена < 30000;",chatId);
                        }
                        case "back" -> {
                            setButtons(messageToUser);
                            messageToUser.setChatId(chatId);
                            messageToUser.setText("Что ты хочешь тут наворотить?");
                            try {
                                execute(messageToUser);
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        case ("confirmOrder")->{
                            forDateAndTime = 0;
                            messageToUser.setText("Выберите день:");
                            LocalDate currentDate = LocalDate.now();

                            // Создаем список кнопок
                            List<InlineKeyboardButton> buttons = new ArrayList<>();

                            // Добавляем кнопки с датами на 6 дней вперед
                            for (int i = 0; i < 6; i++) {
                                LocalDate date = currentDate.plusDays(i + 1);
                                String buttonText = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

                                InlineKeyboardButton button = new InlineKeyboardButton();
                                button.setText(buttonText);
                                button.setCallbackData(buttonText);
                                buttons.add(button);
                            }
                            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                            for (int i = 0; i < buttons.size(); i += 2) {
                                List<InlineKeyboardButton> row = new ArrayList<>();
                                row.add(buttons.get(i));

                                if (i + 1 < buttons.size()) {
                                    row.add(buttons.get(i + 1));
                                }

                                keyboard.add(row);
                            }

                            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                            markup.setKeyboard(keyboard);
                            messageToUser.setReplyMarkup(markup);
                            try {
                                execute(messageToUser);
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        default -> {
                            switch (forDateAndTime){
                                case 0->{
                                    StartArenda = update.getCallbackQuery().getData();
                                    System.out.println("День " + StartArenda);
                                    messageToUser.setText("Выберите время начала аренды: ");
                                    // Создаем список кнопок
                                    List<InlineKeyboardButton> buttons = new ArrayList<>();

                                    // Добавляем кнопки с временем с интервалом в один час
                                    LocalTime startTime = LocalTime.of(10, 0);
                                    LocalTime endTime = LocalTime.of(21, 0);
                                    while (startTime.isBefore(endTime)) {
                                        String buttonText = startTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                                        InlineKeyboardButton button = new InlineKeyboardButton();
                                        button.setText(buttonText);
                                        button.setCallbackData(buttonText);
                                        buttons.add(button);

                                        startTime = startTime.plusHours(1);
                                    }

                                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                                    for (int i = 0; i < buttons.size(); i += 2) {
                                        List<InlineKeyboardButton> row = new ArrayList<>();
                                        row.add(buttons.get(i));

                                        if (i + 1 < buttons.size()) {
                                            row.add(buttons.get(i + 1));
                                        }

                                        keyboard.add(row);
                                    }

                                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                                    markup.setKeyboard(keyboard);
                                    messageToUser.setReplyMarkup(markup);
                                    try {
                                        execute(messageToUser);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                    forDateAndTime++;
                                }
                                case 1->{
                                    Time = update.getCallbackQuery().getData();
                                    messageToUser.setText("Выберите ID Помещения: ");

                                    List<Integer> idList = new ArrayList<>();

                                    try (PreparedStatement statement = connection.prepareStatement("SELECT \"id\" FROM \"houses\" WHERE \"Доступность\" = true")) {

                                        ResultSet resultSet = statement.executeQuery();

                                        while (resultSet.next()) {
                                            int id = resultSet.getInt("id");
                                            idList.add(id);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(e);
                                    }

                                    List<InlineKeyboardButton> buttons = new ArrayList<>();


                                    for (int id : idList) {
                                        InlineKeyboardButton button = InlineKeyboardButton.builder()
                                                .text(String.valueOf(id))
                                                .callbackData(String.valueOf(id))
                                                .build();
                                        buttons.add(button);
                                    }

                                    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                                    List<InlineKeyboardButton> row = new ArrayList<>();

                                    for (InlineKeyboardButton button : buttons) {
                                        row.add(button);

                                        if (row.size() == 2) {
                                            rows.add(row);
                                            row = new ArrayList<>();
                                        }
                                    }


                                    if (!row.isEmpty()) {
                                        rows.add(row);
                                    }
                                    InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(rows).build();
                                    messageToUser.setReplyMarkup(markup);
                                    try {
                                        execute(messageToUser);
                                        System.out.println("exe");
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(e);
                                    }
                                    forDateAndTime++;
                                }
                                case 2->{
                                    idPomesheniya = update.getCallbackQuery().getData();
                                    try (PreparedStatement statement = connection.prepareStatement("SELECT \"Срок\" FROM \"houses\" WHERE \"id\" = ?")) {
                                        statement.setInt(1,Integer.parseInt(idPomesheniya));
                                        ResultSet resultSet = statement.executeQuery();
                                        List<InlineKeyboardButton> buttons = new ArrayList<>();

                                        // Добавляем кнопки с количеством дней/часов/месяцев от 1 до 12
                                        for (int i = 1; i <= 12; i++) {
                                            String buttonText = String.valueOf(i);

                                            InlineKeyboardButton button = new InlineKeyboardButton();
                                            button.setText(buttonText);
                                            button.setCallbackData(buttonText);
                                            buttons.add(button);
                                        }
                                        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                                        for (int i = 0; i < buttons.size(); i += 2) {
                                            List<InlineKeyboardButton> row = new ArrayList<>();
                                            row.add(buttons.get(i));

                                            if (i + 1 < buttons.size()) {
                                                row.add(buttons.get(i + 1));
                                            }

                                            keyboard.add(row);
                                        }

                                        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                                        markup.setKeyboard(keyboard);
                                        messageToUser.setReplyMarkup(markup);
                                        if (resultSet.next()) {
                                                String srok = resultSet.getString("Срок");
                                            switch (srok){
                                                case ("Почасовая аренда") ->{
                                                    messageToUser.setText("Выберите желаемое количество часов");
                                                    execute(messageToUser);
                                                    forDateAndTime++;
                                                }
                                                case ("Посуточно")->{
                                                    messageToUser.setText("Выберите желаемое количество суток");
                                                    execute(messageToUser);
                                                    forDateAndTime++;
                                                }
                                                case ("На месяц")->{
                                                    messageToUser.setText("Выберите желаемое количество месяцев");
                                                    execute(messageToUser);
                                                    forDateAndTime++;
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(e);
                                    }

                                }
                                case 3 -> {
                                   String ArendaSrok = update.getCallbackQuery().getData();
                                    String selectSql = "SELECT COUNT(*) FROM \"client\" WHERE \"id\" = ?";
                                    String insertSql = "INSERT INTO \"client\" (\"id\", \"Имя\") VALUES (?, ?)";

                                    try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                                         PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {

                                        // Проверяем наличие пользователя в таблице
                                        selectStatement.setInt(1, Integer.parseInt(NameID));
                                        ResultSet resultSet = selectStatement.executeQuery();
                                        resultSet.next();
                                        int count = resultSet.getInt(1);

                                        if (count == 0) {
                                            // Пользователь не найден, выполняем вставку
                                            insertStatement.setInt(1, Integer.parseInt(NameID));
                                            insertStatement.setString(2,name);
                                            insertStatement.executeUpdate();
                                            System.out.println("Пользователь добавлен");
                                        } else {
                                            System.out.println("Пользователь уже существует");
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(e);
                                    }
                                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                                    LocalDate date = LocalDate.parse(StartArenda, inputFormatter);
                                    DateTimeFormatter inputFormatter2 = DateTimeFormatter.ofPattern("HH:mm");
                                    LocalTime time = LocalTime.parse(Time, inputFormatter2);
                                    String sql = "INSERT INTO \"orders\" (\"clientid\", \"houseid\", \"rent_date\", \"rent_duration\", \"rent_start_time\") VALUES (?, ?, ?, ?, ?)";
                                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                                        statement.setInt(1, Integer.parseInt(NameID));
                                        statement.setInt(2, Integer.parseInt(idPomesheniya));
                                        statement.setDate(3, Date.valueOf(date));
                                        statement.setInt(4, Integer.parseInt(ArendaSrok));
                                        statement.setTime(5, java.sql.Time.valueOf(time));

                                        int rowsAffected = statement.executeUpdate();
                                        if (rowsAffected > 0) {

                                            String updateSql = "UPDATE \"houses\" SET \"Доступность\" = false WHERE \"id\" = ?";
                                            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                                                updateStatement.setInt(1, Integer.parseInt(idPomesheniya));
                                                updateStatement.executeUpdate();
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                                throw new RuntimeException(e);
                                            }

                                            sql = "SELECT \"Цена\" FROM \"houses\" WHERE \"id\" = ?";

                                            try(PreparedStatement statement2 = connection.prepareStatement(sql)){
                                                statement2.setInt(1,Integer.parseInt(idPomesheniya));
                                                ResultSet resultSet = statement2.executeQuery();
                                                if(resultSet.next()){
                                                    int price = resultSet.getInt("Цена");
                                                    price *= Integer.parseInt(ArendaSrok);
                                                    messageToUser.setText("Аренда прошла успешно, к оплате будет: " + price);
                                                    setButtons(messageToUser);
                                                    execute(messageToUser);
                                                    forDateAndTime++;
                                                }
                                            }
                                            catch (Exception e){
                                                e.printStackTrace();
                                                throw new RuntimeException(e);
                                            }
                                        } else {
                                            messageToUser.setText("Произошла ошибка, для дополнительной информации свяжитесь с нами в разделе help");
                                            execute(messageToUser);
                                        }
                                    } catch (Exception e){
                                        e.printStackTrace();
                                        throw new RuntimeException(e);
                                    }
                                }
                                }
                            }
                        }
                    }
                } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
    }
    ResultSet resultSet = null;
    Statement statement = null;

    public void queryy(String query, String chatId) {
        try {
            // Создание объекта Statement для выполнения запроса
            statement = connection.createStatement();

            // Выполнение запроса выборки
            resultSet = statement.executeQuery(query);

            // Проверка наличия результатов
            if (resultSet.next()) {
                // Обработка результатов выборки
                do {
                    // Получение значений из текущей строки результата
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("Наименование");
                    int pricePH = resultSet.getInt("Цена");
                    String description = resultSet.getString("Описание");
                    String srok = resultSet.getString("Срок");
                    String message = "id помещения: " + id + "\nНазвание: " + name + "\nЦена: " + pricePH + "\nОписание: " + description + "\nСрок: " + srok;
                    messageToUser.setText(message);
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                    List<InlineKeyboardButton> row1 = new ArrayList<>();
                    row1.add(InlineKeyboardButton.builder()
                            .text("Оформить заказ")
                            .callbackData("confirmOrder")
                            .build());
                    row1.add(InlineKeyboardButton.builder()
                            .text("Назад")
                            .callbackData("back")
                            .build());
                    keyboard.add(row1);
                    inlineKeyboardMarkup.setKeyboard(keyboard);
                    messageToUser.setReplyMarkup(inlineKeyboardMarkup);
                    execute(messageToUser);
                    for (int i = 1; i<4;i++){
                        InputFile photo = new InputFile(new File("C:\\Users\\Артур\\IdeaProjects\\TelegramBot\\src\\main\\resources\\"+id+i+".jpg"));
                        photoToUser = new SendPhoto();
                        photoToUser.setChatId(chatId);
                        photoToUser.setPhoto(photo);
                        execute(photoToUser);
                    }
                } while (resultSet.next());
            } else {
                // Результаты не найдены, выполните соответствующие действия
                String message = "Результаты не найдены.";
                messageToUser.setText(message);
                execute(messageToUser);
            }
        } catch (SQLException | TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public void replyKey(CallbackQuery callbackQuery){
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        replyKeyboardRemove.setSelective(false);
        messageToUser.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));
        messageToUser.setReplyMarkup(replyKeyboardRemove);
    }
}