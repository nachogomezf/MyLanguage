package plc.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * See the Parser assignment specification for specific notes on each AST class
 * and how to use it.
 */
public abstract class Ast {

    public static final class Source extends Ast {

        private final List<Global> globals;
        private final List<Ast.Function> functions;

        public Source(List<Global> globals, List<Ast.Function> functions) {
            this.globals = globals;
            this.functions = functions;
        }

        public List<Global> getGlobals() {
            return globals;
        }

        public List<Ast.Function> getFunctions() {
            return functions;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Source &&
                    globals.equals(((Source) obj).globals) &&
                    functions.equals(((Source) obj).functions);
        }

        @Override
        public String toString() {
            return "Ast.Source{" +
                    "globals=" + globals +
                    ", functions=" + functions +
                    '}';
        }

    }

    public static final class Global extends Ast {

        private final String name;
        private final String typeName;
        private final boolean mutable;
        private final Optional<Ast.Expression> value;
        private Environment.Variable variable = null;

        public Global(String name, boolean mutable, Optional<Expression> value) {
            this(name, "Any", mutable, value);
		}

        public Global(String name, String typeName, boolean mutable, Optional<Ast.Expression> value) {
            this.name = name;
            this.typeName = typeName;
            this.mutable = mutable;
            this.value = value;
        }


        public String getName() {
            return name;
        }

        public String getTypeName() {
            return typeName;
        }

        public boolean getMutable() {
            return mutable;
        }

        public Optional<Ast.Expression> getValue() {
            return value;
        }

        public Environment.Variable getVariable() {
            if (variable == null) {
                throw new IllegalStateException("variable is uninitialized");
            }
            return variable;
        }

        public void setVariable(Environment.Variable variable) {
            this.variable = variable;
        }


        @Override
        public boolean equals(Object obj) {
            return obj instanceof Global &&
                    name.equals(((Global) obj).name) &&
                    typeName.equals(((Global) obj).typeName) &&
                    mutable == ((Global) obj).mutable &&
                    value.equals(((Global) obj).value) &&
                    Objects.equals(variable, ((Global) obj).variable);
        }

        @Override
        public String toString() {
            return "Ast.Global{" +
                    "name='" + name + '\'' +
                    ", typeName=" + typeName +
                    ", mutable=" + mutable +
                    ", value=" + value +
                    ", variable=" + variable +
                    '}';
        }

    }

    public static final class Function extends Ast {

        private final String name;
        private final List<String> parameters;
        private final List<String> parameterTypeNames;
        private final Optional<String> returnTypeName;
        private final List<Statement> statements;
        private Environment.Function function = null;
        
        public Function(String name, List<String> parameters, List<Statement> statements) {
            this(name, parameters, new ArrayList<>(), Optional.of("Any"), statements);
            for (int i = 0; i < parameters.size(); i++) {
                parameterTypeNames.add("Any");
            }
        }

        public Function(String name, List<String> parameters, List<String> parameterTypeNames, Optional<String> returnTypeName, List<Statement> statements) {

            this.name = name;
            this.parameters = parameters;
            this.parameterTypeNames = parameterTypeNames;
            this.returnTypeName = returnTypeName;
            this.statements = statements;
        }

        public String getName() {
            return name;
        }

        public List<String> getParameters() {
            return parameters;
        }

        public List<String> getParameterTypeNames() {
            return parameterTypeNames;
        }

        public Optional<String> getReturnTypeName() {
            return returnTypeName;
        }

        public List<Statement> getStatements() {
            return statements;
        }

        public Environment.Function getFunction() {
            if (function == null) {
                throw new IllegalStateException("function is uninitialized");
            }
            return function;
        }

        public void setFunction(Environment.Function function) {
            this.function = function;
        }


