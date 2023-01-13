package sloth.match;

import org.junit.jupiter.api.Test;
import sloth.Lexer;
import sloth.Provider;
import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatcherTest
{
    private static Provider<String> of(String text)
    {
        return new Provider<>(Lexer.lex(text));
    }

    @Test
    void shouldMatchSimpleStructure()
    {
        List<Match> expected = List.of(
                new Match(0, 5, null, Map.of(), false),
                new Match(0, 6, null, Map.of(), false)
        );
        Matcher matcher = new SequenceMatcher(List.of(
                new MultiMatcher(false, new WordMatcher("a")),
                new MultiMatcher(false, new WordMatcher("b"))
        ));
        Provider<String> provider = of("a a a a b b");
        List<Match> existing = List.of(
                new Match(0, 0, null, Map.of(), false)
        );
        List<Match> actual = matcher.match(new MatchingContext(List.of()), provider, existing);
        assertEquals(expected, actual);
    }

    @Test
    void shouldMatchSimpleStructure2()
    {
        List<Match> expected = List.of(
                new Match(0, 2, null, Map.of("v", Lst.of(List.of("a"))), false),
                new Match(0, 4, null, Map.of("v", Lst.of(List.of("a", "b"))), false),
                new Match(0, 6, null, Map.of("v", Lst.of(List.of("a", "b", "c"))), false)
        );
        Matcher matcher = new MultiMatcher(false,
                new SequenceMatcher(List.of(
                        new WordMatcher("a"),
                        new TextMatcher("v")
                ))
        );
        Provider<String> provider = of("a a a b a c");
        List<Match> existing = List.of(
                new Match(0, 0, null, Map.of(), false)
        );
        List<Match> actual = matcher.match(new MatchingContext(List.of()), provider, existing);
        assertEquals(expected, actual);
    }
}