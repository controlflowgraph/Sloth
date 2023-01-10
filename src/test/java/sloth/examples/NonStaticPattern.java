package sloth.examples;

import sloth.pattern.Pattern;
import sloth.pattern.PatternMarker;

public class NonStaticPattern
{
    @PatternMarker
    public Pattern something()
    {
        return new Pattern("something");
    }
}
