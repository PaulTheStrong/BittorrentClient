package by.bsuir.ksis.kursovoi.utils;

import com.github.cdefgah.bencoder4j.model.*;

import java.util.Iterator;

public class BencodePrinter {

    public static void printBencodedString(BencodedByteSequence byteSequence) {
        System.out.print(new String(byteSequence.getByteSequence()));
    }

    public static void printBencodedInteger(BencodedInteger bencodedInteger) {
        System.out.print(((BencodedInteger) bencodedInteger).toString());
    }

    public static void printBencodedDictionary(BencodedDictionary dictionary) {
        Iterator<BencodedByteSequence> keysIterator = dictionary.getKeysIterator();
        System.out.println("{");
        while (keysIterator.hasNext()) {
            BencodedByteSequence key = keysIterator.next();
            BencodedObject value = dictionary.get(key);
            printBencodedString(key);
            System.out.print(" : ");
            printBencodedObject(value);
            System.out.println();
        }
        System.out.println("}");
    }

    public static void printBencodedList(BencodedList list) {
        System.out.print("[");
        int size = list.size();
        for (int i = 0; i < size; i++) {
            printBencodedObject(list.get(i));
            if (i != size - 1) {
                System.out.print(", ");
            }
        }
        System.out.print("]");
    }

    public static void printBencodedObject(BencodedObject value) {
        if (value instanceof BencodedByteSequence) {
            printBencodedString((BencodedByteSequence) value);
        } else if (value instanceof BencodedInteger) {
            printBencodedInteger((BencodedInteger) value);
        }
        else if (value instanceof BencodedDictionary) {
            printBencodedDictionary((BencodedDictionary) value);
        } else if (value instanceof BencodedList) {
            printBencodedList((BencodedList) value);
        }

    }

}
