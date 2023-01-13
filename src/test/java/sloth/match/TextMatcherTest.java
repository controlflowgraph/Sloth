package sloth.match;

import org.junit.jupiter.api.Test;
import sloth.Provider;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TextMatcherTest
{
    private final TextMatcher matcher = new TextMatcher("v");

    private Provider<String> getProvider()
    {
        return new Provider<>(List.of(
                "let",
                "test",
                "be"
        ));
    }

    @Test
    void shouldNotAlterOldMatch()
    {
        List<Match> expected = List.of(
                new Match(0, 2, null, Map.of("v", new Lst<>("test", null)), false)
        );
        List<Match> existing = List.of(
                new Match(0, 1, null, Map.of(), false)
        );
        Provider<String> provider = getProvider();
        List<Match> actual = this.matcher.match(new MatchingContext(List.of()), provider, existing);
        assertEquals(expected, actual);
    }
}