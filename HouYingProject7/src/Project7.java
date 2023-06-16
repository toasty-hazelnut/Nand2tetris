import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Project7 {
    private static HashMap<String, String> arithmeticTbl;

    private static HashMap<String, String> memoryTbl;

    private static HashMap<String, Integer> counterTbl;

    private static final String newline = System.lineSeparator();

    private static String filename;

    private static void initCounter() {
        counterTbl = new HashMap<>();
        counterTbl.put("gt", 0);
        counterTbl.put("lt", 0);
        counterTbl.put("eq", 0);
    }

    private static void initArithmeticTbl() {
        arithmeticTbl = new HashMap<>();

        // add, sub, and, or
        String addCommon = "@SP" + newline + "AM=M-1" + newline + "D=M" + newline + "A=A-1" + newline;
        arithmeticTbl.put("add", addCommon + "M=D+M" + newline);
        arithmeticTbl.put("sub", addCommon + "M=M-D" + newline);
        arithmeticTbl.put("and", addCommon + "M=D&M" + newline);
        arithmeticTbl.put("or", addCommon + "M=D|M" + newline);

        // gt, lt, eq
        // use # as label placeholders
        String gt = "@SP" + newline + "AM=M-1" + newline + "D=M" + newline + "A=A-1" + newline + "D=M-D" + newline
                + "M=-1" + newline + "@#" + newline + "D;JGT" + newline + "@SP" + newline + "A=M-1" + newline + "M=0" + newline + "(#)" + newline;
        arithmeticTbl.put("gt", gt);

        String lt = gt.replace("JGT", "JLT");
        arithmeticTbl.put("lt", lt);

        String eq = gt.replace("JGT", "JEQ");
        arithmeticTbl.put("eq", eq);

        // not, neg
        String not = "@SP" + newline + "A=M-1" + newline + "M=!M" + newline;
        arithmeticTbl.put("not", not);

        String neg = "@SP" + newline + "A=M-1" + newline + "M=-M" + newline;
        arithmeticTbl.put("neg", neg);

    }

    private static void initMemoryTbl() {
        memoryTbl = new HashMap<>();

        String pushDToStack = "@SP" + newline + "AM=M+1" + newline + "A=A-1" + newline + "M=D" + newline;  // push the value in D register to stack
        String popStackToD = "@SP" + newline + "AM=M-1" + newline + "D=M" + newline;    // pop the value at the top of stack to D register

        String constant = "@#" + newline + "D=A" + newline + pushDToStack;
        memoryTbl.put("pushconstant", constant);

        String pushLocalCommon = "D=M" + newline + "@#" + newline + "A=D+A" + newline + "D=M" + newline + pushDToStack;
        memoryTbl.put("pushlocal", "@LCL" + newline + pushLocalCommon);
        memoryTbl.put("pushargument", "@ARG" + newline + pushLocalCommon);
        memoryTbl.put("pushthis", "@THIS" + newline + pushLocalCommon);
        memoryTbl.put("pushthat", "@THAT" + newline + pushLocalCommon);

        String popLocalCommon = "D=M" + newline + "@#" + newline + "D=D+A" + newline + "@R13" + newline + "M=D" + newline
                + popStackToD + "@R13" + newline + "A=M" + newline + "M=D" + newline;
        memoryTbl.put("poplocal", "@LCL" + newline + popLocalCommon);
        memoryTbl.put("popargument", "@ARG" + newline + popLocalCommon);
        memoryTbl.put("popthis", "@THIS" + newline + popLocalCommon);
        memoryTbl.put("popthat", "@THAT" + newline + popLocalCommon);

        memoryTbl.put("pushtemp", "@#" + newline + "D=M" + newline + pushDToStack);
        memoryTbl.put("pushpointer0", "@THIS" + newline + "D=M" + newline + pushDToStack);
        memoryTbl.put("pushpointer1", "@THAT" + newline + "D=M" + newline + pushDToStack);

        memoryTbl.put("poptemp", popStackToD + "@#" + newline + "M=D" + newline);
        memoryTbl.put("poppointer0", popStackToD + "@THIS" + newline + "M=D" + newline);
        memoryTbl.put("poppointer1", popStackToD + "@THAT" + newline + "M=D" + newline);

        memoryTbl.put("pushstatic", "@#" + newline + "D=M" + newline + pushDToStack);
        memoryTbl.put("popstatic", popStackToD + "@#" + newline + "M=D" + newline);
    }


    public static void main(String[] args) {
        initArithmeticTbl();
        initMemoryTbl();
        initCounter();

        ArrayList<String> lines = new ArrayList<String>();

        // remove comments, blank lines, and leading white space
        // After this call, each element in lines is an instruction.
        RemoveWhiteAndComments.remove(args[0], lines);

        // create a writer
        FileWriter writer = createWriter(args[0]);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] words = line.split(" +");
            String asmCode;

            if (words.length == 1) {
                asmCode = handleArithmetic(words);
            } else {
                asmCode = handleMemoryCommand(words);
            }

            try {
                writer.write(asmCode);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        try {
            writer.close();              // close the writer
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static String handleArithmetic(String[] words) {
        String asmCode;
        if (words[0].equals("gt") || words[0].equals("lt") || words[0].equals("eq")) {
            // generate unique label:  <filename>%continue<gt/lt/eq><counter>
            // replace # with label
            int counterVal = counterTbl.get(words[0]);
            String label = filename + "%continue" + words[0] + counterVal;
            asmCode = arithmeticTbl.get(words[0]).replaceAll("#", label);

            // counter++
            counterTbl.replace(words[0], counterVal + 1);
        } else {
            asmCode = arithmeticTbl.get(words[0]);
        }
        return asmCode;
    }

    private static String handleMemoryCommand(String[] words) {
        String asmCode;

        if (words[1].equals("static")) {
            // use filename.<words[2]> for static variable name
            String varName = filename + "." + words[2];
            asmCode = memoryTbl.get(words[0] + "static").replace("#", varName);

        } else if (words[1].equals("pointer")) {
            // look up in the table
            asmCode = memoryTbl.get(words[0] + words[1] + words[2]);

        } else if (words[1].equals("temp")) {
            // calculate RAM address directly, 'cause the temp segment always starts from RAM[5]
            int address = Integer.parseInt(words[2]) + 5;
            asmCode = memoryTbl.get(words[0] + "temp").replace("#", String.valueOf(address));

        } else { // argument, local, this, that, constant
            // replace # with words[2]
            asmCode = memoryTbl.get(words[0] + words[1]).replace("#", words[2]);
        }

        return asmCode;
    }

    private static FileWriter createWriter(String filepath) {
        // get filename from inputfile path.
        // the filename is between the last File.separator and the last .
        File inputFile = new File(filepath);
        int st = inputFile.getPath().lastIndexOf(File.separator);   // if there is no File.separator, still work
        int end = inputFile.getPath().lastIndexOf('.');
        filename = inputFile.getPath().substring(st + 1, end);

        // create output file in the same directory with input file, with the same filename
        File outputFile = new File(inputFile.getParentFile(), filename + ".asm");
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // create writer
        FileWriter writer;
        try {
            writer = new FileWriter(outputFile.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer;
    }
}

