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
                        })
        );
        Provider<String> provider = new Provider<>(Lexer.lex("1 * 2 * 3"));

        List<List<Match>> parse = SyntaxMatcher.parse(patterns, provider);
        for (List<Match> matches : parse)
        {
            System.out.println("-".repeat(100));
            for (int i = 1; i < matches.size(); i++)
            {
                System.out.println(matches.get(i));
            }
        }
    }
}