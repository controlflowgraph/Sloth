package sloth.match;

import sloth.Provider;
import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;

public record TextMatcher(String name) implements Matcher
{
    @Override
    public List<Match> match(List<Pattern> patterns, Provider<String> str, List<Match> matches)
    {
        List<Match> filtered = new ArrayList<>();
        for (Match match : matches)
        {
            str.index(match.end());
            if(str.has())
            {
                String value = str.next();
                filtered.add(match.extend(str.index(), this.name, value));
            }
        }
        return filtered;
    }

    @Override
    public int getMinimumSize()
    {
        return 1;
    }
}
