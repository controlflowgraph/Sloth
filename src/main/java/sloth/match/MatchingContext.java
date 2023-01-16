package sloth.match;

import sloth.pattern.Pattern;

import java.util.*;

public record MatchingContext(List<Pattern> patterns, Map<String, Meta> info, Map<String, List<Match>> cache, Deque<Boolean> stack)
{
    public MatchingContext()
    {
        this(new ArrayList<>());
    }

    public MatchingContext(List<Pattern> patterns)
    {
        this(patterns, new HashMap<>(), new HashMap<>(), new ArrayDeque<>());
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
        this.stack.push(true);
    }

    public void pop()
    {
        this.stack.pop();
    }

    public int size()
    {
        return this.stack.size();
    }
}
