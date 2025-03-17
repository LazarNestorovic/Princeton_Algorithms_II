/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaseballElimination {
    private int teamNum;
    private HashMap<String, Integer> teams;
    private int[] win;
    private int[] loss;
    private int[] left;
    private int[][] games;
    private List<String> r;
    private int numV;
    private int numVGames = 0;
    private int sCapacity;
    // private FordFulkerson ff;

    public BaseballElimination(String filename) {
        readFile(filename);
    }

    private void readFile(String filename) {
        In in = new In(filename);
        r = new ArrayList<String>();
        teamNum = in.readInt();
        teams = new HashMap<String, Integer>();
        win = new int[teamNum];
        loss = new int[teamNum];
        left = new int[teamNum];
        games = new int[teamNum][teamNum];
        int rowCounter = 0;
        while (in.hasNextLine()) {
            teams.put(in.readString(), rowCounter);
            win[rowCounter] = in.readInt();
            loss[rowCounter] = in.readInt();
            left[rowCounter] = in.readInt();
            for (int i = 0; i < teamNum; i++) {
                games[rowCounter][i] = in.readInt();
            }
            in.readLine();
            rowCounter++;
        }
    }

    public int numberOfTeams() {
        return teamNum;
    }

    public Iterable<String> teams() {
        return teams.keySet();
    }

    public int wins(String team) {
        if (!teams.containsKey(team)) throw new IllegalArgumentException();
        return win[teams.get(team)];
    }

    public int losses(String team) {
        if (!teams.containsKey(team)) throw new IllegalArgumentException();
        return loss[teams.get(team)];
    }

    public int remaining(String team) {
        if (!teams.containsKey(team)) throw new IllegalArgumentException();
        return left[teams.get(team)];
    }

    public int against(String team1, String team2) {
        if (!teams.containsKey(team1) || !teams.containsKey(team2))
            throw new IllegalArgumentException();
        return games[teams.get(team1)][teams.get(team2)];
    }

    public boolean isEliminated(String team) {
        if (!teams.containsKey(team)) throw new IllegalArgumentException();
        if (numberOfTeams() == 1) return false;

        int teamX = wins(team) + remaining(team);
        for (String t : teams()) {
            if (t.compareTo(team) == 0) continue;
            if (teamX < wins(t)) {
                r.add(t);
                return true;
            }
        }

        FlowNetwork fn = makeFN(teams.get(team));
        FordFulkerson ff = new FordFulkerson(fn, 0, numV - 1);

        for (String s : teams()) {
            if (ff.inCut(numVGames + 1 + teams.get(s)) && s.compareTo(team) != 0) {
                r.add(s);
            }
        }

        return ff.value() != sCapacity;

    }

    private FlowNetwork makeFN(int skip) {
        numVGames = 0;
        sCapacity = 0;
        for (int i = 0; i < teamNum; i++) {
            for (int j = 1; j < teamNum; j++) {
                if (j > i && games[i][j] > 0 && i != skip && j != skip) {
                    numVGames++;
                    sCapacity += games[i][j];
                }
            }
        }
        numV = teamNum + 1 + numVGames;

        FlowNetwork flowNetwork = new FlowNetwork(numV);
        int temp = 1;
        for (int i = 0; i < teamNum; i++) {
            for (int j = 1; j < teamNum; j++) {
                if (j > i && games[i][j] > 0 && i != skip && j != skip) {
                    FlowEdge e = new FlowEdge(0, temp, games[i][j]);
                    flowNetwork.addEdge(e);
                    FlowEdge e1 = new FlowEdge(temp, numVGames + 1 + i, Double.POSITIVE_INFINITY);
                    flowNetwork.addEdge(e1);
                    FlowEdge e2 = new FlowEdge(temp, numVGames + 1 + j, Double.POSITIVE_INFINITY);
                    flowNetwork.addEdge(e2);
                    temp++;
                }
            }
        }
        int capacityT = win[skip] + left[skip];
        for (int i = 0; i < teamNum; i++) {
            if (i == skip) continue;
            FlowEdge e = new FlowEdge(numVGames + 1 + i, numV - 1, capacityT - win[i]);
            flowNetwork.addEdge(e);
        }
        return flowNetwork;
    }

    public Iterable<String> certificateOfElimination(String team) {
        if (!teams.containsKey(team)) throw new IllegalArgumentException();

        if (!r.isEmpty()) {
            List<String> temp = new ArrayList<String>(r);
            r.clear();
            return temp;
        }
        else return null;
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
