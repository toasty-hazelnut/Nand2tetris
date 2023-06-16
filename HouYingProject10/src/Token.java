public class Token {
    private String val;
    private String type;

    public Token(String type, String val) {
        this.val = val;
        this.type = type;
    }

    public String toString() {
        String outputVal = val;
        switch (val) {
            case "<":
                outputVal = "&lt;";
                break;
            case ">":
                outputVal = "&gt;";
                break;
            case "&":
                outputVal = "&amp;";
                break;
        }
        return "<" + type + "> " + outputVal + " </" + type + ">\n";
    }

    public String getVal() {
        return val;
    }

    public String getType() {
        return type;
    }
}
