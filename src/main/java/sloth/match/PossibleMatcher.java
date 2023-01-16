package sloth.match;

import sloth.Provider;

import java.util.ArrayList;
import java.util.List;

public record PossibleMatcher(Matcher matcher) implements Matcher
{
    @Override
    public List<Match> match(MatchingContext context, Provider<String> str, List<Match> matches)
    {
        if(matches.isEmpty())
            return List.of();
        List<Match> filtered = new ArrayList<>();
        for (Match match : matches)
        {
            filtered.add(match);
            filtered.addAll(this.matcher.match(context, str, List.of(match)));
        }
        return filtered;
    }

}
