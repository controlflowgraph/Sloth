package sloth.match;

import sloth.Provider;

import java.util.List;

public record SequenceMatcher(List<Matcher> matchers) implements Matcher
{
    @Override
    public List<Match> match(MatchingContext context, Provider<String> str, List<Match> matches)
    {
        if(matches.isEmpty())
            return List.of();
        int required = str.require();
        for (int i = 0; !matches.isEmpty() && i < this.matchers.size(); i++)
        {
            Matcher matcher = this.matchers.get(i);
            matches = matcher.match(context, str, matches);
        }
        str.setRequired(required);
        return matches;
    }
}
