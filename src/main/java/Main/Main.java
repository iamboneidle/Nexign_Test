package Main;

import CDR.CDRService;
import UDR.UDRService;


/**
 * <br>Для того чтобы вызвать методы generateReport(msisdn) и generateReport(msisdn, month)
 * нужно вызвать прописать
 * <br>String msisdn = cdrService.getSomeRandomNumber();
 * <br>int month = (1-январь, ..., 12-декабрь);
 * <br>udrService.generateReport(msisdn);
 * <br>udrService.generateReport(msisdn, month);
 */
public class Main {
    public static void main(String[] args) {
        CDRService cdrService = new CDRService();
        cdrService.yearTransactionsGenerator();
        UDRService udrService = new UDRService();
        udrService.generateReport();
    }
}