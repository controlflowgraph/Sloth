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
        if (!provider.hasRemaining())
            return List.of();

        Match start = Match.getStart(match.end());
        List<Match> matches = this.matcher.match(context, provider, List.of(start));

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
