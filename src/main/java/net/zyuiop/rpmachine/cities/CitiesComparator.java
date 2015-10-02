package net.zyuiop.rpmachine.cities;

import net.zyuiop.rpmachine.cities.data.CityFloor;

import java.util.Comparator;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class CitiesComparator implements Comparator<CityFloor> {
	@Override
	public int compare(CityFloor floor1, CityFloor floor2) {
		// On veut trier du plus grand au plus petit
		return floor2.getInhabitants() - floor1.getInhabitants() ;
	}
}
