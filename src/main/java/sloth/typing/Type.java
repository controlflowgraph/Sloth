package sloth.typing;

import java.util.List;

public record Type(boolean param, String name, List<Type> generics)
{
    public Type substitute(Sub sub)
    {
        if (this.param)
        {
            return sub.getSub(this.name);
        }
        return new Type(
                false,
                this.name,
                this.generics.stream()
                        .map(t -> t.substitute(sub))
                        .toList());
    }
}
