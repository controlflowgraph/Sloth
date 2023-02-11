package lang;

import sloth.SlothParser;
import sloth.checking.PrecedenceGraph;
import sloth.inference.*;
import sloth.match.*;
import sloth.model.Interpretation;
import sloth.pattern.Pattern;

import java.util.*;
import java.util.function.Supplier;

import static sloth.inference.InheritanceTree.mustBeType;

public class MathLang
{
    public static void main(String[] args)
    {
        List<Supplier<Pattern>> suppliers = List.of(
                MathLang::variable,
                MathLang::plus,
                MathLang::times,
                MathLang::in,
                MathLang::tuple,
                MathLang::setConst,
                MathLang::setOf,
                MathLang::listOf,
                MathLang::assign,
                MathLang::number,
                MathLang::lambda,
                MathLang::call,
                MathLang::sub
        );

        MatchingContext context = new MatchingContext();
        for (Supplier<Pattern> supplier : suppliers)
            context.add(supplier.get());

        PrecedenceGraph graph = new PrecedenceGraph()
                .add("assign", List.of(), List.of())
                .add("in", List.of("assign"), List.of("plus"))
                .add("plus", List.of("assign"), List.of())
                .add("times", List.of("plus"), List.of())
                .add("tuple", List.of("times"), List.of())
                .add("set-of", List.of("in"), List.of())
                .add("list-of", List.of("in"), List.of())
                .add("set-const", List.of("in"), List.of())
                .add("var", List.of("times"), List.of())
                .add("num", List.of("times"), List.of())
                .add("call", List.of("assign"), List.of())
                .add("lambda", List.of("call"), List.of())
                .add("sub", List.of("assign"), List.of())
                .compile();

        String test = """
                a := 10
                b := 20
                c := a * 4 + b * 3
                d := {1, 2, 3, 4}
                e := 1 in d
                f := {(x, y) | x in d, y in d}
                """;
        String text = """
                a := 10
                b := 20
                c := a + b
                d := c * 20
                e := {1, 2, 3}
                f := [4, 5, 6]
                g := 1 in e
                h := 1 in f
                i := ((a, b) -> a + b)(10, 20)
                """;
        InheritanceTree tree = new InheritanceTree();
        tree.add(new TypeDescription("Collection", List.of("A"), List.of()));
        tree.add(new TypeDescription("UnorderedCollection", List.of("B"), List.of(new Type(false, "Collection", List.of(new Type(true, "B", List.of()))))));
        tree.add(new TypeDescription("Set", List.of("C"), List.of(new Type(false, "UnorderedCollection", List.of(new Type(true, "C", List.of()))))));
        tree.add(new TypeDescription("OrderedCollection", List.of("B"), List.of(new Type(false, "Collection", List.of(new Type(true, "B", List.of()))))));
        tree.add(new TypeDescription("List", List.of("C"), List.of(new Type(false, "OrderedCollection", List.of(new Type(true, "C", List.of()))))));
        tree.add(new TypeDescription("Number", List.of(), List.of()));
        tree.add(new TypeDescription("Lambda", List.of(), List.of()));
        List<Interpretation> parse = SlothParser.parse(text, context, graph);
        List<InferenceContext> contexts = checkTypes(parse, tree);
        System.out.println(contexts.size() + " CONTEXT RESULTING");
        for (InferenceContext inferenceContext : contexts)
        {
            inferenceContext.fragments().forEach((a, b) -> System.out.println(a + " " + b + " -> " + inferenceContext.types().get(a)));
        }
    }

    private static List<InferenceContext> checkTypes(List<Interpretation> interpretations, InheritanceTree tree)
    {
        List<InferenceContext> contexts = new ArrayList<>();
        Map<String, Integer> results = new HashMap<>();
        for (Interpretation interpretation : interpretations)
        {
            try
            {
                contexts.add(checkTypes(interpretation, tree));
                results.put("Valid", results.getOrDefault("Valid", 0) + 1);
            }
            catch (Exception e)
            {
                results.put(e.getMessage(), results.getOrDefault(e.getMessage(), 0) + 1);
            }
        }
        System.out.println("Type Checking Results:");
        results.forEach((a, b) -> System.out.println("\t" + b + " " + a));
        return contexts;
    }

