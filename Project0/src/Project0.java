import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Project0 {

    private static boolean multilineComment;        // need to find the right half */ or not
    private static boolean hasWritten;              // for current line in input file, any characters in it written to the output file?


    public static void main(String[] args) {
        File inputFile = new File(args[0]);
        String filename = getFilename(inputFile);

        Scanner scanner;    // prepare to read from input file
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // create output file in the same directory with input file, with the same filename
        File outputFile = new File(inputFile.getParentFile(), filename + ".out");
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileWriter writer;
        try {
            writer = new FileWriter(outputFile.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        multilineComment = false;
        while (scanner.hasNextLine()) {              // read input file line by line
            String line = scanner.nextLine();
            hasWritten = false;
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
                        st = handleLeftHalfOrSingle(line, st, writer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }

            if (hasWritten) {
                // if we have written characters from current line of input file to output file, then write a new line separator
                try {
                    writer.write(System.lineSeparator());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        try {
            writer.close();              // close the writer
        } catch (IOException e) {
            throw new RuntimeException(e);
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
    private static int handleLeftHalfOrSingle(String line, int st, FileWriter writer) throws IOException {
        // st is the first non-whitespace character

        // find the first occurrence of /* and //, respectively
        int idxLeftHalf = line.indexOf("/*", st);
        int idxSingleComment = line.indexOf("//", st);

        // if there isn't /* or // in the remaining characters of current line,
        // write the remaining characters to output, set hasWritten to true, and move to the next line.
        if (idxLeftHalf == -1 && idxSingleComment == -1) {
            String sub = line.substring(st);
            writer.write(sub);
            hasWritten = true;
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
        writer.write(sub);
        if (sub.length() != 0) {         // if sub is not empty, set hasWritten to true
            hasWritten = true;
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

    private static String getFilename(File inputFile) {
        // get filename from inputfile path.
        // the filename is between the last File.separator and the last .
        int st = inputFile.getPath().lastIndexOf(File.separator);   // if there is no File.separator, still work
        int end = inputFile.getPath().lastIndexOf('.');
        return inputFile.getPath().substring(st + 1, end);
    }
}






























