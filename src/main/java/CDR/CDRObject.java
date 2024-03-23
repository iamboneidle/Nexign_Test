package CDR;

import java.util.StringJoiner;

/**
 * Выступает в роли транзакции, которую совершает пользователь
 */
public class CDRObject {
    /**
     * Тип звонка:
     * Строка "01" - исходящий, "02" - входящий вызовы
     */
    private final String callType;
    /**
     * Номер телефона абонента, свершившего транзакцию
     */
    private final String phoneNumber;
    /**
     * Время начала звонка (Unix time)
     */
    private final long callStart;
    /**
     * Время окончания звонка (Unix time)
     */
    private final long callEnd;

    /**
     * Создает объект, выступающий в роли транзакции пользователя
     *
     * @param callType    тип звонка
     * @param phoneNumber номер телефона абонента, совершившего транзакцию
     * @param callStart   время начала звонка (Unix time)
     * @param callEnd     время окончания звонка (Unix time)
     */
    public CDRObject(String callType, String phoneNumber, long callStart, long callEnd) {
        this.callType = callType;
        this.phoneNumber = phoneNumber;
        this.callStart = callStart;
        this.callEnd = callEnd;
    }

    /**
     * Создает объект по строке, нужен для парсинга транзакции из CDR-файла
     *
     * @param toParse строка с информацией от транзакции
     */
    public CDRObject(String toParse) {
        String[] parsed = toParse.split(",");
        this.callType = parsed[0];
        this.phoneNumber = parsed[1];
        this.callStart = Long.parseLong(parsed[2]);
        this.callEnd = Long.parseLong(parsed[3]);
    }

    /**
     * @return приватное поле типа звонка
     */
    public String getCallType() {
        return callType;
    }

    /**
     * @return приватное поле номера абонента, совершившего транзакцию
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @return время начала звонка (Unix time)
     */
    public long getCallStart() {
        return callStart;
    }

    /**
     * @return время окончания звонка (Unix time)
     */
    public long getCallEnd() {
        return callEnd;
    }

    /**
     * @return строку, готовую к добавлению в CDR-файл формата "callType, phoneNumber,callStart, callEnd"
     * с разделителем ","
     */
    @Override
    public String toString() {
        StringJoiner result = new StringJoiner(",");
        result.add(callType).add(phoneNumber).add(Long.toString(callStart)).add(Long.toString(callEnd));
        return result.toString();
    }
}
