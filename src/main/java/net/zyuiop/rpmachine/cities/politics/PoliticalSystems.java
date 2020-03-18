package net.zyuiop.rpmachine.cities.politics;

public enum PoliticalSystems {
    STATE_OF_RIGHTS(StateOfRights.INSTANCE),
    TYRANNY(Tyranny.INSTANCE);

    public final PoliticalSystem instance;

    PoliticalSystems(PoliticalSystem instance) {
        this.instance = instance;
    }
}