    private static InferenceContext flatten(Interpretation interpretation, InheritanceTree tree)
    {
        InferenceContext inf = new InferenceContext(tree);
        for (Match match : interpretation.matches())
        {
            match.flatten(inf);
        }
        return inf;
    }

    private static void process(InferenceContext context)
    {
        boolean changed = true;
        while (changed)
        {
            changed = false;
            context.flag().set();
            while (context.flag().active())
            {
                context.flag().reset();

                for (Fragment value : context.fragments().values())
                {
                    value.forward(context);
                }

                changed |= context.flag().active();
            }

            context.flag().set();
            while (context.flag().active())
            {
                context.flag().reset();

                for (Fragment value : context.fragments().values())
                {
                    value.backward(context);
                }

                changed |= context.flag().active();
            }
        }
    }

    private static InferenceContext checkTypes(Interpretation interpretation, InheritanceTree tree)
    {
        InferenceContext inf = flatten(interpretation, tree);
        process(inf);

        boolean missing = inf.fragments()
                .keySet()
                .stream()
                .map(inf.types()::get)
                .anyMatch(Objects::isNull);

        if (missing)
            throw new RuntimeException("Problematic!");
        return inf;
    }

    private record Sub(int output, int input) implements Fragment
    {
        @Override
        public void forward(InferenceContext context)
        {
            if (context.isKnown(this.input))
            {
                context.setActual(this.output, context.getType(this.input));
            }
        }

        @Override
        public void backward(InferenceContext context)
        {
            if (context.isKnown(this.output))
            {
                context.setActual(this.input, context.getType(this.output));
            }
        }
    }

    private static Pattern sub()
    {
        return new Pattern(
                "sub",
                new SequenceMatcher(List.of(
                        new WordMatcher("("),
                        new SubMatcher("v"),
                        new WordMatcher(")")
                )),
                m -> "( sub " + m.attempt("v") + " )",
                (m, c) -> {
                    Match v = (Match) m.values().get("v").element();
                    v.checkPrecedence(c);
                },
                (c, m) -> {
                    Match v = (Match) m.values().get("v").element();
                    int flatten = v.flatten(c);
                    int id = c.getId();
                    return new Sub(id, flatten);
                }
        );
    }

    private static final Type BOOLEAN_TYPE = new Type(false, "Boolean", List.of());

    private record Contains(int output, int source, int collection) implements Fragment
    {
        @Override
        public void forward(InferenceContext context)
        {
            if (context.areKnown(List.of(this.source, this.collection)))
            {
                Type s = context.getType(this.source);
                Type c = context.getType(this.collection);


                boolean isCollection = context.tree()
                        .getDifferentiated(c)
                        .stream()
                        .map(Type::name)
                        .map(a -> a.equals("Collection"))
                        .findAny()
                        .isPresent();

                if (!isCollection)
                    throw new RuntimeException("Expected collection!");

                context.setActual(this.output, BOOLEAN_TYPE);
            }
        }

        @Override
        public void backward(InferenceContext context)
        {
            if (context.isKnown(this.output))
            {
                mustBeType(BOOLEAN_TYPE, context.getType(this.output));
            }
        }
    }

    private static Pattern in()
    {
        return binaryOperator(
                "in",
                "in",
                Contains::new
        );
    }

    private static Pattern tuple()
    {
        return new Pattern(
                "tuple",
                new SequenceMatcher(List.of(
                        new WordMatcher("("),
                        new SubMatcher("v"),
                        new MultiMatcher(false, new SequenceMatcher(List.of(
                                new WordMatcher(","),
                                new SubMatcher("v")
                        ))),
                        new WordMatcher(")")
                )),
                m -> "( tuple " + Lst.asList(m.values().get("v")) + " )",
                (m, c) -> Lst.asList(m.values().get("v"))
                        .stream()
                        .map(Match.class::cast)
                        .forEach(a -> a.checkPrecedence(c)),
                (c, m) -> {
                    throw new RuntimeException("TUPLE NOT SUPPORTED!");
                }
        );
    }

