package sloth.typing;

import java.util.List;
import java.util.Set;

public record Node(String name, List<String> params, Set<Type> impls)
{
}
