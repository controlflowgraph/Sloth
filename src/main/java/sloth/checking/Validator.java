package sloth.checking;

import sloth.match.Match;

public interface Validator
{
    Type validate(Match match, CheckingContext context);
}
