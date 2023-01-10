package sloth.pattern;

import sloth.Provider;
import sloth.match.Match;
import sloth.match.Matcher;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record Pattern(String name, Matcher matcher, Function<Match, String> tree)
{
    public Pattern(String name, Matcher matcher)
    {
        this(name, matcher, m -> m.values().toString());
    }

    public List<Match> tryMatch(List<Pattern> patterns, Provider<String> provider, Match match)
    {
        int size = 1; this.matcher.getMinimumSize();
        if (!provider.hasRemaining(size))
            return List.of();
        provider.require(size);
        Match start = new Match(match.end(), match.end(), null, Map.of());
        List<Match> matches = this.matcher.match(patterns, provider, List.of(start));
        provider.free();
        return matches.stream()
                .map(m -> m.extend(this))
                .toList();
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
