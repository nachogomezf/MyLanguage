package plc.project;

public final class Token {

    public enum Type {
        IDENTIFIER,
        INTEGER,
        DECIMAL,
        CHARACTER,
        STRING,
        OPERATOR
    }

    private final Type type;
    private final String literal;
    private final int index;

    public Token(Type type, String literal, int index) {
        this.type = type;
        this.literal = literal;
        this.index = index;
    }

    public Type getType() {
        return type;
    }

    public String getLiteral() {
        return literal;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Token
                && type == ((Token) obj).type
                && literal.equals(((Token) obj).literal)
                && index == ((Token) obj).index;
    }

    @Override
    public String toString() {
        return type + "=" + literal + "@" + index;
    }

}
