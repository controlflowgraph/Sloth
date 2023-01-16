package sloth.match;

import sloth.Provider;

import java.util.List;

public record Require(int amount) implements Matcher
{
    @Override
    public List<Match> match(MatchingContext context, Provider<String> str, List<Match> matches)
    {
        if(matches.isEmpty())
            return List.of();
        str.require(this.amount);
        return matches;
    }

}
