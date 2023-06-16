import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Project6 {
    private static HashMap<String, Integer> symbolTbl;

    private static HashMap<String, String> jmpTbl;
    private static HashMap<String, String> compTbl;
    private static HashMap<String, String> destTbl;

    private static int nextVariableAddr = 16;       // next available RAM address to store variables, starting from 16

    private static void setJmpTbl() {
        jmpTbl = new HashMap<>();
        jmpTbl.put(null, "000");
        jmpTbl.put("JGT", "001");
        jmpTbl.put("JEQ", "010");
        jmpTbl.put("JGE", "011");
        jmpTbl.put("JLT", "100");
        jmpTbl.put("JNE", "101");
        jmpTbl.put("JLE", "110");
        jmpTbl.put("JMP", "111");
    }

    private static void setCompTbl() {
        compTbl = new HashMap<>();
        compTbl.put("0", "0101010");
        compTbl.put("1", "0111111");
        compTbl.put("-1", "0111010");

        compTbl.put("D", "0001100");
        compTbl.put("A", "0110000");
        compTbl.put("M", "1110000");

        compTbl.put("!D", "0001101");
        compTbl.put("!A", "0110001");
        compTbl.put("!M", "1110001");

        compTbl.put("-D", "0001111");
        compTbl.put("-A", "0110011");
        compTbl.put("-M", "1110011");

        compTbl.put("D+1", "0011111");
        compTbl.put("A+1", "0110111");
        compTbl.put("M+1", "1110111");

        compTbl.put("D-1", "0001110");
        compTbl.put("A-1", "0110010");
        compTbl.put("M-1", "1110010");

        compTbl.put("D+A", "0000010");
        compTbl.put("D+M", "1000010");

        compTbl.put("D-A", "0010011");
        compTbl.put("D-M", "1010011");
        compTbl.put("A-D", "0000111");
        compTbl.put("M-D", "1000111");

        compTbl.put("D&A", "0000000");
        compTbl.put("D&M", "1000000");
        compTbl.put("D|A", "0010101");
        compTbl.put("D|M", "1010101");
    }

    private static void setDestTbl() {
        destTbl = new HashMap<>();
        destTbl.put(null, "000");
        destTbl.put("A", "100");
        destTbl.put("D", "010");
        destTbl.put("M", "001");
        destTbl.put("AD", "110");
        destTbl.put("MD", "011");
        destTbl.put("AM", "101");
        destTbl.put("AMD", "111");
    }

    private static void initializeSymbolTbl() {
        symbolTbl = new HashMap<>();
        symbolTbl.put("SP", 0);
        symbolTbl.put("LCL", 1);
        symbolTbl.put("ARG", 2);
        symbolTbl.put("THIS", 3);
        symbolTbl.put("THAT", 4);
        symbolTbl.put("SCREEN", 16384);
        symbolTbl.put("KBD", 24576);
        for (int i = 0; i <= 15; i++) { // R0-R15
            symbolTbl.put("R" + String.valueOf(i), i);
        }

    }

    public static void main(String[] args) {
        // initialize jmpTbl, destTbl, compTbl, symbolTbl
        setJmpTbl();
        setDestTbl();
        setCompTbl();
        initializeSymbolTbl();

        ArrayList<String> lines = new ArrayList<String>();

        // first pass: remove comments, blank lines, and leading white space
        // After this call, each element in lines is an instruction.
        RemoveWhiteAndComments.remove(args[0], lines);

        // second pass: find labels
        findLabels(lines);

        // create a writer
        FileWriter writer = createWriter(args[0]);

        // third pass: translate each A/C instruction to machine language
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            stb.delete(0, stb.length());        // clear stb
            String line = lines.get(i);

            if (line.charAt(0) == '@') {
                handleAInst(stb, line);
            } else {
                handleCInst(stb, line);
            }

            // write stb as a line to the output file
            try {
                writer.write(stb.toString());
                writer.write(System.lineSeparator());
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

    // handle A instruction
    private static void handleAInst(StringBuilder stb, String line) {
        line = line.substring(1).trim();
        int numA;                       // numA is the value to be loaded in A
        if (Utils.isNumeric(line)) {    // @number
            numA = Integer.parseInt(line);
        } else {                        // @symbol
            if (symbolTbl.containsKey(line)) {
                numA = symbolTbl.get(line);
            } else {
                numA = nextVariableAddr;
                symbolTbl.put(line, nextVariableAddr);
                nextVariableAddr++;
            }
        }
        stb.append(Utils.intToBinaryStr(numA));
    }

    // handle C instruction
    private static void handleCInst(StringBuilder stb, String line) {
        // parse the code to dest, comp, and jump, according to = and ;
        int equalIdx = line.indexOf('=');
        int semicolonIdx = line.indexOf(';');
        String comp, dest = null, jump = null;
        if (equalIdx != -1) {
            dest = line.substring(0, equalIdx);
        }
        if (semicolonIdx != -1) {
            jump = line.substring(semicolonIdx + 1).trim();  // remove possible trailing whitespace
        }
        int compEnd = (semicolonIdx != -1) ? semicolonIdx : line.length();
        comp = line.substring(equalIdx + 1, compEnd).trim();

        stb.append("111");
        stb.append(compTbl.get(comp));
        stb.append(destTbl.get(dest));
        stb.append(jmpTbl.get(jump));
    }


    // find all labels and store them to symbolTbl
    private static void findLabels(ArrayList<String> lines) {
        for (int i = 0; i < lines.size(); ) {
            String line = lines.get(i);
            // the current line is a label
            if (line.charAt(0) == '(') {
                int rightParenthesis = line.indexOf(')');
                String label = line.substring(1, rightParenthesis);
                symbolTbl.put(label, i);
                // removing the current line automatically brings the next line to position index i
                lines.remove(i);
            } else {
                // if current line is not a label, move to the next line
                i++;
            }
        }
    }


    private static FileWriter createWriter(String filepath) {
        // get filename from inputfile path.
        // the filename is between the last File.separator and the last .
        File inputFile = new File(filepath);
        int st = inputFile.getPath().lastIndexOf(File.separator);   // if there is no File.separator, still work
        int end = inputFile.getPath().lastIndexOf('.');
        String filename = inputFile.getPath().substring(st + 1, end);

        // create output file in the same directory with input file, with the same filename
        File outputFile = new File(inputFile.getParentFile(), filename + ".hack");
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
