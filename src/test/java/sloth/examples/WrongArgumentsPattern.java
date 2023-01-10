package sloth.examples;

import sloth.Pattern;
import sloth.PatternMarker;

public class WrongArgumentsPattern
{
    @PatternMarker
    public static Pattern something(Object o)
    {
        return new Pattern("something");
    }
}
