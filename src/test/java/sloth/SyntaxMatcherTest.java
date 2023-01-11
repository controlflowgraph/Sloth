package sloth;

import org.junit.jupiter.api.Test;
import sloth.checking.CheckingContext;
import sloth.checking.Type;
import sloth.match.*;
import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                        new SubMatcher("s"),
                        new WordMatcher(")")
                )),
                m -> "( %s )".formatted(m.attempt("s")),
                (m, c) -> {
                    Match match = (Match) m.values().get("s").element();
                    return match.check(c);
                }
        ));
        context.add(new Pattern("times",
                new SequenceMatcher(List.of(
                        new SubMatcher("a"),
                        new WordMatcher("*"),
                        new SubMatcher("b")
                )),
                m -> "( %s * %s )".formatted(m.attempt("a"), m.attempt("b")),
                (m, c) -> {
                    Type a = ((Match) m.values().get("a").element()).check(c);
                    Type b = ((Match) m.values().get("b").element()).check(c);
                    if (a.equals(number) && b.equals(number))
                        return number;
                    throw new RuntimeException("Must be numbers!");
                }
        ));
        context.add(new Pattern("plus",
                new SequenceMatcher(List.of(
                        new SubMatcher("a"),
                        new WordMatcher("+"),
                        new SubMatcher("b")
                )),
                m -> "( %s + %s )".formatted(m.attempt("a"), m.attempt("b")),
                (m, c) -> {
                    Type a = ((Match) m.values().get("a").element()).check(c);
                    Type b = ((Match) m.values().get("b").element()).check(c);
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
//        Provider<String> provider = new Provider<>(Lexer.lex("let a be equal to (1 * 2 + 3). let b be equal to 5."));
        Provider<String> provider = new Provider<>(Lexer.lex("""
                let a be equal to 10 + 20.
                let b be equal to 20 + 30.
                a + b.
                """));
        System.out.println(provider.rest());

        List<List<List<Match>>> parse = SyntaxMatcher.parse(context, provider);

        // clean the starts
        List<List<List<Match>>> cleaned = new ArrayList<>();
        for (List<List<Match>> lists : parse)
        {
            List<List<Match>> cls = new ArrayList<>();
            for (List<Match> matches : lists)
            {
                cls.add(matches.subList(1, matches.size()));
            }
            cleaned.add(cls);
        }

//        System.out.println("RESULTS:");
//        for (List<List<Match>> lists : cleaned)
//        {
//            System.out.println("\tPART:");
//            for (List<Match> matches : lists)
//            {
//                System.out.println("\t\tINTERPRETATION:");
//                for (Match match : matches)
//                {
//                    System.out.println("\t\t\t" + match);
//                }
//            }
//        }

        System.out.println("\n\n\n");
        List<List<Match>> combinations = createCombinations(cleaned);

        List<List<Match>> valid = new ArrayList<>();
        System.out.println("COMBINATIONS: " + combinations.size());
        for (List<Match> combination : combinations)
        {
//            System.out.println("\tVERSION:");
//            for (Match match : combination)
//            {
//                System.out.println("\t\t" + match);
//            }

            CheckingContext check = new CheckingContext();
            try
            {
                for (Match match : combination)
                {
                    match.check(check);
                }
                System.out.println("\t=> Valid!");
                valid.add(combination);
            }
            catch (Exception e)
            {
                System.out.println("\t=> " + e.getMessage());
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("FOUND " + valid.size() + " VALID INTERPRETATIONS!");
        System.out.println();
        for (List<Match> matches : valid)
        {
            System.out.println("INTERPRETATION:");
            for (Match match : matches)
            {
                System.out.println("\t" + match);
            }
            System.out.println();
        }
    }

    private static List<List<Match>> createCombinations(List<List<List<Match>>> source)
    {
        List<List<Match>> combinations = new ArrayList<>();
        combinations.add(List.of());
        for (List<List<Match>> lists : source)
        {
            List<List<Match>> added = new ArrayList<>();
            for (List<Match> list : lists)
            {
                for (List<Match> combination : combinations)
                {
                    List<Match> matches = new ArrayList<>(combination);
                    matches.addAll(list);
                    added.add(matches);
                }
            }
            combinations = added;
        }
        return combinations;
    }
}
