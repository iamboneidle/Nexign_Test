package UDR;

import CDR.CDRObject;
import CDR.CDRService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.vandermeer.asciitable.AT_Context;
import de.vandermeer.asciitable.AsciiTable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Является UDR-сервисом, который отвечает за чтение данных из CDR-файлов, предоставляемых CDR-сервисом,
 * за создание отчетов в папку /reports с расширением .json, а также за вывод таблиц report (методов generateReport())
 * в консоль
 */
public class UDRService {
    /**
     * Путь чтения CDR-файлов
     */
    private static final Path ROOT_PATH = Paths.get(System.getProperty("user.dir") + "/CDRFiles/CDR").toAbsolutePath();
    /**
     * Путь записи отчетов
     */
    private static final Path REPORT_PATH = Paths.get(System.getProperty("user.dir") + "/reports").toAbsolutePath();
    /**
     * Логгер
     */
    private static final Logger LOGGER = Logger.getLogger(CDRService.class.getName());
    /**
     * Ширина таблицы для репорта со всеми месяцами
     */
    private static final int TABLE_WIDTH_BIG = 225;
    /**
     * Ширина таблицы для репорта с одним месяцем
     */
    private static final int TABLE_WIDTH_SMALL = 61;
    /**
     * Время по умолчанию в таблице репорта
     */
    private static final String DEFAULT_TABLE_TIME_VALUE = "00:00:00";
    /**
     * Названия колонок таблиц, которые выводятся методами generateReports() и generateReports(msisdn)
     */
    private final List<String> columnNames;
    /**
     * Названия колонок таблицы, которая выводится методом generateReports(msisdn, month)
     */
    private final List<String> columnNamesOneUserOneMonth;

    /**
     * Создает объект UDRService и присваивает нужные значения полям columnNames и columnNamesOneUserOneMonth
     */
    public UDRService() {
        this.columnNames = new ArrayList<>();
        this.columnNames.add("User");
        for (int monthNum = 1; monthNum <= 12; monthNum++) {
            this.columnNames.add("incomingCall_" + monthNum);
            this.columnNames.add("outcomingCall_" + monthNum);
        }
        this.columnNamesOneUserOneMonth = new ArrayList<>();
        this.columnNamesOneUserOneMonth.add("User");
        this.columnNamesOneUserOneMonth.add("incomingCall");
        this.columnNamesOneUserOneMonth.add("outcomingCall");
        generateReport();
    }

