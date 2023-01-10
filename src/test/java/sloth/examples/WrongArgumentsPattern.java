package sloth.examples;

import sloth.pattern.Pattern;
import sloth.pattern.PatternMarker;

public class WrongArgumentsPattern
{
    @PatternMarker
    public static Pattern something(Object o)
    {
        return new Pattern("something", null);
    }
}
