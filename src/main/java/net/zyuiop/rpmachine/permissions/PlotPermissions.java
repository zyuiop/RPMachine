package net.zyuiop.rpmachine.permissions;


/**
 * @author Louis Vialar
 */
public enum PlotPermissions implements DelegatedPermission {
    ADD_NEW_MEMBER("ajouter un membre à la parcelle"),
    REMOVE_MEMBER("supprimer un membre de la parcelle"),
    BUILD_ON_PLOTS("construire sur la parcelle"),
    LEAVE_PLOT("quitter la parcelle"),
    SELL_PLOT("vendre la parcelle");

    private final String description;

    PlotPermissions(String description) {
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }
}
