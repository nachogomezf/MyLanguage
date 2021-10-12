package plc.project;

import java.util.List;

public class Environment {

    public static final PlcObject NIL = create(new Object() {

        @Override
        public String toString() {
            return "nil";
        }

    });

    public static PlcObject create(Object value) {
        return new PlcObject(new Scope(null), value);
    }

    public static final class PlcObject {

        private final Scope scope;
        private final Object value;

        public PlcObject(Scope scope, Object value) {
            this.scope = scope;
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Object{" +
                    "scope=" + scope +
                    ", value=" + value +
                    '}';
        }

    }

    public static final class Variable {

        private final String name;
        private final boolean mutable;
        private PlcObject value;

        public Variable(String name, boolean mutable, PlcObject value) {
            this.name = name;
            this.mutable = mutable;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public boolean getMutable() {
            return mutable;
        }

        public PlcObject getValue() {
            return value;
        }

        public void setValue(PlcObject value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Variable{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    '}';
        }

    }

    public static final class Function {

        private final String name;
        private final int arity;
        private final java.util.function.Function<List<PlcObject>, PlcObject> function;

        public Function(String name, int arity, java.util.function.Function<List<PlcObject>, PlcObject> function) {
            this.name = name;
            this.arity = arity;
            this.function = function;
        }

        public String getName() {
            return name;
        }

        public int getArity() {
            return arity;
        }

        public PlcObject invoke(List<PlcObject> arguments) {
            return function.apply(arguments);
        }

        @Override
        public String toString() {
            return "Function{" +
                    "name='" + name + '\'' +
                    ", arity=" + arity +
                    ", function=" + function +
                    '}';
        }

    }

}
