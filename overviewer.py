global json
global os
global basePath

import json as json
import os as os

basePath = "/home/zyuiop/RPServer/"

worlds["Overworld"] = basePath + "world"
worlds["Nether"] = basePath + "world_nether"

outputdir = basePath + "overviewer"


def readCities():
    ret = []
    for file in os.listdir(basePath + "plugins/RPMachine/cities/"):
        if file.endswith(".json"):
            with open(basePath + "plugins/RPMachine/cities/" + file, "r") as f:
                js = json.load(f)
                cname = js["cityName"]

                if "spawn" in js:
                    ret.append({
                        'id': 'City',
                        'x': js["spawn"]["x"],
                        'y': js["spawn"]["y"],
                        'z': js["spawn"]["z"],
                        'name': cname
                    })
                else:
                    chunk = js["chunks"][0]

                    x = chunk["x"] * 16 + 8
                    z = chunk["z"] * 16 + 8
                    y = 64

                    ret.append({
                        'id': 'City',
                        'x': x,
                        'y': y,
                        'z': z,
                        'name': cname
                    })

                # Read plots :D
                for name, plot in js["plots"].items():
                    owned = "owner" in plot
                    area = plot["area"]
                    id = None
                    if owned: id = "OwnedPlots"
                    else: id = "FreePlots"


                    x = (area["minX"] + area["maxX"]) / 2
                    y = (area["minY"] + area["maxY"]) / 2
                    z = (area["minZ"] + area["maxZ"]) / 2
                    ret.append({
                        'id': id,
                        'x': x,
                        'y': y,
                        'z': z,
                        'min': str(area["minX"]) + " " + str(area["minY"]) + " " + str(area["minZ"]),
                        'max': str(area["maxX"]) + " " + str(area["maxY"]) + " " + str(area["maxZ"]),
                        'area': str((area["maxX"] - area["minX"] + 1) * (area["maxZ"] - area["minZ"] + 1)) + " sq",
                        'volume': str((area["maxX"] - area["minX"] + 1) * (area["maxZ"] - area["minZ"] + 1) * (area["maxY"] - area["minY"] + 1)) + " bl",
                        'city': cname,
                        'plot': name
                    })

    return ret


def readPortals():
    ret = []
    for file in os.listdir(basePath + "plugins/RPMachine/portals/"):
        if file.endswith(".json"):
            with open(basePath + "plugins/RPMachine/portals/" + file, "r") as f:
                js = json.load(f)
                portalArea = js["portalArea"]

                x = (portalArea["minX"] + portalArea["maxX"]) / 2
                y = (portalArea["minY"] + portalArea["maxY"]) / 2
                z = (portalArea["minZ"] + portalArea["maxZ"]) / 2

                ret.append({
                        'id': 'Portal',
                        'x': x,
                        'y': y,
                        'z': z,
                        'name': "Portail vers " + js["targetWorld"]
                })
    return ret


def readPlotShops():
    ret = []
    for file in os.listdir(basePath + "plugins/RPMachine/shops/"):
        if file.endswith(".json"):
            with open(basePath + "plugins/RPMachine/shops/" + file, "r") as f:
                js = json.load(f)
                if js["signClass"] == "net.zyuiop.rpmachine.shops.types.PlotSign":
                    ret.append({
                        'id': 'PlotShop',
                        'x': js["location"]["x"],
                        'y': js["location"]["y"],
                        'z': js["location"]["z"],
                        'plot': js["plotName"],
                        'city': js["cityName"],
                        'price': js["price"]
                    })
    return ret


def citySpawnFilter(poi):
    if poi['id'] == 'City':
        return poi['name']

def portalFilter(poi):
    if poi['id'] == 'Portal':
        return poi['name']

def plotShopFilter(poi):
    if poi['id'] == 'PlotShop':
        return "<b>Parceille:</b> " + poi['plot'] + '<br>' + "<b>Ville:</b> " + poi['city'] + '<br>' + "<b>Prix:</b> " + str(poi['price'])

def ownedPlotsFilter(poi):
    if poi['id'] == 'OwnedPlots':
        return "<b>Parceille:</b> " + poi['plot'] + \
               '<br>' + "<b>Ville:</b> " + poi['city'] + \
               '<br>' + "<b>Min:</b> " + poi['min'] + \
               '<br>' + "<b>Max:</b> " + poi['max'] + \
               '<br>' + "<b>Surface:</b> " + poi['area'] + \
               '<br>' + "<b>Volume:</b> " + poi['volume']

def freePlotsFilter(poi):
    if poi['id'] == 'FreePlots':
        return "<b>Parceille:</b> " + poi['plot'] + \
               '<br>' + "<b>Ville:</b> " + poi['city'] + \
               '<br>' + "<b>Min:</b> " + poi['min'] + \
               '<br>' + "<b>Max:</b> " + poi['max'] + \
               '<br>' + "<b>Surface:</b> " + poi['area'] + \
               '<br>' + "<b>Volume:</b> " + poi['volume']

renders['render'] = {
    'world':'Overworld',
    'title':'Map principale',
    'manualpois': readCities() + readPortals() + readPlotShops(),
    'markers': [
        dict(name="Villes", filterFunction=citySpawnFilter, checked=True, icon="marker_town.png"),
        dict(name="Parcelles occupées", filterFunction=ownedPlotsFilter, icon="marker_factory_red.png"),
        dict(name="Parcelles libres", filterFunction=freePlotsFilter, icon="marker_factory.png"),
        dict(name="Ventes de parcelles", filterFunction=plotShopFilter),
        dict(name="Portails", filterFunction=portalFilter, icon="marker_mine.png"),
    ],
    'showspawn': False,
    'showlocationmarker': False,
    'defaultzoom': 4
}

renders['nether'] = {
    'world':'Nether',
    'title':'Toutes couches',
    'rendermode': 'nether',
    'showspawn': False,
    'showlocationmarker': False,
    'defaultzoom': 2,
    'forcerender': True
}
renders['nether_64'] = {
    'world':'Nether',
    'title':'Couche 64',
    'rendermode': [Base(), EdgeLines(), Nether(), Depth(min=0, max=64)],
    'showspawn': False,
    'showlocationmarker': False
}
renders['nether_80'] = {
    'world':'Nether',
    'title':'Couche 80',
    'rendermode': [Base(), EdgeLines(), Nether(), Depth(min=0, max=80)],
    'showspawn': False,
    'showlocationmarker': False
}
renders['nether_96'] = {
    'world':'Nether',
    'title':'Couche 96',
    'rendermode': [Base(), EdgeLines(), Nether(), Depth(min=0, max=96)],
    'showspawn': False,
    'showlocationmarker': False,
    'defaultzoom': 2
}
renders['nether_112'] = {
    'world':'Nether',
    'title':'Couche 112',
    'rendermode': [Base(), EdgeLines(), Nether(), Depth(min=0, max=112)],
    'showspawn': False,
    'showlocationmarker': False
}
renders['nether_cave'] = {
    'world':'Nether',
    'title':'Cavités',
    'rendermode': [Base(), EdgeLines(), Cave(), Nether()],
    'showspawn': False,
    'showlocationmarker': False
}




texturepath = basePath + "textures.zip"

