package plc.project;

import java.io.PrintWriter;
import java.util.List;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        print("public class Main {");
        newline(indent);
        newline(++indent);
        for (Ast.Global g : ast.getGlobals()) {
            print(g);
            newline(indent);
        }
        print("public static void main(String[] args) {");
        newline(++indent);
        print("System.exit(new Main().main());");
        newline(--indent);
        print("}");
        for (Ast.Function method : ast.getFunctions()) {
            newline(0);
            newline(indent);
            print(method);
        }
        newline(--indent);
        newline(indent);
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Global ast) {
        Ast.Expression val = null;
        String type = Environment.getType(ast.getTypeName()).getJvmName();
        if (ast.getValue().isPresent()){
            val = ast.getValue().get();
        }
        if (ast.getMutable() && val instanceof Ast.Expression.PlcList){
            print(type, "[] ", ast.getName(), " = ", ast.getValue().get(), ";");
        }
        else {
            if (!ast.getMutable())print("final ");
            print(type, " ", ast.getName());
            if (val != null) print(" = ", val);
            print(";");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Function ast) {
        print(ast.getFunction().getReturnType().getJvmName(), " ", ast.getName(), "(");
        for (int i = 0; i < ast.getParameters().size(); i++){
            print(Environment.getType(ast.getParameterTypeNames().get(i)).getJvmName(), " ", ast.getParameters().get(i));
            if ( i != ast.getParameters().size()-1) print(", ");
        }
        print(") {");
        if (ast.getStatements().isEmpty()) print("}");
        else {
            newline(++indent);
            for (int i = 0; i < ast.getStatements().size(); i++){
                print(ast.getStatements().get(i));
                if ( i != ast.getStatements().size()-1) newline(indent);
                else newline(--indent);
            }
            print("}");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        visit(ast.getExpression());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        if (ast.getValue().isPresent()){
            print(ast.getValue().get().getType().getJvmName()," ", ast.getName()," = ",ast.getValue().get(), ";");
        } else {
            print(Environment.getType(ast.getTypeName().get()).getJvmName()," ",ast.getName(),";");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        print(ast.getReceiver()," = ", ast.getValue(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        print("if (", ast.getCondition(),") {");
        newline(++indent);
        ast.getThenStatements().forEach(this::print);
        if (!ast.getElseStatements().isEmpty()){
            newline(--indent);
            print("} else {");
            newline(++indent);
            ast.getElseStatements().forEach(this::print);
        }
        newline(--indent);
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Switch ast) {
        print("switch (",ast.getCondition(),") {");
        ast.getCases().forEach(this::print);
        newline(--indent);
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Case ast) {
        newline(++indent);
        if(ast.getValue().isPresent()){
            print("case ",ast.getValue().get(),":");

        } else print("default:");
        newline(++indent);
        for (Ast.Statement st :  ast.getStatements()){
            print(st);
            if(!st.equals(ast.getStatements().get(ast.getStatements().size()-1))) newline(indent);
        }
        indent = indent -2;
        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        print("while (", ast.getCondition(), ") {");
        if (!ast.getStatements().isEmpty()) {
            newline(++indent);
            for (int i = 0; i < ast.getStatements().size(); i++){
                print(ast.getStatements().get(i));
                if (i != ast.getStatements().size()-1) newline(indent);
            }
            newline(--indent);
        }
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        print("return ",ast.getValue(),";");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {
        if (Environment.Type.STRING.equals(ast.getType())) {
            print("\"", ast.getLiteral(), "\"");
        } else if (Environment.Type.CHARACTER.equals(ast.getType())) {
            print("'", ast.getLiteral(), "'");
        } else {
            print(ast.getLiteral());
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {
        print("(",ast.getExpression(),")");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        String op = ast.getOperator();
        switch (op){
            case "^":
                print("Math.pow(",ast.getLeft(),",",ast.getRight(),")");
                return null;
            case "AND":
                op = "&&";
                break;
            case "OR":
                op = "||";
                break;
        }
        print(ast.getLeft()," ",op," ",ast.getRight());
        return null;

    }

    @Override
    public Void visit(Ast.Expression.Access ast) {
        print(ast.getVariable().getJvmName());
        if (ast.getOffset().isPresent()){
            print("[",ast.getOffset().get(),"]");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        print(ast.getFunction().getJvmName(),"(");
        for ( int i=0; i<ast.getArguments().size(); i++){
            visit(ast.getArguments().get(i));
            if (i != ast.getArguments().size()-1) print(",");
        }
        print(")");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.PlcList ast) {
        print("{");
        if (!ast.getValues().isEmpty()){
            for (int i = 0; i < ast.getValues().size(); i++){
                print(ast.getValues().get(i));
                if (i != ast.getValues().size()-1) print(", ");
            }
        }
        print("}");
        return null;
    }

}
