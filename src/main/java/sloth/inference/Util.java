package sloth.inference;

import java.util.*;
import java.util.stream.Collectors;

public class Util
{
    private Util()
    {
    }

    public static Signature findSignature(InheritanceTree tree, List<Signature> signatures, String name, List<Type> parameter)
    {
        List<Signature> matches = findAllSignatures(tree, signatures, name, parameter);
        if (matches.isEmpty())
            throw new RuntimeException("No matches found!");
        if (matches.size() > 1)
            throw new RuntimeException("Multiple matches found!");
        return matches.get(0);
    }

    public static List<Signature> findAllSignatures(InheritanceTree tree, List<Signature> signatures, String name, List<Type> parameter)
    {
        List<Signature> matches = new ArrayList<>();
        for (Signature signature : signatures)
        {
            if (signature.name().equals(name))
            {
                if (matches(tree, signature, parameter))
                {
                    matches.add(signature);
                }
            }
        }
        return matches;
    }

    public static boolean matches(InheritanceTree tree, Signature signature, List<Type> parameter)
    {
        List<Type> params = signature.parameters();
        if (parameter.size() != params.size())
            return false;

        List<Set<Type>> filtered = new ArrayList<>();
        for (int i = 0; i < parameter.size(); i++)
        {
            Type type = parameter.get(i);
            Set<Type> actual = tree.differentiate(type);
            Type expected = signature.parameters().get(i);
            Set<Type> collect = actual.stream()
                    .filter(expected::matches)
                    .collect(Collectors.toSet());
            filtered.add(collect);
        }

        List<List<Type>> combinations = combinations(filtered);
        for (List<Type> combination : combinations)
        {
            Map<String, List<Type>> generics = new HashMap<>();
            for (int i = 0; i < combination.size(); i++)
            {
                Type actual = combination.get(i);
                Type expected = signature.parameters().get(i);
                expected.collect(generics, actual);
            }

            if (generics.size() == signature.generics().size())
            {
                boolean reduce = generics.values()
                        .stream()
                        .map(Util::allEqual)
                        .reduce(true, (a, b) -> a && b);
                if (reduce)
                    return true;
            }
        }
        return false;

        // TODO: think about what to do when i have two parameters `<T> ... test(T, T)` and i need to select the most specific
        //       type for both
        //       --> it doesnt matter since there are no functions that can be called on such a "blank" type
        //           as long as it matches it should be fine :D
        // TODO: find all combinations of types and check each for generic validity

//        Map<String, List<Type>> map = new HashMap<>();
//        for (int i = 0; i < params.size(); i++)
//        {
//            Set<Type> actual = tree.differentiate(parameter.get(i));
//            Type expected = params.get(i);
//            List<Type> types = actual.stream()
//                    .filter(expected::matches)
//                    .toList();
//            System.out.println(expected + " <-> " + actual);
//            System.out.println(types);
//            if(types.size() != 1)
//                return false;
//            expected.collect(map, types.get(0));
//        }
//        System.out.println("WHAT???");
//        if(map.size() != signature.generics().size())
//            return false;
//        for (String generic : signature.generics())
//        {
//            if(!map.containsKey(generic))
//                return false;
//            List<Type> over = map.get(generic);
//            Set<Type> simple = tree.differentiate(over.get(0));
//            for (int i = 1; i < over.size(); i++)
//            {
//                simple = SetUtils.intersection(simple, tree.differentiate(over.get(i)));
//            }
//            Set<Type> types = tree.eliminateRedundant(simple);
//            System.out.println(types + " AFTER ELIMINATION");
//            if(types.size() != 1)
//                return false;
//        }
//        // TODO: test if the selection of the generics works :D
//        System.out.println(map);
//        return true;
    }

    private static boolean allEqual(List<Type> elements)
    {
        for (Type element : elements)
        {
            if (!element.equals(elements.get(0)))
                return false;
        }
        return true;
    }

    private static List<List<Type>> combinations(List<Set<Type>> types)
    {
        List<List<Type>> combinations = List.of(List.of());
        for (Set<Type> type : types)
        {
            List<List<Type>> added = new ArrayList<>();
            for (Type type1 : type)
            {
                for (List<Type> combination : combinations)
                {
                    List<Type> copy = new ArrayList<>(combination);
                    copy.add(type1);
                    added.add(copy);
                }
            }
            combinations = added;
        }
        return combinations;
    }
}
