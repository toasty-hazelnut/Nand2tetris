public class Identifier {
    private String name;
    private String kind;  // static, field, argument, local, constructor, function, method
    private int idx;        // index in the corresponding segment

    private String type;    // int, boolean, char, Array, user defined class ...

    public Identifier(String name, String kind, int idx) {
        this.name = name;
        this.kind = kind;
        this.idx = idx;
    }

    public Identifier(String name, String kind, int idx, String type) {
        this.name = name;
        this.kind = kind;
        this.idx = idx;
        this.type = type;
    }

    public Identifier(String name, String kind) {   // idx isn't applicable to subroutine name
        this.name = name;
        this.kind = kind;
    }

    public String toString() {
        return kind + " " + idx;
    }

    public String getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
