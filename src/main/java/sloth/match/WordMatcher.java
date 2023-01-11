package sloth.match;

import sloth.Provider;

import java.util.ArrayList;
import java.util.List;

public record WordMatcher(String word) implements Matcher
{
    @Override
    public List<Match> match(MatchingContext context, Provider<String> str, List<Match> matches)
    {
        List<Match> filtered = new ArrayList<>();
        for (Match match : matches)
        {
            str.index(match.end());
            if (str.matches(this.word))
            {
                str.next();
                filtered.add(match.extend(str.index()));
            }
        }
        return filtered;
    }

}
