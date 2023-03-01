package lang;

import sloth.SlothParser;
import sloth.checking.PrecedenceGraph;
import sloth.eval.EvaluationContext;
import sloth.match.*;
import sloth.model.Interpretation;
import sloth.pattern.Pattern;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MathLang
{
    public static void main(String[] args)
    {
        List<Supplier<Pattern>> suppliers = List.of(
                MathLang::call,
                MathLang::lambda,
                MathLang::emptySet,
                MathLang::variable,
                MathLang::plus,
                MathLang::times,
                MathLang::in,
                MathLang::tuple,
                MathLang::sub,
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
                .add("call", List.of("assign"), List.of())
                .add("in", List.of("assign"), List.of("plus"))
                .add("plus", List.of("assign"), List.of())
                .add("times", List.of("plus"), List.of())
                .add("tuple", List.of("times"), List.of())
                .add("sub", List.of("times"), List.of())
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
                g <- (aaaaa) -> (() -> aaaaa)
                h <- (g(10)())
                """;

        List<Interpretation> parse = SlothParser.parse(test, context, graph);
        for (Interpretation interpretation : parse)
        {
            EvaluationContext eval = new EvaluationContext();
            for (Match match : interpretation.matches())
            {
                match.eval(eval);
            }
            System.out.println("GLOBAL SCOPE:");
            eval.current().forEach((a, b) -> System.out.println(a + ": " + b));
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
                                        new MultiMatcher(
                                                true,
                                                new SubMatcher("p")
                                        )
                                ))
                        ),
                        new WordMatcher(")"),
                        new Require(-1)
                )),
                (m, c) -> {
                    Match v = (Match) m.values().get("v").element();
                    Lst.asList(m.values().get("p"))
                            .stream()
                            .map(Match.class::cast)
                            .forEach(a -> a.check(c));
                    v.check(c);
                    int p = c.getPrecedence(m.pattern().name());
                    int pv = c.getPrecedence(v.pattern().name());
                    if(pv < p)
                        throw new RuntimeException("Mismatching!");
                },
                (m, c) -> {
                    Match v = (Match) m.values().get("v").element();
                    Lmd eval = (Lmd) v.eval(c);
                    List<Object> args = Lst.asList(m.values().get("p"))
                            .stream()
                            .map(Match.class::cast)
                            .map(a -> a.eval(c))
                            .toList();
                    return eval.call(c, args);
                }
        );
    }

    public record Lmd(Map<String, Object> cont, List<String> names, Match body)
    {
        public Object call(EvaluationContext context, List<Object> arguments)
        {
            context.push(this.cont);
            context.push();
            for (int i = 0; i < this.names.size(); i++)
            {
                context.set(this.names.get(i), arguments.get(i));
            }
            Object result = this.body.eval(context);
            context.pop();
            context.pop();
            return result;
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
                },
                (m, c) -> {
                    Map<String, Object> cont = c.flatten();
                    List<String> args = Lst.asList(m.values().get("p"))
                            .stream()
                            .map(String.class::cast)
                            .toList();
                    Match body = (Match) m.values().get("b").element();
                    return new Lmd(cont, args, body);
                }
        );
    }

    private static Pattern in()
    {
        return binaryOperator("in", "in", (a, b) -> ((Collection<?>) b).contains(a));
    }

    private static Pattern sub()
    {
        return new Pattern(
                "sub",
                new SequenceMatcher(List.of(
                        new Require(2),
                        new WordMatcher("("),
                        new Require(-1),
                        new SubMatcher("v"),
                        new WordMatcher(")"),
                        new Require(-1)
                )),
                (m, c) -> ((Match) m.values().get("v").element()).check(c),
                (m, c) -> ((Match) m.values().get("v").element()).eval(c)
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
                (m, c) -> Lst.asList(m.values().get("v"))
                        .stream()
                        .map(Match.class::cast)
                        .forEach(k -> k.check(c)),
                (m, c) -> Lst.asList(m.values().get("v"))
                        .stream()
                        .map(Match.class::cast)
                        .map(a -> a.eval(c))
                        .toList()
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
                },
                (m, c) -> {
                    List<String> variables = Lst.asList(m.values().get("n"))
                            .stream()
                            .map(String.class::cast)
                            .toList();
                    List<Collection<Object>> iterators = Lst.asList(m.values().get("s"))
                            .stream()
                            .map(Match.class::cast)
                            .map(a -> a.eval(c))
                            .map(a -> (Collection<Object>) a)
                            .toList();
                    Match op = (Match) m.values().get("v").element();
                    c.push();
                    Set<Object> results = new HashSet<>();
                    gooo(results, variables, iterators, 0, c, op);
                    c.pop();
                    return results;
                }
        );
    }

    private static void gooo(Set<Object> results, List<String> name, List<Collection<Object>> sources, int index, EvaluationContext context, Match m)
    {
        if(index + 1 == sources.size())
        {
            for (Object o : sources.get(index))
            {
                context.set(name.get(index), o);
                results.add(m.eval(context));
            }
        }
        else
        {
            for (Object o : sources.get(index))
            {
                context.set(name.get(index), o);
                gooo(results, name, sources, index + 1, context, m);
            }
        }
    }

    private static Pattern emptySet()
    {
        return new Pattern(
                "empty-set",
                new SequenceMatcher(List.of(
                        new WordMatcher("{"),
                        new WordMatcher("}")
                )),
                (m, c) -> {},
                (m, c) -> {
                    throw new RuntimeException("ERRORING!");
                }
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
                },
                (m, c) -> Lst.asList(m.values().get("v"))
                        .stream()
                        .map(Match.class::cast)
                        .map(a -> a.eval(c))
                        .collect(Collectors.toSet())
        );
    }

    private static Pattern plus()
    {
        return binaryOperator("plus", "+", (a, b) -> ((Double) a) + ((Double) b));
    }

    private static Pattern times()
    {
        return binaryOperator("times", "*", (a, b) -> ((Double) a) * ((Double) b));
    }

    private static Pattern binaryOperator(String name, String symbol, BiFunction<Object, Object, Object> operator)
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
                },
                (m, c) -> {
                    Match a = (Match) m.values().get("a").element();
                    Match b = (Match) m.values().get("b").element();
                    Object oa = a.eval(c);
                    Object ob = b.eval(c);
                    return operator.apply(oa, ob);
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
                },
                (m, c) -> {
                    Match v = (Match) m.values().get("v").element();
                    String n = (String) m.values().get("n").element();
                    Object r = v.eval(c);
                    c.set(n, r);
                    return null;
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
                },
                (m, c) -> {
                    String name = (String) m.values().get("n").element();
                    return c.get(name);
                }
        );
    }

    private static Pattern number()
    {
        return new Pattern(
                "num",
                new NumMatcher(),
                (m, c) -> {},
                (m, c) -> Double.parseDouble((String) m.values().get("val").element())
        );
    }
}
