package co.casterlabs.sora;

public class SoraUtil {

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
