package net.zyuiop.rpmachine.permissions;


/**
 * @author Louis Vialar
 */
public enum ProjectPermissions implements DelegatedPermission {
    ADD_NEW_MEMBER("ajouter un membre au projet"),
    REMOVE_MEMBER("supprimer un membre du projet"),
    ACT_AS_PROJECT("agir en tant que projet"),
    BUILD_ON_PROJECT("construire/intéragir sur le projet"),
    INTERACT_ON_PROJECT("intéragir sur le projet"),
    LEAVE_PLOT("quitter le projet");

    private final String description;

    ProjectPermissions(String description) {
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }
}
