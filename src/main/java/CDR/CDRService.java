package CDR;

import DataBase.DBConnection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Является CDR-сервисом, который отвечает за запись транзакций в базу данных, вызов методов генерации транзакций
 * пользователей и запись транзакций в CDR-файлы
 */
public class CDRService {
    /**
     * Список номеров пользователей
     */
    private final List<String> userNumbers;
    /**
     * Объект класса DBConnection, с помощью которого реализуется запись данных в БД
     */
    private final DBConnection dbConnection;
    /**
     * Номер случайно выбранного пользователя
     */
    private final String someRandomNumber;
    /**
     * Путь, по которому записываются CDR-файлы
     */
    private static final Path ROOT_PATH = Paths.get(System.getProperty("user.dir") + "/CDRFiles").toAbsolutePath();
    /**
     * Логгер
     */
    private static final Logger LOGGER = Logger.getLogger(CDRService.class.getName());

    /**
     * Получает номера пользователей из БД, если их нет генерирует с помощью метода phoneNumbersGenerator
     * класса CDRGenerator и записывает их в БД
     * Также получает номер случайно выбранного пользователя
     */
    public CDRService() {
        DBConnection dbConnection = new DBConnection();
        List<String> numbers = dbConnection.getUsersNumbers();
        if (!numbers.isEmpty()) {
            this.userNumbers = numbers;
            LOGGER.log(Level.INFO, "OK: userNumbers data was pulled from DB successfully");
        } else {
            this.userNumbers = CDRGenerator.phoneNumbersGenerator();
            dbConnection.insertUsersData(userNumbers);
            LOGGER.log(Level.INFO, "OK: userNumbers data was inserted successfully");
        }
        someRandomNumber = this.userNumbers.get((int) (Math.random() * userNumbers.size() - 1));
        this.dbConnection = dbConnection;
    }

    /**
     * Вызывает метод monthTransactionsGenerator для каждого из 12 месяцев в году (1-Январь, 12-Декабрь)
     */
    public void yearTransactionsGenerator() {
        for (int i = 1; i <= 12; i++) {
            monthTransactionsGenerator(i);
        }
    }

    /**
     * Для каждого пользователя метод генерирует случайное число транзакций от 10 до 20,
     * перемешивает транзакции (по требованиям задания), затем сортирует их по времени начала звонка,
     * записывает их в БД, вызывая метод insertUsersTransactionsData для объекта dbConnection,
     * вызывает метод writeDataToCdrFiles
     *
     * @param monthNum номер месяца, за который нужно создать отчет по транзакциям
     */
    private void monthTransactionsGenerator(int monthNum) {
        List<CDRObject> cdrObjects = new ArrayList<>();
        userNumbers.forEach(number -> {
            for (int i = 0; i < (int) (Math.random() * 11 + 10); i++) {
                long time1 = CDRGenerator.timeGenerator();
                long time2 = CDRGenerator.timeGenerator();
                CDRObject newCDRObject = new CDRObject(CDRGenerator.callTypeGenerator(), number,
                        Math.min(time1, time2), Math.max(time1, time2));
                cdrObjects.add(newCDRObject);
            }
        });
        Collections.shuffle(cdrObjects);
        cdrObjects.sort(Comparator.comparingLong(CDRObject::getCallStart));
        dbConnection.insertUsersTransactionsData(cdrObjects);
        Path filePath = Paths.get(ROOT_PATH + "/CDR" + monthNum + ".txt");
        writeDataToCdrFiles(filePath, cdrObjects);
    }

    /**
     * Записывает транзакции абонентов в СDR-файлы
     * <br> Обрабатывает и логгирует IOException при ошибке записи в файл
     *
     * @param filePath   путь, по которому создаются CDR-файлы
     * @param cdrObjects список транзакций пользователя
     * @throws RuntimeException при ошибке записи в файл
     */
    private void writeDataToCdrFiles(Path filePath, List<CDRObject> cdrObjects) {
        try {
            if (!Files.exists(ROOT_PATH.toAbsolutePath())) {
                Files.createDirectory(ROOT_PATH.toAbsolutePath());
            }
            Files.deleteIfExists(filePath);
            Path file = Files.createFile(filePath);
            try (FileOutputStream outputStream = new FileOutputStream(file.toFile())) {
                for (CDRObject obj : cdrObjects) {
                    outputStream.write((obj.toString() + "\n").getBytes());
                    outputStream.flush();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: troubles with creating file " + filePath, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод нужен, чтобы получить поле номера случайного абонента для того, чтобы удобно вызвать методы
     * generateReport(msisdn) и generateReport(msisdn, monthNum) класса UDRService
     *
     * @return номер случайно выбранного абонента
     */
    public String getSomeRandomNumber() {
        return someRandomNumber;
    }
}