    private record Call(int output, int source, List<Integer> parameters) implements Fragment
    {
        @Override
        public void forward(InferenceContext context)
        {
            if (context.isKnown(this.source) && context.areKnown(this.parameters))
            {
                Type fn = context.getType(this.source);
                List<Type> params = this.parameters.stream()
                        .map(context::getType)
                        .toList();
                if (!fn.name().equals("Lambda"))
                    throw new RuntimeException("Requiring lambda to call!");
                if (fn.generics().size() != params.size() + 1)
                    throw new RuntimeException("Mismatching number of lambda parameters! " + fn.generics().size() + " <=> " + params.size());
                for (int i = 0; i < params.size(); i++)
                {
                    Type type = fn.generics().get(i);
                    Type actual = params.get(i);
                    boolean assignable = context.tree().isAssignable(type, actual);
                    if (!assignable)
                        throw new RuntimeException("Mismatching parameter type!");
                }
                context.setActual(this.output, fn.generics().get(fn.generics().size() - 1));
            }
        }

        @Override
        public void backward(InferenceContext context)
        {
            // skip for now
        }
    }

    private static Pattern call()
    {
        return new Pattern(
                "call",
                new SequenceMatcher(List.of(
                        new Require(2),
                        new SubMatcher("v"),
                        new WordMatcher("("),
                        new Require(-1),
                        new PossibleMatcher(
                                new SequenceMatcher(List.of(
                                        new SubMatcher("p"),
                                        new MultiMatcher(true,
                                                new SequenceMatcher(List.of(
                                                        new WordMatcher(","),
                                                        new SubMatcher("p")
                                                ))
                                        )
                                ))
                        ),
                        new WordMatcher(")"),
                        new Require(-1)
                )),
                m -> "( call " + m.attempt("v") + " " + Lst.asList(m.values().get("p")) + ")",
                (m, c) -> {
                    Match v = (Match) m.values().get("v").element();
                    v.checkPrecedence(c);
                    List<Match> paras = Lst.asList(m.values().get("p"))
                            .stream()
                            .map(Match.class::cast)
                            .toList();
                    paras.forEach(p -> p.checkPrecedence(c));

                    int pre = c.getPrecedence("call");
                    int val = c.getPrecedence(v.pattern().name());
                    if (val < pre)
                        throw new RuntimeException("Mismatching precedence!");
                },
                (c, m) -> {
                    Match v = (Match) m.values().get("v").element();
                    List<Match> paras = Lst.asList(m.values().get("p"))
                            .stream()
                            .map(Match.class::cast)
                            .toList();
                    int source = v.flatten(c);
                    List<Integer> par = paras.stream()
                            .map(a -> a.flatten(c))
                            .toList();
                    int out = c.getId();
                    return new Call(out, source, par);
                }
        );
    }

    private record Lambda(int output, List<Integer> inputs, int res) implements Fragment
    {
        private void process(InferenceContext context)
        {
            if (context.isKnown(this.res) && context.areKnown(this.inputs))
            {
                List<Type> generics = new ArrayList<>();
                this.inputs.stream()
                        .map(context::getType)
                        .forEach(generics::add);
                generics.add(context.getType(this.res));
                context.setExpected(this.output, new Type(false, "Lambda", generics));
            }
        }

        @Override
        public void forward(InferenceContext context)
        {
            process(context);
        }

        @Override
        public void backward(InferenceContext context)
        {
            process(context);
        }
    }

    private record Parameter(int output, String id) implements Fragment
    {
        @Override
        public void forward(InferenceContext context)
        {

        }

        @Override
        public void backward(InferenceContext context)
        {

        }
    }

