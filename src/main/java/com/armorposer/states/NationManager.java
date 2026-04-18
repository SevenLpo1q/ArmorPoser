package com.armorposer.states;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class NationManager {
    private final JavaPlugin plugin;
    private final Map<String, Nation> nations = new HashMap<>();
    private final Map<UUID, String> playerNation = new HashMap<>();
    private final Map<UUID, String> pendingInvites = new HashMap<>();
    private final Set<UUID> nationChatMode = new HashSet<>();

    public NationManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Optional<Nation> getNation(String nationId) {
        return Optional.ofNullable(nations.get(nationId.toLowerCase()));
    }

    public Optional<Nation> getNationOf(UUID playerId) {
        String nationId = playerNation.get(playerId);
        if (nationId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(nations.get(nationId));
    }

    public boolean isInAnyNation(UUID playerId) {
        return playerNation.containsKey(playerId);
    }

    public boolean createNation(String nationId, String displayName, UUID ownerId) {
        String id = nationId.toLowerCase();
        if (nations.containsKey(id) || isInAnyNation(ownerId)) {
            return false;
        }

        Nation nation = new Nation(id, displayName, ownerId);
        nations.put(id, nation);
        playerNation.put(ownerId, id);
        return true;
    }

    public void invite(UUID targetId, String nationId) {
        pendingInvites.put(targetId, nationId.toLowerCase());
    }

    public Optional<String> getInvite(UUID targetId) {
        return Optional.ofNullable(pendingInvites.get(targetId));
    }

    public void clearInvite(UUID targetId) {
        pendingInvites.remove(targetId);
    }

    public boolean joinInvitedNation(UUID playerId) {
        if (isInAnyNation(playerId)) {
            return false;
        }
        String nationId = pendingInvites.get(playerId);
        if (nationId == null) {
            return false;
        }

        Nation nation = nations.get(nationId);
        if (nation == null) {
            pendingInvites.remove(playerId);
            return false;
        }

        nation.getMembers().add(playerId);
        playerNation.put(playerId, nationId);
        pendingInvites.remove(playerId);
        return true;
    }

    public boolean kickMember(String nationId, UUID target) {
        Nation nation = nations.get(nationId.toLowerCase());
        if (nation == null) {
            return false;
        }

        if (nation.getOwner().equals(target)) {
            return false;
        }

        boolean removed = nation.getCoOwners().remove(target) || nation.getMembers().remove(target);
        if (removed) {
            playerNation.remove(target);
            nationChatMode.remove(target);
        }
        return removed;
    }

    public boolean leaveNation(UUID playerId) {
        Nation nation = getNationOf(playerId).orElse(null);
        if (nation == null || nation.getOwner().equals(playerId)) {
            return false;
        }

        boolean removed = nation.getCoOwners().remove(playerId) || nation.getMembers().remove(playerId);
        if (removed) {
            playerNation.remove(playerId);
            nationChatMode.remove(playerId);
        }
        return removed;
    }

    public boolean appointCoOwner(UUID ownerId, UUID targetId) {
        Nation nation = getNationOf(ownerId).orElse(null);
        if (nation == null || !nation.getOwner().equals(ownerId)) {
            return false;
        }

        if (!nation.contains(targetId) || nation.getOwner().equals(targetId)) {
            return false;
        }

        nation.getMembers().remove(targetId);
        nation.getCoOwners().add(targetId);
        return true;
    }


    public boolean demoteCoOwner(UUID ownerId, UUID targetId) {
        Nation nation = getNationOf(ownerId).orElse(null);
        if (nation == null || !nation.getOwner().equals(ownerId)) {
            return false;
        }
        if (!nation.getCoOwners().contains(targetId)) {
            return false;
        }

        nation.getCoOwners().remove(targetId);
        nation.getMembers().add(targetId);
        return true;
    }

    public boolean transferLeadership(UUID ownerId, UUID newOwner) {
        Nation nation = getNationOf(ownerId).orElse(null);
        if (nation == null || !nation.getOwner().equals(ownerId)) {
            return false;
        }

        if (!nation.contains(newOwner) || ownerId.equals(newOwner)) {
            return false;
        }

        nation.getCoOwners().remove(newOwner);
        nation.getMembers().remove(newOwner);

        UUID oldOwner = nation.getOwner();
        nation.setOwner(newOwner);
        nation.getCoOwners().add(oldOwner);
        return true;
    }

    public boolean disband(UUID ownerId) {
        Nation nation = getNationOf(ownerId).orElse(null);
        if (nation == null || !nation.getOwner().equals(ownerId)) {
            return false;
        }

        Set<UUID> all = new HashSet<>();
        all.add(nation.getOwner());
        all.addAll(nation.getCoOwners());
        all.addAll(nation.getMembers());

        all.forEach(playerNation::remove);
        all.forEach(nationChatMode::remove);
        pendingInvites.entrySet().removeIf(entry -> entry.getValue().equals(nation.getId()));

        nations.remove(nation.getId());
        return true;
    }

    public boolean isOwner(UUID playerId) {
        return getNationOf(playerId).map(n -> n.getOwner().equals(playerId)).orElse(false);
    }

    public boolean isCoOwner(UUID playerId) {
        return getNationOf(playerId).map(n -> n.getCoOwners().contains(playerId)).orElse(false);
    }

    public boolean canManageMembers(UUID playerId) {
        return isOwner(playerId) || isCoOwner(playerId);
    }

    public boolean toggleNationChat(UUID playerId) {
        if (nationChatMode.contains(playerId)) {
            nationChatMode.remove(playerId);
            return false;
        }
        nationChatMode.add(playerId);
        return true;
    }

    public boolean isNationChatEnabled(UUID playerId) {
        return nationChatMode.contains(playerId);
    }

    public Collection<UUID> getAllNationMembers(String nationId) {
        Nation nation = nations.get(nationId.toLowerCase());
        if (nation == null) {
            return List.of();
        }

        Set<UUID> all = new HashSet<>();
        all.add(nation.getOwner());
        all.addAll(nation.getCoOwners());
        all.addAll(nation.getMembers());
        return all;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "nations.yml");
        if (!file.exists()) {
            return;
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection nationsSection = cfg.getConfigurationSection("nations");
        if (nationsSection != null) {
            for (String nationId : nationsSection.getKeys(false)) {
                ConfigurationSection section = nationsSection.getConfigurationSection(nationId);
                if (section == null) {
                    continue;
                }

                UUID owner = parseUuid(section.getString("owner"));
                if (owner == null) {
                    continue;
                }

                Nation nation = new Nation(nationId, section.getString("display", nationId), owner);
                nation.getCoOwners().addAll(parseUuidList(section.getStringList("coowners")));
                nation.getMembers().addAll(parseUuidList(section.getStringList("members")));
                nations.put(nation.getId(), nation);

                playerNation.put(owner, nation.getId());
                nation.getCoOwners().forEach(id -> playerNation.put(id, nation.getId()));
                nation.getMembers().forEach(id -> playerNation.put(id, nation.getId()));
            }
        }

        ConfigurationSection invites = cfg.getConfigurationSection("invites");
        if (invites != null) {
            for (String uuidRaw : invites.getKeys(false)) {
                UUID uuid = parseUuid(uuidRaw);
                if (uuid == null) {
                    continue;
                }
                String nationId = invites.getString(uuidRaw, "").toLowerCase();
                if (nations.containsKey(nationId)) {
                    pendingInvites.put(uuid, nationId);
                }
            }
        }
    }

    public void save() {
        File folder = plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            return;
        }

        File file = new File(folder, "nations.yml");
        YamlConfiguration cfg = new YamlConfiguration();

        for (Nation nation : nations.values()) {
            String base = "nations." + nation.getId();
            cfg.set(base + ".display", nation.getDisplayName());
            cfg.set(base + ".owner", nation.getOwner().toString());
            cfg.set(base + ".coowners", toStringList(nation.getCoOwners()));
            cfg.set(base + ".members", toStringList(nation.getMembers()));
        }

        pendingInvites.forEach((uuid, nationId) -> cfg.set("invites." + uuid, nationId));

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить nations.yml: " + e.getMessage());
        }
    }

    private List<UUID> parseUuidList(List<String> values) {
        return values.stream().map(this::parseUuid).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private List<String> toStringList(Collection<UUID> uuids) {
        return uuids.stream().map(UUID::toString).toList();
    }
}
