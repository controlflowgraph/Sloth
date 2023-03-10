package sloth.match;

import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record MatchingContext(List<Pattern> patterns, Map<String, Meta> info)
{
    public MatchingContext()
    {
        this(new ArrayList<>());
    }

    public MatchingContext(List<Pattern> patterns)
    {
        this(patterns, new HashMap<>());
    }

    public void add(Pattern pattern)
    {
        this.patterns.add(pattern);
    }

    public void add(Meta info)
    {
        this.info.put(info.name(), info);
    }
}
