package sloth;

import sloth.match.Match;
import sloth.match.MatchingContext;
import sloth.model.Part;
import sloth.model.Segment;
import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;

public class SyntaxMatcher
{
    private SyntaxMatcher() { }
    public static List<Part> parse(MatchingContext context, Provider<String> provider)
    {
        List<List<String>> splits = split(provider);
        List<Part> matches = new ArrayList<>();
        for (List<String> split : splits)
        {
            context.cache().clear();
            matches.add(matches(context, new Provider<>(split)));
        }
        System.out.println(matches);
        return matches;
    }

    private static List<List<String>> split(Provider<String> provider)
    {
        List<List<String>> lists = new ArrayList<>();
        List<String> current = new ArrayList<>();
        lists.add(current);
        while (provider.has())
        {
            String next = provider.next();
            if(next.equals("."))
            {
                current = new ArrayList<>();
                lists.add(current);
            }
            else
            {
                current.add(next);
            }
        }
        return lists;
    }

    private static Part matches(MatchingContext context, Provider<String> provider)
    {
        List<Segment> iterations = List.of(
                new Segment(List.of(Match.getStart(0)))
        );
        boolean changed;
        do
        {
            changed = false;
            List<Segment> iterated = new ArrayList<>();
            for (Segment iteration : iterations)
            {
                Match last = iteration.matches().get(iteration.matches().size() - 1);
                provider.index(last.end());
                if(last.end() == provider.size())
                {
                    iterated.add(iteration);
                }
                else
                {
                    for (Pattern pattern : context.patterns())
                    {
                        Match start = Match.getStart(last.end());
                        List<Match> matches = pattern.tryMatch(context, provider, start);
                        for (Match match : matches)
                        {
                            List<Match> set = new ArrayList<>(iteration.matches());
                            set.add(match);
                            iterated.add(new Segment(set));
                            changed = true;
                        }
                    }
                }
            }
            iterations = iterated;
        }
        while (changed);
        return new Part(iterations);
    }
}