        @Override
        public boolean equals(Object obj) {
            return obj instanceof Ast.Function &&
                    name.equals(((Ast.Function) obj).name) &&
                    parameters.equals(((Ast.Function) obj).parameters) &&
                    parameterTypeNames.equals(((Ast.Function) obj).parameterTypeNames) &&
                    returnTypeName.equals(((Ast.Function) obj).returnTypeName) &&
                    statements.equals(((Ast.Function) obj).statements) &&
                    Objects.equals(function, ((Ast.Function) obj).function);
        }


        @Override
        public String toString() {
            return "Ast.Function{" +
                    "name='" + name + '\'' +
                    ", parameters=" + parameters +
                    ", parameterTypeNames=" + parameterTypeNames +
                    ", returnTypeName='" + returnTypeName + '\'' +
                    ", statements=" + statements +
                    ", function=" + function +
                    '}';
        }

    }

    public static abstract class Statement extends Ast {

        public static final class Expression extends Statement {

            private final Ast.Expression expression;

            public Expression(Ast.Expression expression) {
                this.expression = expression;
            }

            public Ast.Expression getExpression() {
                return expression;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Ast.Statement.Expression &&
                        expression.equals(((Ast.Statement.Expression) obj).expression);
            }

            @Override
            public String toString() {
                return "Ast.Statement.Expression{" +
                        "expression=" + expression +
                        '}';
            }

        }

        public static final class Declaration extends Statement {

            private String name;
            private final Optional<String> typeName;
            private Optional<Ast.Expression> value;
            private Environment.Variable variable = null;

            public Declaration(String name, Optional<Ast.Expression> value) {
                this(name, Optional.empty(), value);
            }

            public Declaration(String name, Optional<String> typeName, Optional<Ast.Expression> value) {
                this.name = name;
                this.typeName = typeName;
                this.value = value;
            }

            public String getName() {
                return name;
            }

            public Optional<String> getTypeName() {
                return typeName;
            }

            public Optional<Ast.Expression> getValue() {
                return value;
            }

            public Environment.Variable getVariable() {
                if (variable == null) {
                    throw new IllegalStateException("variable is uninitialized");
                }
                return variable;
            }

            public void setVariable(Environment.Variable variable) {
                this.variable = variable;
            }
            
            
            @Override
            public boolean equals(Object obj) {
                return obj instanceof Declaration &&
                        name.equals(((Declaration) obj).name) &&
                        typeName.equals(((Declaration) obj).typeName) &&
                        value.equals(((Declaration) obj).value) &&
                        Objects.equals(variable, ((Declaration) obj).variable);
            }

            @Override
            public String toString() {
                return "Ast.Statement.Declaration{" +
                        "name='" + name + '\'' +
                        ", typeName=" + typeName +
                        ", value=" + value +
                        ", variable=" + variable +
                        '}';
            }

        }

        public static final class Assignment extends Statement {

            private final Ast.Expression receiver;
            private final Ast.Expression value;

            public Assignment(Ast.Expression receiver, Ast.Expression value) {
                this.receiver = receiver;
                this.value = value;
            }

            public Ast.Expression getReceiver() {
                return receiver;
            }

            public Ast.Expression getValue() {
                return value;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Assignment &&
                        receiver.equals(((Assignment) obj).receiver) &&
                        value.equals(((Assignment) obj).value);
            }

            @Override
            public final String toString() {
                return "Ast.Statement.Assignment{" +
                        "receiver=" + receiver +
                        ", value=" + value +
                        '}';
            }

        }

        public static final class If extends Statement {

            private final Ast.Expression condition;
            private final List<Statement> thenStatements;
            private final List<Statement> elseStatements;


            public If(Ast.Expression condition, List<Statement> thenStatements, List<Statement> elseStatements) {
                this.condition = condition;
                this.thenStatements = thenStatements;
                this.elseStatements = elseStatements;
            }

            public Ast.Expression getCondition() {
                return condition;
            }

            public List<Statement> getThenStatements() {
                return thenStatements;
            }

