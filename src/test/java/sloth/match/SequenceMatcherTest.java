package sloth.match;

import org.junit.jupiter.api.Test;
import sloth.Provider;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SequenceMatcherTest
{
    private final SequenceMatcher matcher = new SequenceMatcher(List.of(
            new WordMatcher("a"),
            new WordMatcher("b"),
            new WordMatcher("c")
    ));

    private Provider<String> getProvider()
    {
        return new Provider<>(List.of(
                "a",
                "b",
                "c",
                "d"
        ));
    }

    @Test
    void shouldMatchSequence()
    {
        List<Match> expected = List.of(
                new Match(0, 3, null, Map.of(), false)
        );

        List<Match> existing = List.of(
                new Match(0, 0, null, Map.of(), false),
                new Match(0, 1, null, Map.of(), false)
        );

        Provider<String> provider = getProvider();
        List<Match> actual = this.matcher.match(new MatchingContext(List.of()), provider, existing);
        assertEquals(expected, actual);
    }
}