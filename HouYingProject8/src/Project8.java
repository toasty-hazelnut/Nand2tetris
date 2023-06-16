import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Project8 {
    // each function has its own counter, to count the labels in it
    private static HashMap<String, Integer> counter = new HashMap<>();

    private static HashMap<String, String> cmdTbl = new HashMap<>();

    private static String newline = System.lineSeparator();

    public static void main(String[] args) {
        TblInitializer.initCmdTbl(cmdTbl);

        File folder = new File(args[0]);

        // create a writer
        FileOperation.createWriter(folder);

        counter.put("", 0);  // when we're not in any function (only possible in bootstrap code), set its counter to 0

        writeBootstrapCode();

        File[] files = folder.listFiles();
        for (File file : files) {
            // only process .vm files in the folder
            if (!file.getPath().endsWith(".vm")) {
                continue;
            }

            String filename = FileOperation.getFileName(file.getPath());

            // remove comments, blank lines, and leading white space
            // After this call, each element in lines is an instruction.
            ArrayList<String> lines = new ArrayList<>();
            RemoveWhiteAndComments.remove(file, lines);

            translateLines(lines, filename);
        }

        FileOperation.closeWriter();
    }

    private static String handleLenOneCmd(String[] words, String currentFuncName) {
        String asmCode;
        if (words[0].equals("gt") || words[0].equals("lt") || words[0].equals("eq")) {
            // generate unique label:  <function name>$<counter>
            // replace # with label
            int counterVal = counter.get(currentFuncName);
            String label = currentFuncName + "$" + counterVal;
            asmCode = cmdTbl.get(words[0]).replace("#", label);
            asmCode = asmCode.replace("#", label);

            // counter++
            counter.replace(currentFuncName, counterVal + 1);
        } else { // add, sub, and, or, neg, not, return.
            asmCode = cmdTbl.get(words[0]);
        }
        return asmCode;
    }

    private static String handleMemoryCommand(String[] words, String filename) {
        String asmCode;

        if (words[1].equals("static")) {
            // use filename.<words[2]> for static variable name
            String varName = filename + "." + words[2];
            asmCode = cmdTbl.get(words[0] + "static").replace("#", varName);

        } else if (words[1].equals("pointer")) {
            // look up in the table
            asmCode = cmdTbl.get(words[0] + words[1] + words[2]);

        } else if (words[1].equals("temp")) {
            // calculate RAM address directly, 'cause the temp segment always starts from RAM[5]
            int address = Integer.parseInt(words[2]) + 5;
            asmCode = cmdTbl.get(words[0] + "temp").replace("#", String.valueOf(address));

        } else { // argument, local, this, that, constant
            // replace # with words[2]
            asmCode = cmdTbl.get(words[0] + words[1]).replace("#", words[2]);
        }

        return asmCode;
    }


    private static String handleFuncDeclaration(String[] words) {
        StringBuilder stb = new StringBuilder();

        stb.append("(" + words[1] + ")" + newline);     // label for the function definition

        int n = Integer.parseInt(words[2]);

        String push0 = cmdTbl.get(words[0]);
        for (int i = 0; i < n; i++) {                   // push n local variables to the stack, with initial value 0
            stb.append(push0);
        }

        return stb.toString();
    }

    private static String handleCallCmd(String[] words, String currentFuncName) {
        String asmcode = "";

        // generate unique label:  <function name>$<counter>
        int counterVal = counter.get(currentFuncName);
        String label = currentFuncName + "$" + counterVal;

        // counter++
        counter.replace(currentFuncName, counterVal + 1);

        asmcode += "@" + label + newline;
        asmcode += cmdTbl.get(words[0]);
        asmcode += "(" + label + ")" + newline;

        // replace # with n+5
        int n = Integer.parseInt(words[2]);
        asmcode = asmcode.replace("#", String.valueOf(n + 5));

        // replace % with function name
        asmcode = asmcode.replace("%", words[1]);

        return asmcode;
    }

    private static void writeBootstrapCode() {
        // SP=256
        String setSP = "@256" + newline + "D=A" + newline + "@SP" + newline + "M=D" + newline;
        FileOperation.writeOutput(setSP);

        // call Sys.init
        ArrayList<String> lines = new ArrayList<>();
        lines.add("call Sys.init 0");
        translateLines(lines, "");
    }

    private static void translateLines(ArrayList<String> lines, String filename) {
        String currentFuncName = "";

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] words = line.split(" +");
            String asmCode;

            if (words.length == 1) { // arithmetic commands or "return" command
                asmCode = handleLenOneCmd(words, currentFuncName);
            } else if (words.length == 2) { // label xx, goto xx, if-goto xx
                asmCode = cmdTbl.get(words[0]).replace("#", currentFuncName + "$" + words[1]);

            } else if (words[0].equals("pop") || words[0].equals("push")) { // push/pop segment i
                asmCode = handleMemoryCommand(words, filename);

            } else if (words[0].equals("function")) { // function f nVars
                // update current function name, and initialize a counter for the current function
                currentFuncName = words[1];
                counter.put(currentFuncName, 0);

                asmCode = handleFuncDeclaration(words);
            } else { // call f nArgs
                asmCode = handleCallCmd(words, currentFuncName);
            }

            FileOperation.writeOutput(asmCode);
        }
    }

}


