package sloth.model;

import sloth.checking.CheckingContext;
import sloth.checking.PrecedenceGraph;
import sloth.match.Match;

import java.util.List;

public record Interpretation(List<Match> matches, CheckingContext context)
{
}
