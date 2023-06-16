import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

public class Parser {

    private static int ptr;
    private static ArrayList<Token> tokens;

    private static ArrayList<String> ret;


    private static String className;
    private static String subroutineName;
    private static int labelCnt = 0;        // each subroutine has its label counter
    private static ArrayList<Hashtable<String, Identifier>> symbolTbls;

    private static HashSet<String> ops = new HashSet<>();

    public static void init() {  // init ops HashSet
        ops.add("+");
        ops.add("-");
        ops.add("*");
        ops.add("/");
        ops.add("&");
        ops.add("|");
        ops.add("<");
        ops.add(">");
        ops.add("=");
    }

    public static ArrayList<String> parse(ArrayList<Token> inputTokens) {
        ret = new ArrayList<>();
        ptr = 0;
        tokens = inputTokens;

        compileClass(); // every .jack file is the structure of a class

        return ret;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static void compileStatements() {
//        ret.add("<statements>\n");

        String val;
        boolean stop = false;
        while (!stop) {
            val = tokens.get(ptr).getVal();
            switch (val) {
                case "let":
                    compileLetStatement();
                    break;
                case "if":
                    compileIfStatement();
                    break;
                case "while":
                    compileWhileStatement();
                    break;
                case "do":
                    compileDoStatement();
                    break;
                case "return":
                    compileReturnStatement();
                    break;
                default:        // not a statement, then break the loop
                    stop = true;
                    break;
            }
        }

//        ret.add("</statements>\n");
    }

    private static void compileWhileStatement() {
        // ret.add("<whileStatement>\n");

        //addTokens(2);   // 'while' '('
        ptr += 2;

        String label1 = subroutineName + "$" + labelCnt;
        labelCnt++;

        String label2 = subroutineName + "$" + labelCnt;
        labelCnt++;

        ret.add("label " + label1 + "\n");      // label L1
        compileExpression();

        ret.add("not\n");
        ret.add("if-goto " + label2 + "\n");

        //addTokens(2);   // ')' '{'
        ptr += 2;

        compileStatements();

        ret.add("goto " + label1 + "\n");

        ret.add("label " + label2 + "\n");

        //addTokens(1);   // '}'
        ptr += 1;

        // ret.add("</whileStatement>\n");
    }

    private static void compileIfStatement() {
        //ret.add("<ifStatement>\n");

        // addTokens(2);   // 'if' '('
        ptr += 2;

        compileExpression();    // condition
        ret.add("not\n");

        String label1 = subroutineName + "$" + labelCnt;
        labelCnt++;

        ret.add("if-goto " + label1 + "\n");


        //addTokens(2);   // ')' '{'
        ptr += 2;

        compileStatements();

        String label2 = subroutineName + "$" + labelCnt;
        labelCnt++;
        ret.add("goto " + label2 + "\n");
        ret.add("label " + label1 + "\n");      // label L1, e.g. label classname.f1$1

        // addTokens(1);   // '}'
        ptr++;

        String val = tokens.get(ptr).getVal();
        if (val.equals("else")) {           // have the else clause
            //addTokens(2);               // 'else' '{'
            ptr += 2;

            compileStatements();            // If there are no statements in the else clause, we still need to increase ptr to skip 'else', '{', '}'

            //addTokens(1);               // '}'
            ptr++;
        }

        ret.add("label " + label2 + "\n");      // label L2

        //ret.add("</ifStatement>\n");
    }

    private static void compileDoStatement() {
        //ret.add("<doStatement>\n");

        // addTokens(1);       // 'do'
        ptr++;

        compileSubroutineCall();

        // addTokens(1);       // ';'
        ptr++;

        ret.add("pop temp 0\n");    // clean up returned value after call

        //ret.add("</doStatement>\n");
    }


    private static void compileSubroutineCall() {
        // addTokens(1);           // (subroutineName|className|varName)
        String name = tokens.get(ptr).getVal();
        String subroutineName;      // className.f
        Boolean isMethod;

        ptr++;
        String val = tokens.get(ptr).getVal();

        // figure out subroutineName and isMethod
        if (!val.equals(".")) {  // f()
            subroutineName = className + "." + name;
            Identifier subroutine = getIdentifier(name);
            if (subroutine.getKind().equals("method")) {
                isMethod = true;
                ret.add("push pointer 0\n");  // push the object on which the method is invoked  这里对吗。。
            } else {
                isMethod = false;
            }
        } else { // the className|varName.f() pattern

            // '.' subroutineName
            ptr++;
            String postDot = tokens.get(ptr).getVal();
            ptr++;

            Identifier prevDot = getIdentifier(name);
            if (prevDot == null || prevDot.getKind().equals("constructor") || prevDot.getKind().equals("function") ||
                    prevDot.getKind().equals("method")) {    // className.f
                subroutineName = name + "." + postDot;
                isMethod = false;
            } else {    // object1.f
                subroutineName = prevDot.getType() + "." + postDot; // 要知道object 是什么类型的，所以要存一个var是什么类型的，比如Ball类型，Student类型
                isMethod = true;
                ret.add("push " + prevDot.toString() + "\n");     // push the object on which the method is invoked
            }


        }


        // addTokens(1);                 // '('
        ptr++;

        int args = compileExpressionList();        // expressionList

        // after having pushed arguments, call the function
        args = isMethod ? args + 1 : args;
        ret.add("call " + subroutineName + " " + args + "\n");

        // addTokens(1);               // ')'
        ptr++;
    }

    private static void compileLetStatement() {
//        ret.add("<letStatement>\n");

        // addTokens(2); // 'let' varName
        ptr++;
        String leftVarName = tokens.get(ptr).getVal();
        Identifier leftVar = getIdentifier(leftVarName);
        ptr++;

        String val = tokens.get(ptr).getVal();

        // 1. let varName = exp;
        if (!val.equals("[")) {
            compileExpression();
            ret.add("pop " + leftVar.toString() + "\n");
            return;
        }

        // 2. let varName[exp1] = exp2;
        if (val.equals("[")) {      // the varName[expression] pattern
            ret.add("push " + leftVar.toString() + "\n");

            //addTokens(1);       // '['
            ptr++;

            compileExpression();  // expression1

            ret.add("add\n");


            //addTokens(1);      // ']'
            ptr++;
        }

        // addTokens(1);           // '='
        ptr++;

        compileExpression();       // expression2

        // addTokens(1);           // ';'
        ptr++;

        ret.add("pop temp 0\n");
        ret.add("pop pointer 1\n");
        ret.add("push temp 0\n");
        ret.add("pop that 0\n");

//        ret.add("</letStatement>\n");
    }

    private static void compileReturnStatement() {
//        ret.add("<returnStatement>\n");

        // addTokens(1);   // 'return'
        ptr++;

        String val = tokens.get(ptr).getVal();
        if (!val.equals(";")) {   // expression
            compileExpression();
        } else {    // return void
            ret.add("push constant 0\n");
        }

        //addTokens(1);   // ';'
        ptr++;

//        ret.add("</returnStatement>\n");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void compileExpression() {
//        ret.add("<expression>\n");

        compileTerm();

        String val = tokens.get(ptr).getVal();
        while (ops.contains(val)) { // term (op term)*
            // addTokens(1);              // op
            String op = tokens.get(ptr).getVal();
            ptr++;

            compileTerm();                // term

            writeOpSymbol(op);
            val = tokens.get(ptr).getVal();
        }

//        ret.add("</expression>\n");
    }

    private static void writeOpSymbol(String op) {
        switch (op) {
            case "+":
                ret.add("add\n");
                break;
            case "-":
                ret.add("sub\n");
                break;
            case "*":
                ret.add("call Math.multiply 2\n");
                break;
            case "/":
                ret.add("call Math.divide 2\n");
                break;
            case "&":
                ret.add("and\n");
                break;
            case "|":
                ret.add("or\n");
                break;
            case "<":
                ret.add("lt\n");
                break;
            case ">":
                ret.add("gt\n");
                break;
            case "=":
                ret.add("eq\n");
                break;
            default:
                break;
        }
    }

    private static void compileTerm() {
//        ret.add("<term>\n");

        Token t = tokens.get(ptr);
        String type = t.getType();
        String val = t.getVal();

        if (type.equals("keyword")) {
            writeKeywordConst(val);
            ptr++;
        } else if (type.equals("integerConstant")) {
            ret.add("push constant " + val + "\n");
            ptr++;
        } else if (type.equals("stringConstant")) {
            int nChars = val.length();
            ret.add("push constant " + nChars + "\n");
            ret.add("call String.new 1\n");
            for (int i = 0; i < nChars; i++) {
                ret.add("push constant " + (int) val.charAt(i) + "\n");
                ret.add("call String.appendChar 2\n");
            }

            ptr++;
        } else if (val.equals("(")) {       // '(' expression ')'
            // addTokens(1);               // '('
            ptr++;
            compileExpression();            // expression
            //addTokens(1);               // ')'
            ptr++;
        } else if (val.equals("-") || val.equals("~")) { // unaryOp term
            String op = val;
            // addTokens(1);               // '~' | '-'
            ptr++;
            compileTerm();
            writeOpSymbol(op);
        } else { // begins with identifier
            // peek next val
            String nextVal = tokens.get(ptr + 1).getVal();
            if (nextVal.equals("[")) {    // varName[expression]
                Identifier var = getIdentifier(val);
                ret.add("push " + var.toString() + "\n");

                // addTokens(2);           // varName '['
                ptr += 2;

                compileExpression();
                ret.add("add\n");
                ret.add("pop pointer 1\n");
                ret.add("push that 0\n");

                // addTokens(1);           // ']'
                ptr++;
            } else if (nextVal.equals(".") || nextVal.equals("(")) { // subroutineCall
                compileSubroutineCall();
            } else {    // varName
                Identifier var = getIdentifier(val);
                ret.add("push " + var.toString() + "\n");
                // addTokens(1);
                ptr++;
            }
        }

//        ret.add("</term>\n");
    }

    private static void writeKeywordConst(String keyword) {
        switch (keyword) {
            case "true":    // -1
                ret.add("push constant 1\n");
                ret.add("neg\n");
                break;
            case "false":   // 0
            case "null":
                ret.add("push constant 0\n");
                break;
            case "this":
                ret.add("push pointer 0\n");
                break;
        }
    }


    // the values of the expressions are pushed onto stack
    // this function returns the number of expressions in the list
    private static int compileExpressionList() {
        // ret.add("<expressionList>\n");

        int nExps = 0;

        // since in Jack, expressionList only appears in a pair of parentheses,
        // we can tell if an expressionList ends by whether the next element is ')'
        String val = tokens.get(ptr).getVal();
        if (!val.equals(")")) {   // the first expression
            compileExpression();
            nExps++;
        }

        val = tokens.get(ptr).getVal();
        while (!val.equals(")")) {    // (',' expression)*
            // addTokens(1);           // ','
            ptr++;

            compileExpression();    // expression
            nExps++;

            val = tokens.get(ptr).getVal();
        }

        // ret.add("</expressionList>\n");
        return nExps;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static void compileClass() {
//        ret.add("<class>\n");

        // 'class' keyword, classname, '{'
        // addTokens(3);
        ptr++;
        className = tokens.get(ptr).getVal();                   // set className
        ptr += 2;

        symbolTbls.add(new Hashtable<String, Identifier>());    // symbol table for the class level

        int staticCnt = 0, fieldCnt = 0;                       // counters for static variables and instance variables
        String val = tokens.get(ptr).getVal();
        while (val.equals("static") || val.equals("field")) { // classVarDec*
            if (val.equals("static")) {
                staticCnt = compileClassVarDec(staticCnt, val);
            } else {
                fieldCnt = compileClassVarDec(fieldCnt, val);
            }
            val = tokens.get(ptr).getVal();
        }

        // first pass of subroutine declarations, to store subroutine names in class-level symbol table
        int subroutinesStartPtr = ptr;
        getSubroutineNames();

        // second pass of subroutine declarations
        ptr = subroutinesStartPtr;  // reset ptr
        while (!val.equals("}")) { // subroutineDec*
            compileSubroutineDec(fieldCnt);
            val = tokens.get(ptr).getVal();
        }

        //addTokens(1);    // '}'
        ptr++;

//        ret.add("</class>\n");

        symbolTbls.remove(symbolTbls.size() - 1);     // remove class level symbol table when finishing compiling this class
    }


    // compile a classVarDec, and return updated cnt
    private static int compileClassVarDec(int cnt, String kind) {
//        ret.add("<classVarDec>\n");

        // static|field, type, varName
        // addTokens(3);
        ptr++;
        String type = tokens.get(ptr).getVal();
        ptr++;
        String name = tokens.get(ptr).getVal();
        ptr++;
        symbolTbls.get(0).put(name, new Identifier(name, kind, cnt, type));
        cnt++;


        String val = tokens.get(ptr).getVal();
        while (val.equals(",")) {     // (',' varName) *
            //addTokens(2);
            ptr++;
            name = tokens.get(ptr).getVal();
            ptr++;
            symbolTbls.get(0).put(name, new Identifier(name, kind, cnt, type));
            cnt++;
            val = tokens.get(ptr).getVal();
        }

        // addTokens(1);   // ';'
        ptr++;

//        ret.add("</classVarDec>\n");
        return cnt;
    }

    // get subroutine names of this class, and store them in the class-level symbol table
    private static void getSubroutineNames() {
        String val;

        // find 'constructor'/'function'/'method'
        while (ptr < tokens.size()) {
            val = tokens.get(ptr).getVal();
            if (val.equals("constructor") || val.equals("function") || val.equals("method")) {
                ptr += 2;
                String name = tokens.get(ptr).getVal();
                symbolTbls.get(0).put(name, new Identifier(name, val));
                ptr += 5;
            } else {
                ptr++;
            }
        }

    }

    private static void compileSubroutineDec(int fieldCnt) {
//        ret.add("<subroutineDec>\n");

        //addTokens(4);   // ('constructor'|'function'|'method') ('void' | type) subroutineName '('
        String subroutineKind = tokens.get(ptr).getVal();   // constructor/function/method
        ptr++;
        // Boolean returnVoid = tokens.get(ptr).getVal().equals("void") ? true : false;
        ptr++;
        subroutineName = className + "." + tokens.get(ptr).getVal();    // className.f
        ptr += 2;

        labelCnt = 0;   // reset labelCnt


        symbolTbls.add(new Hashtable<String, Identifier>());    // symbol table for the subroutine level

        if (subroutineKind.equals("method")) {
            compileParameterList(1);
        } else {
            compileParameterList(0);
        }

        //addTokens(1);   // ')'
        ptr++;

        compileSubroutineBody(subroutineKind, fieldCnt);

//        ret.add("</subroutineDec>\n");

        symbolTbls.remove(symbolTbls.size() - 1);         // remove subroutine level symbol table when finishing compiling this subroutine
    }

    private static void compileParameterList(int startIdx) { // 0 for constructor and function, 1 for method
//        ret.add("<parameterList>\n");

        String val = tokens.get(ptr).getVal();
        String name, type;

        if (!val.equals(")")) {   // type varName
            type = tokens.get(ptr).getVal();
            ptr++;
            name = tokens.get(ptr).getVal();
            symbolTbls.get(1).put(name, new Identifier(name, "argument", startIdx, type));
            startIdx++;
            ptr++;
            //addTokens(2);
        }

        val = tokens.get(ptr).getVal();
        while (!val.equals(")")) {        // ',' type varName
            //addTokens(3);
            ptr++;
            type = tokens.get(ptr).getVal();
            ptr++;
            name = tokens.get(ptr).getVal();
            symbolTbls.get(1).put(name, new Identifier(name, "argument", startIdx, type));
            startIdx++;
            ptr++;

            val = tokens.get(ptr).getVal();
        }

//        ret.add("</parameterList>\n");
    }

    private static void compileSubroutineBody(String subroutineKind, int fieldCnt) {
//        ret.add("<subroutineBody>\n");


        //addTokens(1);   // '{'
        ptr++;

        // local variables
        int localVarIdx = 0;
        String val = tokens.get(ptr).getVal();
        while (val.equals("var")) {  // varDec*
            localVarIdx = compileVarDec(localVarIdx);
            val = tokens.get(ptr).getVal();
        }

        ret.add("function " + className + "." + subroutineName + " " + localVarIdx + "\n");  // function A.f nVars

        // set this, (and allocate memory)
        if (subroutineKind.equals("constructor")) {
            ret.add("push " + fieldCnt + "\n");
            ret.add("call Memory.alloc 1\n");
            ret.add("pop pointer 0\n");
        } else if (subroutineKind.equals("method")) {
            ret.add("push argument 0\n");   // 'this' is the first argument
            ret.add("pop pointer 0\n");
        }

        //
        compileStatements();

        //addTokens(1);   // '}'
        ptr++;


//        ret.add("</subroutineBody>\n");
    }

    private static int compileVarDec(int localVarIdx) {
//        ret.add("<varDec>\n");

        //addTokens(3);  // 'var' type varName
        ptr++;
        String type = tokens.get(ptr).getVal();
        ptr++;
        String name = tokens.get(ptr).getVal();
        symbolTbls.get(0).put(name, new Identifier(name, "local", localVarIdx, type));
        localVarIdx++;
        ptr++;

        String val = tokens.get(ptr).getVal();
        while (val.equals(",")) {     // (',' varName)*
            //addTokens(2);
            ptr++;
            name = tokens.get(ptr).getVal();
            symbolTbls.get(0).put(name, new Identifier(name, "local", localVarIdx, type));
            localVarIdx++;
            ptr++;
            val = tokens.get(ptr).getVal();
        }

        //addTokens(1);   // ';'
        ptr++;

//        ret.add("</varDec>\n");

        return localVarIdx;
    }


    // add n tokens into ret, and move the ptr accordingly
    private static void addTokens(int n) {
        for (int i = 0; i < n; i++) {
            ret.add(tokens.get(ptr).toString());
            ptr++;
        }
    }

    // look up from the current table upwards
    private static Identifier getIdentifier(String name) {
        for (int i = symbolTbls.size() - 1; i >= 0; i--) {
            Hashtable<String, Identifier> mp = symbolTbls.get(i);
            if (mp.contains(name)) {
                return mp.get(name);
            }
        }
        return null;
    }

}
