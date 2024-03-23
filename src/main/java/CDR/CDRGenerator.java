package CDR;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Генерирует пользователей и их транзакции
 */
public class CDRGenerator {
    public static String callTypeGenerator() {
        int number = (int) (Math.random() * 2 + 1);
        return "0" + number;
    }

    /**
     * Генерирует время формата Unix time
     * прибавляет к настоящему времени случайное количество секунд от 0 до 3599
     *
     * @return время формата Unix time
     */
    public static long timeGenerator() {
        double v = Math.random() * 3600;
        LocalDateTime start = LocalDateTime.now().plusSeconds(Double.valueOf(v).longValue());
        Timestamp timestamp = Timestamp.valueOf(start);
        return timestamp.getTime();
    }

    /**
     * Генерирует телефонные номера абонентов
     * создает номер следующего вида:
     * номер начинается с "79", затем к нему прибавляется строка с числом от 10 до 99, затем остальная часть номера от
     * 1000000 до 10000000
     *
     * @return список строк номеров
     */
    public static List<String> phoneNumbersGenerator() {
        List<String> phoneNumbers = new ArrayList<>();
        StringBuilder number = new StringBuilder();
        int usersQuantity = 20;
        for (int i = 0; i < usersQuantity; i++) {
            number.append("79").append((int) (10 + Math.random() * 90)).append((int) (1000000 + Math.random() * 9000000));
            phoneNumbers.add(number.toString());
            number.delete(0, number.length());
        }
        return phoneNumbers;
    }
}
