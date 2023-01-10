package sloth.examples;

import sloth.Pattern;
import sloth.PatternMarker;

public class NonStaticPattern
{
    @PatternMarker
    public Pattern something()
    {
        return new Pattern("something");
    }
}
