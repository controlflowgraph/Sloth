package sloth.match;

import sloth.Provider;
import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;

public record SubMatcher(String name) implements Matcher
{
    @Override
    public List<Match> match(List<Pattern> patterns, Provider<String> str, List<Match> matches)
    {
        List<Match> filtered = new ArrayList<>();
        for (Match match : matches)
        {
            for (Pattern pattern : patterns)
            {
                str.index(match.end());
                List<Match> t = pattern.tryMatch(patterns, str, match);
                for (Match m : t)
                {
                    filtered.add(match.extend(m.end(), this.name, m));
                }
            }
        }
        return filtered;
    }

    @Override
    public int getMinimumSize()
    {
        return 0;
    }
}
