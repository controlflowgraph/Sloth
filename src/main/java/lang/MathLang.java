package lang;

import sloth.SlothParser;
import sloth.checking.CheckingContext;
import sloth.checking.PrecedenceGraph;
import sloth.checking.Type;
import sloth.match.*;
import sloth.model.Interpretation;
import sloth.pattern.Pattern;

import java.util.List;
import java.util.function.Supplier;

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
                MathLang::assign,
                MathLang::number
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
                .add("set-const", List.of("in"), List.of())
                .add("var", List.of("times"), List.of())
                .add("num", List.of("times"), List.of())
                .compile();

        String test = """
                a <- 10
                b <- 20
                c <- a * 4 + b * 3
                d <- {1, 2, 3, 4}
                e <- 1 in d
                f <- {(x, y) | x in d, y in d}
                """;
        List<Interpretation> parse = SlothParser.parse(test, context, graph);
    }

    private static final Type NUMBER_TYPE = new Type("Number");
    private static final Type NONE_TYPE = new Type("None");
    private static final Type BOOLEAN_TYPE = new Type("Boolean");
    private static final Type OBJECT_TYPE = new Type("Object");

    private interface Processor
    {
        Type process(CheckingContext context, Match a, Match b);
    }

    private static Pattern in()
    {
        return binaryOperator(
                "in",
                "in",
                (c, a, b) -> {
                    a.check(c);
                    b.check(c);
                    return BOOLEAN_TYPE;
                }
        );
    }

    private static Pattern tuple()
    {
        return new Pattern(
                "tuple",
                new SequenceMatcher(List.of(
                        new WordMatcher("("),
                        new SubMatcher("v"),
                        new MultiMatcher(true, new SequenceMatcher(List.of(
                                new WordMatcher(","),
                                new SubMatcher("v")
                        ))),
                        new WordMatcher(")")
                )),
                m -> "( tuple " + Lst.asList(m.values().get("v")) + " )",
                (m, c) -> {
                    List<Type> v = Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .map(a -> a.check(c))
                            .toList();
                    return new Type("Tuple", v);
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
                    List<String> n = Lst.asList(m.values().get("n"))
                            .stream()
                            .map(String.class::cast)
                            .toList();
                    List<Match> s = Lst.asList(m.values().get("s"))
                            .stream()
                            .map(Match.class::cast)
                            .toList();

                    List<Type> types = s.stream()
                            .map(t -> t.check(c))
                            .toList();

                    c.push("set-const");
                    for (int i = 0; i < types.size(); i++)
                    {
                        Type t = types.get(i);
                        if(!t.name().equals("Set") || t.generics().size() != 1)
                            throw new RuntimeException("Mismatching type!");
                        String name = n.get(i);
                        c.definedVariable(name, t);
                    }

                    Match v = (Match) m.values().get("v").element();
                    Type tv = v.check(c);
                    c.pop();

                    return tv;
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
                (m, c) -> {
                    List<Type> v = Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .map(a -> a.check(c))
                            .toList();
                    if(v.isEmpty())
                        return new Type("Set", List.of(OBJECT_TYPE));
                    Type t = v.get(0);
                    for (Type type : v)
                        if(!t.equals(type))
                            throw new RuntimeException("Mismatching type!");
                    return new Type("Set", List.of(t));
                }
        );
    }

    private static Pattern plus()
    {
        return simpleBinaryOperator("plus", "+", NUMBER_TYPE, NUMBER_TYPE);
    }

    private static Pattern times()
    {
        return simpleBinaryOperator("times", "*", NUMBER_TYPE, NUMBER_TYPE);
    }

    private static Pattern simpleBinaryOperator(String name, String symbol, Type required, Type result)
    {
        return binaryOperator(name, symbol, (c, a, b) -> {
            Type ta = a.check(c);
            Type tb = b.check(c);
            if (!ta.equals(required))
                throw new RuntimeException("Mismatching type!");
            if (!tb.equals(required))
                throw new RuntimeException("Mismatching type!");
            return result;
        });
    }

    private static Pattern binaryOperator(String name, String symbol, Processor processor)
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
                    return processor.process(c, a, b);
                }
        );
    }

    private static Pattern assign()
    {
        return new Pattern(
                "assign",
                new SequenceMatcher(List.of(
                        new Require(1),
                        new TextMatcher("n"),
                        new WordMatcher("<-"),
                        new Require(-1),
                        new SubMatcher("v")
                )),
                m -> "( " + m.attempt("n") + " <- " + m.attempt("v") + " )",
                (m, c) -> {
                    String name = (String) m.values().get("n").element();
                    Match v = (Match) m.values().get("v").element();
                    if (c.isVariableDefinedLocally(name))
                        throw new RuntimeException("Variable already defined");
                    Type check = v.check(c);
                    c.definedVariable(name, check);
                    return NONE_TYPE;
                }
        );
    }

    private static Pattern variable()
    {
        return new Pattern(
                "var",
                new VariableMatcher("n"),
                m -> m.attempt("n"),
                (m, c) -> {
                    String name = (String) m.values().get("n").element();
                    if (!c.isVariableDefined(name))
                        throw new RuntimeException("Variable not in scope!");
                    return c.getVariableType(name);
                }
        );
    }

    private static Pattern number()
    {
        return new Pattern(
                "num",
                new NumMatcher(),
                m -> m.attempt("val"),
                (m, c) -> NUMBER_TYPE
        );
    }
}
