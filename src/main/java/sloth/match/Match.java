package sloth.match;

import sloth.pattern.Pattern;

import java.util.*;

import static sloth.match.Lst.add;

public record Match(int start, int end, Pattern pattern, Map<String, Lst<Object>> values)
{
    public Match extend(int end)
    {
        return new Match(this.start, end, this.pattern, this.values);
    }

    public Match extend(int end, String name, Object value)
    {
        Map<String, Lst<Object>> objects = new HashMap<>(this.values);
        objects.put(name, add(value, objects.get(name)));
        return new Match(this.start, end, this.pattern, objects);
    }

    public Match extend(Pattern pattern)
    {
        return new Match(this.start, this.end, pattern, this.values);
    }

    @Override
    public String toString()
    {
        if(this.pattern != null)
            return this.pattern.tree().apply(this);
        return "Match{" + start +
                ", " + end +
                ", " + pattern +
                ", " + values +
                '}';
    }
}
