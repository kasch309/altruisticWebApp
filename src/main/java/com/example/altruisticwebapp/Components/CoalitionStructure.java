package com.example.altruisticwebapp.Components;
import com.example.altruisticwebapp.Exceptions.*;


import java.util.HashMap;
import java.util.HashSet;


public class CoalitionStructure extends HashMap<Integer, Coalition> {

    public CoalitionStructure(){
    }
    public void addCoalition(Coalition c){
        c.setKey(this.size());
        c.setName("Coalition " + c.getKey());
        this.put(this.size(), c);
    }

    public Coalition getCoalition(int key){
        return this.get(key);
    }

    public void removeCoalition(Integer key){
        for (Player p : this.get(key)){
            if (key == 0){
                this.get(1).add(p);
            }
            else{
                this.get(0).add(p);
            }
        }
        this.remove(key);
        for(int i = key+1; i < this.size()+1; i++){
            Coalition d = this.get(i);
            remove(i);
            d.setKey(i-1);
            put(i-1, d);
        }
    }

    public Coalition getPlayersCoalition(Player p) {
        for (int i = 0; i < this.size(); i++){
            Coalition c = this.get(i);
            if (c.contains(p)) return c;
        }
        return null;
    }

    public HashSet<Coalition> blockingCoalitions(Game g, LOA loa) throws Exception {
        /*
        Coalition C blocks coalition structure T if for each player from C it holds that
        coalition C is preferred to any coalition of which i is part of
        */

        HashSet<Coalition> blockers = new HashSet<>();
        HashSet<CoalitionStructure> all = g.getPlayers().generateCoalitionStructures();
        for (CoalitionStructure cs : all){
            for (int j = 0; j < cs.size(); j++){
                boolean blocks = true;
                for (int i = 0; i < g.getSize(); i++){
                    if (cs.get(j).contains(g.getPlayer(i))){
                        if (!g.getPlayer(i).prefers(cs.get(j), this.getPlayersCoalition(g.getPlayer(i)), g.getNetwork(), loa)) {
                            blocks = false;
                            break;
                        }
                    }
                }
                if (blocks) blockers.add(cs.get(j));
            }
        }
        for (Coalition wb : blockers){
            g.addEntry("Coalition '" + wb.getName() + "' blocks.");
        }
        return blockers;
    }

