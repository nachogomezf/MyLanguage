package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);

    public Interpreter(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {

        for(Ast.Global g : ast.getGlobals()) {
            visit(g);
        }
        for(Ast.Function f : ast.getFunctions()) {
            visit(f);
        }

        return scope.lookupFunction("main", 0).invoke(Collections.emptyList());
    }

    @Override
    public Environment.PlcObject visit(Ast.Global ast) {
            Optional<Ast.Expression> opt = ast.getValue();
            if (opt.isPresent()) {
                scope.defineVariable(ast.getName(), ast.getMutable(), visit(opt.get()));
            }
            else  {
            scope.defineVariable(ast.getName(), ast.getMutable(), Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Function ast) {

        scope.defineFunction(ast.getName(), ast.getParameters().size(), args -> {
            try {
                scope = new Scope(scope);
                for(int i = 0; i < args.size(); i++) { // define arguments
                    scope.defineVariable(ast.getParameters().get(i),true, args.get(i));
                }
                for(Ast.Statement stmt : ast.getStatements()) { // evaluate statements
                    visit(stmt);
                }
            }
            catch(Return r) {
                return r.value;
            }
            finally { // restore scope
                scope = scope.getParent();
            }
            return Environment.NIL;
        });
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Expression ast) {
        visit(ast.getExpression());
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Declaration ast) {
        Environment.PlcObject value = Environment.NIL;
        if(ast.getValue().isPresent()) {
            value = visit(ast.getValue().get());
        }
        scope.defineVariable(ast.getName(),true, value);
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Assignment ast) {
        Ast.Expression receiver = ast.getReceiver();
        Ast.Expression value = ast.getValue();
        if (receiver instanceof Ast.Expression.Access) {
            Environment.Variable variable = scope.lookupVariable(((Ast.Expression.Access) receiver).getName());
            if (!(((Ast.Expression.Access) receiver).getOffset().equals(Optional.empty()))) {
                Ast.Expression.PlcList plclist = new Ast.Expression.PlcList((List<Ast.Expression>) variable.getValue().getValue());
                int off = ((BigInteger) ((Ast.Expression.Literal) ((Ast.Expression.Access) receiver).getOffset().get()).getLiteral()).intValue();
                List<Object> alist = new ArrayList<>();
                List<Object> newlist = new ArrayList<>();
                newlist.addAll(plclist.getValues());
                for (Object a : newlist){
                    alist.add(a);
                }
                alist.set(off,((Ast.Expression.Literal) value).getLiteral());
                variable.setValue(Environment.create(alist));
            } else variable.setValue(visit(ast.getValue()));
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.If ast) {
        if(requireType(Boolean.class, visit(ast.getCondition())) != null) {
            try {
                scope = new Scope(scope);
                if((Boolean) visit(ast.getCondition()).getValue()) {
                    ast.getThenStatements().forEach(this::visit);
                } else {
                    ast.getElseStatements().forEach(this::visit);
                }
            } finally {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Switch ast) {
        try{
            scope = new Scope(scope);

                for (Ast.Statement.Case c : ast.getCases()) {
                    if(c.getValue().isPresent()){
                        Ast.Expression expr = c.getValue().get();
                        if (visit(ast.getCondition()).getValue().equals(visit(expr).getValue())) {
                            c.getStatements().forEach(this::visit);
                        }
                    }

                }
        } finally{
            scope = scope.getParent();
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Case ast) {
        //scope = new Scope(scope);
        //Environment.PlcObject obj = new Environment.PlcObject(scope,visit(ast.getValue().get()));
        //scope = scope.getParent();
        return visit(ast.getValue().get());
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.While ast) {
        while(requireType(Boolean.class, visit(ast.getCondition()))) {
            try { // enter new scope
                scope = new Scope(scope);
                ast.getStatements().forEach(this::visit);
            } finally { // restore scope
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Return ast) {
        Environment.PlcObject value = visit(ast.getValue());
        throw new Return(value);
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Literal ast) {
        if(ast.getLiteral() == null) {
            return Environment.NIL;
        }
        return Environment.create(ast.getLiteral());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Group ast) {
        return visit(ast.getExpression());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Binary ast) {
        Environment.PlcObject left = visit(ast.getLeft());

        switch (ast.getOperator()) {
            case "+":
                if(left.getValue() instanceof BigInteger) { // integer addition
                    if(visit(ast.getRight()).getValue() instanceof BigInteger) {
                        return Environment.create(
                                requireType(BigInteger.class, left).add(requireType(BigInteger.class, visit(ast.getRight())))
                        );
                    }
                    throw new RuntimeException();
                }
                if(left.getValue() instanceof BigDecimal) { // decimal addition
                    if(visit(ast.getRight()).getValue() instanceof BigDecimal) {
                        return Environment.create(
                                requireType(BigDecimal.class, left).add(requireType(BigDecimal.class, visit(ast.getRight())))
                        );
                    }
                    throw new RuntimeException();
                }
                if(left.getValue() instanceof String) {
                    if(visit(ast.getRight()).getValue() instanceof String) { // string concatenation
                        return Environment.create(
                                requireType(String.class, left) + requireType(String.class, visit(ast.getRight()))
                        );
                    }
                    throw new RuntimeException();
                }
                break;

            case "-":
                if(left.getValue() instanceof BigInteger) {
                    if(visit(ast.getRight()).getValue() instanceof BigInteger) { // integer subtraction
                        return Environment.create(
                                requireType(BigInteger.class, left).subtract(requireType(BigInteger.class, visit(ast.getRight())))
                        );
                    }
                    throw new RuntimeException();
                }
                if(left.getValue() instanceof BigDecimal) {
                    if(visit(ast.getRight()).getValue() instanceof BigDecimal) {
                        return Environment.create(
                                requireType(BigDecimal.class, left).subtract(requireType(BigDecimal.class, visit(ast.getRight())))
                        );
                    }
                    throw new RuntimeException();
                }
                break;

            case "*":
                if(left.getValue() instanceof BigInteger) { // integer multiplication
                    if(visit(ast.getRight()).getValue() instanceof BigInteger) {
                        return Environment.create(
                                requireType(BigInteger.class, left).multiply(requireType(BigInteger.class, visit(ast.getRight())))
                        );
                    }
                    throw new RuntimeException();
                }
                if(left.getValue() instanceof BigDecimal) { // decimal multiplication
                    if(visit(ast.getRight()).getValue() instanceof BigDecimal) {
                        return Environment.create(
                                requireType(BigDecimal.class, left).multiply(requireType(BigDecimal.class, visit(ast.getRight())))
                        );
                    }
                    throw new RuntimeException();
                }
                break;

            case "/":
                if(left.getValue() instanceof BigInteger) { // integer division
                    if(visit(ast.getRight()).getValue() instanceof BigInteger) {
                        if(((BigInteger) visit(ast.getRight()).getValue()).intValue() == 0) {
                            throw new RuntimeException();
                        }
                        return Environment.create(
                                requireType(BigInteger.class, left).divide(requireType(BigInteger.class, visit(ast.getRight())))
                        );
                    }
                    throw new RuntimeException();
                }
                if(left.getValue() instanceof BigDecimal) { // decimal division
                    if(visit(ast.getRight()).getValue() instanceof BigDecimal) {
                        if(((BigDecimal) visit(ast.getRight()).getValue()).doubleValue() == 0) { // divide by 0 error
                            throw new RuntimeException();
                        }
                        return Environment.create(
                                requireType(BigDecimal.class, left).divide(requireType(BigDecimal.class, visit(ast.getRight())), RoundingMode.HALF_EVEN)
                        );
                    }
                    throw new RuntimeException();
                }
                break;

            case "&&":
                if(left.getValue() instanceof Boolean && !(Boolean)left.getValue()) {
                    return Environment.create(false);
                }
                if(visit(ast.getRight()).getValue() instanceof Boolean && !(Boolean)visit(ast.getRight()).getValue()) {
                    return Environment.create(false);
                }
                if(left.getValue() instanceof Boolean) {
                    if(visit(ast.getRight()).getValue() instanceof Boolean) {
                        return Environment.create(true);
                    }
                    throw new RuntimeException();
                }
                break;

            case "||":
                if(left.getValue() instanceof Boolean && (Boolean)left.getValue()) {
                    return Environment.create(true);
                }
                if(visit(ast.getRight()).getValue() instanceof Boolean && (Boolean)visit(ast.getRight()).getValue()) {
                    return Environment.create(true);
                }
                if(left.getValue() instanceof Boolean) {
                    if(visit(ast.getRight()).getValue() instanceof Boolean) {
                        return Environment.create(false);
                    }
                    throw new RuntimeException();
                }
                break;

            case "==":
                return Environment.create(
                        Objects.equals(left.getValue(), visit(ast.getRight()).getValue())
                );

            case "!=":
                return Environment.create(
                        !Objects.equals(left.getValue(), visit(ast.getRight()).getValue())
                );

            case "<":
                if(left.getValue() instanceof Comparable) {
                    Environment.PlcObject right = visit(ast.getRight());
                    if(requireType(left.getValue().getClass(), right) != null) {
                        return Environment.create(((Comparable) left.getValue()).compareTo(right.getValue()) < 0);
                    }
                }
                break;

            case "<=":
                if(left.getValue() instanceof Comparable) {
                    Environment.PlcObject right = visit(ast.getRight());
                    if(requireType(left.getValue().getClass(), right) != null) {
                        return Environment.create(((Comparable) left.getValue()).compareTo(right.getValue()) <= 0);
                    }
                }
                break;

            case ">":
                if(left.getValue() instanceof Comparable) {
                    Environment.PlcObject right = visit(ast.getRight());
                    if(requireType(left.getValue().getClass(), right) != null) {
                        return Environment.create(((Comparable) left.getValue()).compareTo(right.getValue()) > 0);
                    }
                }
                break;

            case ">=":
                if(left.getValue() instanceof Comparable) {
                    Environment.PlcObject right = visit(ast.getRight());
                    if(requireType(left.getValue().getClass(), right) != null) {
                        return Environment.create(((Comparable) left.getValue()).compareTo(right.getValue()) >= 0);
                    }
                }
                break;
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Access ast) {

        if ( !ast.getOffset().equals(Optional.empty()) ) {
            Object list = scope.lookupVariable(ast.getName()).getValue().getValue();
            Ast.Expression.PlcList plclist = new Ast.Expression.PlcList((List<Ast.Expression>) list);
            List<Object> alist = new ArrayList<>(plclist.getValues());
            int off = ((BigInteger) ((Ast.Expression.Literal) ast.getOffset().get()).getLiteral()).intValue();
            return Environment.create(alist.get(off));

        }
        return scope.lookupVariable(ast.getName()).getValue();
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Function ast) {
        List<Environment.PlcObject> arguments = new ArrayList<>();
        for (Ast.Expression argument : ast.getArguments()) {
            arguments.add(visit(argument));
        }
        //scope = new Scope(scope);
        Environment.Function function = scope.lookupFunction(ast.getName(), ast.getArguments().size());
        return function.invoke(arguments);
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.PlcList ast) {
        List<Object> list = new ArrayList<>();
        for (Ast.Expression a : ast.getValues()){
            list.add(visit(a).getValue());
        }
        return new Environment.PlcObject(scope, list);
    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject value;

        private Return(Environment.PlcObject value) {
            this.value = value;
        }

    }

}
