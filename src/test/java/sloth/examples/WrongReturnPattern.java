package sloth.examples;

import sloth.Pattern;
import sloth.PatternMarker;

public class WrongReturnPattern
{
    @PatternMarker
    public static Object something()
    {
        return new Pattern("something");
    }
}
