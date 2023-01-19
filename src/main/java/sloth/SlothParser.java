package sloth;

import sloth.checking.CheckingContext;
import sloth.checking.PrecedenceGraph;
import sloth.match.Match;
import sloth.match.MatchingContext;
import sloth.model.Interpretation;
import sloth.model.Part;
import sloth.model.Segment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.function.Predicate.not;

public class SlothParser
{
    private SlothParser()
    {
    }

    public static List<Interpretation> parse(String text, MatchingContext context, PrecedenceGraph graph)
    {
        Provider<String> provider = new Provider<>(Lexer.lex(text));
        System.out.println(provider.rest());
        List<Part> parse = SyntaxMatcher.parse(context, provider);
        List<Part> cleaned = removeEmptyMatches(parse);
        System.out.println(cleaned.size() + " SYNTACTIC VALUES FOUND!");
//        for (Part part : cleaned)
//        {
//            System.out.println("====");
//            for (Segment segment : part.segments())
//            {
//                System.out.println("\t===");
//                for (Match match : segment.matches())
//                {
//                    System.out.println("\t\t" + match);
//                }
//            }
//        }
        List<Interpretation> interpretations = processCombinations(createCombinations(cleaned), graph);
        printInterpretations(interpretations);
        return interpretations;
    }

    private static List<List<Match>> createCombinations(List<Part> matches)
    {
        List<List<Match>> combinations = new ArrayList<>();
        combinations.add(List.of());
        for (Part match : matches)
        {
            List<List<Match>> iterated = new ArrayList<>();
            for (Segment segment : match.segments())
            {
                for (List<Match> combination : combinations)
                {
                    List<Match> seg = new ArrayList<>();
                    seg.addAll(combination);
                    seg.addAll(segment.matches());
                    iterated.add(seg);
                }
            }
            combinations = iterated;
        }
        return combinations;
    }

    private static List<Interpretation> processCombinations(List<List<Match>> matches, PrecedenceGraph graph)
    {
        List<Interpretation> interpretations = new ArrayList<>();
        Map<String, Integer> results = new HashMap<>();
        for (List<Match> match : matches)
        {
            CheckingContext context = new CheckingContext(graph);
            try
            {
                for (Match match1 : match)
                {
                    match1.checkPrecedence(context);
                }
                results.put("Valid!", results.getOrDefault("Valid!", 0) + 1);
                interpretations.add(new Interpretation(match, context));
            }
            catch (Exception e)
            {
                results.put(e.getMessage(), results.getOrDefault(e.getMessage(), 0) + 1);
            }
        }
        System.out.println("Results:");
        results.forEach((a, b) -> System.out.println("\t" + b + " " + a));
        return interpretations;
    }

    private static List<Part> removeEmptyMatches(List<Part> parts)
    {
        List<Part> cleaned = new ArrayList<>();
        for (Part part : parts)
        {
            List<Segment> cls = new ArrayList<>();
            for (Segment segment : part.segments())
            {
                cls.add(new Segment(segment
                        .matches()
                        .stream()
                        .filter(not(Match::empty))
                        .toList()));
            }
            cleaned.add(new Part(cls));
        }
        return cleaned;
    }

    private static void printInterpretations(List<Interpretation> interpretations)
    {
        System.out.println(interpretations.size() + " interpretations found!");
        for (Interpretation interpretation : interpretations)
        {
            for (Match match : interpretation.matches())
            {
                System.out.println(match);
            }
            System.out.println();
        }
    }
}
