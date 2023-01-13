package sloth;

import org.junit.jupiter.api.Test;
import sloth.checking.CheckingContext;
import sloth.checking.PrecedenceGraph;
import sloth.checking.Type;
import sloth.match.*;
import sloth.pattern.Pattern;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SyntaxMatcherTest
{
    public static void main(String[] args)
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
//        Provider<String> provider = new Provider<>(Lexer.lex("let a be equal to (1 * 2 + 3). let b be equal to 5."));
//        Provider<String> provider = new Provider<>(Lexer.lex("""
//                let a be equal to 10.
//                let b be equal to 20.
//                let c be equal to a set containing a, b, a + b
//                """));

        Provider<String> provider = new Provider<>(Lexer.lex("""
                let a be equal to a set containing 10, 20 and 30.
                let b be equal to a set containing 30 and 40.
                let c be equal to the union of a, a set containing 123 and 456 and b.
                """));

//        Provider<String> provider = new Provider<>(Lexer.lex("123 * 456 + 10 * 1 * 2"));

        List<List<List<Match>>> parse = SyntaxMatcher.parse(context, provider);
        System.out.println(parse.size() + " SYNTACTIC VALUES FOUND!");
        // clean the starts
        List<List<List<Match>>> cleaned = new ArrayList<>();
        System.out.println(parse.size() + " CLEANED SYNTACTIC VALUES FOUND!");
        for (List<List<Match>> lists : parse)
        {
            List<List<Match>> cls = new ArrayList<>();
            for (List<Match> matches : lists)
            {
                cls.add(matches.subList(1, matches.size()));
            }
            cleaned.add(cls);
        }

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

        List<List<Match>> interpretations = new ArrayList<>();
        List<CheckingContext> contexts = new ArrayList<>();
        interpretations.add(List.of());
        contexts.add(new CheckingContext(graph));


        for (List<List<Match>> lists : cleaned)
        {
            List<List<Match>> interpreted = new ArrayList<>();
            List<CheckingContext> cont = new ArrayList<>();

            for (List<Match> list : lists)
            {
                for (int i = 0; i < interpretations.size(); i++)
                {
                    CheckingContext current = contexts.get(i).clone();
                    try
                    {
                        for (Match match : list)
                        {
                            match.check(current);
                        }
                        List<Match> interpretation = new ArrayList<>(interpretations.get(i));
                        interpretation.addAll(list);
                        interpreted.add(interpretation);
                        cont.add(current);
                    }
                    catch (Exception e)
                    {
                        System.out.println("=> " + e.getMessage());
                    }
                }
            }
            interpretations = interpreted;
            contexts = cont;
        }

        printInterpretations(interpretations);
    }

    private static void printInterpretations(List<List<Match>> interpretations)
    {
        System.out.println(interpretations.size() + " interpretations found!");
        for (List<Match> interpretation : interpretations)
        {
            for (Match match : interpretation)
            {
                System.out.println(match);
            }
            System.out.println();
        }
    }
}
