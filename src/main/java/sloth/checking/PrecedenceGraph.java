package sloth.checking;

import java.util.*;

public class PrecedenceGraph
{
    private final Map<String, List<String>> mapping = new HashMap<>();
    private final Map<String, Integer> precedences = new HashMap<>();

    public PrecedenceGraph add(String name, List<String> weaker, List<String> stronger)
    {
        this.mapping.computeIfAbsent(name, k -> new ArrayList<>());
        for (String s : weaker)
            append(name, s);
        for (String s : stronger)
            append(s, name);
        return this;
    }

    private void append(String to, String value)
    {
        this.mapping.computeIfAbsent(to, k -> new ArrayList<>()).addAll(Arrays.asList(value));
    }

    public PrecedenceGraph compile()
    {
        this.precedences.clear();
        Set<String> visited = new HashSet<>();
        Set<String> missing = this.mapping.keySet();
        int iteration = 0;
        boolean changed;
        do
        {
            changed = false;
            Set<String> miss = new HashSet<>();
            for (String s : missing)
            {
                List<String> required = this.mapping.get(s);
                boolean isResolved = required.stream()
                        .map(visited::contains)
                        .reduce(true, (a, b) -> a && b);
                if(isResolved)
                {
                    visited.add(s);
                    this.precedences.put(s, iteration);
                    changed = true;
                }
                else
                {
                    miss.add(s);
                }
            }
            iteration++;
            missing = miss;
        }
        while (changed);
        if(!missing.isEmpty())
            throw new RuntimeException("There are some patterns remaining: " + missing + "!");
        return this;
    }

    public int get(String name)
    {
        return this.precedences.get(name);
    }
}

