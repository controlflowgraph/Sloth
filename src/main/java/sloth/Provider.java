package sloth;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class Provider<T>
{
    private final List<T> elements;
    private int index;
    private int summed;

    public Provider(List<T> elements)
    {
        this.elements = elements;
    }

    public void require(int count)
    {
        this.summed += count;
    }

    public int require()
    {
        return this.summed;
    }

    public void setRequired(int req)
    {
        this.summed = req;
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

    public boolean hasRemaining()
    {
        return this.index + this.summed <= this.elements.size() + 1;
    }

    public int size()
    {
        return this.elements.size();
    }

    public String rest()
    {
        return this.elements.subList(this.index, this.elements.size()).toString();
    }
}
