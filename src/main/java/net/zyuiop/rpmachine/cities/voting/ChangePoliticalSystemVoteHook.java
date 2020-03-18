package net.zyuiop.rpmachine.cities.voting;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.cities.politics.PoliticalSystems;

public class ChangePoliticalSystemVoteHook extends VotationFinishHook {
    private PoliticalSystems targetSystem;

    public ChangePoliticalSystemVoteHook() {
    }

    public ChangePoliticalSystemVoteHook(PoliticalSystems targetSystem) {
        this.targetSystem = targetSystem;
    }

    @Override
    protected void onFinish(City city, String result) {
        if (result.equals(Votation.YES)) {
            city.setPoliticalSystem(targetSystem);
        }
    }
}
