import java.util.ArrayList;
import java.util.HashSet;

public class Parser {

    private static int ptr;
    private static ArrayList<Token> tokens;

    private static ArrayList<String> ret;

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

    private static void compileStatements() {
        ret.add("<statements>\n");

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

        ret.add("</statements>\n");
    }

    private static void compileWhileStatement() {
        ret.add("<whileStatement>\n");

        addTokens(2);   // 'while' '('

        compileExpression();

        addTokens(2);   // ')' '{'

        compileStatements();

        addTokens(1);   // '}'

        ret.add("</whileStatement>\n");
    }

    private static void compileIfStatement() {
        ret.add("<ifStatement>\n");

        addTokens(2);   // 'if' '('

        compileExpression();

        addTokens(2);   // ')' '{'

        compileStatements();

        addTokens(1);   // '}'

        String val = tokens.get(ptr).getVal();
        if (val.equals("else")) {           // have the else clause
            addTokens(2);               // 'else' '{'
            compileStatements();
            addTokens(1);               // '}'
        }

        ret.add("</ifStatement>\n");
    }

    private static void compileDoStatement() {
        ret.add("<doStatement>\n");

        addTokens(1);       // 'do'

        compileSubroutineCall();

        addTokens(1);       // ';'

        ret.add("</doStatement>\n");
    }

    private static void compileSubroutineCall() {
        addTokens(1);           // (subroutineName|className|varName)
        String val = tokens.get(ptr).getVal();
        if (val.equals(".")) {      // the className|varName.subroutineName pattern
            addTokens(2);       // '.' subroutineName
        }

        addTokens(1);                 // '('
        compileExpressionList();        // expressionList
        addTokens(1);               // ')'
    }

    private static void compileLetStatement() {
        ret.add("<letStatement>\n");

        addTokens(2); // 'let' varName

        String val = tokens.get(ptr).getVal();
        if (val.equals("[")) {      // the varName[expression] pattern
            addTokens(1);       // '['
            compileExpression();  // expression
            addTokens(1);      // ']'
        }

        addTokens(1);           // '='
        compileExpression();       // expression
        addTokens(1);           // ';'

        ret.add("</letStatement>\n");
    }

    private static void compileReturnStatement() {
        ret.add("<returnStatement>\n");

        addTokens(1);   // 'return'

        String val = tokens.get(ptr).getVal();
        if (!val.equals(";")) {   // expression
            compileExpression();
        }

        addTokens(1);   // ';'

        ret.add("</returnStatement>\n");
    }

    private static void compileExpression() {
        ret.add("<expression>\n");

        compileTerm();

        String val = tokens.get(ptr).getVal();
        if (ops.contains(val)) {
            addTokens(1);              // op
            compileTerm();                // term
            val = tokens.get(ptr).getVal();
        }

        ret.add("</expression>\n");
    }

    private static void compileTerm() {
        ret.add("<term>\n");

        Token t = tokens.get(ptr);
        String type = t.getType();
        String val = t.getVal();

        if (type.equals("keyword") || type.equals("integerConstant") || type.equals("stringConstant")) {
            addTokens(1);
        } else if (val.equals("(")) {       // '(' expression ')'
            addTokens(1);               // '('
            compileExpression();            // expression
            addTokens(1);               // ')'
        } else if (val.equals("-") || val.equals("~")) { // unaryOp term
            addTokens(1);               // '~' | '-'
            compileTerm();
        } else { // begins with identifier
            if ((ptr + 1) < tokens.size()) {
                String nextVal = tokens.get(ptr + 1).getVal();
                if (nextVal.equals("[")) {    // varName[expression]
                    addTokens(2);           // varName '['
                    compileExpression();
                    addTokens(1);           // ']'
                } else if (nextVal.equals(".") || nextVal.equals("(")) { // subroutineCall
                    compileSubroutineCall();
                } else {    // varName
                    addTokens(1);
                }
            } else {    // varName
                addTokens(1);
            }
        }

        ret.add("</term>\n");
    }


    private static void compileExpressionList() {
        ret.add("<expressionList>\n");

        // since in Jack, expressionList only appears in a pair of parentheses,
        // we can tell if an expressionList ends by whether the next element is ')'
        String val = tokens.get(ptr).getVal();
        if (!val.equals(")")) {   // the first expression
            compileExpression();
        }

        val = tokens.get(ptr).getVal();
        while (!val.equals(")")) {    // (',' expression)*
            addTokens(1);           // ','
            compileExpression();    // expression
            val = tokens.get(ptr).getVal();
        }

        ret.add("</expressionList>\n");
    }

    private static void compileClass() {
        ret.add("<class>\n");

        // 'class' keyword, classname, '{'
        addTokens(3);

        String val = tokens.get(ptr).getVal();
        while (val.equals("static") || val.equals("field")) { // classVarDec*
            compileClassVarDec();
            val = tokens.get(ptr).getVal();
        }

        while (!val.equals("}")) { // subroutineDec*
            compileSubroutineDec();
            val = tokens.get(ptr).getVal();
        }

        addTokens(1);    // '}'

        ret.add("</class>\n");

    }

    private static void compileClassVarDec() {
        ret.add("<classVarDec>\n");

        // static|field, type, varName
        addTokens(3);

        String val = tokens.get(ptr).getVal();
        while (val.equals(",")) {     // (',' varName) *
            addTokens(2);
            val = tokens.get(ptr).getVal();
        }

        addTokens(1);   // ';'

        ret.add("</classVarDec>\n");

    }

    private static void compileSubroutineDec() {
        ret.add("<subroutineDec>\n");

        addTokens(4);   // ('constructor'|'function'|'method') ('void' | type) subroutineName '('

        compileParameterList();

        addTokens(1);   // ')'

        compileSubroutineBody();

        ret.add("</subroutineDec>\n");
    }

    private static void compileParameterList() {
        ret.add("<parameterList>\n");

        String val = tokens.get(ptr).getVal();

        if (!val.equals(")")) {   // type varName
            addTokens(2);
        }

        val = tokens.get(ptr).getVal();
        while (!val.equals(")")) {        // ',' type varName
            addTokens(3);
            val = tokens.get(ptr).getVal();
        }

        ret.add("</parameterList>\n");
    }

    private static void compileSubroutineBody() {
        ret.add("<subroutineBody>\n");

        addTokens(1);   // '{'

        String val = tokens.get(ptr).getVal();
        while (val.equals("var")) {  // varDec*
            compileVarDec();
            val = tokens.get(ptr).getVal();
        }

        compileStatements();

        addTokens(1);   // '}'

        ret.add("</subroutineBody>\n");
    }

    private static void compileVarDec() {
        ret.add("<varDec>\n");

        addTokens(3);  // 'var' type varName

        String val = tokens.get(ptr).getVal();
        while (val.equals(",")) {     // (',' varName)*
            addTokens(2);
            val = tokens.get(ptr).getVal();
        }

        addTokens(1);   // ';'

        ret.add("</varDec>\n");
    }

    // add n tokens into ret, and move the ptr accordingly
    private static void addTokens(int n) {
        for (int i = 0; i < n; i++) {
            ret.add(tokens.get(ptr).toString());
            ptr++;
        }
    }
}
