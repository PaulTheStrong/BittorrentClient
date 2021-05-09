package by.bsuir.ksis.kursovoi;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void testHex2ByteArray() {
        String hexString = "%01%0a%0fabc";
        byte[] result = Utils.hex2ByteArray(hexString);
        byte[] expected = new byte[] {1, 10, 15, 'a', 'b', 'c'};
        Assert.assertArrayEquals(result, expected);
    }

}