package sloth;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class Provider<T>
{
    private final Deque<Integer> consumed = new ArrayDeque<>();
    private final List<T> elements;
    private int index;

    public Provider(List<T> elements)
    {
        this.elements = elements;
    }

    public void require(int count)
    {
        this.consumed.push(count);
    }

    public void free()
    {
        this.consumed.pop();
    }

    private int consumed()
    {
        if (this.consumed.isEmpty())
            return 0;
        int summed = -this.consumed.peek();
        for (int integer : this.consumed)
            summed += integer;
        return summed;
    }

    public int index()
    {
        return this.index;
    }

    public void index(int index)
    {
        this.index = index;
    }

    public T next()
    {
        return this.elements.get(this.index++);
    }

    public T peek()
    {
        return this.elements.get(this.index);
    }

    public boolean has()
    {
        return this.index < this.elements.size();
    }

    public boolean matches(Predicate<T> check)
    {
        return has() && check.test(peek());
    }

    public boolean matches(T element)
    {
        return matches(e -> Objects.equals(element, e));
    }

    public boolean hasRemaining(int required)
    {
        return this.index + consumed() <= this.elements.size();
    }

    public int size()
    {
        return this.elements.size();
    }
}
