package sloth.pattern;

import sloth.Provider;
import sloth.checking.Validator;
import sloth.match.Match;
import sloth.match.Matcher;
import sloth.match.MatchingContext;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record Pattern(String name, Matcher matcher, Function<Match, String> tree, Validator validator)
{
    public Pattern(String name, Matcher matcher, Function<Match, String> tree)
    {
        this(name, matcher, tree, null);
    }

    public Pattern(String name, Matcher matcher)
    {
        this(name, matcher, m -> m.values().toString());
    }

    public List<Match> tryMatch(MatchingContext context, Provider<String> provider, Match match)
    {
        int size = 1; this.matcher.getMinimumSize();
        if (!provider.hasRemaining(size))
            return List.of();
        provider.require(size);
        Match start = new Match(match.end(), match.end(), null, Map.of());
        List<Match> matches = this.matcher.match(context, provider, List.of(start));
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
