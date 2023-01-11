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
        List<Pattern> patterns = List.of(
                new Pattern("num", new NumMatcher(), m -> {
                    if(!m.values().containsKey("val"))
                        return "€";
                    return m.values().get("val").element().toString();
                }),
                new Pattern("sub", new SequenceMatcher(List.of(
                        new WordMatcher("("),
                        new SubMatcher("s"),
                        new WordMatcher(")")
                ))),
                new Pattern("times", new SequenceMatcher(List.of(
                        new SubMatcher("a"),
                        new WordMatcher("*"),
                        new SubMatcher("b")
                )),
                        m -> {
                            if(!m.values().containsKey("a"))
                                return "( € * € )";
                            if(!m.values().containsKey("b"))
                                return "( " + m.values().get("a").element() + " * € ) ";
                            return "( " + m.values().get("a").element() + " * " + m.values().get("b").element() + ")";
                        }),
                new Pattern("plus", new SequenceMatcher(List.of(
                        new SubMatcher("a"),
                        new WordMatcher("+"),
                        new SubMatcher("b")
                )),
                        m -> {
                            if(!m.values().containsKey("a"))
                                return "( € + € )";
                            if(!m.values().containsKey("b"))
                                return "( " + m.values().get("a").element() + " + € ) ";
                            return "( " + m.values().get("a").element() + " + " + m.values().get("b").element() + ")";
                        }),
                new Pattern("let", new SequenceMatcher(List.of(
                        new WordMatcher("let"),
                        new TextMatcher("n"),
                        new WordMatcher("be"),
                        new WordMatcher("equal"),
                        new WordMatcher("to"),
                        new SubMatcher("v")
                )),
                        m -> {
                            if(!m.values().containsKey("n"))
                                return "( let ? be equal to ? )";
                            if(!m.values().containsKey("v"))
                                return "( " + m.values().get("n").element() + " be equal to ? ) ";
                            return "( let " + m.values().get("n").element() + " be equal to " + m.values().get("v").element() + ")";
                        }),
                new Pattern("var", new TextMatcher("name"))
        );
        Provider<String> provider = new Provider<>(Lexer.lex("let a be equal to ((1 * 2) + 3). let b be equal to 5."));
        MatchingContext context = new MatchingContext(patterns);

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