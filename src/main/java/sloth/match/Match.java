package sloth.match;

import java.util.*;

import static sloth.match.Lst.add;

public record Match(int start, int end, Map<String, Lst<Object>> values)
{
    public Match extend(int end)
    {
        return new Match(this.start, end, this.values);
    }

    public Match extend(int end, String name, Object value)
    {
        Map<String, Lst<Object>> objects = new HashMap<>(this.values);
        objects.put(name, add(value, objects.get(name)));
        return new Match(this.start, end, objects);
    }
}
