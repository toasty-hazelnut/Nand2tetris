import java.io.File;
import java.util.ArrayList;

public class Project11 {
    public static void main(String[] args) {
        File input = new File(args[0]);     // a .jack file or a folder which contains .jack files
        File outFolder;                     // the folder to store output files
        File[] files;

        if (input.isDirectory()) {
            files = input.listFiles();
            outFolder = input;
        } else {
            files = new File[]{input};
            outFolder = input.getParentFile();
        }

        Tokenizer.init();
        Parser.init();

        for (File file : files) {
//             only process .jack files
            if (!file.getPath().endsWith(".jack")) {
                continue;
            }

            // remove comments, blank lines, and leading white space
            // After this call, each element in lines is an instruction.
            ArrayList<String> lines = new ArrayList<>();
            RemoveWhiteAndComments.remove(file, lines);

            // tokenize
            ArrayList<Token> tokens = Tokenizer.tokenize(lines);

            ArrayList<String> tokenStr = new ArrayList<>();
            tokenStr.add("<tokens>\n");
            for (Token token : tokens) {
                tokenStr.add(token.toString());
            }
            tokenStr.add("</tokens>\n");
            FileOp.writeStrings(outFolder, file, tokenStr,"T");

            // parse
            ArrayList<String> parseResults = Parser.parse(tokens);

            // write output
            FileOp.writeStrings(outFolder, file, parseResults, "");
        }
    }
}
