package sloth.match;

import org.junit.jupiter.api.Test;
import sloth.Provider;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WordMatcherTest
{
    private final WordMatcher matcher = new WordMatcher("something");

    private Provider<String> getProvider()
    {
        return new Provider<>(List.of(
                "this",
                "is",
                "something"
        ));
    }

    @Test
    void shouldMatchWhenWordFollows()
    {
        List<Match> expected = List.of(
                new Match(0, 3, Map.of())
        );
        Provider<String> provider = getProvider();
        List<Match> matches = List.of(
                new Match(0, 0, Map.of()),
                new Match(0, 2, Map.of())
        );
        List<Match> actual = this.matcher.match(List.of(), provider, matches);
        assertEquals(expected, actual);
    }
}