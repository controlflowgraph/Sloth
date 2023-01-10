package sloth.match;

import java.util.ArrayList;
import java.util.List;

public record Lst<T>(T element, Lst<T> parent)
{
    public static <T> Lst<T> of(List<T> elements)
    {
        Lst<T> current = null;
        for (T element : elements)
        {
            current = new Lst<>(element, current);
        }
        return current;
    }
    public static <T> Lst<T> add(T element, Lst<T> rest)
    {
        return new Lst<>(element, rest);
    }

    public static <T> List<T> asList(Lst<T> lst)
    {
        List<T> list = new ArrayList<>();
        while (lst != null)
        {
            list.add(lst.element);
            lst = lst.parent;
        }
        return list;
    }
}
