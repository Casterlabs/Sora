package co.casterlabs.sora;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.NonNull;

public class SoraUtil {
    private static final int BUFFER_SIZE = 4096;

    public static byte[] readAllBytes(@NonNull InputStream in, int length) throws IOException {
        byte[] bytes = new byte[length];

        in.read(bytes);
        in.close();

        return bytes;
    }

    public static void writeInputStreamToOutputStream(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = 0;

        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    public static <T> boolean arrayContains(T[] arr, T find) {
        for (T item : arr) {
            if (item.equals(find)) {
                return true;
            }
        }

        return false;
    }

    public static boolean stringArrayContains(String[] arr, String find, boolean ignoreCase) {
        for (String item : arr) {
            if (ignoreCase && item.equalsIgnoreCase(find)) {
                return true;
            } else if (item.equals(find)) {
                return true;
            }
        }

        return false;
    }

    public static boolean stringArrayMatches(String[] arr, String find) {
        for (String item : arr) {
            if (item.matches(find)) {
                return true;
            }
        }

        return false;
    }

    public static String join(Enum<?>[] enums, String delimiter) {
        StringBuilder sb = new StringBuilder();

        for (Enum<?> e : enums) {
            sb.append(delimiter).append(e.name());
        }

        return sb.substring(delimiter.length());
    }

}
