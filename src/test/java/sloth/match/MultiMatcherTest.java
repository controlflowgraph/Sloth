package sloth.match;

import org.junit.jupiter.api.Test;
import sloth.Provider;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MultiMatcherTest
{
    private final MultiMatcher matcher = new MultiMatcher(false, new WordMatcher("a"));

    @Test
    void shouldRetainOldMatches()
    {
        List<Match> expected = List.of(
                new Match(0, 1, null, Map.of()),
                new Match(0, 2, null, Map.of())
        );
        List<Match> existing = List.of(
                new Match(0, 0, null, Map.of())
        );
        Provider<String> provider = new Provider<>(List.of(
                "a",
                "a",
                "b"
        ));
        List<Match> actual = this.matcher.match(List.of(), provider, existing);
        assertEquals(expected, actual);
    }
}