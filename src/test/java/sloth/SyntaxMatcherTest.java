package sloth;

import org.junit.jupiter.api.Test;
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
        context.add(new Pattern("num",
                new NumMatcher(),
                m -> m.attempt("val")
        ));
        context.add(new Pattern("sub",
                new SequenceMatcher(List.of(
                        new WordMatcher("("),
                        new SubMatcher("s"),
                        new WordMatcher(")")
                ))));
        context.add(new Pattern("times",
                new SequenceMatcher(List.of(
                        new SubMatcher("a"),
                        new WordMatcher("*"),
                        new SubMatcher("b")
                )),
                m -> "( %s * %s )".formatted(m.attempt("a"), m.attempt("b"))
        ));
        context.add(new Pattern("plus",
                new SequenceMatcher(List.of(
                        new SubMatcher("a"),
                        new WordMatcher("+"),
                        new SubMatcher("b")
                )),
                m -> "( %s + %s )".formatted(m.attempt("a"), m.attempt("b"))
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
                m -> "( let %s be equal to %s )".formatted(m.attempt("n"), m.attempt("v"))
        ));
        context.add(new Pattern("var",
                new TextMatcher("name")
        ));
//        Provider<String> provider = new Provider<>(Lexer.lex("let a be equal to (1 * 2 + 3). let b be equal to 5."));
        Provider<String> provider = new Provider<>(Lexer.lex("1 * 2 + 3. 1 * 2 + 3"));

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
        System.out.println("COMBINATIONS: " + combinations.size());
        for (List<Match> combination : combinations)
        {
            System.out.println("\tVERSION:");
            for (Match match : combination)
            {
                System.out.println("\t\t" + match);
            }
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
