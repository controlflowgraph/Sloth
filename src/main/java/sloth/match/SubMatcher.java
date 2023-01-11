package sloth.match;

import sloth.Provider;
import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record SubMatcher(String name) implements Matcher
{
    @Override
    public List<Match> match(MatchingContext context, Provider<String> str, List<Match> matches)
    {
        List<Match> filtered = new ArrayList<>();
        for (Match match : matches)
        {
            for (Pattern pattern : context.patterns())
            {
                str.index(match.end());
                Match start = new Match(match.end(), match.end(), null, Map.of());
                List<Match> t = pattern.tryMatch(context, str, start);
                for (Match m : t)
                {
                    filtered.add(match.extend(m.end(), this.name, m));
                }
            }
        }
        return filtered;
    }

}
