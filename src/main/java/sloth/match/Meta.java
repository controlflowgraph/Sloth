package sloth.match;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record Meta(String name, Set<Object> flags, Map<String, Object> attributes)
{
    public Meta(String name)
    {
        this(name, new HashSet<>(), new HashMap<>());
    }

    public Meta add(Object obj)
    {
        this.flags.add(obj);
        return this;
    }

    public Meta add(String name, Object obj)
    {
        this.attributes.put(name, obj);
        return this;
    }

    public boolean has(Object obj)
    {
        return this.flags.contains(obj);
    }

    public <T> T get(String key, Class<T> cls)
    {
        return cls.cast(get(key));
    }

    public Object get(String key)
    {
        return this.attributes.get(key);
    }
}
