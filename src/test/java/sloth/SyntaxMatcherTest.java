package sloth;

import org.junit.jupiter.api.Test;
import sloth.match.*;
import sloth.pattern.Pattern;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SyntaxMatcherTest
{
    public static void main(String[] args)
    {
        List<Pattern> patterns = List.of(
                new Pattern("num", new NumMatcher()),
                new Pattern("times", new SequenceMatcher(List.of(
                        new SubMatcher("a"),
                        new WordMatcher("*"),
                        new SubMatcher("b")
                )))
        );
        Provider<String> provider = new Provider<>(Lexer.lex("10 * 20 * 30"));
        List<List<Match>> parse = SyntaxMatcher.parse(patterns, provider);
        for (List<Match> matches : parse)
        {
            System.out.println("-".repeat(100));
            for (Match match : matches)
            {
                System.out.println(match);
            }
        }
    }
}