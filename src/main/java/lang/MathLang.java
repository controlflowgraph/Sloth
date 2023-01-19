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

    private static Pattern in()
    {
        return binaryOperator(
                "in",
                "in"
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
                (m, c) -> Lst.asList(m.values().get("v"))
                        .stream()
                        .map(Match.class::cast)
                        .forEach(a -> a.checkPrecedence(c))
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
                        .forEach(a -> a.checkPrecedence(c))
        );
    }

    private static Pattern plus()
    {
        return simpleBinaryOperator("plus", "+");
    }

    private static Pattern times()
    {
        return simpleBinaryOperator("times", "*");
    }

    private static Pattern simpleBinaryOperator(String name, String symbol)
    {
        return binaryOperator(name, symbol);
    }

    private static Pattern binaryOperator(String name, String symbol)
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
                    v.checkPrecedence(c);
                    if (c.isVariableDefinedLocally(name))
                        throw new RuntimeException("Variable already defined");
                    c.definedVariable(name, null);
                }
        );
    }

    private static Pattern variable()
    {
        return new Pattern(
                "var",
                new VariableMatcher("n"),
                m -> m.attempt("n"),
                (m, c) -> {}
        );
    }

    private static Pattern number()
    {
        return new Pattern(
                "num",
                new NumMatcher(),
                m -> m.attempt("val"),
                (m, c) -> {}
        );
    }
}
