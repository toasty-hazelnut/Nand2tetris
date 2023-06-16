import java.util.HashMap;

public class TblInitializer {
    private static String newline = System.lineSeparator();

    // push the value in D register to stack ;
    private static String pushDToStack = "@SP" + newline + "AM=M+1" + newline + "A=A-1" + newline + "M=D" + newline;

    // pop the value at the top of stack to D register
    private static String popStackToD = "@SP" + newline + "AM=M-1" + newline + "D=M" + newline;

    public static void initCmdTbl(HashMap<String, String> cmdTbl) {
        initArithmeticCmd(cmdTbl);
        initMemoryCmd(cmdTbl);
        initFlowFuncCmd(cmdTbl);
    }

    private static void initArithmeticCmd(HashMap<String, String> cmdTbl) {

        // add, sub, and, or
        String addCommon = "@SP" + newline + "AM=M-1" + newline + "D=M" + newline + "A=A-1" + newline;
        cmdTbl.put("add", addCommon + "M=D+M" + newline);
        cmdTbl.put("sub", addCommon + "M=M-D" + newline);
        cmdTbl.put("and", addCommon + "M=D&M" + newline);
        cmdTbl.put("or", addCommon + "M=D|M" + newline);

        // gt, lt, eq
        // use # as label placeholders
        String gt = "@SP" + newline + "AM=M-1" + newline + "D=M" + newline + "A=A-1" + newline + "D=M-D" + newline
                + "M=-1" + newline + "@#" + newline + "D;JGT" + newline + "@SP" + newline + "A=M-1" + newline + "M=0" + newline + "(#)" + newline;
        cmdTbl.put("gt", gt);

        String lt = gt.replace("JGT", "JLT");
        cmdTbl.put("lt", lt);

        String eq = gt.replace("JGT", "JEQ");
        cmdTbl.put("eq", eq);

        // not, neg
        String not = "@SP" + newline + "A=M-1" + newline + "M=!M" + newline;
        cmdTbl.put("not", not);

        String neg = "@SP" + newline + "A=M-1" + newline + "M=-M" + newline;
        cmdTbl.put("neg", neg);

    }

    private static void initMemoryCmd(HashMap<String, String> cmdTbl) {

        String constant = "@#" + newline + "D=A" + newline + pushDToStack;
        cmdTbl.put("pushconstant", constant);

        String pushLocalCommon = "D=M" + newline + "@#" + newline + "A=D+A" + newline + "D=M" + newline + pushDToStack;
        cmdTbl.put("pushlocal", "@LCL" + newline + pushLocalCommon);
        cmdTbl.put("pushargument", "@ARG" + newline + pushLocalCommon);
        cmdTbl.put("pushthis", "@THIS" + newline + pushLocalCommon);
        cmdTbl.put("pushthat", "@THAT" + newline + pushLocalCommon);

        String popLocalCommon = "D=M" + newline + "@#" + newline + "D=D+A" + newline + "@R13" + newline + "M=D" + newline
                + popStackToD + "@R13" + newline + "A=M" + newline + "M=D" + newline;
        cmdTbl.put("poplocal", "@LCL" + newline + popLocalCommon);
        cmdTbl.put("popargument", "@ARG" + newline + popLocalCommon);
        cmdTbl.put("popthis", "@THIS" + newline + popLocalCommon);
        cmdTbl.put("popthat", "@THAT" + newline + popLocalCommon);

        cmdTbl.put("pushtemp", "@#" + newline + "D=M" + newline + pushDToStack);
        cmdTbl.put("pushpointer0", "@THIS" + newline + "D=M" + newline + pushDToStack);
        cmdTbl.put("pushpointer1", "@THAT" + newline + "D=M" + newline + pushDToStack);

        cmdTbl.put("poptemp", popStackToD + "@#" + newline + "M=D" + newline);
        cmdTbl.put("poppointer0", popStackToD + "@THIS" + newline + "M=D" + newline);
        cmdTbl.put("poppointer1", popStackToD + "@THAT" + newline + "M=D" + newline);

        cmdTbl.put("pushstatic", "@#" + newline + "D=M" + newline + pushDToStack);
        cmdTbl.put("popstatic", popStackToD + "@#" + newline + "M=D" + newline);
    }

    private static void initFlowFuncCmd(HashMap<String, String> cmdTbl) {
        cmdTbl.put("label", "(#)" + newline);
        cmdTbl.put("goto", "@#" + newline + "0;JMP" + newline);

        // if the topmost element of the stack is not 0, jump
        cmdTbl.put("if-goto", popStackToD + "@#" + newline + "D;JNE" + newline);

        // push 0 to stack. initialize local variable to 0
        cmdTbl.put("function", "@SP" + newline + "AM=M+1" + newline + "A=A-1" + newline + "M=0" + newline);

        putCall(cmdTbl);    // add "call f n" command
        putReturn(cmdTbl);  // add "return" command
    }

    private static void putCall(HashMap<String, String> cmdTbl) {
        StringBuilder stb = new StringBuilder();

        stb.append("D=A" + newline + pushDToStack); // push return address
        stb.append("@LCL" + newline + "D=M" + newline + pushDToStack);  // push LCL
        stb.append("@ARG" + newline + "D=M" + newline + pushDToStack);  // push ARG
        stb.append("@THIS" + newline + "D=M" + newline + pushDToStack);  // push THIS
        stb.append("@THAT" + newline + "D=M" + newline + pushDToStack);  // push THAT

        stb.append("@SP" + newline + "D=M" + newline + "@#" + newline + "D=D-A" + newline + "@ARG" + newline + "M=D" + newline); // ARG = SP - n - 5.  # is n+5
        stb.append("@SP" + newline + "D=M" + newline + "@LCL" + newline + "M=D" + newline); // LCL=SP
        stb.append("@%" + newline + "0;JMP" + newline); // goto f.   % is function name


        cmdTbl.put("call", stb.toString());
    }

    private static void putReturn(HashMap<String, String> cmdTbl) {
        StringBuilder stb = new StringBuilder();

        stb.append("@LCL" + newline + "D=M" + newline + "@FRAME" + newline + "M=D" + newline); // FRAME = LCL
        stb.append("@FRAME" + newline + "D=M" + newline + "@5" + newline + "A=D-A" + newline + "D=M" + newline + "@RET" + newline + "M=D" + newline); // RET = *(FRAME-5)
        stb.append("@SP" + newline + "AM=M-1" + newline + "D=M" + newline + "@ARG" + newline + "A=M" + newline + "M=D" + newline); // *ARG = pop()
        stb.append("@ARG" + newline + "D=M+1" + newline + "@SP" + newline + "M=D" + newline); // SP = ARG+1

        String getFrameMinus1 = "@FRAME" + newline + "AM=M-1" + newline + "D=M" + newline;    // FRAME--; store RAM[FRAME] to D
        stb.append(getFrameMinus1 + "@THAT" + newline + "M=D" + newline); // FRAME--; THAT = *FRAME
        stb.append(getFrameMinus1 + "@THIS" + newline + "M=D" + newline); // FRAME--; THIS = *FRAME
        stb.append(getFrameMinus1 + "@ARG" + newline + "M=D" + newline); // FRAME--; ARG = *FRAME
        stb.append(getFrameMinus1 + "@LCL" + newline + "M=D" + newline); // FRAME--; LCL = *FRAME

        stb.append("@RET" + newline + "A=M" + newline + "0;JMP" + newline); // goto RET

        cmdTbl.put("return", stb.toString());
    }

}
