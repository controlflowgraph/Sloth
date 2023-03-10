package sloth.match;

import sloth.Provider;

import java.util.List;

public record SequenceMatcher(List<Matcher> matchers) implements Matcher
{
    @Override
    public List<Match> match(MatchingContext context, Provider<String> str, List<Match> matches)
    {
        for (Matcher matcher : this.matchers)
        {
            matches = matcher.match(context, str, matches);
        }
        return matches;
    }
}
