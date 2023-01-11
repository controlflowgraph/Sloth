package sloth.match;

import sloth.Provider;

import java.util.List;

public record SequenceMatcher(List<Matcher> matchers) implements Matcher
{
    @Override
    public List<Match> match(MatchingContext context, Provider<String> str, List<Match> matches)
    {
        List<Matcher> matcherList = this.matchers;
        for (int i = 0; !matches.isEmpty() && i < matcherList.size(); i++)
        {
            Matcher matcher = matcherList.get(i);
            matches = matcher.match(context, str, matches);
        }
        return matches;
    }

    @Override
    public int getMinimumSize()
    {
        return this.matchers.stream().mapToInt(Matcher::getMinimumSize).sum();
    }
}
