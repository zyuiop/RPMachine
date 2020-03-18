package net.zyuiop.rpmachine.cities.voting;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.StoredEntity;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Votation implements StoredEntity {
    public static final String YES = "oui";
    public static final String NO = "non";

    private int id;
    private String cityName;
    private String question;
    private Map<String, String> answers;
    private long endTime;
    private Map<UUID, String> recordedVotes = new HashMap<>();
    private VotationFinishHook onFinish;
    private boolean closed = false;

    public Votation() {
    }

    public Votation(String cityName, String question, Map<String, String> answers, long endTime, VotationFinishHook onFinish) {
        this.cityName = cityName;
        this.question = question;
        this.answers = answers;
        this.endTime = endTime;
        this.onFinish = onFinish;
    }

    public static Votation yesNoVotation(String cityName, String question, long endTime, VotationFinishHook onFinish) {
        return new Votation(cityName, question, Map.of(YES, "Oui", NO, "Non"), endTime, onFinish);
    }

    public boolean vote(Player voter, String vote) {
        if (closed) {
            voter.sendMessage(ChatColor.RED + "Erreur: cette votation est terminée.");
            return false;
        }

        if (!hasVoteRight(voter.getUniqueId())) {
            voter.sendMessage(ChatColor.RED + "Vous n'êtes pas citoyen de cette ville et ne pouvez donc pas voter pour cette élection.");
        }

        if (!answers.containsKey(vote)) {
            voter.sendMessage(ChatColor.RED + "Erreur: l'option choisie n'est pas valide.");
            return false;
        }

        if (recordedVotes.containsKey(voter.getUniqueId())) {
            voter.sendMessage(ChatColor.RED + "Erreur: vous avez déjà voté pour cette votation.");
            return false;
        }

        voter.sendMessage(ChatColor.GREEN + "Votre vote a bien été comptabilisé.");
        recordedVotes.put(voter.getUniqueId(), vote);
        return true;
    }

    public boolean hasVoteRight(UUID player) {
        var city = RPMachine.getInstance().getCitiesManager().getPlayerCity(player);

        return city != null && city.getCityName().equals(this.cityName);
    }

    public void print(Player target) {
        target.sendMessage(ChatColor.GOLD + "*** Votation ***");
        target.sendMessage(ChatColor.GRAY + question);
        target.sendMessage(ChatColor.YELLOW + "* Réponses possibles *");
        answers.forEach((k, v) -> {
            var tc = new TextComponent(" - " + v);
            tc.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote " + id + " " + k));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GREEN + "Cliquez ici pour voter " + ChatColor.YELLOW + v)));
            target.sendMessage(tc);
        });
    }

    public void printShort(Player target) {
        var tc = new TextComponent(" - " + getQuestion());
        tc.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote " + getId()));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GREEN + "Cliquez ici pour afficher les votes possibles")));
        target.sendMessage(tc);
    }

    public void finish() {
        if (this.closed) {
            return;
        }
        this.closed = true;


        var voteNumbers = recordedVotes.values().stream().collect(Collectors.groupingBy(s -> s));
        var maxVotes = 0;
        var maxOption = "";
        var totalVotes = 0;
        for (var entry : voteNumbers.entrySet()) {
            totalVotes += entry.getValue().size();
            if (entry.getValue().size() > maxVotes) {
                maxVotes = entry.getValue().size();
                maxOption = entry.getKey();
            }
        }

        int finalTotalVotes = totalVotes;
        recordedVotes.keySet().forEach(voter -> {
            Messages.sendMessage(RPMachine.getInstance().getDatabaseManager().getPlayerData(voter), ChatColor.YELLOW + "Votation " + ChatColor.WHITE + question + ChatColor.YELLOW + " terminée !");
            voteNumbers.forEach((k, v) ->
                    Messages.sendMessage(RPMachine.getInstance().getDatabaseManager().getPlayerData(voter),
                            " - " + answers.get(k) + " : " + String.format("%.2f", (v.size() * 100D) / finalTotalVotes) + " %")
            );
        });

        this.onFinish.onFinish(RPMachine.getInstance().getCitiesManager().getCity(cityName), maxOption);
    }

    private boolean hasMajority(int numberOfVoters) {
        var voteNumbers = recordedVotes.values().stream().collect(Collectors.groupingBy(s -> s));

        return voteNumbers.values().stream().anyMatch(lst -> lst.size() > numberOfVoters/2D);
    }

    public boolean isOverdue() {
        return System.currentTimeMillis() > endTime || hasMajority(RPMachine.getInstance().getCitiesManager().getCity(cityName).getInhabitants().size());
    }

    public boolean isClosed() {
        return this.closed;
    }

    public int getId() {
        return id;
    }

    public String getCityName() {
        return cityName;
    }

    public String getQuestion() {
        return question;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public long getEndTime() {
        return endTime;
    }

    public Map<UUID, String> getRecordedVotes() {
        return recordedVotes;
    }

    public VotationFinishHook getOnFinish() {
        return onFinish;
    }

    @Override
    public String getFileName() {
        return "" + id + ".json";
    }

    @Override
    public void setFileName(String name) {
        this.id = Integer.parseInt(name.replaceAll("[^0-9]", ""));
    }

    public boolean hasVoted(UUID uniqueId) {
        return recordedVotes.containsKey(uniqueId);
    }
}
