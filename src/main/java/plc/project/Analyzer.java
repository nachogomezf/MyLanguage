package plc.project;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Void> {

    public Scope scope;
    private Ast.Function function;
    private Ast.Expression returnType;

    public Analyzer(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Void visit(Ast.Source ast) {

        for(Ast.Global g : ast.getGlobals()) {
            visit(g);
        }
        for(Ast.Function f : ast.getFunctions()) {
            visit(f);
        }


        if (scope.lookupFunction("main",0).getName().isEmpty()) throw new RuntimeException();
        requireAssignable(Environment.Type.INTEGER, scope.lookupFunction("main", 0).getReturnType());
        //if (scope.lookupFunction("main",0).getReturnType() instanceof Environment.Type.INTEGER){}
        return null;
    }

    @Override
    public Void visit(Ast.Global ast) {
        if (ast.getValue().isPresent()) visit(ast.getValue().get());
        ast.setVariable(scope.defineVariable(ast.getName(), ast.getName(), Environment.getType(ast.getTypeName()), ast.getMutable(), Environment.NIL));
        requireAssignable(ast.getVariable().getType(), ast.getValue().get().getType());
        return null;
    }

    @Override
    public Void visit(Ast.Function ast) {
        List<Environment.Type> paramTypes = new ArrayList<>();
        ast.getParameterTypeNames().forEach(s -> {
            paramTypes.add(Environment.getType(s));
        });

        Environment.Type returnType = Environment.Type.NIL;
        if (ast.getReturnTypeName().isPresent()) {
            returnType = Environment.getType(ast.getReturnTypeName().get());
        }

        ast.setFunction(scope.defineFunction(ast.getName(), ast.getName(), paramTypes, returnType, args -> Environment.NIL));
        try {
            scope = new Scope(scope);

            for (int i = 0; i < ast.getParameters().size(); i++) {
                scope.defineVariable(ast.getParameters().get(i), ast.getParameters().get(i), paramTypes.get(i), true, Environment.NIL);
            }

            for (Ast.Statement stmt : ast.getStatements()) {
                visit(stmt);
            }
        } finally {
            scope = scope.getParent();
        }

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        if (!(ast.getExpression() instanceof Ast.Expression.Function)) throw new RuntimeException();
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {

        if ( ast.getTypeName().isPresent() && ast.getValue().isPresent()){
            visit(ast.getValue().get());
            if ( !(ast.getTypeName().get().equals(ast.getValue().get().getType().getName()))) throw new RuntimeException();
            ast.setVariable(scope.defineVariable(ast.getName(), ast.getTypeName().get(),ast.getValue().get().getType(), true, Environment.create(ast.getValue().get())));
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        if ( !(ast.getReceiver() instanceof Ast.Expression.Access)){
            throw new RuntimeException();
        }
        visit(ast.getValue());
        visit(ast.getReceiver());
        requireAssignable(scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).getType(),ast.getValue().getType());
        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        /*visit(ast.getCondition());
        requireAssignable(ast.getCondition().getType(), Environment.Type.BOOLEAN);
        if (ast.getThenStatements().isEmpty()) throw new RuntimeException();
        try{
            scope = new Scope(scope);
            ast.getThenStatements().forEach(this::visit);
        }
        finally{
            scope = scope.getParent();
        }
        if (!ast.getElseStatements().isEmpty()) {
            try {
                scope = new Scope(scope);
                ast.getElseStatements().forEach(this::visit);
            } finally {
                scope = scope.getParent();
            }
        }
        return null;*/
        visit(ast.getCondition());
        if (ast.getCondition().getType() != Environment.Type.BOOLEAN || ast.getThenStatements().isEmpty()) {
            throw new RuntimeException();
        }

        for (Ast.Statement then : ast.getThenStatements()) {
            try {
                scope = new Scope(scope);
                visit(then);
            } finally {
                scope = scope.getParent();
            }
        }
        for (Ast.Statement elseStmt : ast.getElseStatements()) {
            try {
                scope = new Scope(scope);
                visit(elseStmt);
            } finally {
                scope = scope.getParent();
            }
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Switch ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Case ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        visit(ast.getCondition());
        if (ast.getCondition().getType() != Environment.Type.BOOLEAN) {
            scope = new Scope(scope);
            for (Ast.Statement stmt : ast.getStatements()) {
                visit(stmt);
            }
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {

        if (ast.getValue().getType() != returnType.getType()) {
            throw new RuntimeException();
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {
        if ( ast.getLiteral() instanceof BigInteger){
            if (((BigInteger) ast.getLiteral()).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0){
                throw new RuntimeException();
            }
            else if (((BigInteger) ast.getLiteral()).compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0){
                throw new RuntimeException();
            }
            else {
                ast.setType(Environment.Type.INTEGER);
            }
        }
        else if ( ast.getLiteral() instanceof BigDecimal){
            if ( (((BigDecimal) ast.getLiteral()).doubleValue() == Double.NEGATIVE_INFINITY) || (((BigDecimal) ast.getLiteral()).doubleValue() == Double.POSITIVE_INFINITY)){
                throw new RuntimeException();
            }
            else {
                ast.setType(Environment.Type.DECIMAL);
            }
        }
        else {
            if (ast.getLiteral() instanceof Boolean){
                ast.setType(Environment.Type.BOOLEAN);
            }
            else if ( ast.getLiteral() instanceof Character){
                ast.setType(Environment.Type.CHARACTER);            }
            else if ( ast.getLiteral() instanceof String){
                ast.setType(Environment.Type.STRING);            }
            else if ( ast.getLiteral() == null){
                ast.setType(Environment.Type.NIL);            }

        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {
        if ( ast.getExpression() instanceof Ast.Expression.Binary ){
            visit(ast);
            ast.setType(ast.getExpression().getType());
            return null;
        }
        else throw new RuntimeException();
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        visit(ast.getLeft());
        visit(ast.getRight());
        switch (ast.getOperator()) {
            case "&&": case "||":
                requireAssignable(Environment.Type.BOOLEAN,ast.getLeft().getType());
                requireAssignable(Environment.Type.BOOLEAN,ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
                break;
            case "<": case ">": case "==": case "!=":
                requireAssignable(Environment.Type.COMPARABLE,ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE,ast.getRight().getType());
                requireAssignable(ast.getLeft().getType(),ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
                break;
            case "+":
                if ( ast.getLeft().getType().equals(Environment.Type.STRING) || ast.getRight().getType().equals(Environment.Type.STRING)){
                    ast.setType(Environment.Type.STRING);
                }
                else if ( ast.getLeft().getType().equals(Environment.Type.INTEGER) ){
                    requireAssignable(Environment.Type.INTEGER, ast.getRight().getType());
                    ast.setType(Environment.Type.INTEGER);
                }
                else if ( ast.getLeft().getType().equals(Environment.Type.DECIMAL)){
                    requireAssignable(Environment.Type.DECIMAL,ast.getRight().getType());
                    ast.setType(Environment.Type.DECIMAL);
                }
                break;
            case "-": case "*": case "/":
                if ( ast.getLeft().getType().equals(Environment.Type.INTEGER) ){
                    requireAssignable(Environment.Type.INTEGER, ast.getRight().getType());
                    ast.setType(Environment.Type.INTEGER);
                }
                else if ( ast.getLeft().getType().equals(Environment.Type.DECIMAL)){
                    requireAssignable(Environment.Type.DECIMAL,ast.getRight().getType());
                    ast.setType(Environment.Type.DECIMAL);
                }
                break;
            case "^":
                requireAssignable(Environment.Type.INTEGER, ast.getRight().getType());
                if ( ast.getLeft().getType().equals(Environment.Type.INTEGER) ){
                    ast.setType(Environment.Type.INTEGER);
                }
                else if ( ast.getLeft().getType().equals(Environment.Type.DECIMAL)){
                    ast.setType(Environment.Type.DECIMAL);
                }
                break;
        }
        return null;
        }

    @Override
    public Void visit(Ast.Expression.Access ast) {
        if (ast.getOffset().isPresent()){
            //visit(ast);
            requireAssignable(Environment.Type.INTEGER,ast.getOffset().get().getType());
            Environment.Variable var = scope.lookupVariable(ast.getName());
            ast.setVariable(var);
        }
        Environment.Variable var = scope.lookupVariable(ast.getName());
        ast.setVariable(var);
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.PlcList ast) {
        List<Ast.Expression> list = ast.getValues();
        for ( Ast.Expression elem : list){
            requireAssignable(elem.getType(),ast.getType());
        }
        return null;
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type) {
        if (target.getJvmName().equals("Object")) return;
        if( target.getJvmName().equals("Comparable") && ( type.getJvmName().equals("String") || type.getJvmName().equals("boolean") || type.getJvmName().equals("char") || type.getJvmName().equals("double")|| type.getJvmName().equals("int"))) return;
        System.out.println(target.getJvmName());
        System.out.println(type.getJvmName());
        if ( !target.getJvmName().equals(type.getJvmName()) ){
            throw new RuntimeException();
        }
    }

}
