package sloth;

import sloth.checking.CheckingContext;
import sloth.checking.PrecedenceGraph;
import sloth.match.Match;
import sloth.match.MatchingContext;

import java.util.ArrayList;
import java.util.List;

import static java.util.function.Predicate.not;

public class SlothParser
{
    private SlothParser()
    {
    }

    public static List<List<Match>> parse(String text, MatchingContext context, PrecedenceGraph graph)
    {
        Provider<String> provider = new Provider<>(Lexer.lex(text));
        List<List<List<Match>>> parse = SyntaxMatcher.parse(context, provider);
        List<List<List<Match>>> cleaned = removeEmptyMatches(parse);
        System.out.println(cleaned.size() + " SYNTACTIC VALUES FOUND!");
        List<List<Match>> interpretations = processCombinations(cleaned, graph);
        printInterpretations(interpretations);
        return interpretations;
    }

    private static List<List<Match>> processCombinations(List<List<List<Match>>> matches, PrecedenceGraph graph)
    {
        List<List<Match>> interpretations = new ArrayList<>();
        List<CheckingContext> contexts = new ArrayList<>();
        interpretations.add(List.of());
        contexts.add(new CheckingContext(graph));


        for (List<List<Match>> lists : matches)
        {
            List<List<Match>> interpreted = new ArrayList<>();
            List<CheckingContext> cont = new ArrayList<>();

            for (List<Match> list : lists)
            {
                for (int i = 0; i < interpretations.size(); i++)
                {
                    CheckingContext current = contexts.get(i).clone();
                    try
                    {
                        for (Match match : list)
                        {
                            match.check(current);
                        }
                        List<Match> interpretation = new ArrayList<>(interpretations.get(i));
                        interpretation.addAll(list);
                        interpreted.add(interpretation);
                        cont.add(current);
                    }
                    catch (Exception e)
                    {
                        System.out.println("=> " + e.getMessage());
                    }
                }
            }
            interpretations = interpreted;
            contexts = cont;
        }
        return interpretations;
    }
    private static List<List<List<Match>>> removeEmptyMatches(List<List<List<Match>>> pa)
    {
        List<List<List<Match>>> cleaned = new ArrayList<>();
        for (List<List<Match>> lists : pa)
        {
            List<List<Match>> cls = new ArrayList<>();
            for (List<Match> matches : lists)
            {
                cls.add(matches.stream()
                        .filter(not(Match::empty))
                        .toList());
            }
            cleaned.add(cls);
        }
        return cleaned;
    }

    private static void printInterpretations(List<List<Match>> interpretations)
    {
        System.out.println(interpretations.size() + " interpretations found!");
        for (List<Match> interpretation : interpretations)
        {
            for (Match match : interpretation)
            {
                System.out.println(match);
            }
            System.out.println();
        }
    }
}