    private static Pattern lambda()
    {
        return new Pattern(
                "lambda",
                new SequenceMatcher(List.of(
                        new WordMatcher("("),
                        new PossibleMatcher(
                                new SequenceMatcher(List.of(
                                        new TextMatcher("n"),
                                        new MultiMatcher(true,
                                                new SequenceMatcher(List.of(
                                                        new WordMatcher(","),
                                                        new TextMatcher("n")
                                                ))
                                        )
                                ))
                        ),
                        new WordMatcher(")"),
                        new WordMatcher("->"),
                        new SubMatcher("v")
                )),
                m -> "( lambda " + Lst.asList(m.values().get("n")) + " " + m.attempt("v") + " )",
                (m, c) -> {
                    int pre = c.getPrecedence("lambda");
                    Match match = (Match) m.values().get("v").element();
                    match.checkPrecedence(c);
                    int sub = c.getPrecedence(match.pattern().name());
                    if (sub < pre)
                        throw new RuntimeException("Mismatching precedence!");
                },
                (c, m) -> {
                    c.push();
                    List<Parameter> names = Lst.asList(m.values().get("n"))
                            .stream()
                            .map(String.class::cast)
                            .map(n -> {
                                int i = c.defineLocally(n);
                                return new Parameter(i, n);
                            })
                            .toList();
                    names.forEach(c::register);
                    List<Integer> ids = names.stream()
                            .map(Parameter::output)
                            .toList();
                    Match v = (Match) m.values().get("v").element();
                    int flatten = v.flatten(c);
                    c.pop();
                    int out = c.getId();
                    return new Lambda(out, ids, flatten);
                }
        );
    }

    private static Pattern setConst()
    {
        return new Pattern(
                "set-const",
                new SequenceMatcher(List.of(
                        new WordMatcher("{"),
                        new SubMatcher("v"),
                        new WordMatcher("|"),
                        new SequenceMatcher(List.of(
                                new TextMatcher("n"),
                                new WordMatcher("in"),
                                new SubMatcher("s")
                        )),
                        new MultiMatcher(false,
                                new SequenceMatcher(List.of(
                                        new WordMatcher(","),
                                        new TextMatcher("n"),
                                        new WordMatcher("in"),
                                        new SubMatcher("s")
                                ))),
                        new WordMatcher("}")
                )),
                m -> "( set-const " + m.attempt("v") + " " + Lst.asList(m.values().get("n")) + " " + Lst.asList(m.values().get("s")) + " )",
                (m, c) -> {
                    Lst.asList(m.values().get("s"))
                            .stream()
                            .map(Match.class::cast)
                            .forEach(a -> a.checkPrecedence(c));
                    Match v = (Match) m.values().get("v").element();
                    v.checkPrecedence(c);
                }
        );
    }

    private record SetOf(int output, List<Integer> sources) implements Fragment
    {
        @Override
        public void forward(InferenceContext context)
        {
            if (context.areKnown(this.sources))
            {
                List<Type> types = this.sources.stream()
                        .map(context::getType)
                        .toList();
                Type over = types.get(0);
                for (Type type : types)
                {
                    over = context.getSuperType(over, type);
                }
                context.setActual(this.output, new Type(false, "Set", List.of(over)));
            }
        }

        @Override
        public void backward(InferenceContext context)
        {
            if (context.isKnown(this.output))
            {
                Type type = context.getType(this.output);
                if (!type.name().equals("Set") || type.generics().size() != 1)
                    throw new RuntimeException("Mismatching type name!");
                Type type1 = type.generics().get(0);
                for (int source : this.sources)
                {
                    context.setExpected(source, type1);
                }
            }
        }
    }

    private record ListOf(int output, List<Integer> sources) implements Fragment
    {
        @Override
        public void forward(InferenceContext context)
        {
            if (context.areKnown(this.sources))
            {
                List<Type> types = this.sources.stream()
                        .map(context::getType)
                        .toList();
                Type over = types.get(0);
                for (Type type : types)
                {
                    over = context.getSuperType(over, type);
                }
                context.setActual(this.output, new Type(false, "List", List.of(over)));
            }
        }