    public HashSet<Coalition> weaklyBlockingCoalitions(Game g, LOA loa) throws Exception {
        /*
        Coalition C weakly blocks if there is at least one player who prefers C to any coalition
        that i is part of while another player j weakly prefers C to any coalition of which j
        is part of
        */

        HashSet<Coalition> weakBlockers = new HashSet<>();
        HashSet<CoalitionStructure> all = g.getPlayers().generateCoalitionStructures();
        for (CoalitionStructure cs : all){
            for (int j = 0; j < cs.size(); j++){
                boolean blocks = false;
                for (int i = 0; i < g.getSize(); i++){
                    if (cs.get(j).contains(g.getPlayer(i))){
                        if (g.getPlayer(i).prefers(cs.get(j), cs.getPlayersCoalition(g.getPlayer(i)), g.getNetwork(), loa)) {
                            blocks = true;
                            for (int k = 0; k < g.getSize(); k++){
                                if (!g.getPlayer(k).weaklyPrefers(cs.get(j), this.getPlayersCoalition(g.getPlayer(k)), g.getNetwork(), loa)){
                                    blocks = false;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (blocks) weakBlockers.add(cs.get(j));
            }
        }
        for (Coalition wb : weakBlockers){
            g.addEntry("Coalition '" + wb.getName() + "' blocks weakly.");
        }
        return weakBlockers;
    }

    public boolean individuallyRational(Game g, LOA loa) throws NoPlayerSetAssignedException, NoNetworkAssignedException, InvalidLevelOfAltruismException, CoalitionIsNullException {
        for (int i = 0; i < g.getSize(); i++){
            if (!g.getPlayer(i).acceptable(getPlayersCoalition(g.getPlayer(i)), g.getNetwork(), loa)) {
                g.addEntry("The coalition structure is not individually rational.");
                return false;
            }
        }
        g.addEntry("The coalition structure is individually rational.");
        return true;
    }

    public boolean nashStable(Game g, LOA loa) throws NoPlayerSetAssignedException, NoNetworkAssignedException, InvalidLevelOfAltruismException, CoalitionIsNullException {

        // For all players i their own coalition is weakly preferred to any other, if it would contain i additionally
        Coalition empty = new Coalition();
        this.addCoalition(empty);
        for (int i = 0; i < g.getSize(); i++){
            for (int j = 0; j < this.size(); j++){
                Coalition c = this.get(j);
                Coalition d = c.duplicate();
                d.add(g.getPlayer(i));
                if (!g.getPlayer(i).weaklyPrefers(getPlayersCoalition(g.getPlayer(i)), d, g.getNetwork(), loa)){
                    this.remove(empty);
                    g.addEntry("The coalition structure is not Nash stable");
                    return false;
                }
            }
        }
        this.remove(empty);
        g.addEntry("The coalition structure is Nash stable.");
        return true;
    }

    public boolean individuallyStable(Game g, LOA loa) throws NoNetworkAssignedException, PlayerNotFoundException, NoPlayerSetAssignedException, InvalidLevelOfAltruismException, CoalitionIsNullException {

        /*
        For all players i they either weakly prefer their own coalition to any other coalition c if the other one
        contains them too or there is a player j who prefers c if it contains i too
        */
        Coalition empty = new Coalition();
        this.addCoalition(empty);
        for (int i = 0; i < g.getSize(); i++){
            Player p = g.getPlayer(i);
            for (int j = 0; j < this.size(); j++){
                Coalition c = this.get(j);
                if (this.getPlayersCoalition(p).equals(c)) continue;
                Coalition dup = c.duplicate();
                dup.add(p);
                if (!p.weaklyPrefers(c, dup, g.getNetwork(), loa)){
                    for(Player q : c){
                        if(!q.prefers(c, dup, g.getNetwork(), loa)) {
                            this.remove(empty);
                            g.addEntry("The coalition structure is not individually stable.");
                            return false;
                        }
                    }
                }
            }
        }
        g.addEntry("The coalition structure is individually stable.");
        this.remove(empty);
        return true;
    }

    public boolean contractuallyIndividuallyStable(Game g, LOA loa) throws NoPlayerSetAssignedException, PlayerNotFoundException, NoNetworkAssignedException, InvalidLevelOfAltruismException, CoalitionIsNullException {

        /*
        For all players i they either weakly prefer their own coalition to any other coalition c if the other one
        contains them too or there is a player j who prefers c if it contains i too OR there is player k, i != k, k
        is in coalition of i, k prefers coalition of i over coalition of i if it would not contain i
        */

        Coalition empty = new Coalition();
        this.addCoalition(empty);
        for (int i = 0; i < g.getSize(); i++){
            for (int j = 0; j < this.size(); j++){
                Coalition c = this.get(j);
                Coalition d = c.duplicate();
                d.add(g.getPlayer(i));
                if (!g.getPlayer(i).weaklyPrefers(getPlayersCoalition(g.getPlayer(i)), d, g.getNetwork(), loa)){
                    for (int k = 0; k < g.getSize(); k++){
                        if (!g.getPlayer(k).prefers(c, d, g.getNetwork(), loa)){
                            for (Player p : this.getPlayersCoalition(g.getPlayer(i))){
                                Coalition e = c.duplicate();
                                e.remove(g.getPlayer(i));
                                if (!p.prefers(c, d, g.getNetwork(), loa)) {
                                    this.remove(empty);
                                    g.addEntry("The coalition structure is not contractually individually stable.");
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        this.remove(empty);
        g.addEntry("The coalition structure is contractually individually stable.");
        return true;
    }

    public boolean strictlyPopular(Game g, LOA loa) throws Exception {
        int countThis = 0;
        int countCmp = 0;
        HashSet<CoalitionStructure> all = g.getPlayers().generateCoalitionStructures();
        for (int i = 0; i < g.getSize(); i++){
            for (CoalitionStructure cs : all){
                if (g.getPlayer(i).prefers(this.getPlayersCoalition(g.getPlayer(i)), cs.getPlayersCoalition(g.getPlayer(i)), g.getNetwork(), loa)) countThis++;
                else countCmp++;
            }
        }
        if (countThis > countCmp){
            g.addEntry("The coalition structure is strictly popular.");
            return true;
        }
        else{
            g.addEntry("The coalition structure is not strictly popular.");
            return false;
        }
    }

    public boolean popular(Game g, LOA loa) throws Exception {
        int countThis = 0;
        int countCmp = 0;
        HashSet<CoalitionStructure> all = g.getPlayers().generateCoalitionStructures();
        for (int i = 0; i < g.getSize(); i++) {
            for (CoalitionStructure cs : all){
                if (g.getPlayer(i).prefers(this.getPlayersCoalition(g.getPlayer(i)), cs.getPlayersCoalition(g.getPlayer(i)), g.getNetwork(), loa))
                    countThis++;
                else countCmp++;
            }
        }
        if (countThis >= countCmp){
            g.addEntry("The coalition structure is popular.");
            return true;
        }
        else{
            g.addEntry("The coalition structure is not popular.");
            return false;
        }
    }

    public boolean coreStable(Game g, LOA loa) throws Exception {
        if (blockingCoalitions(g, loa).isEmpty()){
            g.addEntry("The coalition structure is core stable.");
            return true;
        }
        else{
            g.addEntry("The coalition structure is not core stable.");
            return false;
        }
    }

    public boolean strictlyCoreStable(Game g, LOA loa) throws Exception {
        if(weaklyBlockingCoalitions(g, loa).isEmpty()){
            g.addEntry("The coalition structure is core stable.");
            return true;
        }
        else {
            g.addEntry("The coalition structure is not core stable.");
            return false;
        }
    }

    public boolean perfect(Game g, LOA loa) throws NoPlayerSetAssignedException, PlayerNotFoundException, NoNetworkAssignedException, InvalidLevelOfAltruismException, CoalitionIsNullException {

        // i prefers own coalition over any other possible one

        for (int i = 0; i < g.getSize(); i++){
            for (int j = 0; j < this.size(); j++){
                Coalition c = this.get(i);
                if (!g.getPlayer(j).weaklyPrefers(getPlayersCoalition(g.getPlayer(j)), c, g.getNetwork(), loa)) {
                    g.addEntry("The coalition structure is not perfect.");
                    return false;
                }
            }
        }
        g.addEntry("The coalition structure is perfect.");
        return true;
    }
}
