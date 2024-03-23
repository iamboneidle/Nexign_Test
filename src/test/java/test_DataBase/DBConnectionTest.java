package test_DataBase;

import DataBase.DBConnection;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;


/**
 * Проверка для DBConnection
 */
public class DBConnectionTest {
    /**
     * Проверка правильности конвертации времени
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Test
    public void testTimeConverter() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        DBConnection dbConnection = new DBConnection();
        Method timeConverter = DBConnection.class.getDeclaredMethod("timeConverter", long.class, long.class);
        timeConverter.setAccessible(true);
        assertEquals("02:14:43", timeConverter.invoke(dbConnection, 1711220978433L, 1711218261433L));
        timeConverter.setAccessible(false);
    }
}
