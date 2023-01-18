package sloth;

import sloth.checking.PrecedenceGraph;
import sloth.checking.Type;
import sloth.match.*;
import sloth.model.Interpretation;
import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;

class SlothParserTest
{
    public static void main(String[] args)
    {
        example0();
    }
    // TODO: refactor the stack to a variable / counter to avoid resource useless consumption!!!

    public static void example0()
    {
        MatchingContext context = new MatchingContext();
        Type number = new Type("Number");
        Type nothing = new Type("Nothing");
        context.add(new Pattern("num",
                new NumMatcher(),
                m -> m.attempt("val"),
                (m, c) -> number
        ));
        context.add(new Pattern("sub",
                new SequenceMatcher(List.of(
                        new WordMatcher("("),
                        new Require(1),
                        new SubMatcher("s"),
                        new WordMatcher(")"),
                        new Require(-1)
                )),
                m -> "( %s )".formatted(m.attempt("s")),
                (m, c) -> null
        ));

        context.add(new Pattern("tuple",
                new SequenceMatcher(List.of(
                        new WordMatcher("("),
                        new Require(1),
                        new SubMatcher("s"),
                        new MultiMatcher(false, new SequenceMatcher(List.of(
                                new WordMatcher(","),
                                new SubMatcher("s")
                        ))),
                        new WordMatcher(")"),
                        new Require(-1)
                )),
                m -> "( tuple %s )".formatted(Lst.asList(m.values().get("s"))),
                (m, c) -> null
        ));
        context.add(new Pattern("times",
                new SequenceMatcher(List.of(
                        new Require(1),
                        new SubMatcher("a"),
                        new WordMatcher("*"),
                        new Require(-1),
                        new SubMatcher("b")
                )),
                m -> "( %s * %s )".formatted(m.attempt("a"), m.attempt("b")),
                (m, c) -> {
                    int pre = c.getPrecedence("times");
                    Match a = (Match) m.values().get("a").element();
                    a.check(c);
                    int pa = c.getPrecedence(a.pattern().name());

                    Match b = (Match) m.values().get("b").element();
                    b.check(c);
                    int pb = c.getPrecedence(b.pattern().name());

                    if (pa < pre || pb <= pre)
                        throw new RuntimeException("Precedence mismatch!");
                    return null;
                }
        ));
        context.add(new Pattern("plus",
                new SequenceMatcher(List.of(
                        new Require(1),
                        new SubMatcher("a"),
                        new WordMatcher("+"),
                        new Require(-1),
                        new SubMatcher("b")
                )),
                m -> "( %s + %s )".formatted(m.attempt("a"), m.attempt("b")),
                (m, c) -> {
                    int pre = c.getPrecedence("plus");
                    Match a = (Match) m.values().get("a").element();
                    a.check(c);
                    int pa = c.getPrecedence(a.pattern().name());

                    Match b = (Match) m.values().get("b").element();
                    b.check(c);
                    int pb = c.getPrecedence(b.pattern().name());

                    if (pa < pre || pb <= pre)
                        throw new RuntimeException("Precedence mismatch!");
                    return null;
                }
        ));

        context.add(new Pattern("and",
                new SequenceMatcher(List.of(
                        new Require(1),
                        new SubMatcher("a"),
                        new WordMatcher("and"),
                        new Require(-1),
                        new SubMatcher("b")
                )),
                m -> "( %s & %s )".formatted(m.attempt("a"), m.attempt("b")),
                (m, c) -> {
                    int pre = c.getPrecedence("and");
                    Match a = (Match) m.values().get("a").element();
                    a.check(c);
                    int pa = c.getPrecedence(a.pattern().name());

                    Match b = (Match) m.values().get("b").element();
                    b.check(c);
                    int pb = c.getPrecedence(b.pattern().name());

                    if (pa < pre || pb <= pre)
                        throw new RuntimeException("Precedence mismatch!");
                    return null;
                }
        ));

        context.add(new Pattern("or",
                new SequenceMatcher(List.of(
                        new Require(1),
                        new SubMatcher("a"),
                        new WordMatcher("or"),
                        new Require(-1),
                        new SubMatcher("b")
                )),
                m -> "( %s | %s )".formatted(m.attempt("a"), m.attempt("b")),
                (m, c) -> {
                    int pre = c.getPrecedence("or");
                    Match a = (Match) m.values().get("a").element();
                    a.check(c);
                    int pa = c.getPrecedence(a.pattern().name());

                    Match b = (Match) m.values().get("b").element();
                    b.check(c);
                    int pb = c.getPrecedence(b.pattern().name());

                    if (pa < pre || pb <= pre)
                        throw new RuntimeException("Precedence mismatch!");
                    return null;
                }
        ));

        context.add(new Pattern("eq",
                new SequenceMatcher(List.of(
                        new Require(1),
                        new SubMatcher("a"),
                        new WordMatcher("="),
                        new Require(-1),
                        new SubMatcher("b")
                )),
                m -> "( %s = %s )".formatted(m.attempt("a"), m.attempt("b")),
                (m, c) -> {
                    int pre = c.getPrecedence("eq");
                    Match a = (Match) m.values().get("a").element();
                    a.check(c);
                    int pa = c.getPrecedence(a.pattern().name());

                    Match b = (Match) m.values().get("b").element();
                    b.check(c);
                    int pb = c.getPrecedence(b.pattern().name());

                    if (pa <= pre || pb <= pre)
                        throw new RuntimeException("Precedence mismatch!");
                    return null;
                }
        ));

        context.add(new Pattern("in",
                new SequenceMatcher(List.of(
                        new Require(1),
                        new SubMatcher("a"),
                        new WordMatcher("in"),
                        new Require(-1),
                        new SubMatcher("b")
                )),
                m -> "( %s in %s )".formatted(m.attempt("a"), m.attempt("b")),
                (m, c) -> {
                    int pre = c.getPrecedence("in");
                    Match a = (Match) m.values().get("a").element();
                    a.check(c);
                    int pa = c.getPrecedence(a.pattern().name());

                    Match b = (Match) m.values().get("b").element();
                    b.check(c);
                    int pb = c.getPrecedence(b.pattern().name());

                    if (pa <= pre || pb <= pre)
                        throw new RuntimeException("Precedence mismatch!");
                    return null;
                }
        ));

        context.add(new Pattern("let",
                new SequenceMatcher(List.of(
                        new Require(1),
                        new TextMatcher("n"),
                        new WordMatcher("<-"),
                        new Require(-1),
                        new SubMatcher("v")
                )),
                m -> "( %s <- %s )".formatted(m.attempt("n"), m.attempt("v")),
                (m, c) -> {
                    Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .forEach(a -> a.check(c));
                    return null;
                }
        ));
        context.add(new Pattern("var",
                new VariableMatcher("name"),
                m -> m.attempt("name"),
                (m, c) -> null
        ));

        context.add(new Pattern("set",
                new SequenceMatcher(List.of(
                        new WordMatcher("{"),
                        new Require(1),
                        new PossibleMatcher(
                                new SequenceMatcher(List.of(
                                        new SubMatcher("v"),
                                        new MultiMatcher(true,
                                                new SequenceMatcher(List.of(
                                                        new WordMatcher(","),
                                                        new SubMatcher("v")
                                                )))
                                ))
                        ),
                        new WordMatcher("}"),
                        new Require(-1)
                )),
                m -> "( set " + Lst.asList(m.values().get("v")) + " )",
                (m, c) -> {
                    Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .forEach(a -> a.check(c));
                    return null;
                }
        ));

        context.add(new Pattern("list",
                new SequenceMatcher(List.of(
                        new WordMatcher("["),
                        new Require(1),
                        new SubMatcher("v"),
                        new MultiMatcher(true,
                                new SequenceMatcher(List.of(
                                        new WordMatcher(","),
                                        new SubMatcher("v")
                                ))),
                        new WordMatcher("]"),
                        new Require(-1)
                )),
                m -> "( list " + Lst.asList(m.values().get("v")) + " )",
                (m, c) -> {
                    Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .forEach(a -> a.check(c));
                    return null;
                }
        ));

        context.add(new Pattern("set-const",
                new SequenceMatcher(List.of(
                        new WordMatcher("{"),
                        new Require(2),
                        new SubMatcher("v"),
                        new WordMatcher("|"),
                        new Require(-1),
                        new SubMatcher("s"),
//                        new SequenceMatcher(List.of(
//                                new TextMatcher("n"),
//                                new WordMatcher("in"),
//                                new Require(-1),
//                                new SubMatcher("s")
//                        )),
//                        new MultiMatcher(true,
//                                new SequenceMatcher(List.of(
//                                        new WordMatcher(","),
//                                        new SequenceMatcher(List.of(
//                                                new TextMatcher("n"),
//                                                new WordMatcher("in"),
//                                                new SubMatcher("s")
//                                        )))
//                                )),
                        new WordMatcher("}"),
                        new Require(-1)
                )),
                m -> "( set-const " + Lst.asList(m.values().get("v")) + " " + Lst.asList(m.values().get("n")) + " " + Lst.asList(m.values().get("s")) + " )",
                (m, c) -> {
                    Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .forEach(a -> a.check(c));

                    Lst.asList(m.values().get("s"))
                            .stream()
                            .map(Match.class::cast)
                            .forEach(a -> a.check(c));
                    return null;
                }
        ));

        PrecedenceGraph graph = new PrecedenceGraph()
                .add("let", List.of(), List.of())
                .add("or", List.of("let"), List.of())
                .add("and", List.of("or"), List.of())
                .add("in", List.of("and"), List.of())
                .add("plus", List.of("in"), List.of())
                .add("times", List.of("plus"), List.of())
                .add("sub", List.of("times"), List.of())
                .add("tuple", List.of("times"), List.of())
                .add("num", List.of("times"), List.of())
                .add("var", List.of("times"), List.of())
                .add("set", List.of("let", "in"), List.of("plus"))
                .add("list", List.of("let", "in"), List.of("plus"))
                .add("set-const", List.of("let", "in"), List.of("plus"))
                .add("eq", List.of("let", "and"), List.of("set", "set-const"))
                .compile();

        String text = """
                E <- {1, 2, 3, 4, 5 * 6 * 8 + 4}.
                a <- {(x, (y, z)) | x in E and y in E and z in {1, 2, 3, 4}}.
                a + b * a * 10 + c.
                [1, 2, 3, 4].
                a & b | c.
                """;

        String test = """
                { { } , { 1 } , { 2 }, { 1 , 2 } }.
                a <- {(x, (y, z)) | z in {1, 2, 3, 4}}.
                a = b and c = d.
                """;
        List<Interpretation> parse = SlothParser.parse(test, context, graph);
    }

