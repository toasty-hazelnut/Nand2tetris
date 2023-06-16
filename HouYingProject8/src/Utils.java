public class Utils {

    // if a string is numeric, return true. Otherwise, return false
    public static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // convert an integer to its binary representation in 16-character string format
    public static String intToBinaryStr(int num) {
        StringBuilder sb = new StringBuilder();
        String binaryNum = Integer.toBinaryString(num);
        for (int i = 0; i < 16 - binaryNum.length(); i++) {
            sb.append('0');
        }
        sb.append(binaryNum);
        return sb.toString();
    }
}
