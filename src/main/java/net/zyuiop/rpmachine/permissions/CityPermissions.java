package net.zyuiop.rpmachine.permissions;


/**
 * @author Louis Vialar
 */
public enum CityPermissions implements DirectPermission {
    // Plots
    CREATE_PLOT("créer des parcelles"),
    REDEFINE_EMPTY_PLOT("redéfinir des parcelles inhabitées"),
    REDEFINE_OCCUPIED_PLOT("redéfinir des parcelles habitées"),
    DELETE_EMPTY_PLOT("supprimer des parcelles inhabitées"),
    DELETE_OCCUPIED_PLOT("supprimer des parcelles habitées"),
    CHANGE_PLOT_MEMBERS("changer les membres d'une parcelle"),

    BUILD_IN_CITY("construire dans toute la ville hors des parcelles"),
    BUILD_IN_PLOTS("construire dans les parcelles des habitants"),
    INTERACT_IN_PLOTS("interragir dans les parcelles des habitants"),

    // Territory
    EXPAND_CITY("agrandir la ville"),

    // Members
    INVITE_MEMBER("inviter un joueur dans la ville"),
    KICK_MEMBER("exclure un joueur de la ville"),

    // Management
    SET_TAXES("changer les impots de la ville"),
    SET_PRIVACY("changer le type de ville (publique/privée)"),
    SET_SPAWN("changer le spawn de la ville"),
    SET_CHAT_COLOR("changer la couleur dans le chat"),
    CHECK_TAXES("vérifier les impots en retard"),

    // Council
    ADD_COUNCIL("ajouter un nouveau conseiller à la ville"),
    CHANGE_COUNCIL_PERMS("changer les permissions des conseillers de la ville"),
    REMOVE_COUNCIL("supprimer un conseiller de la ville");

    private final String description;

    CityPermissions(String description) {
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }
}
