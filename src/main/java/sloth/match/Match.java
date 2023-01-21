package sloth.match;

import sloth.checking.CheckingContext;
import sloth.inference.Fragment;
import sloth.inference.InferenceContext;
import sloth.pattern.Pattern;

import java.util.*;
import java.util.function.Function;

import static sloth.match.Lst.add;

public record Match(int start, int end, Pattern pattern, Map<String, Lst<Object>> values, boolean empty)
{
    public static Match getStart(int position)
    {
        return new Match(position, position, null, Map.of(), true);
    }

    private Match(int start, int end, Pattern pattern, Map<String, Lst<Object>> values)
    {
        this(start, end, pattern, values, false);
    }

    public int flatten(InferenceContext context)
    {
        Fragment f = this.pattern.flatten().apply(context, this);
        context.register(f);
        return f.output();
    }
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

    public String attempt(Function<Match, String> func, String other)
    {
        // this is a very ugly hack to reduce the amount of code needed to display incomplete matches
        try
        {
            return func.apply(this);
        }
        catch (Exception e)
        {
            return other;
        }
    }

    public String attempt(String name)
    {
        return attempt(m -> m.values().get(name).element().toString(), "?");
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

    public void checkPrecedence(CheckingContext context)
    {
        this.pattern.validator().validate(this, context);
    }
}
