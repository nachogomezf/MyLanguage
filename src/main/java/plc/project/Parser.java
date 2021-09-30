package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architect
 * ure to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Global parseGlobal() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code list} rule. This method should only be called if the
     * next token declares a list, aka {@code LIST}.
     */
    public Ast.Global parseList() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code mutable} rule. This method should only be called if the
     * next token declares a mutable global variable, aka {@code VAR}.
     */
    public Ast.Global parseMutable() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code immutable} rule. This method should only be called if the
     * next token declares an immutable global variable, aka {@code VAL}.
     */
    public Ast.Global parseImmutable() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Function parseFunction() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code block} rule. This method should only be called if the
     * preceding token indicates the opening a block.
     */
    public List<Ast.Statement> parseBlock() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Statement parseStatement() {
        Ast.Expression st = parseExpression();
        if (match("=")){
            Ast.Expression st2 = parseExpression();
            if (match(";")) return new Ast.Statement.Assignment(st,st2);
            else throw new ParseException("Missing semicolon", tokens.index);
        }
        if (match(";")) return new Ast.Statement.Expression(st);
        else throw new ParseException("Missing semicolon", tokens.index);
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a switch statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a switch statement, aka
     * {@code SWITCH}.
     */
    public Ast.Statement.Switch parseSwitchStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a case or default statement block from the {@code switch} rule.
     * This method should only be called if the next tokens start the case or
     * default block of a switch statement, aka {@code CASE} or {@code DEFAULT}.
     */
    public Ast.Statement.Case parseCaseStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Statement.Return parseReturnStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression() {
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expression parseLogicalExpression() {
        Ast.Expression left = parseComparisonExpression();
        while(match("&&") || match("||")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expression right = parseComparisonExpression();
            left = new Ast.Expression.Binary(operator,left,right);
        }
        return left;
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expression parseComparisonExpression() {
        Ast.Expression left = parseAdditiveExpression();
        while (match("<") || match(">") || match("==") || match("!=")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expression right = parseAdditiveExpression();
            left = new Ast.Expression.Binary(operator,left,right);
        }
        return left;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression() {
        Ast.Expression left = parseMultiplicativeExpression();
        while(match("+") || match("-")){
            String operator = tokens.get(-1).getLiteral();
            Ast.Expression right = parseMultiplicativeExpression();
            left = new Ast.Expression.Binary(operator,left,right);
        }
        return left;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression(){
        Ast.Expression left = parsePrimaryExpression();
        while(match("*") || match("/") || match("^")){
            String operator = tokens.get(-1).getLiteral();
            Ast.Expression right = parsePrimaryExpression();
            left = new Ast.Expression.Binary(operator,left,right);
        }
        return left;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expression parsePrimaryExpression(){
        if (match("NIL")){
            return new Ast.Expression.Literal(null);
        }
        else if (match("TRUE")){
            return new Ast.Expression.Literal(Boolean.TRUE);
        } else if (match("FALSE")){
            return new Ast.Expression.Literal(Boolean.FALSE);
        } else if (peek(Token.Type.INTEGER)){
                BigInteger num = new BigInteger((tokens.get(0)).getLiteral());
                match(Token.Type.INTEGER);
                return new Ast.Expression.Literal(num);
        } else if (peek(Token.Type.DECIMAL)){
            BigDecimal num = new BigDecimal((tokens.get(0)).getLiteral());
            match(Token.Type.DECIMAL);
            return new Ast.Expression.Literal(num);
        } else if (peek(Token.Type.CHARACTER)){
            String s = tokens.get(0).getLiteral();
            return new Ast.Expression.Literal(s.charAt(1));
        } else if (peek(Token.Type.STRING)) {
            String s = tokens.get(0).getLiteral();
            s = s.substring(1,s.length()-1);
            if (s.contains("\\")) {
                s = s.replace("\\n", "\n")
                        .replace("\\t", "\t")
                        .replace("\\b", "\b")
                        .replace("\\r", "\r")
                        .replace("\\'", "'")
                        .replace("\\\\", "\\")
                        .replace("\\\"", "\"");
            }
            return new Ast.Expression.Literal(s);
        } else if (match("(")){
            Ast.Expression expr = parseExpression();
            if (match(")")) return new Ast.Expression.Group(expr);
            else throw new ParseException("Missing closing bracket",tokens.index);
        } else if (match(Token.Type.IDENTIFIER)){
            String id = tokens.get(-1).getLiteral();
            //if (!match("(") || !match("[")) return new Ast.Expression.Access(Optional.empty(),id);
            if (match("(")){
                if(match(")")) return new Ast.Expression.Function(id, Collections.emptyList());
                List<Ast.Expression> list = new ArrayList<>();
                list.add(parseExpression());
                while (match(",")){
                    list.add(parseExpression());
                }
                if (match(")")){
                    return new Ast.Expression.Function(id,list);
                }
                else throw new ParseException("Missing closing parenthesis", tokens.index);
            }
            else if (match("[")) {
                Ast.Expression st = parseExpression();
                if (match("]")) {
                    return new Ast.Expression.Access(Optional.of(st), id);
                } else return new Ast.Expression.Access(Optional.empty(),id);
            } else return new Ast.Expression.Access(Optional.empty(),id);
        } else if (match("(")){
            Ast.Expression expr = parseExpression();
            if (match(")")) return new Ast.Expression.Group(expr);
            else throw new ParseException("Missing closing bracket", tokens.index);
        }
        throw new ParseException("Error parsing", tokens.index);
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for ( int i = 0; i < patterns.length; i++ ){
            if (!tokens.has(i)) {
                return false;
            } else if (patterns[i] instanceof Token.Type){
                if (patterns[i] != tokens.get(i).getType()){
                    return false;
                }
            } else if (patterns[i] instanceof String){
                if (!patterns[i].equals(tokens.get(i).getLiteral())){
                    return false;
                }
            } else {
                throw new AssertionError("Invalid pattern object " + patterns[i].getClass());
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);
        if (peek) {
            for (int i =0; i<patterns.length; i++){
                tokens.advance();
            }
        }
        return peek;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

        public Token previous() {
            return tokens.get(index - 1);
        }

    }

}
