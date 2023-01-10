package sloth.match;

import sloth.Provider;
import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;

public record PossibleMatcher(Matcher matcher) implements Matcher
{
    @Override
    public List<Match> match(List<Pattern> patterns, Provider<String> str, List<Match> matches)
    {
        List<Match> filtered = new ArrayList<>();
        for (Match match : matches)
        {
            filtered.add(match);
            filtered.addAll(this.matcher.match(patterns, str, List.of(match)));
        }
        return filtered;
    }
}