        @Override
        public void backward(InferenceContext context)
        {
            if (context.isKnown(this.output))
            {
                Type type = context.getType(this.output);
                if (!type.name().equals("List") || type.generics().size() != 1)
                    throw new RuntimeException("Mismatching type name!");
                Type type1 = type.generics().get(0);
                for (int source : this.sources)
                {
                    context.setExpected(source, type1);
                }
            }
        }
    }

    private static Pattern listOf()
    {
        return new Pattern(
                "list-of",
                new SequenceMatcher(List.of(
                        new WordMatcher("["),
                        new PossibleMatcher(
                                new SequenceMatcher(List.of(
                                        new SubMatcher("v"),
                                        new MultiMatcher(true,
                                                new SequenceMatcher(List.of(
                                                        new WordMatcher(","),
                                                        new SubMatcher("v")
                                                ))
                                        )
                                ))
                        ),
                        new WordMatcher("]")
                )),
                m -> "(list-of " + Lst.asList(m.values().get("v")) + " )",
                (m, c) -> Lst.asList(m.values().get("v"))
                        .stream()
                        .map(Match.class::cast)
                        .forEach(a -> a.checkPrecedence(c)),
                (c, m) -> {
                    List<Integer> sources = Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .map(a -> a.flatten(c))
                            .toList();
                    int out = c.getId();
                    return new ListOf(out, sources);
                }
        );
    }

    private static Pattern setOf()
    {
        return new Pattern(
                "set-of",
                new SequenceMatcher(List.of(
                        new WordMatcher("{"),
                        new PossibleMatcher(
                                new SequenceMatcher(List.of(
                                        new SubMatcher("v"),
                                        new MultiMatcher(true,
                                                new SequenceMatcher(List.of(
                                                        new WordMatcher(","),
                                                        new SubMatcher("v")
                                                ))
                                        )
                                ))
                        ),
                        new WordMatcher("}")
                )),
                m -> "(set-of " + Lst.asList(m.values().get("v")) + " )",
                (m, c) -> Lst.asList(m.values().get("v"))
                        .stream()
                        .map(Match.class::cast)
                        .forEach(a -> a.checkPrecedence(c)),
                (c, m) -> {
                    List<Integer> sources = Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .map(a -> a.flatten(c))
                            .toList();
                    int out = c.getId();
                    return new SetOf(out, sources);
                }
        );
    }

    private static class NumericBinaryOperator
    {
        public static void forward(InferenceContext context, int a, int b, int output)
        {
            if (context.areKnown(List.of(a, b)))
            {
                Type ta = context.getType(a);
                Type tb = context.getType(b);
                mustBeType(Num.NUMBER_TYPE, ta);
                mustBeType(Num.NUMBER_TYPE, tb);
            }
            context.setActual(output, Num.NUMBER_TYPE);
        }

        public static void backward(InferenceContext context, int a, int b, int output)
        {
            if (context.isKnown(output))
            {
                Type e = context.getType(output);
                mustBeType(Num.NUMBER_TYPE, e);
                context.setExpected(a, Num.NUMBER_TYPE);
                context.setExpected(b, Num.NUMBER_TYPE);
            }
        }
    }

    private record Plus(int output, int a, int b) implements Fragment
    {
        @Override
        public void forward(InferenceContext context)
        {
            NumericBinaryOperator.forward(context, this.a, this.b, this.output);
        }

        @Override
        public void backward(InferenceContext context)
        {
            NumericBinaryOperator.backward(context, this.a, this.b, this.output);
        }
    }

    private static Pattern plus()
    {
        return binaryOperator("plus", "+", Plus::new);
    }

    private record Times(int output, int a, int b) implements Fragment
    {
        @Override
        public void forward(InferenceContext context)
        {
            NumericBinaryOperator.forward(context, this.a, this.b, this.output);
        }

        @Override
        public void backward(InferenceContext context)
        {
            NumericBinaryOperator.backward(context, this.a, this.b, this.output);
        }
    }

    private static Pattern times()
    {
        return binaryOperator("times", "*", Times::new);
    }

    private interface BinaryOperatorFactory
    {
        Fragment create(int output, int a, int b);
    }

