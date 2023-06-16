import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileOp {
    private static FileWriter writer;       // current writer

    // create a .xml file with the same name as input file in the folder, and write stings to it.
    public static void writeStrings(File folder, File input, ArrayList<String> strings, String filenameAdditional) {
        createWriter(folder, input, filenameAdditional);

        try {
            for (String string : strings) {
                writer.write(string);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        closeWriter();
    }

    // get the file/folder name from its path, which is not . or .., and does not end with File.separator
    private static String getFileName(String path) {
        // if there is no File.separator in the path, the function still works
        int st = path.lastIndexOf(File.separator);
        String filename = path.substring(st + 1);

        int dotPos = filename.lastIndexOf('.');
        if (dotPos != -1) {
            filename = filename.substring(0, dotPos);
        }

        return filename;
    }

    // create a writer, to write into the newly created output file (with the same name as the inputFile) in the folder
    private static void createWriter(File folder, File inputFile, String filenameAdditional) {
        String filename = getFileName(inputFile.getPath());

        // create output file in the input folder, with the same name as the folder
        File outputFile = new File(folder, filename + filenameAdditional + ".xml");
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // create writer
        try {
            writer = new FileWriter(outputFile.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
