package plc.project;

public final class ParseException extends RuntimeException {

    private final int index;

    public ParseException(String message, int index) {
        super(message);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
