package plc.project;

import java.util.ArrayList;
import java.util.List;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are * helpers you need to use, they will make the implementation a lot easier. */
public final class Lexer {

    private final CharStream chars;
    private int start = 0;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        //throw new UnsupportedOperationException(); //TODO
        List<Token> tokens=new ArrayList<Token>();
        while(chars.has(0)) {
            if (!match("[ \b\n\r\t]")) {
                chars.skip();
                tokens.add(lexToken());
            }
        }
        return tokens;
    }
    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     *
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     * @return
     */
    public Token lexToken() {

        if (peek("[@A-Za-z]")){
            return lexIdentifier();
        }
        if (peek("[\\-0-9]")){
            return lexNumber();
        }
        if (peek("'")){
            return lexCharacter();
        }
        if (peek("\"")){
            return lexString();
        }
        if (peek("'\\' [bnrt'\"\\\\]")){
            return lexEscape();
        }
        if (peek("[!=]'='?|'&&'|'||'|.")) {
            return lexOperator();
        }
        //return null;
        throw new ParseException("Lexing error",this.chars.getIndex()); //TODO
    }

    public Token lexIdentifier() {
        match("[@A-Za-z]");
        while (peek("[A-Za-z0-9_-]")) {
            match("[A-Za-z0-9_-]");
        }
        return chars.emit(Token.Type.IDENTIFIER);
    }

    public Token lexNumber() {
        if (peek("\\-")){
            match("\\-");
        }
        /*
        if(peek("[1-9]")){
            match("[1-9]");
        }
        */
        if (peek("0")){
            match("0");

            if (peek("\\.")){
                match("\\.");
            } else if (peek("[0-9]")) {
                throw new ParseException("INVALID LEADING ZERO", this.chars.getIndex());
            }
            while (peek("[0-9]")){
                match("[0-9]");
            }
            return chars.emit(Token.Type.DECIMAL);
        }

        else if (peek("[1-9]")){
            match("[1-9]");
            if (peek("\\.")){
                match("\\.");
                if (peek("[0-9]")) {
                    match("[0-9]");
                    while (peek("[0-9]")){
                        match("[0-9]");
                    }
                    return chars.emit(Token.Type.DECIMAL);
                } else {
                    throw new ParseException("INVALID DECIMAL NUMBER", this.chars.getIndex());
                }

            }
            while(peek("[0-9]")) {
                match("[0-9]");
            }

            if (peek("\\.")){
                match("\\.");

                {
                    while (peek("[0-9]")) {
                        match("[0-9]");
                    }
                    return this.chars.emit(Token.Type.DECIMAL);
                }
            }
        }
        return chars.emit(Token.Type.INTEGER);
    }

    public Token lexCharacter() {
        match("\'");
        if (peek("\\\\")){
            match("\\\\");
            if (peek("[bnrt\'\"\\\\]")){
                match("[bnrt\'\"\\\\]");
            }
            if (peek("\'")){
                match("\'");
            }
            return chars.emit(Token.Type.CHARACTER);
        }
        else {
            if (peek("[^\']")){
                match("[^\']");
                if (peek("\'")) {
                    match("\'");
                }
                return chars.emit(Token.Type.CHARACTER);
            }
        }
        return null;
        /*
        while (peek("[^'\\n\\r\\\\]|'\\'[bnrt'\"\\\\]")){
            match("[^'\\n\\r\\\\]|['\n']");
        }
        */


    }

    public Token lexString() {
        match("\"");

        while (peek("[^\"]")){
            if (peek("\\\\")){
                match("\\\\");
                if (peek("[bnrt\"\'\\\\]")){
                    match("[bnrt\"\'\\\\]");
                }
                else{
                    throw new ParseException("INVALID ESCAPE",this.chars.getIndex());
                }
            }
            match("[^\"]");
        }
        if (peek("\"")){
            match("\"");
            return chars.emit(Token.Type.STRING);
        }
        else {
            throw new ParseException("Unterminated string",chars.getIndex());
        }
    }

    public Token lexEscape() {
        match("'\\'[bnrt'\"\\\\]");
        return null;
    }

    public Token lexOperator() {
        if (peek("!")){
            match("!");
            if (peek("=")) match("=");
            return chars.emit(Token.Type.OPERATOR);
        }
        else if (peek("|")){
            match("|");
            if (peek("|")) match("|");
            return chars.emit(Token.Type.OPERATOR);
        }
        else if (peek("&")){
            match("&");
            if (peek("&")) match("&");
        }
        match("[!=]'='?|'&&'|'||'|.");
        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
        //throw new UnsupportedOperationException();
        for ( int i=0; i<patterns.length; i++){
            if ( !chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])) {
                return false;
            }
            return true;
        }
        System.out.println("Token generated");
        //throw new UnsupportedOperationException();
        return true;
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        //throw new UnsupportedOperationException();
        boolean peek = peek(patterns);
        if (peek){
            for (int i=0; i<patterns.length; i++){
                chars.advance();
            }
        }
        return peek;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     *
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0; //!

        public int getLength(){ return this.length; };

        public int getIndex(){ return this.index; };

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }
}