package net.zyuiop.rpmachine.cities;

import com.google.common.collect.ImmutableSet;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.database.filestorage.FileEntityStore;
import net.zyuiop.rpmachine.entities.LegalEntityRepository;
import net.zyuiop.rpmachine.utils.ConfigFunction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;

public class CitiesManager extends FileEntityStore<City> implements LegalEntityRepository<City> {
    public static final Set<ChatColor> ALLOWED_COLORS = ImmutableSet.of(
            ChatColor.YELLOW, ChatColor.DARK_AQUA, ChatColor.AQUA, ChatColor.GREEN, ChatColor.DARK_GREEN, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE, ChatColor.DARK_GRAY, ChatColor.GRAY,
            ChatColor.BLUE, ChatColor.GOLD
    );

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
        f = new CreationPriceFunction(ConfigFunction.getFunction(section, x -> 500 + 1050 * x));

        rpMachine.getLogger().info("City creation parameters test:");
        for (int i = 0; i < 20; ++i) {
            rpMachine.getLogger().info((i + 1) + "th city will cost " + f.roundedPrice(i));
        }

        Bukkit.getScheduler().runTaskTimer(RPMachine.getInstance(), () -> cities.values().forEach(City::cleanPlots), 60 * 20, 60 * 20);
        Bukkit.getScheduler().runTaskTimer(RPMachine.getInstance(), () -> cities.values().forEach(City::sendWarnings), 60 * 20 * 5, 60 * 20 * 10);
    }

    public List<City> getSpawnCities() {
        return cities.values().stream().filter(City::isAllowSpawn).collect(Collectors.toList());
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
            city.requestTaxes(force);
        }
    }

    public City getCity(String name) {
        return cities.get(name.toLowerCase());
    }

    public Collection<City> getCities() {
        return cities.values();
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

            if (city == null) {
                return RPMachine.getInstance().getProjectsManager().canInteractWithBlock(player, location);
            }
            return city.canInteractWithBlock(player, location);
        } else {
            return RPMachine.getInstance().getProjectsManager().canInteractWithBlock(player, location);
        }
    }

    public boolean canInteractWithEntity(Player player, Location location) {
        if (bypass.contains(player.getUniqueId()))
            return true;

        if (location.getWorld().getName().equals("world")) {
            City city = getCityHere(location.getChunk());

            if (city == null) {
                return RPMachine.getInstance().getProjectsManager().canInteractWithEntity(player, location);
            }
            return city.canInteractWithBlock(player, location);
        } else {
            return RPMachine.getInstance().getProjectsManager().canInteractWithEntity(player, location);
        }
    }

    public double getCreationPrice() {
        return f.roundedPrice(cities.size());
    }

    public void removeCity(City city) {
        super.removeEntity(city);
        cities.remove(city.getCityName().toLowerCase());
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
        if (entity.getChatColor() == null) {
            // Pick random
            Random random = new Random();
            int index = random.nextInt(ALLOWED_COLORS.size());
            ChatColor color = ALLOWED_COLORS.toArray(new ChatColor[ALLOWED_COLORS.size()])[index];

            entity.setChatColor(color);
            saveCity(entity);
        }

        cities.put(entity.getCityName().toLowerCase(), entity);
    }

    /**
     * Create a city and its associated file
     *
     * @param city the city to create
     * @return true if the city could be created, false if not
     */
    public boolean createCity(City city) {
        if (cities.containsKey(city.getCityName().toLowerCase()))
            return false;

        String fileName = city.getCityName().replace("/", "_");
        fileName = fileName.replace("\\", "_");
        return super.createEntity(fileName, city);
    }

    public void saveCity(City city) {
        super.saveEntity(city);
    }

    public class CreationPriceFunction {
        final DoubleFunction<Double> baseFunc;
        final double ROUND_NEAREST = 100D;

        public CreationPriceFunction(DoubleFunction<Double> baseFunc) {
            this.baseFunc = baseFunc;
        }

        int roundedPrice(int cities) {
            double p = baseFunc.apply(cities) / ROUND_NEAREST;
            return (int) (Math.round(p) * ROUND_NEAREST);
        }
    }

}