            public List<Statement> getElseStatements() {
                return elseStatements;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof If &&
                        condition.equals(((If) obj).condition) &&
                        thenStatements.equals(((If) obj).thenStatements) &&
                        elseStatements.equals(((If) obj).elseStatements);
            }

            @Override
            public String toString() {
                return "Ast.Statement.If{" +
                        "condition=" + condition +
                        ", thenStatements=" + thenStatements +
                        ", elseStatements=" + elseStatements +
                        '}';
            }

        }

        public static final class Switch extends Statement {

            private final Ast.Expression condition;
            private final List<Ast.Statement.Case> cases;

            public Switch(Ast.Expression condition, List<Ast.Statement.Case> cases) {
                this.condition = condition;
                this.cases = cases;
            }

            public Ast.Expression getCondition() {
                return condition;
            }

            public List<Ast.Statement.Case> getCases() { return cases; }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Switch &&
                        condition.equals(((Switch) obj).condition) &&
                        cases.equals(((Switch) obj).cases);
            }

            @Override
            public String toString() {
                return "Ast.Statement.Switch{" +
                        "condition=" + condition +
                        ", cases=" + cases +
                        '}';
            }

        }

        public static final class Case extends Statement {

            private final Optional<Ast.Expression> value;
            private final List<Statement> statements;

            public Case(Optional<Ast.Expression> value, List<Statement> statements) {
                this.value = value;
                this.statements = statements;
            }

            public Optional<Ast.Expression> getValue() {
                return value;
            }

            public List<Statement> getStatements() {
                return statements;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Case &&
                        value.equals(((Case) obj).value) &&
                        statements.equals(((Case) obj).statements);
            }

            @Override
            public String toString() {
                return "Ast.Statement.Case{" +
                        "value=" + value +
                        ", statements=" + statements +
                        '}';
            }

        }

        public static final class While extends Statement {

            private final Ast.Expression condition;
            private final List<Statement> statements;

            public While(Ast.Expression condition, List<Statement> statements) {
                this.condition = condition;
                this.statements = statements;
            }

            public Ast.Expression getCondition() {
                return condition;
            }

            public List<Statement> getStatements() {
                return statements;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof While &&
                        condition.equals(((While) obj).condition) &&
                        statements.equals(((While) obj).statements);
            }

            @Override
            public String toString() {
                return "Ast.Statement.While{" +
                        "condition=" + condition +
                        ", statements=" + statements +
                        '}';
            }

        }

        public static final class Return extends Statement {

            private final Ast.Expression value;

            public Return(Ast.Expression value) {
                this.value = value;
            }

            public Ast.Expression getValue() {
                return value;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Return &&
                        value.equals(((Return) obj).value);
            }

