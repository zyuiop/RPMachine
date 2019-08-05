package net.zyuiop.rpmachine.cities;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.CityFloor;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.database.filestorage.FileEntityStore;
import net.zyuiop.rpmachine.entities.LegalEntityRepository;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CitiesManager extends FileEntityStore<City> implements LegalEntityRepository<City> {

    private final RPMachine rpMachine;
    private ConcurrentHashMap<String, City> cities = new ConcurrentHashMap<>();
    private TreeSet<CityFloor> floors = new TreeSet<>((floor1, floor2) -> Integer.compare(floor2.getInhabitants(), floor1.getInhabitants()));
    private HashSet<UUID> bypass = new HashSet<>();
    private CreationPriceFunction f;

    public CitiesManager(RPMachine plugin) {
        super(City.class, "cities");

        this.rpMachine = plugin;
        super.load(); // Load all the cities

        Configuration conf = rpMachine.getConfig();

        // Load floors
        RPMachine.getInstance().getLogger().info("Loading floors...");
        for (Map<?, ?> floor : conf.getMapList("floors")) {
            String name = (String) floor.get("name");
            int inhabitants = (Integer) floor.get("inhabitants");
            int maxsurface = (Integer) floor.get("max-chunks");
            int maxtaxes = (Integer) floor.get("max-taxes");
            int chunkPrice = (Integer) floor.get("chunk-price");
            int tpTax = (Integer) ((Optional) Optional.ofNullable(floor.get("max-tp-tax"))).orElse(1);

            floors.add(new CityFloor(name, inhabitants, maxsurface, maxtaxes, tpTax, chunkPrice));
            plugin.getLogger().info("Loaded CityFloor " + name);
        }


        ConfigurationSection section = conf.getConfigurationSection("createcity");
        f = CreationPriceFunctions.valueOf(
                section.getString("function", "LINEAR")
        ).function(section);

        rpMachine.getLogger().info("City creation parameters test:");
        for (int i = 0; i < 20; ++i) {
            rpMachine.getLogger().info((i + 1) + "th city will cost " + f.roundedPrice(i));
        }
    }

    public void addBypass(UUID id) {
        bypass.add(id);
    }

    public void removeBypass(UUID id) {
        bypass.remove(id);
    }

    public boolean isBypassing(UUID id) {
        return bypass.contains(id);
    }

    public void payTaxes(boolean force) {
        for (City city : cities.values()) {
            city.payTaxes(force);
        }
    }

    public City getCity(String name) {
        return cities.get(name);
    }

    public ConcurrentHashMap<String, City> getCities() {
        return cities;
    }


    public CityFloor getFloor(City city) {
        for (CityFloor floor : floors) {
            if (floor.getInhabitants() <= city.countInhabitants())
                return floor;
        }
        return null;
    }

    public TreeSet<CityFloor> getFloors() {
        return floors;
    }

    public City getPlayerCity(UUID player) {
        for (City city : cities.values()) {
            if (city.getInhabitants().contains(player))
                return city;
        }
        return null;
    }

    public City getPlayerCity(Player player) {
        return getPlayerCity(player.getUniqueId());
    }

    public City getCityHere(Location loc) {
        return getCityHere(loc.getChunk());
    }

    public City getCityHere(Chunk chunk) {
        if (!chunk.getWorld().getName().equals("world"))
            return null;

        VirtualChunk vchunk = new VirtualChunk(chunk);
        for (City city : cities.values()) {
            if (city.getChunks().contains(vchunk))
                return city;
        }
        return null;
    }

    public boolean canBuild(Player player, Location location) {
        if (bypass.contains(player.getUniqueId()))
            return true;

        if (location.getWorld().getName().equals("world")) {
            City city = getCityHere(location.getChunk());

            if (city == null) {
                return RPMachine.getInstance().getProjectsManager().canBuild(player, location);
            }
            return city.canBuild(player, location);
        } else {
            return RPMachine.getInstance().getProjectsManager().canBuild(player, location);
        }
    }

    public boolean isProtected(Location location) {
        if (location.getWorld().getName().equals("world")) {
            City city = getCityHere(location.getChunk());

            if (city == null) {
                return RPMachine.getInstance().getProjectsManager().isProtected(location);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean canInteractWithBlock(Player player, Location location) {
        if (bypass.contains(player.getUniqueId()))
            return true;

        if (location.getWorld().getName().equals("world")) {
            City city = getCityHere(location.getChunk());
            return city == null || city.canInteractWithBlock(player, location);
        } else {
            return RPMachine.getInstance().getProjectsManager().canInteractWithBlock(player, location);
        }
    }

    public double getCreationPrice() {
        return f.roundedPrice(cities.size());
    }

    public void removeCity(City city) {
        super.removeEntity(city);
        cities.remove(city.getCityName());
    }

    public boolean checkCityTeleport(Player player) {
        City current = getCityHere(player.getLocation().getChunk());
        if (current == null) {
            player.sendMessage(ChatColor.RED + "Vous devez vous trouver dans une ville pour vous téléporter !");
            return false;
        }
        return true;
    }

    @Override
    public City findEntity(String tag) {
        return getCity(tag);
    }

    @Override
    public String getTag(City entity) {
        return entity.getCityName();
    }

    @Override
    protected void loadedEntity(City entity) {
        cities.put(entity.getCityName(), entity);
    }

    /**
     * Create a city and its associated file
     *
     * @param city the city to create
     * @return true if the city could be created, false if not
     */
    public boolean createCity(City city) {
        if (cities.containsKey(city.getCityName()))
            return false;

        String fileName = city.getCityName().replace("/", "_");
        fileName = fileName.replace("\\", "_");
        return super.createEntity(fileName, city);
    }

    public void saveCity(City city) {
        super.saveEntity(city);
    }

    public void move(City city, String cityName) {
        removeCity(city);
        city.setCityName(cityName);
        createCity(city);
    }

    enum CreationPriceFunctions {
        LINEAR(c -> {
            double p0 = c.getDouble("p0");
            double coeff = c.getDouble("coeff");
            return cities -> ((int) (p0 + coeff * cities));
        }),
        EXPONENTIAL(c -> {
            double p0 = c.getDouble("p0");
            double basis = c.getDouble("basis");
            return cities -> ((int) (p0 * Math.pow(basis, cities)));
        }),
        SLOW_EXPONENTIAL(c -> {
            double p0 = c.getDouble("p0");
            double basis = c.getDouble("basis");
            return cities -> ((int) (p0 * Math.pow(basis, Math.sqrt(cities))));
        }),
        QUADRATIC(c -> {
            double p0 = c.getDouble("p0");
            double power = c.getDouble("power");
            return cities -> ((int) (p0 * Math.pow(cities + 1, power)));
        }),
        SIGMOID(c -> {
            double ampl = c.getDouble("max");
            double delta = c.getDouble("delta");
            double coeff = c.getDouble("coeff");
            return cities -> ((int) (ampl / (1 + Math.exp(-coeff * (cities - delta)))));
        });

        private final Function<ConfigurationSection, CreationPriceFunction> creator;

        CreationPriceFunctions(Function<ConfigurationSection, CreationPriceFunction> creator) {
            this.creator = creator;
        }

        public CreationPriceFunction function(ConfigurationSection s) {
            return creator.apply(s);
        }
    }

    public interface CreationPriceFunction {
        final double ROUND_NEAREST = 100D;

        double computePrice(int cities);

        default int roundedPrice(int cities) {
            double p = computePrice(cities) / ROUND_NEAREST;
            return (int) (Math.round(p) * ROUND_NEAREST);
        }
    }

}
