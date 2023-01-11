package sloth;

import org.junit.jupiter.api.Test;
import sloth.match.*;
import sloth.pattern.Pattern;

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
        Provider<String> provider = new Provider<>(Lexer.lex("let a be equal to ((1 * 2) + 3). let b be equal to 5."));

        List<List<List<Match>>> parse = SyntaxMatcher.parse(context, provider);
        System.out.println("RESULTS:");
        for (List<List<Match>> lists : parse)
        {
            System.out.println("\tPART:");
            for (List<Match> matches : lists)
            {
                System.out.println("\t\tINTERPRETATION:");
                for (int i = 1; i < matches.size(); i++)
                {
                    System.out.println("\t\t\t" + matches.get(i));
                }
            }
        }
    }
}