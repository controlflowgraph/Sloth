package sloth.match;

import sloth.pattern.Pattern;

import java.util.*;

public record MatchingContext(List<Pattern> patterns, Map<String, Meta> info, Map<String, List<Match>> cache, Counter counter)
{
    public MatchingContext()
    {
        this(new ArrayList<>());
    }

    public MatchingContext(List<Pattern> patterns)
    {
        this(patterns, new HashMap<>(), new HashMap<>(), new Counter());
    }

    public void add(Pattern pattern)
    {
        this.patterns.add(pattern);
    }

    public void add(Meta info)
    {
        this.info.put(info.name(), info);
    }

    public void push()
    {
        this.counter.inc();
    }

    public void pop()
    {
        this.counter.dec();
    }

    public int size()
    {
        return this.counter.get();
    }
}
