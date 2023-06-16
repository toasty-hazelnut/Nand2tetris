import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class RemoveWhiteAndComments {
    private static boolean multilineComment;        // need to find the right half */ or not

    public static void remove(File inputFile, ArrayList<String> instLines) {

        Scanner scanner;    // prepare to read from input file
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        multilineComment = false;
        while (scanner.hasNextLine()) {              // read input file line by line
            String line = scanner.nextLine();
            int st = 0;

            while (st < line.length()) {
                if (multilineComment) {               // if we need to find */
                    st = findRightHalf(line, st);
                } else {
                    // remove leading white space
                    while (st < line.length() && Character.isWhitespace(line.charAt(st))) {
                        st++;
                    }
                    if (st == line.length()) {   // if all remaining characters are white spaces, move on to the next line
                        break;
                    }

                    try {
                        st = handleLeftHalfOrSingle(line, st, instLines);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }

        }

    }

    // find */ from index st in string line
    private static int findRightHalf(String line, int st) {
        int idxRightHalf = line.indexOf("*/", st);
        // if we find the right half */, increment st to the character after */, and switch multilineComment to false
        // if we don't find it, return line.length(), so that we can move to the next line in input file to continue finding
        if (idxRightHalf != -1) {
            multilineComment = false;
            st = idxRightHalf + 2;
            return st;
        } else {
            return line.length();
        }
    }

    // handle // or /*
    private static int handleLeftHalfOrSingle(String line, int st, ArrayList<String> instLines) throws IOException {
        // st is the first non-whitespace character

        // find the first occurrence of /* and //, respectively
        int idxLeftHalf = line.indexOf("/*", st);
        int idxSingleComment = line.indexOf("//", st);

        // if there isn't /* or // in the remaining characters of current line,
        // write the remaining characters to output, set hasWritten to true, and move to the next line.
        if (idxLeftHalf == -1 && idxSingleComment == -1) {
            String sub = line.substring(st);
            instLines.add(sub);
            return line.length();
        }

        // we pick the first occurrence of // or /*.
        // whichever comes first acts as a comment. set idxSubstrEnd to its position
        int idxSubstrEnd;
        if (idxLeftHalf == -1) {
            idxSubstrEnd = idxSingleComment;
        } else if (idxSingleComment == -1) {
            idxSubstrEnd = idxLeftHalf;
        } else {
            idxSubstrEnd = Math.min(idxLeftHalf, idxSingleComment);
        }

        // get the string between st and the comment symbol, and write it to the output
        String sub = line.substring(st, idxSubstrEnd);
        if (sub.length() != 0) {         // if sub is not empty, set hasWritten to true
            instLines.add(sub);
        }

        // if the comment symbol that takes effect is /*, then we need to find the right half, i.e. */.
        // if the comment symbol that takes effect is //, then move to the next line
        if (idxSubstrEnd == idxLeftHalf) {
            multilineComment = true;
            st = idxLeftHalf + 2;
            return st;
        } else {
            return line.length();
        }

    }
}


