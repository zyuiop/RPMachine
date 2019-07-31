package net.zyuiop.rpmachine.permissions;


/**
 * @author Louis Vialar
 */
public enum EconomyPermissions implements DelegatedPermission {
    PAY_MONEY_TO_PLAYER("payer de l'argent à un joueur"),
    PAY_MONEY_TO_CITY("payer de l'argent à une ville (don)"),
    PAY_LATE_TAXES("payer les impots en retard");

    private final String description;

    EconomyPermissions(String description) {
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }
}
