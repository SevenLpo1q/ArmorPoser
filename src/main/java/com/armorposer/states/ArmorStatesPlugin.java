package com.armorposer.states;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ArmorStatesPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private NationManager nationManager;
    private final Map<UUID, String> disbandPasswords = new HashMap<>();

    @Override
    public void onEnable() {
        this.nationManager = new NationManager(this);
        this.nationManager.load();

        Objects.requireNonNull(getCommand("state")).setExecutor(this);
        Objects.requireNonNull(getCommand("state")).setTabCompleter(this);
        Objects.requireNonNull(getCommand("stateadmin")).setExecutor(this);
        Objects.requireNonNull(getCommand("stateadmin")).setTabCompleter(this);
        Objects.requireNonNull(getCommand("sc")).setExecutor(this);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        nationManager.save();
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Optional<Nation> nationOpt = nationManager.getNationOf(uuid);
        if (nationOpt.isEmpty()) {
            return;
        }

        Nation nation = nationOpt.get();
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component prefix = buildPrefix(source.getUniqueId(), nation);
            return prefix
                    .append(Component.text(source.getName(), NamedTextColor.WHITE))
                    .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                    .append(message);
        });
    }

    private Component buildPrefix(UUID playerId, Nation nation) {
        if (nation.getOwner().equals(playerId)) {
            return Component.text("(Глава " + nation.getDisplayName() + ") ", NamedTextColor.GOLD);
        }
        if (nation.getCoOwners().contains(playerId)) {
            return Component.text("(" + nation.getDisplayName() + " | Со-владелец) ", NamedTextColor.YELLOW);
        }
        return Component.text("(" + nation.getDisplayName() + ") ", NamedTextColor.AQUA);
    }

    private void sendNationMessage(Nation nation, String senderName, Component message) {
        Component composed = Component.text("[Чат государства] ", NamedTextColor.GREEN)
                .append(Component.text(senderName, NamedTextColor.WHITE))
                .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                .append(message);

        for (UUID memberId : nationManager.getAllNationMembers(nation.getId())) {
            Player online = Bukkit.getPlayer(memberId);
            if (online != null) {
                online.sendMessage(composed);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("stateadmin")) {
            return handleAdminCommand(sender, args);
        }
        if (command.getName().equalsIgnoreCase("sc")) {
            return handleStateChatCommand(sender, args);
        }
        return handleStateCommand(sender, args);
    }

    private boolean handleStateChatCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков.");
            return true;
        }

        Nation nation = nationManager.getNationOf(player.getUniqueId()).orElse(null);
        if (nation == null) {
            player.sendMessage("§cВы не состоите в государстве.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cИспользование: /sc <сообщение>");
            return true;
        }

        sendNationMessage(nation, player.getName(), Component.text(String.join(" ", args)));
        return true;
    }

    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("states.admin")) {
            sender.sendMessage("§cНет прав.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§e/stateadmin create <игрок> <id_гос-ва> [название]");
            sender.sendMessage("§e/stateadmin remove <id_гос-ва>");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 3) {
                sender.sendMessage("§cИспользование: /stateadmin create <игрок> <id_гос-ва> [название]");
                return true;
            }

            OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
            String nationId = args[2].toLowerCase(Locale.ROOT);
            String displayName = args.length >= 4 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : args[2];

            boolean created = nationManager.createNation(nationId, displayName, owner.getUniqueId());
            if (!created) {
                sender.sendMessage("§cНе удалось создать государство (ID занят или игрок уже в другом государстве).");
                return true;
            }

            sender.sendMessage("§aГосударство " + displayName + " создано. Владелец: " + owner.getName());
            if (owner.isOnline()) {
                Objects.requireNonNull(owner.getPlayer()).sendMessage("§aВы назначены владельцем государства " + displayName + ".");
            }
            nationManager.save();
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length != 2) {
                sender.sendMessage("§cИспользование: /stateadmin remove <id_гос-ва>");
                return true;
            }

            Optional<Nation> nation = nationManager.getNation(args[1]);
            if (nation.isEmpty()) {
                sender.sendMessage("§cГосударство не найдено.");
                return true;
            }

            boolean ok = nationManager.disband(nation.get().getOwner());
            if (ok) {
                sender.sendMessage("§aГосударство расформировано администратором.");
                nationManager.save();
            } else {
                sender.sendMessage("§cНе удалось расформировать государство.");
            }
            return true;
        }

        sender.sendMessage("§cНеизвестная подкоманда.");
        return true;
    }

    private boolean handleStateCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков.");
            return true;
        }

        if (args.length == 0) {
            sendStateHelp(player);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "invite" -> {
                if (!nationManager.canManageMembers(player.getUniqueId())) {
                    player.sendMessage("§cТолько владелец или совладелец может приглашать.");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage("§cИспользование: /state invite <player>");
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    player.sendMessage("§cИгрок должен быть онлайн.");
                    return true;
                }
                if (nationManager.isInAnyNation(target.getUniqueId())) {
                    player.sendMessage("§cИгрок уже состоит в другом государстве.");
                    return true;
                }

                Nation nation = nationManager.getNationOf(player.getUniqueId()).orElse(null);
                if (nation == null) {
                    player.sendMessage("§cУ вас нет государства.");
                    return true;
                }

                nationManager.invite(target.getUniqueId(), nation.getId());
                player.sendMessage("§aПриглашение отправлено: " + target.getName());
                target.sendMessage("§eВас пригласили в государство " + nation.getDisplayName() + ". Используйте /state accept");
                nationManager.save();
            }
            case "accept" -> {
                if (!nationManager.joinInvitedNation(player.getUniqueId())) {
                    player.sendMessage("§cНет активного приглашения или вы уже в другом государстве.");
                    return true;
                }
                Nation nation = nationManager.getNationOf(player.getUniqueId()).orElse(null);
                if (nation != null) {
                    player.sendMessage("§aВы вступили в государство " + nation.getDisplayName() + ".");
                }
                nationManager.save();
            }
            case "kick" -> {
                if (!nationManager.canManageMembers(player.getUniqueId())) {
                    player.sendMessage("§cТолько владелец или совладелец может исключать.");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage("§cИспользование: /state kick <player>");
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                Nation nation = nationManager.getNationOf(player.getUniqueId()).orElse(null);
                if (nation == null) {
                    player.sendMessage("§cУ вас нет государства.");
                    return true;
                }
                if (target.getUniqueId().equals(nation.getOwner())) {
                    player.sendMessage("§cНельзя исключить владельца.");
                    return true;
                }
                if (!nation.contains(target.getUniqueId())) {
                    player.sendMessage("§cИгрок не в вашем государстве.");
                    return true;
                }

                if (nationManager.kickMember(nation.getId(), target.getUniqueId())) {
                    player.sendMessage("§aИгрок исключён: " + target.getName());
                    if (target.isOnline()) {
                        Objects.requireNonNull(target.getPlayer()).sendMessage("§cВы были исключены из государства " + nation.getDisplayName() + ".");
                    }
                    nationManager.save();
                } else {
                    player.sendMessage("§cНе удалось исключить игрока.");
                }
            }
            case "leave" -> {
                if (nationManager.isOwner(player.getUniqueId())) {
                    player.sendMessage("§cВладелец не может выйти. Используйте /state transfer или /state disband.");
                    return true;
                }
                if (!nationManager.leaveNation(player.getUniqueId())) {
                    player.sendMessage("§cВы не состоите в государстве.");
                    return true;
                }
                player.sendMessage("§eВы покинули государство.");
                nationManager.save();
            }
            case "promote" -> {
                if (!nationManager.isOwner(player.getUniqueId())) {
                    player.sendMessage("§cТолько владелец может повышать до совладельца.");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage("§cИспользование: /state promote <player>");
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (nationManager.appointCoOwner(player.getUniqueId(), target.getUniqueId())) {
                    player.sendMessage("§aИгрок повышен до совладельца: " + target.getName());
                    if (target.isOnline()) {
                        Objects.requireNonNull(target.getPlayer()).sendMessage("§aВы повышены до совладельца государства.");
                    }
                    nationManager.save();
                } else {
                    player.sendMessage("§cНе удалось повысить игрока. Он должен быть участником вашего государства.");
                }
            }
            case "demote" -> {
                if (!nationManager.isOwner(player.getUniqueId())) {
                    player.sendMessage("§cТолько владелец может понижать совладельцев.");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage("§cИспользование: /state demote <player>");
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (nationManager.demoteCoOwner(player.getUniqueId(), target.getUniqueId())) {
                    player.sendMessage("§aСовладелец понижен до участника: " + target.getName());
                    if (target.isOnline()) {
                        Objects.requireNonNull(target.getPlayer()).sendMessage("§eВы понижены до участника государства.");
                    }
                    nationManager.save();
                } else {
                    player.sendMessage("§cНе удалось понизить игрока. Он должен быть совладельцем вашего государства.");
                }
            }
            case "transfer" -> {
                if (!nationManager.isOwner(player.getUniqueId())) {
                    player.sendMessage("§cТолько владелец может передавать лидерство.");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage("§cИспользование: /state transfer <player>");
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (nationManager.transferLeadership(player.getUniqueId(), target.getUniqueId())) {
                    player.sendMessage("§aЛидерство передано игроку " + target.getName());
                    if (target.isOnline()) {
                        Objects.requireNonNull(target.getPlayer()).sendMessage("§aВы стали владельцем государства.");
                    }
                    nationManager.save();
                } else {
                    player.sendMessage("§cНе удалось передать лидерство. Игрок должен быть членом вашего государства.");
                }
            }
            case "disband" -> {
                if (!nationManager.isOwner(player.getUniqueId())) {
                    player.sendMessage("§cТолько владелец может расформировать государство.");
                    return true;
                }

                if (args.length == 1) {
                    String password = String.format("%04d", new Random().nextInt(10000));
                    disbandPasswords.put(player.getUniqueId(), password);
                    player.sendMessage("§cВНИМАНИЕ: вы действительно хотите расформировать государство?");
                    player.sendMessage("§eДля подтверждения введите: §6/state disband " + password);
                    return true;
                }

                String expected = disbandPasswords.get(player.getUniqueId());
                if (expected == null || !expected.equals(args[1])) {
                    player.sendMessage("§cНеверный пароль подтверждения. Повторите /state disband для получения нового пароля.");
                    return true;
                }

                disbandPasswords.remove(player.getUniqueId());
                if (nationManager.disband(player.getUniqueId())) {
                    player.sendMessage("§cГосударство расформировано.");
                    nationManager.save();
                } else {
                    player.sendMessage("§cНе удалось расформировать государство.");
                }
            }
            default -> sendStateHelp(player);
        }
        return true;
    }

    private void sendStateHelp(Player player) {
        player.sendMessage("§6/state invite <player> §7- пригласить игрока в государство");
        player.sendMessage("§6/state accept §7- принять приглашение");
        player.sendMessage("§6/state kick <player> §7- выгнать игрока из государства");
        player.sendMessage("§6/state leave §7- покинуть государство");
        player.sendMessage("§6/state promote <player> §7- повысить до совладельца");
        player.sendMessage("§6/state demote <player> §7- понизить совладельца до игрока");
        player.sendMessage("§6/state transfer <player> §7- передать лидерство");
        player.sendMessage("§6/state disband [пароль] §7- удалить государство (с подтверждением)");
        player.sendMessage("§6/sc <сообщение> §7- отправить сообщение в чат государства");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("stateadmin")) {
            if (args.length == 1) {
                return partial(args[0], List.of("create", "remove"));
            }
            return List.of();
        }

        if (command.getName().equalsIgnoreCase("sc")) {
            return List.of();
        }

        if (args.length == 1) {
            return partial(args[0], List.of("invite", "accept", "kick", "leave", "promote", "demote", "transfer", "disband"));
        }

        if (args.length == 2 && List.of("invite", "kick", "promote", "demote", "transfer").contains(args[0].toLowerCase(Locale.ROOT))) {
            List<String> online = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            return partial(args[1], online);
        }

        return List.of();
    }

    private List<String> partial(String input, List<String> variants) {
        String lowered = input.toLowerCase(Locale.ROOT);
        return variants.stream().filter(v -> v.toLowerCase(Locale.ROOT).startsWith(lowered)).toList();
    }
}
