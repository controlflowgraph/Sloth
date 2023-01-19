package sloth.checking;

import sloth.match.Match;

public interface Validator
{
    void validate(Match match, CheckingContext context);
}
