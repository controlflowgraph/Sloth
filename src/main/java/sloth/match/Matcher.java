package sloth.match;

import sloth.Provider;
import sloth.pattern.Pattern;

import java.util.List;

public interface Matcher
{
    List<Match> match(List<Pattern> patterns, Provider<String> str, List<Match> matches);

    int getMinimumSize();
}
