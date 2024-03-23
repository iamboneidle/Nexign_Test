package DataBase;

import CDR.CDRObject;
import CDR.CDRService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

/**
 * Отвечает за реализацию подключения к БД и запись данных в нее
 */
public class DBConnection {
    /**
     * Соединение с БД h2
     */
    private final Connection connection;
    /**
     * Логгер
     */
    private static final Logger LOGGER = Logger.getLogger(CDRService.class.getName());
    /**
     * url для БД
     */
    private static final String DB_URL = "jdbc:h2:mem:Nexign";
    /**
     * username для БД
     */
    private static final String USER = "iamboneidle";
    /**
     * пароль для БД
     */
    private static final String PASSWORD = "";
    /**
     * classname драйвера для БД
     */
    private static final String CLASS_NAME = "org.h2.Driver";

    /**
     * Создает объект DBConnection, открывает соединение, создает таблицу пользователей (Users) и
     * таблицу транзакций пользователя (UsersTransactions)
     */
    public DBConnection() {
        connection = startConnection();
        createUsersTable();
        LOGGER.log(Level.INFO, "OK: Users table was created successfully");
        createUsersTransactionsTable();
        LOGGER.log(Level.INFO, "OK: UsersTransactions table was created successfully");
    }

    /**
     * Создает соединение с БД h2
     * <br>Обрабатывает и логгирует SQLException и ClassNotFoundException, если возникает подключения к БД
     *
     * @return соединений с БД
     * @throws RuntimeException в случае возникновения исключении к подключении к БД
     */
    private Connection startConnection() {
        Connection connection;
        try {
            Class.forName(CLASS_NAME);
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            LOGGER.log(Level.INFO, "OK: Connected successfully");
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: ", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Получает номера всех пользователей из БД из таблицы Users
     * <br>Обрабатывает и логгирует SQLException, если возникает ошибка получения данных из БД
     *
     * @return список строк с номерами пользователей.
     */
    public List<String> getUsersNumbers() {
        List<String> userNumbers = new ArrayList<>();
        try {
            String selectDataSQL = "SELECT * FROM Users";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectDataSQL);
            while (resultSet.next()) {
                userNumbers.add(resultSet.getString("phoneNumber"));
            }
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: ", e.getMessage());
        }
        return userNumbers;
    }

    /**
     * Конвертирует время формата Unix time в строку формата "HH:MM:SS"
     *
     * @param timeStart время начала звонка
     * @param timeEnd   время окончания звонка
     * @return возвращает строку со временем формата "HH:MM:SS"
     */
    private String timeConverter(long timeStart, long timeEnd) {
        long totalTime = timeEnd - timeStart;
        Date quantity = new Date(totalTime);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(quantity);
    }

    /**
     * Создает таблицу Users в БД
     * <br>Обрабатывает и логгирует SQLException, если возникает ошибка создания таблицы в БД
     *
     * @throws RuntimeException в случае исключения при создании таблицы Users в БД
     */
    private void createUsersTable() {
        try {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS Users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "phoneNumber VARCHAR(15) NOT NULL" +
                    ")";

            Statement statement = connection.createStatement();
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: ", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Создает таблицу UsersTransactions в БД.
     * <br>Обрабатывает и логгирует SQLException, если возникает ошибка создания таблицы в БД
     *
     * @throws RuntimeException в случае исключения при создании таблицы UsersTransactions в БД
     */
    private void createUsersTransactionsTable() {
        try {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS UsersTransactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "userId INT," +
                    "callType VARCHAR(2) NOT NULL," +
                    "incomingCallTime VARCHAR(8)," +
                    "outcomingCallTime VARCHAR(8)," +
                    "FOREIGN KEY (userId) REFERENCES Users(id)" +
                    ")";

            Statement statement = connection.createStatement();
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: ", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Записывает одним запросом номера всех абонентов в таблицу Users в БД
     * <br>Обрабатывает и логгирует SQLException, если возникает ошибка записи данных в БД
     *
     * @param phoneNumber список строк с номерами абонентов.
     */
    public void insertUsersData(List<String> phoneNumber) {
        try {
            StringBuilder insertDataSQL = new StringBuilder("INSERT INTO Users (phoneNumber) VALUES (" + phoneNumber.get(0) + ")");
            for (int i = 1; i < phoneNumber.size(); i++) {
                insertDataSQL.append(", (");
                insertDataSQL.append(phoneNumber.get(i));
                insertDataSQL.append(")");
            }
            insertDataSQL.append(";");
            PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: ", e.getMessage());
        }
    }

    /**
     * Метод записывает данные о транзакциях пользователей в таблицу UsersTransactions, вызывая метод
     * insertTransactionsHelper, получает с помощью метода getUserIdByPhoneNumber отношение между userId и phoneNumber
     *
     * @param cdrObjects список транзакций пользователя
     */
    public void insertUsersTransactionsData(List<CDRObject> cdrObjects) {
        int userId;
        Map<String, Integer> phoneNumberToUserIdMap = getUserIdByPhoneNumber();
        for (CDRObject obj : cdrObjects) {
            userId = phoneNumberToUserIdMap.get(obj.getPhoneNumber());
            insertTransactionsHelper(userId, obj.getCallType(), timeConverter(obj.getCallEnd(), obj.getCallStart()));
        }
    }

    /**
     * Имплементирует запись в таблицу UsersTransactions данных о транзакция пользователя
     * <br>Обрабатывает и логгирует SQLException, если возникает ошибка при записи данных в БД
     *
     * @param userId   id пользователя с определенным номером телефона, на котором завязана транзакция из таблицы Users
     * @param callType тип звонка
     * @param callTime продолжительность звонка
     */
    private void insertTransactionsHelper(int userId, String callType, String callTime) {
        try {
            String insertDataSQL = "INSERT INTO UsersTransactions (userId, callType, incomingCallTime, outcomingCallTime) VALUES (?, ?, ?, ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, callType);
            if ("01".equals(callType)) {
                preparedStatement.setString(3, callTime);
                preparedStatement.setNull(4, Types.VARCHAR);
            } else {
                preparedStatement.setNull(3, Types.VARCHAR);
                preparedStatement.setString(4, callTime);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: ", e.getMessage());
        }
    }

    /**
     * Создает отношение между userId и phoneNumber из таблицы Users
     * <br> Обрабатывает и логгирует SQLException, если возникает ошибка получения данных из БД
     *
     * @return HashMap данного отношения
     */
    private Map<String, Integer> getUserIdByPhoneNumber() {
        Map<String, Integer> phoneNumberToUserIdMap = new HashMap<>();
        String phoneNumber;
        try {
            String selectUserIdSQL = "SELECT * FROM Users ";

            PreparedStatement preparedStatement = connection.prepareStatement(selectUserIdSQL);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                phoneNumber = resultSet.getString("phoneNumber");
                if (!phoneNumberToUserIdMap.containsKey(phoneNumber)) {
                    phoneNumberToUserIdMap.put(phoneNumber, resultSet.getInt("id"));
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: ", e.getMessage());
        }
        return phoneNumberToUserIdMap;
    }
}