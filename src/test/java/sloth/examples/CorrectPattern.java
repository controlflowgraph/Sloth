package sloth.examples;

import sloth.pattern.Pattern;
import sloth.pattern.PatternMarker;

public class CorrectPattern
{
    @PatternMarker
    public static Pattern something()
    {
        return new Pattern("something", null, null, null);
    }
}
