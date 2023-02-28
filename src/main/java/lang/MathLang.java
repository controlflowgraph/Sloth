package lang;

import sloth.SlothParser;
import sloth.checking.CheckingContext;
import sloth.checking.PrecedenceGraph;
import sloth.checking.Type;
import sloth.match.*;
import sloth.model.Interpretation;
import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class MathLang
{
    public static void main(String[] args)
    {
        List<Supplier<Pattern>> suppliers = List.of(
                MathLang::lambda,
                MathLang::emptySet,
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
                .add("lambda", List.of("assign"), List.of())
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
                g <- () -> (a) -> a
                """;
        List<Interpretation> parse = SlothParser.parse(test, context, graph);
    }
    private static Pattern lambda()
    {
        return new Pattern(
                "lambda",
                new SequenceMatcher(List.of(
                        new WordMatcher("("),
                        new PossibleMatcher(
                                new SequenceMatcher(List.of(
                                        new TextMatcher("p"),
                                        new MultiMatcher(
                                                true,
                                                new SequenceMatcher(List.of(
                                                        new WordMatcher(","),
                                                        new TextMatcher("p")
                                                ))
                                        )
                                ))
                        ),
                        new WordMatcher(")"),
                        new WordMatcher("->"),
                        new SubMatcher("b")
                )),
                (m, c) -> {
                    c.push("lambda");
                    Lst.asList(m.values().get("p"))
                            .stream()
                            .map(String.class::cast)
                            .forEach(c::definedVariable);
                    Match b = (Match) m.values().get("b").element();
                    b.check(c);
                    c.pop();
                }
        );
    }

    private static Pattern in()
    {
        return binaryOperator("in", "in");
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
                (m, c) -> {
                    Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .forEach(k -> k.check(c));
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
                (m, c) -> {
                    List<Match> s = Lst.asList(m.values().get("s"))
                            .stream()
                            .map(Match.class::cast)
                            .toList();
                    List<String> n = Lst.asList(m.values().get("n"))
                            .stream()
                            .map(String.class::cast)
                            .toList();

                    c.push("set-construction-via-pattern");
                    for (int i = 0; i < s.size(); i++)
                    {
                        c.definedVariable(n.get(i));
                        s.get(i).check(c);
                    }
                    Match v = (Match) m.values().get("v").element();
                    v.check(c);
                    c.pop();
                }
        );
    }

    private static Pattern emptySet()
    {
        return new Pattern(
                "empty-set",
                new SequenceMatcher(List.of(
                        new WordMatcher("{"),
                        new WordMatcher("}")
                )),
                (m, c) -> {}
        );
    }

    private static Pattern setOf()
    {
        return new Pattern(
                "set-of",
                new SequenceMatcher(List.of(
                        new WordMatcher("{"),
                        new SequenceMatcher(List.of(
                                new SubMatcher("v"),
                                new MultiMatcher(true,
                                        new SequenceMatcher(List.of(
                                                new WordMatcher(","),
                                                new SubMatcher("v")
                                        ))
                                )
                        )),
                        new WordMatcher("}")
                )),
                (m, c) -> {
                    Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .forEach(k -> k.check(c));
                }
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
                (m, c) -> {
                    Match a = (Match) m.values().get("a").element();
                    Match b = (Match) m.values().get("b").element();
                    a.check(c);
                    b.check(c);
                    int p = c.getPrecedence(m.pattern().name());
                    int pa = c.getPrecedence(a.pattern().name());
                    int pb = c.getPrecedence(b.pattern().name());
                    if(pa < p || pb <= p)
                        throw new RuntimeException("Mismatching!");
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
                (m, c) -> {
                    if(c.isVariableDefinedLocally(m.attempt("n")))
                        throw new RuntimeException("Variable '" + m.attempt("n") + "' is already defined in scope!");
                    c.definedVariable(m.attempt("n"));
                    Match v = (Match) m.values().get("v").element();
                    v.check(c);
                    int p = c.getPrecedence(m.pattern().name());
                    int pv = c.getPrecedence(v.pattern().name());
                    if(pv <= p)
                        throw new RuntimeException("Mismatching!");
                }
        );
    }

    private static Pattern variable()
    {
        return new Pattern(
                "var",
                new VariableMatcher("n"),
                (m, c) -> {
                    if (!c.isVariableDefined(m.attempt("n")))
                    {
                        throw new RuntimeException("Variable " + m.attempt("n") + " is not defined!");
                    }
                }
        );
    }

    private static Pattern number()
    {
        return new Pattern(
                "num",
                new NumMatcher(),
                (m, c) -> {}
        );
    }
}
