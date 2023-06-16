import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Tokenizer {
    public static int ptr;

    private static HashSet<String> keywordDict = new HashSet<>();

    public static void init() { // initialize keywordDict
        keywordDict.add("class");
        keywordDict.add("constructor");
        keywordDict.add("function");
        keywordDict.add("method");
        keywordDict.add("field");
        keywordDict.add("static");
        keywordDict.add("var");
        keywordDict.add("int");
        keywordDict.add("char");
        keywordDict.add("boolean");
        keywordDict.add("void");
        keywordDict.add("true");
        keywordDict.add("false");
        keywordDict.add("null");
        keywordDict.add("this");
        keywordDict.add("let");
        keywordDict.add("do");
        keywordDict.add("if");
        keywordDict.add("else");
        keywordDict.add("while");
        keywordDict.add("return");
    }


    public static ArrayList<Token> tokenize(ArrayList<String> lines) {
        ArrayList<Token> tokens = new ArrayList<>();

        for (String line : lines) {
            ptr = 0;
            for (; ptr < line.length(); ) {         // iterate through the characters, and identify tokens
                char c = line.charAt(ptr);
                if (Character.isWhitespace(c)) {
                    skipWhitespace(line);
                } else if (Character.isDigit(c)) {  // integer constant
                    readInt(line, tokens);
                } else if (c == '"') {              // string constant
                    readString(line, tokens);
                } else if (Character.isLetter(c) || c == '_') { // keyword or identifier
                    readKeywordIdentifier(line, tokens);
                } else {  // symbol
                    readSymbol(line, tokens);
                }
            }

        }

        return tokens;
    }

    // skip whitespace characters, and increase ptr
    private static void skipWhitespace(String line) {
        while (ptr < line.length() && Character.isWhitespace(line.charAt(ptr))) {
            ptr++;
        }
    }

    // read an int from line[ptr], increase ptr accordingly
    private static void readInt(String line, ArrayList<Token> tokens) {
        String integer;
        int savedPtr = ptr;
        while (ptr < line.length() && Character.isDigit(line.charAt(ptr))) {
            ptr++;
        }

        integer = line.substring(savedPtr, ptr);

        tokens.add(new Token("integerConstant", integer));
    }

    // read a String from line[ptr], increase ptr accordingly
    private static void readString(String line, ArrayList<Token> tokens) {
        String str;
        ptr++;  // skip the opening "
        int savedPtr = ptr;

        while (ptr < line.length() && line.charAt(ptr) != '"') { // find the closing "
            ptr++;
        }

        str = line.substring(savedPtr, ptr);

        tokens.add(new Token("stringConstant", str));  // str can be empty string
        ptr++;  // skip the closing "
    }

    // read a keyword or identifier from line[ptr], increase ptr accordingly
    private static void readKeywordIdentifier(String line, ArrayList<Token> tokens) {
        String str;
        int savedPtr = ptr;

        // move forward ptr until the character is not letter, digit, or '_'
        while (ptr < line.length() && (Character.isLetterOrDigit(line.charAt(ptr)) || line.charAt(ptr) == '_')) {
            ptr++;
        }

        str = line.substring(savedPtr, ptr);

        if (keywordDict.contains(str)) {  // is keyword
            tokens.add(new Token("keyword", str));
        } else {    // is identifier
            tokens.add(new Token("identifier", str));
        }
    }


    // read a symbol from line[ptr], increase ptr accordingly
    private static void readSymbol(String line, ArrayList<Token> tokens) {
        tokens.add(new Token("symbol", line.substring(ptr, ptr + 1)));
        ptr++;
    }
}
