package sloth;

import sloth.checking.CheckingContext;
import sloth.checking.PrecedenceGraph;
import sloth.match.Match;
import sloth.match.MatchingContext;
import sloth.model.Interpretation;
import sloth.model.Part;
import sloth.model.Segment;

import java.util.ArrayList;
import java.util.List;

import static java.util.function.Predicate.not;

public class SlothParser
{
    private SlothParser()
    {
    }

    public static List<Interpretation> parse(String text, MatchingContext context, PrecedenceGraph graph)
    {
        Provider<String> provider = new Provider<>(Lexer.lex(text));
        List<Part> parse = SyntaxMatcher.parse(context, provider);
        List<Part> cleaned = removeEmptyMatches(parse);
        System.out.println(cleaned.size() + " SYNTACTIC VALUES FOUND!");
        List<Interpretation> interpretations = processCombinations(cleaned, graph);
        printInterpretations(interpretations);
        return interpretations;
    }

    private static List<Interpretation> processCombinations(List<Part> matches, PrecedenceGraph graph)
    {
        List<Interpretation> interpretations = new ArrayList<>();
        interpretations.add(new Interpretation(List.of(), new CheckingContext(graph)));


        for (Part lists : matches)
        {
            List<Interpretation> interpreted = new ArrayList<>();

            for (Segment list : lists.segments())
            {
                for (Interpretation value : interpretations)
                {
                    CheckingContext current = value.context().clone();
                    try
                    {
                        for (Match match : list.matches())
                        {
                            match.check(current);
                        }
                        List<Match> interpretation = new ArrayList<>(value.matches());
                        interpretation.addAll(list.matches());
                        interpreted.add(new Interpretation(interpretation, current));
                    }
                    catch (Exception e)
                    {
                        System.out.println("=> " + e.getMessage());
                    }
                }
            }
            interpretations = interpreted;
        }
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
