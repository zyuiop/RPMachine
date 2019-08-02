package net.zyuiop.rpmachine.cities.data;

public class CityFloor {
	private String name;
	private int inhabitants;
	private int maxsurface;
	private int maxtaxes;
	private int maxTpTax = 1;
	private int maxJoinTax = 0;
	private double maxPlotSellTax = 0.2;
	private int chunkPrice;

	public CityFloor() {

	}

	public CityFloor(String name, int inhabitants, int maxsurface, int maxtaxes, int maxTpTax, int maxJoinTax, double maxPlotSellTax, int chunkPrice) {
		this.name = name;
		this.inhabitants = inhabitants;
		this.maxsurface = maxsurface;
		this.maxtaxes = maxtaxes;
		this.maxTpTax = maxTpTax;
		this.maxJoinTax = maxJoinTax;
		this.maxPlotSellTax = maxPlotSellTax;
		this.chunkPrice = chunkPrice;
	}

	public int getChunkPrice() {
		return chunkPrice;
	}

	public void setChunkPrice(int chunkPrice) {
		this.chunkPrice = chunkPrice;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getInhabitants() {
		return inhabitants;
	}

	public void setInhabitants(int inhabitants) {
		this.inhabitants = inhabitants;
	}

	public int getMaxsurface() {
		return maxsurface;
	}

	public void setMaxsurface(int maxsurface) {
		this.maxsurface = maxsurface;
	}

	public int getMaxtaxes() {
		return maxtaxes;
	}

	public void setMaxtaxes(int maxtaxes) {
		this.maxtaxes = maxtaxes;
	}

	public int getMaxTpTax() {
		return maxTpTax;
	}

	public int getMaxJoinTax() {
		return maxJoinTax;
	}

	public double getMaxPlotSellTax() {
		return maxPlotSellTax;
	}
}
