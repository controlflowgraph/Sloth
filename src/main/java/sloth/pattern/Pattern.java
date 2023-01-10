package sloth.pattern;

import sloth.Provider;
import sloth.match.Match;
import sloth.match.Matcher;

import java.util.List;
import java.util.Map;

public record Pattern(String name, Matcher matcher)
{
    public List<Match> tryMatch(List<Pattern> patterns, Provider<String> provider, Match match)
    {
        int size = this.matcher.getMinimumSize();
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
}
