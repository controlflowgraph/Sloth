package sloth.checking;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

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
        Set<String> missing = this.mapping.keySet();
        Set<String> m = this.mapping.values()
                .stream()
                .flatMap(List::stream)
                .filter(not(missing::contains))
                .collect(Collectors.toSet());
        if(!m.isEmpty())
            throw new RuntimeException("Undefined dependencies! " + m);
        int iteration = 0;
        boolean changed;
        do
        {
            changed = false;
            Set<String> miss = new HashSet<>();
            for (String s : missing)
            {
                List<String> required = this.mapping.get(s);
                boolean isMissing = required.stream()
                        .map(missing::contains)
                        .reduce(false, (a, b) -> a || b);
                if(isMissing)
                {
                    miss.add(s);
                }
                else
                {
                    this.precedences.put(s, iteration);
                    changed = true;
                }
            }
            iteration++;
            missing = miss;
        }
        while (changed);
        if(!missing.isEmpty())
        {
            StringBuilder text = new StringBuilder();
            for (String s : missing)
            {
                text.append("\n")
                        .append(s)
                        .append(" -> ")
                        .append(this.mapping.get(s)
                                .stream()
                                .filter(not(this.precedences::containsKey))
                                .toList()
                        );
            }
            throw new RuntimeException("There are some patterns remaining: " + text);
        }
        return this;
    }

    public int get(String name)
    {
        return this.precedences.get(name);
    }
}

