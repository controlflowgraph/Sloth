package sloth.typing;

import java.util.*;

public class Main
{
    public static void main_(String[] args)
    {
        Descriptor coll = new Descriptor(
                "Collection",
                List.of("A"),
                List.of()
        );
        Descriptor list = new Descriptor(
                "List",
                List.of("B"),
                List.of(
                        new Type(false, "Collection", List.of(
                                new Type(true, "B", List.of()))
                        )
                )
        );
        Descriptor arrList = new Descriptor(
                "ArrayList",
                List.of("C"),
                List.of(
                        new Type(false, "List", List.of(
                                new Type(true, "C", List.of()))
                        )
                )
        );
        Descriptor set = new Descriptor(
                "Set",
                List.of("D"),
                List.of(
                        new Type(false, "Collection", List.of(
                                new Type(true, "D", List.of()))
                        )
                )
        );

        InheritanceTree tree = new InheritanceTree();
        tree.add(coll);
        tree.add(list);
        tree.add(arrList);
        tree.add(set);
        Type l = new Type(false, "List", List.of(new Type(false, "int", List.of())));
        Type s = new Type(false, "Set", List.of(new Type(false, "int", List.of())));
        System.out.println(tree.getSuperType(l, s));
        System.out.println(tree.getSuperType(l, l));

        Descriptor t1 = new Descriptor(
                "T1",
                List.of(),
                List.of()
        );

        Descriptor t2 = new Descriptor(
                "T2",
                List.of(),
                List.of()
        );

        Descriptor s1 = new Descriptor(
                "S1",
                List.of(),
                List.of(
                        new Type(false, "T1", List.of()),
                        new Type(false, "T2", List.of())
                )
        );
        Descriptor s2 = new Descriptor(
                "S2",
                List.of(),
                List.of(
                        new Type(false, "T1", List.of()),
                        new Type(false, "T2", List.of())
                )
        );


        InheritanceTree t = new InheritanceTree();
        t.add(t1);
        t.add(t2);
        t.add(s1);
        t.add(s2);

        Type st1 = new Type(false, "S1", List.of());
        Type st2 = new Type(false, "S2", List.of());
        Type tt1 = new Type(false, "T1", List.of());
        Type tt2 = new Type(false, "T2", List.of());
        System.out.println(t.getSuperType(st1, st2));
        System.out.println(t.isAssignable(tt1, st2));
        System.out.println(t.isAssignable(tt1, st1));
        System.out.println(t.isAssignable(tt1, tt2));
    }

    public static void main(String[] args)
    {
        Descriptor t1 = new Descriptor(
                "T1",
                List.of(),
                List.of()
        );

        Descriptor t2 = new Descriptor(
                "T2",
                List.of(),
                List.of()
        );

        Descriptor s1 = new Descriptor(
                "S1",
                List.of(),
                List.of(
                        new Type(false, "T1", List.of()),
                        new Type(false, "T2", List.of())
                )
        );
        Descriptor s2 = new Descriptor(
                "S2",
                List.of(),
                List.of(
                        new Type(false, "T1", List.of()),
                        new Type(false, "T2", List.of())
                )
        );


        InheritanceTree t = new InheritanceTree();
        t.add(t1);
        t.add(t2);
        t.add(s1);
        t.add(s2);

        Type st1 = new Type(false, "S1", List.of());
        Type st2 = new Type(false, "S2", List.of());
        Type tt1 = new Type(false, "T1", List.of());
        Type tt2 = new Type(false, "T2", List.of());


        List<Signature> signatures = List.of(
                new Signature("something", List.of(), List.of(), new Type(false, "lol", List.of())),
                new Signature("something", List.of(), List.of(new Type(false, "S1", List.of())), new Type(false, "lol", List.of())),
                new Signature("something", List.of(), List.of(new Type(false, "T1", List.of())), new Type(false, "lol", List.of())),
                new Signature("LOOOL", List.of("P"), List.of(new Type(true, "P", List.of()), new Type(true, "P", List.of())), new Type(false, "lol", List.of()))
        );

        Signature something = Util.findSignature(t, signatures, "LOOOL", List.of(new Type(false, "S1", List.of()), new Type(false, "S1", List.of())));
        System.out.println(something);
    }
}