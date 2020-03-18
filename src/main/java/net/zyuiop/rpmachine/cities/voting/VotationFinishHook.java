package net.zyuiop.rpmachine.cities.voting;

import net.zyuiop.rpmachine.cities.City;

public abstract class VotationFinishHook {
    protected abstract void onFinish(City city, String result);
}
