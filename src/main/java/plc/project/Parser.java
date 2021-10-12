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
    private ParseException errorHandle(String message) {
        if (tokens.has(0)) {
            return new ParseException(message, tokens.get(0).getIndex());
        } else {
            return new ParseException(message, (tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length()));
        }
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        List<Ast.Global> g = new ArrayList<>();
        List<Ast.Function> f = new ArrayList<>();
        while ((peek("LIST") || peek("VAR") || peek("VAL")) && tokens.has(0)){
            g.add(parseGlobal());
        }
        while(match("FUN") && tokens.has(0)) {
            f.add(parseFunction());
        }
        if(tokens.has(0)) throw errorHandle("Nothing expected after functions");
        return new Ast.Source(g,f);
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     * global ::= ( list | mutable | immutable ) ';'
     */
    public Ast.Global parseGlobal() throws ParseException {
        if (match("LIST")){
            Ast.Global g = parseList();
            if (match(";")) return g;
            else throw errorHandle("Expected ;");
        } else if (match("VAR")){
            Ast.Global g = parseMutable();
            if (match(";")) return g;
            else throw errorHandle("Expected ;");
        } else if (match("VAL")){
            Ast.Global g = parseImmutable();
            if (match(";")) return g;
            else throw errorHandle("Expected ;");
        } else throw errorHandle("Expected LIST, VAR or VAL");
        //throw errorHandle("Expected LIST, VAR or VAL");
    }

    /**
     * Parses the {@code list} rule. This method should only be called if the
     * next token declares a list, aka {@code LIST}.
     * list ::= 'LIST' identifier '=' '[' expression (',' expression)* ']'
     */
    public Ast.Global parseList() throws ParseException {
        if (match(Token.Type.IDENTIFIER)){
            String id = tokens.get(-1).getLiteral();
            List<Ast.Expression> expressions = new ArrayList<>();
            if (match("=")){
                if (match("[")){
                    if (match("]")) return new Ast.Global(id,true,Optional.empty());
                    expressions.add(parseExpression());
                    while (match(",")){
                        expressions.add(parseExpression());
                    }
                    if (match("]")) return new Ast.Global(id,true,Optional.of(new Ast.Expression.PlcList(expressions)));
                } else throw errorHandle("Expected [");
            } else throw errorHandle("Expected =");
        } else throw errorHandle("Expected identifier");
        throw new UnsupportedOperationException();
    }

    /**
     * Parses the {@code mutable} rule. This method should only be called if the
     * next token declares a mutable global variable, aka {@code VAR}.
     * mutable ::= 'VAR' identifier ('=' expression)?
     */
    public Ast.Global parseMutable() throws ParseException {
        if (match(Token.Type.IDENTIFIER)) {
            String id = tokens.get(-1).getLiteral();
            if (match("=")){
                return new Ast.Global(id,true,Optional.of(parseExpression()));
            }
            return new Ast.Global(id,true,Optional.empty());
        } else throw errorHandle("Expected identifier");
    }

    /**
     * Parses the {@code immutable} rule. This method should only be called if the
     * next token declares an immutable global variable, aka {@code VAL}.
     * immutable ::= 'VAL' identifier '=' expression
     */
    public Ast.Global parseImmutable() throws ParseException {
        if (match(Token.Type.IDENTIFIER)) {
            String id = tokens.get(-1).getLiteral();
            if (match("=")){
                return new Ast.Global(id,false,Optional.of(parseExpression()));
            } else throw errorHandle("Expected =");
        } else throw errorHandle("Expected identifier");
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     * function ::= 'FUN' identifier '(' (identifier (',' identifier)* )? ')' 'DO' block 'END'
     */
    public Ast.Function parseFunction() throws ParseException {
        if (match(Token.Type.IDENTIFIER)){
            String id = tokens.get(-1).getLiteral();
            if (match("(")){
                List<String> list = new ArrayList<>();
                if (match(Token.Type.IDENTIFIER)){
                    list.add(tokens.get(-1).getLiteral());
                }
                while (match(",")){
                    if (match(Token.Type.IDENTIFIER)){
                        list.add(tokens.get(-1).getLiteral());
                    } else throw errorHandle("Expected ID");
                }
                if (match(")")) {
                    if (match("DO")) {
                        List<Ast.Statement> b = parseBlock();
                        if (match("END")) {
                            return new Ast.Function(id, list, b);
                        } else throw errorHandle("Expected END");
                    } else throw errorHandle("Expected DO");
                } else throw errorHandle("Expected )");
            } else throw errorHandle("Expected (");
        } else throw errorHandle("Expected id");
    }

    /**
     * Parses the {@code block} rule. This method should only be called if the
     * preceding token indicates the opening a block.
     * block ::= statement*
     */
    public List<Ast.Statement> parseBlock() throws ParseException {
        List<Ast.Statement> list = new ArrayList<>();
        while (peek("LET") || peek("SWITCH") || peek("IF") || peek("RETURN")){
            list.add(parseStatement());
        }
        while (tokens.has(0) && !peek("END") && !peek("ELSE") && !peek("DEFAULT") && !peek("RETURN")){
            list.add(parseStatement());
        }
        return list;
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     * statement ::=
     *     'LET' identifier ('=' expression)? ';' |
     *     'SWITCH' expression ('CASE' expression ':' block)* 'DEFAULT' block 'END' |
     *     'IF' expression 'DO' block ('ELSE' block)? 'END' |
     *     'WHILE' expression 'DO' block 'END' |
     *     'RETURN' expression ';' |
     *     expression ('=' expression)? ';'
     */
    public Ast.Statement parseStatement() {
        if (match("LET")){
            return parseDeclarationStatement();
        } else if (match("SWITCH")){
            return parseSwitchStatement();
        } else if (match("IF")){
            return parseIfStatement();
        } else if (match("RETURN")){
            return parseReturnStatement();
        } else if (match("WHILE")){
            return parseWhileStatement();
        }
        Ast.Expression st = parseExpression();
        if (match("=")) {
            Ast.Expression st2 = parseExpression();
            if (match(";")) return new Ast.Statement.Assignment(st, st2);
            else throw new ParseException("Missing semicolon", tokens.index);
        } else if (match(";")) return new Ast.Statement.Expression(st);
        else throw new ParseException("Missing semicolon", tokens.index);
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        if (!match(Token.Type.IDENTIFIER)) throw new ParseException("Expected id", tokens.index);
        Optional<Ast.Expression> value = Optional.empty();
        String s = tokens.get(-1).getLiteral();
        if (match("=")){
            value = Optional.of(parseExpression());
            if (match(";")) return new Ast.Statement.Declaration(s,value);
            else throw errorHandle("Missing semicolon");
        }
        return new Ast.Statement.Declaration(s,Optional.empty());
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
            Ast.Expression expr = parseExpression();
            if (match("DO")){
                List<Ast.Statement> dolist = parseBlock();
                List<Ast.Statement> a = new ArrayList<>();
                if (match("ELSE")){
                     a.addAll(parseBlock());
                }
                if (match("END")){
                    return new Ast.Statement.If(expr,dolist,a);
                } else throw errorHandle("Expected ELSE");
            } else throw errorHandle("Expected DO");
    }

    /**
     * Parses a switch statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a switch statement, aka
     * {@code SWITCH}.
     * 'SWITCH' expression ('CASE' expression ':' block)* 'DEFAULT' block 'END'
     */
    public Ast.Statement.Switch parseSwitchStatement() throws ParseException {
        Ast.Expression expr = parseExpression();
        List<Ast.Statement.Case> cases = new ArrayList<>();
        while (peek("CASE")) {
            cases.add(parseCaseStatement());
        }
        if (peek("DEFAULT")){
            cases.add(parseCaseStatement());
        } else throw errorHandle("Missing DEFAULT");
        if (match("END")) return new Ast.Statement.Switch(expr,cases);
        else throw errorHandle("Expected END");
    }

    /**
     * Parses a case or default statement block from the {@code switch} rule.
     * This method should only be called if the next tokens start the case or
     * default block of a switch statement, aka {@code CASE} or {@code DEFAULT}.
     */
    public Ast.Statement.Case parseCaseStatement() throws ParseException {
        if (match("CASE")){
            Ast.Expression e = parseExpression();
            if (match(":")){
                return new Ast.Statement.Case(Optional.of(e),parseBlock());
            } else throw errorHandle("Missing :");
        } else if (match("DEFAULT")){
            return new Ast.Statement.Case(Optional.empty(),parseBlock());
        } else throw errorHandle("Expected DEFAULT");
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     * 'WHILE' expression 'DO' block 'END'
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {
            Ast.Expression expr = parseExpression();
            if (match("DO")){
                List<Ast.Statement> b = parseBlock();
                if (match("END")){
                    return new Ast.Statement.While(expr,b);
                } else throw  errorHandle("Expected END");
            } else throw errorHandle("Expected DO");
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     * 'RETURN' expression ';'
     */
    public Ast.Statement.Return parseReturnStatement() throws ParseException {
            Ast.Expression expr = parseExpression();
            if (match(";")) return new Ast.Statement.Return(expr);
            else throw errorHandle("Expected ;");
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
            if (s.contains("\\")) {
                s = s.replace("\\n", "\n")
                        .replace("\\t", "\t")
                        .replace("\\b", "\b")
                        .replace("\\r", "\r")
                        .replace("\\'", "'")
                        .replace("\\\\", "\\")
                        .replace("\\\"", "\"");
            }
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
            else throw errorHandle("Missing closing bracket");
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
                else throw errorHandle("Expected )");
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
            else throw errorHandle("Missing closing bracket");
        }
        throw errorHandle("Not a literal");
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