    /**
     * Сохраняет отчеты в папку /reports и выводит таблицу транзакций каждого пользователя по каждому месяцу
     * <br> Обрабатывает и логгирует IOException в случае ошибки чтения файла
     *
     * @throws RuntimeException в случае ошибки чтения файла
     */
    private void generateReport() {
        List<String> calls;
        Map<String, List<String>> forTable = new HashMap<>();
        try {
            for (int monthNum = 1; monthNum <= 12; monthNum++) {
                Path filePath = Paths.get(ROOT_PATH.toString() + monthNum + ".txt");
                if (Files.exists(filePath)) {
                    calls = Files.readAllLines(filePath);
                    Map<String, UDRObject> monthInfo = getMonthInfo(calls);
                    for (UDRObject udrObject : monthInfo.values()) {
                        reportByMonth(monthNum, udrObject);
                        prepareDataForTable(forTable, udrObject);
                    }
                }
            }
            makeTable(forTable);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: troubles with reading file: ", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Сохраняет отчеты в папку /reports и выводит таблицу транзакций конкретного (по номеру телефона)
     * <br> Обрабатывает и логгирует IOException в случае ошибки чтения файла
     * пользователя по каждому месяцу
     *
     * @param msisdn номер телефона абонента
     * @throws RuntimeException в случае ошибки чтения файла
     */
    public void generateReport(String msisdn) {
        List<String> calls;
        Map<String, List<String>> forTable = new HashMap<>();
        try {
            for (int monthNum = 1; monthNum <= 12; monthNum++) {
                Path filePath = Paths.get(ROOT_PATH.toString() + monthNum + ".txt");

                if (Files.exists(filePath)) {
                    calls = Files.readAllLines(filePath);
                    UDRObject udrObject = getMonthInfoByMsisdn(calls, msisdn);
                    reportByMonth(monthNum, udrObject);
                    prepareDataForTable(forTable, udrObject);
                }
            }
            makeTable(forTable);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: troubles with reading file : ", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Сохраняет отчеты в папку /reports и выводит таблицу транзакций
     * конкретного (по номеру телефона) пользователя по конкретному (monthNum) месяцу
     * <br> Обрабатывает и логгирует IOException в случае ошибки чтения файла
     *
     * @param msisdn   номер телефона абонента
     * @param monthNum номер месяца
     */
    public void generateReport(String msisdn, int monthNum) {
        Path filePath = Paths.get(ROOT_PATH.toString() + monthNum + ".txt");
        try {
            if (Files.exists(filePath)) {
                List<String> calls = Files.readAllLines(filePath);
                UDRObject udrObject = getMonthInfoByMsisdn(calls, msisdn);
                reportByMonth(monthNum, udrObject);
                List<String> forTable = new ArrayList<>();
                forTable.add(udrObject.getMsisdn());
                forTable.add(udrObject.getIncomingCall().getStrTotalTime());
                forTable.add(udrObject.getOutcomingCall().getStrTotalTime());
                makeTableForOneUserInOneMonth(forTable);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: troubles with reading file " + filePath, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Готовит данные для внесения в таблицу
     *
     * @param forTable  данные таблицы
     * @param udrObject объект UDR
     */
    private void prepareDataForTable(Map<String, List<String>> forTable, UDRObject udrObject) {
        List<String> toPut;
        if (forTable.containsKey(udrObject.getMsisdn())) {
            toPut = forTable.get(udrObject.getMsisdn());
            toPut.add(udrObject.getIncomingCall() != null ? udrObject.getIncomingCall().getStrTotalTime() : DEFAULT_TABLE_TIME_VALUE);
            toPut.add(udrObject.getOutcomingCall() != null ? udrObject.getOutcomingCall().getStrTotalTime() : DEFAULT_TABLE_TIME_VALUE);
        } else {
            toPut = new ArrayList<>();
            toPut.add(udrObject.getMsisdn());
            toPut.add(udrObject.getIncomingCall().getStrTotalTime());
            toPut.add(udrObject.getOutcomingCall().getStrTotalTime());
            forTable.put(udrObject.getMsisdn(), toPut);
        }
    }

    /**
     * Создает .json файлы отчетов за месяц по каждому пользователю
     * <br> Обрабатывает и логгирует IOException в случае ошибки записи файла
     *
     * @param monthNum  номер месяца
     * @param udrObject UDR-объъект
     */
    private void reportByMonth(int monthNum, UDRObject udrObject) {
        Path reportPath = Paths.get(REPORT_PATH + "/" + udrObject.getMsisdn() + "_" + monthNum + ".json");
        try {
            if (!Files.exists(REPORT_PATH.toAbsolutePath())) {
                Files.createDirectory(REPORT_PATH.toAbsolutePath());
            }
            Files.deleteIfExists(reportPath);
            Path file = Files.createFile(reportPath);

            try (FileOutputStream outputStream = new FileOutputStream(file.toFile())) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                outputStream.write(objectMapper.writeValueAsString(udrObject).getBytes());
                outputStream.flush();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "EXCEPTION: troubles with creating file " + reportPath, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Конвертер CDR-объекта к UDR-объекту
     *
     * @param cdrObject CDR-объект
     * @return UDR-объект
     */
    private UDRObject cdrObjectToUdrObject(CDRObject cdrObject) {
        IncomingCall incomingCall = null;
        OutcomingCall outcomingCall = null;
        if ("01".equals(cdrObject.getCallType())) {
            incomingCall = new IncomingCall(cdrObject.getCallStart(), cdrObject.getCallEnd());
        } else {
            outcomingCall = new OutcomingCall(cdrObject.getCallStart(), cdrObject.getCallEnd());
        }
        return new UDRObject(cdrObject.getPhoneNumber(), incomingCall, outcomingCall);
    }

    /**
     * Возвращает информацию по звонкам абонента за месяц
     *
     * @param calls список звонков пользователя
     * @return отношение номера телефона абонента к списку его UDR-объектов
     */
    private Map<String, UDRObject> getMonthInfo(List<String> calls) {
        Map<String, UDRObject> msisdnToUDRObjectMap = new HashMap<>();
        calls.forEach(call -> {
            CDRObject inCall = new CDRObject(call);
            UDRObject udrObject = cdrObjectToUdrObject(inCall);
            if (msisdnToUDRObjectMap.containsKey(udrObject.getMsisdn())) {
                UDRObject existObject = msisdnToUDRObjectMap.get(udrObject.getMsisdn());
                if ("01".equals(inCall.getCallType())) {
                    if (existObject.getIncomingCall() == null) {
                        existObject.setIncomingCall(udrObject.getIncomingCall());
                    } else {
                        existObject.getIncomingCall().addTotalTime(udrObject.getIncomingCall().getTotalTime());
                    }
                } else {
                    if (existObject.getOutcomingCall() == null) {
                        existObject.setOutcomingCall(udrObject.getOutcomingCall());
                    } else {
                        existObject.getOutcomingCall().addTotalTime(udrObject.getOutcomingCall().getTotalTime());
                    }
                }
            } else {
                msisdnToUDRObjectMap.put(udrObject.getMsisdn(), udrObject);
            }
        });
        return msisdnToUDRObjectMap;
    }

    /**
     * Создает UDR-объект для пользователя по номеру телефона и списку его звонков
     *
     * @param calls  список звонков пользователя
     * @param msisdn номер телефона пользователя
     * @return UDR-объект с информацией за месяц по транзакциям пользователя
     */
    private UDRObject getMonthInfoByMsisdn(List<String> calls, String msisdn) {
        UDRObject resultUDRObject = new UDRObject(msisdn, new IncomingCall(0, 0), new OutcomingCall(0, 0));
        calls.forEach(call -> {
            CDRObject inCall = new CDRObject(call);
            if (msisdn.equals(inCall.getPhoneNumber())) {
                UDRObject udrObject = cdrObjectToUdrObject(inCall);
                if (udrObject.getIncomingCall() == null) {
                    resultUDRObject.getOutcomingCall().addTotalTime(udrObject.getOutcomingCall().getTotalTime());
                } else {
                    resultUDRObject.getIncomingCall().addTotalTime(udrObject.getIncomingCall().getTotalTime());
                }
            }
        });
        return resultUDRObject;
    }

    /**
     * Создает таблицу для всех пользователей за все месяцы или таблицу для одного пользователя за все месяцы
     * в зависимости от получаемы=х данных на вход
     *
     * @param data отношение номера телефона пользователя к данным о его звонках
     */
    private void makeTable(Map<String, List<String>> data) {
        AT_Context atContext = new AT_Context();
        atContext.setWidth(TABLE_WIDTH_BIG);
        AsciiTable at = new AsciiTable(atContext);
        at.addRule();
        at.addRow(columnNames);
        for (String msisdn : data.keySet()) {
            at.addRule();
            List<String> userCallsTime = data.get(msisdn);
            userCallsTime.addAll(Collections.nCopies(columnNames.size() - userCallsTime.size(), DEFAULT_TABLE_TIME_VALUE));
            at.addRow(userCallsTime);
        }
        at.addRule();
        String rend = at.render();
        LOGGER.log(Level.INFO,"REPORT: \n" + rend);
    }

    /**
     * Создает таблицу для одного пользователя за один месяц
     *
     * @param data список данных для таблицы
     */
    private void makeTableForOneUserInOneMonth(List<String> data) {
        AT_Context atContext = new AT_Context();
        atContext.setWidth(TABLE_WIDTH_SMALL);
        AsciiTable at = new AsciiTable(atContext);
        at.addRule();
        at.addRow(columnNamesOneUserOneMonth);
        data.addAll(Collections.nCopies(columnNamesOneUserOneMonth.size() - data.size(), DEFAULT_TABLE_TIME_VALUE));
        at.addRow(data);
        at.addRule();
        String rend = at.render();
        LOGGER.log(Level.INFO," REPORT: \n" + rend);
    }
}
