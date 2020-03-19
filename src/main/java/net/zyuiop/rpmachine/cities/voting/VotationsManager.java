package net.zyuiop.rpmachine.cities.voting;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.database.filestorage.FileEntityStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VotationsManager extends FileEntityStore<Votation> {
    private int currentVotationId = 1;
    private Map<Integer, Votation> currentVotations = new HashMap<>();
    private Map<Integer, Votation> pastVotations = new HashMap<>();

    public VotationsManager() {
        super(Votation.class, "votations");

        super.load();

        Bukkit.getScheduler().runTaskTimerAsynchronously(RPMachine.getInstance(), this::checkExpiration, 100L, 100L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(RPMachine.getInstance(), () ->
                RPMachine.getInstance().getCitiesManager().getCities().forEach(this::broadcastVotations), 100L, 60 * 10 * 20L);
    }

    public Votation getCurrentVotationById(int id) {
        return currentVotations.get(id);
    }

    public void createVotation(Votation v) {
        createEntity("" + (++currentVotationId), v);

        RPMachine.getInstance().getCitiesManager().getCity(v.getCityName()).getOnlineInhabitants().forEach(p -> {
            p.sendMessage(ChatColor.GOLD + "Nouvelle votation lancée dans votre ville !");
            v.print(p);
        });
    }

    public void broadcastVotations(City c) {
        var votations = getVotationsIn(c.getCityName());

        c.getOnlineInhabitants().forEach(player -> {
            var notVoted = votations.stream().filter(v -> !v.hasVoted(player.getUniqueId())).collect(Collectors.toList());

            if (notVoted.size() > 0) {
                player.sendMessage(ChatColor.RED + "Attention ! Vous devez voter pour certaines votations !");
                notVoted.forEach(v -> v.printShort(player));
            }
        });
    }

    public List<Votation> getVotationsIn(String city) {
        return currentVotations.values().stream().filter(v -> v.getCityName().equalsIgnoreCase(city)).collect(Collectors.toList());
    }

    public void checkExpiration() {
        var finished = currentVotations.entrySet().stream().filter(e -> e.getValue().isOverdue()).map(Map.Entry::getKey);
        finished.forEach(fId -> {
            var votation = currentVotations.remove(fId);
            votation.finish();
            pastVotations.put(fId, votation);
            saveEntity(votation);
        });
    }

    @Override
    protected void loadedEntity(Votation entity) {
        this.currentVotationId = Math.max(entity.getId() + 1, this.currentVotationId);
        if (entity.isClosed()) {
            this.pastVotations.put(entity.getId(), entity);
        } else {
            this.currentVotations.put(entity.getId(), entity);
        }
    }

    public void save(Votation votation) {
        super.saveEntity(votation);
    }
}
