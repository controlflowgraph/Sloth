package sloth.match;

import sloth.Provider;

import java.util.List;

public interface Matcher
{
    List<Match> match(MatchingContext context, Provider<String> str, List<Match> matches);

    int getMinimumSize();
}
