package net.zyuiop.rpmachine.cities;

import net.zyuiop.rpmachine.cities.data.CityFloor;

import java.util.Comparator;

public class CitiesComparator implements Comparator<CityFloor> {
	@Override
	public int compare(CityFloor floor1, CityFloor floor2) {
		// On veut trier du plus grand au plus petit
		return floor2.getInhabitants() - floor1.getInhabitants() ;
	}
}
