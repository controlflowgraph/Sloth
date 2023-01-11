package sloth.match;

import sloth.pattern.Pattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record MatchingContext(List<Pattern> patterns, Map<String, Meta> info)
{
    public MatchingContext(List<Pattern> patterns)
    {
        this(patterns, new HashMap<>());
    }
}
