package com.armorposer.states;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Nation {
    private final String id;
    private String displayName;
    private UUID owner;
    private final Set<UUID> coOwners = new HashSet<>();
    private final Set<UUID> members = new HashSet<>();

    public Nation(String id, String displayName, UUID owner) {
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Set<UUID> getCoOwners() {
        return coOwners;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean contains(UUID uuid) {
        return owner.equals(uuid) || coOwners.contains(uuid) || members.contains(uuid);
    }
}
