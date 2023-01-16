package sloth.match;

import sloth.Provider;

import java.util.ArrayList;
import java.util.List;

public record MultiMatcher(boolean zero, Matcher matcher) implements Matcher
{
    @Override
    public List<Match> match(MatchingContext context, Provider<String> str, List<Match> matches)
    {
        if(matches.isEmpty())
            return List.of();
        List<Match> filtered = new ArrayList<>();
        for (Match match : matches)
        {
            if (this.zero)
                filtered.add(match);
            List<Match> iteration = List.of(match);
            while (!iteration.isEmpty())
            {
                iteration = this.matcher.match(context, str, iteration);
                filtered.addAll(iteration);
            }
        }
        return filtered;
    }
}
