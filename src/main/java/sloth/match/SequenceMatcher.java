package sloth.match;

import sloth.Provider;
import sloth.pattern.Pattern;

import java.util.List;

public record SequenceMatcher(List<Matcher> matchers) implements Matcher
{
    @Override
    public List<Match> match(List<Pattern> patterns, Provider<String> str, List<Match> matches)
    {
        for (Matcher matcher : this.matchers)
        {
            matches = matcher.match(patterns, str, matches);
        }
        return matches;
    }
}
