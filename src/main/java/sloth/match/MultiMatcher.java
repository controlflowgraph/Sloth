package sloth.match;

import sloth.Provider;
import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;

public record MultiMatcher(boolean zero, Matcher matcher) implements Matcher
{
    @Override
    public List<Match> match(List<Pattern> patterns, Provider<String> str, List<Match> matches)
    {
        List<Match> filtered = new ArrayList<>();
        for (Match match : matches)
        {
            if(this.zero)
                filtered.add(match);
            List<Match> iteration = List.of(match);
            while (!iteration.isEmpty())
            {
                iteration = this.matcher.match(patterns, str, iteration);
                filtered.addAll(iteration);
            }
        }
        return filtered;
    }

    @Override
    public int getMinimumSize()
    {
        return this.zero ? 0 : this.matcher.getMinimumSize();
    }
}
