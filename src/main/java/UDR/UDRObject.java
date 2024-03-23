package UDR;

/**
 * Выступает в роли сборщика информации о транзакция пользователя с номером телефона
 */
public class UDRObject {
    /**
     * номер телефона абонента (msisdn как в ТЗ)
     */
    private final String msisdn;
    /**
     * Объект входящего звонка
     */
    private IncomingCall incomingCall;
    /**
     * Объект исходящего звонка
     */
    private OutcomingCall outcomingCall;

    /**
     * Создает объект UDRObject, присваивая полям класса нужные значения
     *
     * @param msisdn        номер телефона абонента
     * @param incomingCall  объект входящего звонка
     * @param outcomingCall объект исходящего взонка
     */
    public UDRObject(String msisdn, IncomingCall incomingCall, OutcomingCall outcomingCall) {
        this.msisdn = msisdn;
        this.incomingCall = incomingCall;
        this.outcomingCall = outcomingCall;
    }

    /**
     * @return номер телефона абонента
     */
    public String getMsisdn() {
        return msisdn;
    }

    /**
     * @return объект входящего звонка
     */
    public IncomingCall getIncomingCall() {
        return incomingCall;
    }

    /**
     * @return объект исходящего звонка
     */
    public OutcomingCall getOutcomingCall() {
        return outcomingCall;
    }

    /**
     * Задает новое значение полю incomingCall
     *
     * @param incomingCall новый объект входящего звонка
     */
    public void setIncomingCall(IncomingCall incomingCall) {
        this.incomingCall = incomingCall;
    }

    /**
     * Задает новое значение полю outcomingCall
     *
     * @param outcomingCall новый объект исходящего звонка
     */
    public void setOutcomingCall(OutcomingCall outcomingCall) {
        this.outcomingCall = outcomingCall;
    }
}
