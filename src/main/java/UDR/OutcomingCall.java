package UDR;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Отвечает за реализацию исходящего звонка
 */
public class OutcomingCall {
    /**
     * Полное время исходящего звонка абонента в формате Unix time
     */
    private long totalTime;

    /**
     * Создает объект OutcomingCall, задавая поле totalTime
     * как разницу между временем начала звонка и временем его окончания
     *
     * @param timeStart время начала звонка
     * @param timeEnd   время окончания звонка
     */
    public OutcomingCall(long timeStart, long timeEnd) {
        this.totalTime = timeEnd - timeStart;
    }

    /**
     * @return полное время длительности звонка в формате Unix time
     */
    @JsonIgnore
    public long getTotalTime() {
        return totalTime;
    }

    /**
     * @return полное время длительности звонка строкой формата "HH:MM:SS"
     */
    @JsonProperty("totalTime")
    public String getStrTotalTime() {
        Date quantity = new Date(totalTime);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(quantity);
    }

    /**
     * Добавляет время к полю totalTime
     *
     * @param totalTime полное время нового исходящего звонка в формате Unix time
     */
    public void addTotalTime(long totalTime) {
        this.totalTime += totalTime;
    }

}
