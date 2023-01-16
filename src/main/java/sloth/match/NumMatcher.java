package sloth.match;

import sloth.Provider;

import java.util.ArrayList;
import java.util.List;

public record NumMatcher() implements Matcher
{
    @Override
    public List<Match> match(MatchingContext context, Provider<String> str, List<Match> matches)
    {
        if(matches.isEmpty())
            return List.of();
        List<Match> filtered = new ArrayList<>();
        for (Match match : matches)
        {
            str.index(match.end());
            if (str.matches(s -> s.matches("\\d+")))
            {
                String value = str.next();
                filtered.add(match.extend(str.index(), "val", value));
            }
        }
        return filtered;
    }

}