    private static Pattern binaryOperator(String name, String symbol, BinaryOperatorFactory conv)
    {
        return new Pattern(
                name,
                new SequenceMatcher(List.of(
                        new Require(1),
                        new SubMatcher("a"),
                        new WordMatcher(symbol),
                        new Require(-1),
                        new SubMatcher("b")
                )),
                m -> "( " + m.attempt("a") + " " + symbol + " " + m.attempt("b") + " )",
                (m, c) -> {
                    int pred = c.getPrecedence(name);

                    Match a = (Match) m.values().get("a").element();
                    Match b = (Match) m.values().get("b").element();

                    int pa = c.getPrecedence(a.pattern().name());
                    int pb = c.getPrecedence(b.pattern().name());
                    if (pa < pred || pb <= pred)
                        throw new RuntimeException("Mismatching precedence!");
                },
                (c, m) -> {
                    Match a = (Match) m.values().get("a").element();
                    Match b = (Match) m.values().get("b").element();
                    int va = a.flatten(c);
                    int vb = b.flatten(c);
                    int out = c.getId();
                    return conv.create(out, va, vb);
                }
        );
    }

    private record Assign(int output, String destination, int source) implements Fragment
    {
        @Override
        public void forward(InferenceContext context)
        {
            if (context.isKnown(this.source))
            {
                context.setActual(this.output, context.getType(this.source));
            }
        }

        @Override
        public void backward(InferenceContext context)
        {
            if (context.isKnown(this.output))
            {
                context.setExpected(this.source, context.getType(this.output));
            }
        }
    }

    private static Pattern assign()
    {
        return new Pattern(
                "assign",
                new SequenceMatcher(List.of(
                        new Require(1),
                        new TextMatcher("n"),
                        new WordMatcher(":="),
                        new Require(-1),
                        new SubMatcher("v")
                )),
                m -> "( " + m.attempt("n") + " := " + m.attempt("v") + " )",
                (m, c) -> {
                    String name = (String) m.values().get("n").element();
                    Match v = (Match) m.values().get("v").element();
                    v.checkPrecedence(c);
                    if (c.isVariableDefinedLocally(name))
                        throw new RuntimeException("Variable already defined");
                    c.definedVariable(name);
                },
                (c, m) -> {
                    String n = (String) m.values().get("n").element();
                    int source = ((Match) m.values().get("v").element()).flatten(c);
                    if (c.isDefinedLocally(n))
                        throw new RuntimeException("Variable '" + n + "' is already defined locally!");
                    int out = c.defineLocally(n);
                    return new Assign(out, n, source);
                }
        );
    }

    private record Var(int output, String name, int source) implements Fragment
    {
        @Override
        public void forward(InferenceContext context)
        {
            if (context.isKnown(this.source))
            {
                context.setActual(this.output, context.getType(this.source));
            }
        }

        @Override
        public void backward(InferenceContext context)
        {
            if (context.isKnown(this.output))
            {
                context.setExpected(this.source, context.getType(this.output));
            }
        }
    }

    private static Pattern variable()
    {
        return new Pattern(
                "var",
                new VariableMatcher("n"),
                m -> m.attempt("n"),
                (m, c) -> {
                },
                (c, m) -> {
                    String name = (String) m.values().get("n").element();
                    return new Var(c.getId(), name, c.getId(name));
                }
        );
    }

    private record Num(int output, String val) implements Fragment
    {
        public static final Type NUMBER_TYPE = new Type(false, "Number", List.of());

        @Override
        public void forward(InferenceContext context)
        {
            context.setActual(this.output, NUMBER_TYPE);
        }

        @Override
        public void backward(InferenceContext context)
        {
            if (context.isKnown(this.output))
            {
                mustBeType(context.getType(this.output), Num.NUMBER_TYPE);
            }
        }
    }

    private static Pattern number()
    {
        return new Pattern(
                "num",
                new NumMatcher(),
                m -> m.attempt("val"),
                (m, c) -> {
                },
                (c, m) -> new Num(c.getId(), (String) m.values().get("val").element())
        );
    }
}
