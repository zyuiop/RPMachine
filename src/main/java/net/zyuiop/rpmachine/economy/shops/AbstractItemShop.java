package net.zyuiop.rpmachine.economy.shops;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractItemShop extends AbstractShopSign {
	protected Material itemType;
	protected short damage;
	protected int amountPerPackage;
	protected ShopAction action;

	public AbstractItemShop(Class<? extends AbstractItemShop> clazz) {
		super(clazz);
	}

	public AbstractItemShop(Class<? extends AbstractItemShop> clazz, Location location) {
		super(clazz, location);
	}

	public Material getItemType() {
		return itemType;
	}

	public void setItemType(Material itemType) {
		this.itemType = itemType;
	}

	public int getAmountPerPackage() {
		return amountPerPackage;
	}

	public void setAmountPerPackage(int amountPerPackage) {
		this.amountPerPackage = amountPerPackage;
	}

	public ShopAction getAction() {
		return action;
	}

	public void setAction(ShopAction action) {
		this.action = action;
	}

	public short getDamage() {
		return damage;
	}

	public void setDamage(short damage) {
		this.damage = damage;
	}

	public ItemStack getNewStack() {
		return new ItemStack(itemType, amountPerPackage, damage);
	}

	public boolean isItemValid(ItemStack itemStack) {
		return itemStack != null && itemStack.getType() == itemType && itemStack.getDurability() == damage;
	}
 }
