package sloth.examples;

import sloth.Pattern;
import sloth.PatternMarker;

public class CorrectPattern
{
    @PatternMarker
    public static Pattern something()
    {
        return new Pattern("something");
    }
}
