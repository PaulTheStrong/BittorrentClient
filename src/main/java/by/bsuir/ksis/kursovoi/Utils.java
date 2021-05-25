package by.bsuir.ksis.kursovoi;

import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class Utils {

    @SneakyThrows
    public static String SHAsum(byte[] convertme) {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(convertme));
    }

    public static String byteArray2Hex(final byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            char c = (char) b;
            if (c >= '0' && c <= '9'
                    || c >= 'a' && c <= 'z'
                    || c >= 'A' && c <= 'Z'
                    || c == '.' || c == '-'
                    || c == '_' || c == '~') {
                formatter.format("%c", c);
            } else {
                formatter.format("%%%02x", b);
            }
        }
        return formatter.toString();
    }

    public static byte[] hex2ByteArray(final String hex) {
        List<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < hex.length(); i++) {
            if (hex.charAt(i) == '%') {
                char c1 = hex.charAt(i + 1);
                char c2 = hex.charAt(i + 2);
                int b1 = hexadecimalToDecimal(c1);
                int b2 = hexadecimalToDecimal(c2);

                bytes.add((byte) (b1 * 16 + b2));
                i += 2;
            } else {
                bytes.add((byte) hex.charAt(i));
            }
        }
        Byte[] array = new Byte[bytes.size()];
        bytes.toArray(array);
        return ArrayUtils.toPrimitive(array);
    }

    private static int hexadecimalToDecimal(char c1) {
        return c1 >= '0' && c1 <= '9' ? c1 - '0' : c1 - 'a' + 10;
    }

}
