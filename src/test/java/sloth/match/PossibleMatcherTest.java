package sloth.match;

import org.junit.jupiter.api.Test;
import sloth.Provider;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PossibleMatcherTest
{
    private final PossibleMatcher matcher = new PossibleMatcher(new WordMatcher("b"));

    @Test
    void shouldRetainOldMatches()
    {
        List<Match> expected = List.of(
                new Match(0, 0, Map.of()),
                new Match(0, 1, Map.of()),
                new Match(0, 2, Map.of())
        );
        List<Match> existing = List.of(
                new Match(0, 0, Map.of()),
                new Match(0, 1, Map.of())
        );
        Provider<String> provider = new Provider<>(List.of(
                "a",
                "b"
        ));
        List<Match> actual = this.matcher.match(List.of(), provider, existing);
        assertEquals(expected, actual);
    }
}