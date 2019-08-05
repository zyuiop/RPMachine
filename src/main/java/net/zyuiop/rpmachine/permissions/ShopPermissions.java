package net.zyuiop.rpmachine.permissions;


/**
 * @author Louis Vialar
 */
public enum ShopPermissions implements DelegatedPermission {
    CREATE_SELL_SHOPS("créer des shops de vente d'item"),
    CREATE_BUY_SHOPS("créer des shops d'achat d'item"),
    CREATE_PLOT_SHOPS("créer des shops de vente de parcelles"),
    CREATE_TOLL_SHOPS("créer des péages"),
    REFILL_SHOP("remplir un shop"),
    GET_SHOP_STOCK("récupérer le stock d'un shop"),
    DESTROY_SHOP("détruire un shop"),

    // ActAs shops user
    BUY_ITEMS("acheter des items dans un shop"),
    BUY_EXPERIENCE("acheter de l'XP dans un shop"),
    BUY_ENCHANTS("acheter des enchantements dans un shop"),
    USE_TOLL("payer une entrée payante"),
    SELL_ITEMS("vendre des items dans un shop"),
    BUY_PLOTS("acheter des parcelles dans un shop");

    private final String description;

    ShopPermissions(String description) {
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }
}
