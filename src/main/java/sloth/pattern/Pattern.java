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
        if (!provider.hasRemaining(this.matcher.getMinimumSize()))
            return List.of();
        Match start = new Match(match.end(), match.end(), null, Map.of());
        List<Match> matches = this.matcher.match(patterns, provider, List.of(start));
        return matches.stream()
                .map(m -> m.extend(this))
                .toList();
    }
}