            @Override
            public String toString() {
                return "Ast.Statement.Return{" +
                        "value=" + value +
                        '}';
            }

        }

    }

    public static abstract class Expression extends Ast {

        public abstract Environment.Type getType();

        public static final class Literal extends Ast.Expression {

            private final Object literal;
            private Environment.Type type = null;
            
            public Literal(Object literal) {
                this.literal = literal;
            }

            public Object getLiteral() {
                return literal;
            }

            @Override
            public Environment.Type getType() {
                if (type == null) {
                    throw new IllegalStateException("type is uninitialized");
                }
                return type;
            }

            public void setType(Environment.Type type) {
                this.type = type;
            }



            @Override
            public boolean equals(Object obj) {
                return obj instanceof Literal &&
                        Objects.equals(literal, ((Literal) obj).literal) &&
                        Objects.equals(type, ((Literal) obj).type);
            }

            @Override
            public String toString() {
                return "Ast.Expression.Literal{" +
                        "literal=" + literal +
                        ", type=" + type +
                        '}';
            }

        }

        public static final class Group extends Ast.Expression {

            private final Ast.Expression expression;
            private Environment.Type type = null;

            public Group(Ast.Expression expression) {
                this.expression = expression;
            }

            public Ast.Expression getExpression() {
                return expression;
            }

            @Override
            public Environment.Type getType() {
                if (type == null) {
                    throw new IllegalStateException("type is uninitialized");
                }
                return type;
            }

            public void setType(Environment.Type type) {
                this.type = type;
            }


            @Override
            public boolean equals(Object obj) {
                return obj instanceof Group &&
                        expression.equals(((Group) obj).expression) &&
                        Objects.equals(type, ((Group) obj).type);
            }


            @Override
            public String toString() {
                return "Ast.Expression.Group{" +
                        "expression=" + expression +
                        ", type=" + type +
                        '}';
            }

        }

        public static final class Binary extends Ast.Expression {

            private final String operator;
            private final Ast.Expression left;
            private final Ast.Expression right;
            private Environment.Type type = null;

            public Binary(String operator, Ast.Expression left, Ast.Expression right) {
                this.operator = operator;
                this.left = left;
                this.right = right;
            }

            public String getOperator() {
                return operator;
            }

            public Ast.Expression getLeft() {
                return left;
            }

            public Ast.Expression getRight() {
                return right;
            }

            @Override
            public Environment.Type getType() {
                if (type == null) {
                    throw new IllegalStateException("type is uninitialized");
                }
                return type;
            }

            public void setType(Environment.Type type) {
                this.type = type;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Binary &&
                        operator.equals(((Binary) obj).operator) &&
                        left.equals(((Binary) obj).left) &&
                        right.equals(((Binary) obj).right) &&
                        Objects.equals(type, ((Binary) obj).type);
            }

            @Override
            public String toString() {
                return "Ast.Expression.Binary{" +
                        "operator='" + operator + '\'' +
                        ", left=" + left +
                        ", right=" + right +
                        ", type=" + type +
                        '}';
            }

        }


        public static final class Access extends Ast.Expression {

            private final Optional<Ast.Expression> offset;
            private final String name;
            private Environment.Variable variable = null;

            public Access(Optional<Ast.Expression> offset, String name) {
                this.offset = offset;
                this.name = name;
            }

            public Optional<Ast.Expression> getOffset() {
                return offset;
            }

            public String getName() {
                return name;
            }

            public Environment.Variable getVariable() {
                if (variable == null) {
                    throw new IllegalStateException("variable is uninitialized");
                }
                return variable;
            }

            public void setVariable(Environment.Variable variable) {
                this.variable = variable;
            }

            @Override
            public Environment.Type getType() {
                return getVariable().getType();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Access &&
                        offset.equals(((Access) obj).offset) &&
                        name.equals(((Access) obj).name) &&
                        Objects.equals(variable, ((Access) obj).variable);
            }


            @Override
            public String toString() {
                return "Ast.Expression.Access{" +
                        "offset=" + offset +
                        ", name='" + name + '\'' +
                        ", variable=" + variable +
                        '}';
            }

        }

        public static final class Function extends Ast.Expression {

            private final String name;
            private final List<Ast.Expression> arguments;
            private Environment.Function function = null;

            public Function(String name, List<Ast.Expression> arguments) {
                this.name = name;
                this.arguments = arguments;
            }

            public String getName() {
                return name;
            }

            public List<Ast.Expression> getArguments() {
                return arguments;
            }

            public Environment.Function getFunction() {
                if (function == null) {
                    throw new IllegalStateException("function is uninitialized");
                }
                return function;
            }

            public void setFunction(Environment.Function function) {
                this.function = function;
            }

            @Override
            public Environment.Type getType() {
                return getFunction().getReturnType();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Ast.Expression.Function &&
                        name.equals(((Ast.Expression.Function) obj).name) &&
                        arguments.equals(((Ast.Expression.Function) obj).arguments) &&
                        Objects.equals(function, ((Ast.Expression.Function) obj).function);
            }

            @Override
            public String toString() {
                return "Ast.Expression.Function{" +
                        "name='" + name + '\'' +
                        ", arguments=" + arguments +
                        ", function=" + function +
                        '}';
            }

        }

        public static final class PlcList extends Ast.Expression {

            private final List<Ast.Expression> values;
            private Environment.Type type = null;


            public PlcList(List<Ast.Expression> values) {
                this.values = values;
            }

            public List<Ast.Expression> getValues() {
                return values;
            }

            @Override
            public Environment.Type getType() {
                if (type == null) {
                    throw new IllegalStateException("type is uninitialized");
                }
                return type;
            }

            public void setType(Environment.Type type) {
                this.type = type;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Ast.Expression.PlcList &&
                        values.equals(((Ast.Expression.PlcList) obj).values) &&
                        Objects.equals(type, ((Ast.Expression.PlcList) obj).type);
            }

            @Override
            public String toString() {
                return "Ast.Expression.PlcList{" +
                        "values=[" + values + "]" +
                        ", type=" + type +
                        '}';
            }

        }

    }

    public interface Visitor<T> {

        default T visit(Ast ast) {
            if (ast instanceof Ast.Source) {
                return visit((Ast.Source) ast);
            } else if (ast instanceof Ast.Global) {
                return visit((Ast.Global) ast);
            } else if (ast instanceof Ast.Function) {
                return visit((Ast.Function) ast);
            } else if (ast instanceof Ast.Statement.Expression) {
                return visit((Ast.Statement.Expression) ast);
            } else if (ast instanceof Ast.Statement.Declaration) {
                return visit((Ast.Statement.Declaration) ast);
            } else if (ast instanceof Ast.Statement.Assignment) {
                return visit((Ast.Statement.Assignment) ast);
            } else if (ast instanceof Ast.Statement.If) {
                return visit((Ast.Statement.If) ast);
            } else if (ast instanceof Ast.Statement.Switch) {
                return visit((Ast.Statement.Switch) ast);
            } else if (ast instanceof Ast.Statement.Case) {
                return visit((Ast.Statement.Case) ast);
            } else if (ast instanceof Ast.Statement.While) {
                return visit((Ast.Statement.While) ast);
            } else if (ast instanceof Ast.Statement.Return) {
                return visit((Ast.Statement.Return) ast);
            } else if (ast instanceof Ast.Expression.Literal) {
                return visit((Ast.Expression.Literal) ast);
            } else if (ast instanceof Ast.Expression.Group) {
                return visit((Ast.Expression.Group) ast);
            } else if (ast instanceof Ast.Expression.Binary) {
                return visit((Ast.Expression.Binary) ast);
            } else if (ast instanceof Ast.Expression.Access) {
                return visit((Ast.Expression.Access) ast);
            } else if (ast instanceof Ast.Expression.Function) {
                return visit((Ast.Expression.Function) ast);
            } else if (ast instanceof Ast.Expression.PlcList) {
                return visit((Ast.Expression.PlcList) ast);
            } else {
                throw new AssertionError("Unimplemented AST type: " + ast.getClass().getName() + ".");
            }
        }

        T visit(Ast.Source ast);

        T visit(Ast.Global ast);

        T visit(Ast.Function ast);

        T visit(Ast.Statement.Expression ast);

        T visit(Ast.Statement.Declaration ast);

        T visit(Ast.Statement.Assignment ast);

        T visit(Ast.Statement.If ast);

        T visit(Ast.Statement.Switch ast);

        T visit(Ast.Statement.Case ast);

        T visit(Ast.Statement.While ast);

        T visit(Ast.Statement.Return ast);

        T visit(Ast.Expression.Literal ast);

        T visit(Ast.Expression.Group ast);

        T visit(Ast.Expression.Binary ast);

        T visit(Ast.Expression.Access ast);

        T visit(Ast.Expression.Function ast);

        T visit(Ast.Expression.PlcList ast);
    }

}