    private static Matcher sub(String name)
    {
        return new SubMatcher(name);
    }

    private static Matcher text(String name)
    {
        return new TextMatcher(name);
    }

    private static Matcher zero(Object... arr)
    {
        return new MultiMatcher(true, seq(arr));
    }

    private static Matcher one(Object... arr)
    {
        return new MultiMatcher(true, seq(arr));
    }

    private static Matcher seq(Object... arr)
    {
        return new SequenceMatcher(conv(arr));
    }


    private static List<Matcher> conv(Object... arr)
    {
        List<Matcher> matchers = new ArrayList<>();
        for (Object o : arr)
        {
            if (o instanceof String s)
            {
                matchers.add(new WordMatcher(s));
            }
            else if (o instanceof Integer i)
            {
                matchers.add(new Require(i));
            }
            else if (o instanceof Matcher m)
            {
                matchers.add(m);
            }
            else
            {
                throw new RuntimeException("Unknown type!");
            }
        }
        return matchers;
    }

    public static void example1()
    {
        MatchingContext context = new MatchingContext();
        Type number = new Type("Number");
        Type nothing = new Type("Nothing");
        context.add(new Pattern("num",
                new NumMatcher(),
                m -> m.attempt("val"),
                (m, c) -> number
        ));
        context.add(new Pattern("sub",
                new SequenceMatcher(List.of(
                        new WordMatcher("("),
                        new Require(1),
                        new SubMatcher("s"),
                        new WordMatcher(")"),
                        new Require(-1)
                )),
                m -> "( %s )".formatted(m.attempt("s")),
                (m, c) -> {
                    Match match = (Match) m.values().get("s").element();
                    return match.check(c);
                }
        ));
        context.add(new Pattern("times",
                new SequenceMatcher(List.of(
                        new Require(1),
                        new SubMatcher("a"),
                        new WordMatcher("*"),
                        new Require(-1),
                        new SubMatcher("b")
                )),
                m -> "( %s * %s )".formatted(m.attempt("a"), m.attempt("b")),
                (m, c) -> {
                    int pre = c.getPrecedence("times");
                    Match ma = (Match) m.values().get("a").element();
                    Match mb = (Match) m.values().get("b").element();
                    int pa = c.getPrecedence(ma.pattern().name());
                    int pb = c.getPrecedence(mb.pattern().name());
                    if (pa < pre || pb <= pre)
                        throw new RuntimeException("Precedence mismatch!");
                    Type a = ma.check(c);
                    Type b = mb.check(c);
                    if (a.equals(number) && b.equals(number))
                        return number;
                    throw new RuntimeException("Must be numbers!");
                }
        ));
        context.add(new Pattern("plus",
                new SequenceMatcher(List.of(
                        new Require(1),
                        new SubMatcher("a"),
                        new WordMatcher("+"),
                        new Require(-1),
                        new SubMatcher("b")
                )),
                m -> "( %s + %s )".formatted(m.attempt("a"), m.attempt("b")),
                (m, c) -> {
                    int pre = c.getPrecedence("plus");
                    Match ma = (Match) m.values().get("a").element();
                    Match mb = (Match) m.values().get("b").element();
                    int pa = c.getPrecedence(ma.pattern().name());
                    int pb = c.getPrecedence(mb.pattern().name());
                    if (pa < pre || pb <= pre)
                        throw new RuntimeException("Precedence mismatch!");
                    Type a = ma.check(c);
                    Type b = mb.check(c);
                    if (a.equals(number) && b.equals(number))
                        return number;
                    throw new RuntimeException("Must be numbers!");
                }
        ));
        context.add(new Pattern("let",
                new SequenceMatcher(List.of(
                        new WordMatcher("let"),
                        new TextMatcher("n"),
                        new WordMatcher("be"),
                        new WordMatcher("equal"),
                        new WordMatcher("to"),
                        new SubMatcher("v")
                )),
                m -> "( let %s be equal to %s )".formatted(m.attempt("n"), m.attempt("v")),
                (m, c) -> {
                    String name = (String) m.values().get("n").element();
                    Type v = ((Match) m.values().get("v").element()).check(c);
                    c.definedVariable(name, v);
                    return nothing;
                }
        ));
        context.add(new Pattern("var",
                new VariableMatcher("name"),
                m -> m.attempt("name"),
                (m, c) -> {
                    String name = (String) m.values().get("name").element();
                    return c.getVariableType(name);
                }
        ));
        context.add(new Pattern("set",
                new SequenceMatcher(List.of(
                        new WordMatcher("a"),
                        new WordMatcher("set"),
                        new WordMatcher("containing"),
                        new SubMatcher("v"),
                        new MultiMatcher(true,
                                new SequenceMatcher(List.of(
                                        new WordMatcher(","),
                                        new SubMatcher("v")
                                ))),
                        new PossibleMatcher(
                                new SequenceMatcher(List.of(
                                        new WordMatcher("and"),
                                        new SubMatcher("v")
                                ))
                        )
                )),
                m -> "( set " + Lst.asList(m.values().get("v")) + " )",
                (m, c) -> {
                    int pre = c.getPrecedence("set");
                    List<Match> values = Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .toList();
                    boolean mismatch = values.stream()
                            .map(v -> c.getPrecedence(v.pattern().name()))
                            .map(v -> v < pre)
                            .reduce(false, (a, b) -> a || b);
                    if (mismatch)
                        throw new RuntimeException("Precedence mismatch!");

                    List<Type> types = values.stream()
                            .map(v -> v.check(c))
                            .toList();
                    return new Type("Set");
                }
        ));
        context.add(new Pattern("set-union",
                new SequenceMatcher(List.of(
                        new WordMatcher("the"),
                        new WordMatcher("union"),
                        new WordMatcher("of"),
                        new SubMatcher("v"),
                        new MultiMatcher(true,
                                new SequenceMatcher(List.of(
                                        new WordMatcher(","),
                                        new SubMatcher("v")
                                ))),
                        new PossibleMatcher(
                                new SequenceMatcher(List.of(
                                        new WordMatcher("and"),
                                        new SubMatcher("v")
                                ))
                        )
                )),
                m -> "( union " + Lst.asList(m.values().get("v")) + " )",
                (m, c) -> {
                    int pre = c.getPrecedence("set-union");
                    List<Match> values = Lst.asList(m.values().get("v"))
                            .stream()
                            .map(Match.class::cast)
                            .toList();
                    boolean mismatch = values.stream()
                            .map(v -> c.getPrecedence(v.pattern().name()))
                            .map(v -> v <= pre)
                            .reduce(false, (a, b) -> a || b);
                    if (mismatch)
                        throw new RuntimeException("Precedence mismatch!");

                    boolean mismatchingTypes = values.stream()
                            .map(v -> v.check(c))
                            .anyMatch(v -> !v.name().equals("Set"));
                    if (mismatchingTypes)
                        throw new RuntimeException("Only sets in union allowed!");
                    return new Type("Set");
                }
        ));

        PrecedenceGraph graph = new PrecedenceGraph()
                .add("let", List.of(), List.of())
                .add("plus", List.of("let"), List.of())
                .add("times", List.of("plus"), List.of())
                .add("sub", List.of("times"), List.of())
                .add("num", List.of("times"), List.of())
                .add("var", List.of("times"), List.of())
                .add("set-union", List.of("let"), List.of("set"))
                .add("set", List.of("let"), List.of("plus"))
                .compile();

        String text = """
                let a be equal to a set containing 10, 20 and 30.
                let b be equal to a set containing 30 and 40.
                let c be equal to the union of a, a set containing 123 and 456 and b.
                """;
        List<Interpretation> parse = SlothParser.parse(text, context, graph);
    }
}