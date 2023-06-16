import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOperation {
    private static FileWriter writer;

    // get the file/folder name from its path, which is not . or .., and does not end with File.separator
    public static String getFileName(String path) {
        // if there is no File.separator in the path, the function still works
        int st = path.lastIndexOf(File.separator);
        String filename = path.substring(st + 1);

        int dotPos = filename.lastIndexOf('.');
        if (dotPos != -1) {
            filename = filename.substring(0, dotPos);
        }

        return filename;
    }


    // get the folder name from File folder
    // support ., ..
    private static String getFolerName(File folder) {
        String folderPath = folder.getPath();
        if (folderPath.equals(".")) {
            folderPath = System.getProperty("user.dir");
        } else if (folderPath.equals("..")) {
            Path current = Paths.get(System.getProperty("user.dir"));
            folderPath = current.getParent().toString();
        }

        return getFileName(folderPath);
    }


    public static void createWriter(File folder) {
        String folderName = getFolerName(folder);

        // create output file in the input folder, with the same name as the folder
        File outputFile = new File(folder, folderName + ".asm");
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

    public static void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeOutput(String str) {
        try {
            writer.write(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
