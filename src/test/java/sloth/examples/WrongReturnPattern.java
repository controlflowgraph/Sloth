package sloth.examples;

import sloth.pattern.Pattern;
import sloth.pattern.PatternMarker;

public class WrongReturnPattern
{
    @PatternMarker
    public static Object something()
    {
        return new Pattern("something", null, null,null);
    }
}
