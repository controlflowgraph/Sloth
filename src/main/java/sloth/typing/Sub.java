package sloth.typing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Sub(Map<String, Type> mapping)
{
    public static Sub of(List<String> names, List<Type> subs)
    {
        Sub sub = new Sub();
        for (int i = 0; i < names.size(); i++)
        {
            sub.add(names.get(i), subs.get(i));
        }
        return sub;
    }

    public Sub()
    {
        this(new HashMap<>());
    }

    public void add(String name, Type type)
    {
        this.mapping.put(name, type);
    }

    public boolean hasSub(String name)
    {
        return tryGetSub(name).isPresent();
    }

    public Type getSub(String name)
    {
        return tryGetSub(name).orElseThrow();
    }

    public Optional<Type> tryGetSub(String name)
    {
        if (this.mapping.containsKey(name))
            return Optional.of(this.mapping.get(name));
        return Optional.empty();
    }
}
