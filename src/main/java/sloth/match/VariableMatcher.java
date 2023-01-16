package sloth.match;

import sloth.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public record VariableMatcher(String name) implements Matcher
{
    private static Predicate<String> VARIABLE_NAME_CHECK = Pattern.compile("[_a-zA-Z]\\w*").asPredicate();

    @Override
    public List<Match> match(MatchingContext context, Provider<String> str, List<Match> matches)
    {
        if(matches.isEmpty())
            return List.of();
        List<Match> filtered = new ArrayList<>();
        for (Match match : matches)
        {
            str.index(match.end());
            if (str.matches(VARIABLE_NAME_CHECK))
            {
                String value = str.next();
                filtered.add(match.extend(str.index(), this.name, value));
            }
        }
        return filtered;
    }

}
