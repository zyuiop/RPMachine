package net.zyuiop.rpmachine.permissions;


/**
 * @author Louis Vialar
 */
public enum ShopPermissions implements DelegatedPermission {
    CREATE_SELL_SHOPS("créer des shops de vente d'item"),
    CREATE_BUY_SHOPS("créer des shops d'achat d'item"),
    CREATE_PLOT_SHOPS("créer des shops de vente de parcelles"),
    REFILL_SHOP("remplir un shop"),
    GET_SHOP_STOCK("récupérer le stock d'un shop"),
    DESTROY_ITEM_SHOP("détruire un shop d'items"),
    DESTROY_PLOT_SHOP("détruire un shop de parcelles"),

    // ActAs shops user
    BUY_ITEMS("acheter des items dans un shop"),
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
