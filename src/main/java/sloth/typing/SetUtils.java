package sloth.typing;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SetUtils
{
    private SetUtils()
    {
    }

    public static <T> Set<T> intersection(Set<T> a, Set<T> b)
    {
        return a.stream()
                .filter(b::contains)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public static <T> Set<T> union(Set<T> a, Set<T> b)
    {
        Set<T> set = new HashSet<>(a.size() + b.size());
        set.addAll(a);
        set.addAll(b);
        return set;
    }

    public static <T> boolean isSubset(Set<T> sub, Set<T> over)
    {
        return sub.stream()
                .map(over::contains)
                .reduce(true, (a, b) -> a && b);
    }
}
