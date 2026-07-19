package io.github.ethan23.pickaxeLoans.gui;

import io.github.ethan23.pickaxeLoans.PickaxeLoans;
import io.github.ethan23.pickaxeLoans.util.ComponentBuilder;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PlayerInputListener implements Listener {

    private final ConcurrentHashMap<UUID, Consumer<String>> waitingPlayers = new ConcurrentHashMap<>();

    public void requestInput(Player player, Consumer<String> callback) {
        waitingPlayers.put(player.getUniqueId(), callback);
        player.sendMessage(ComponentBuilder.parse("<gray>Type <red>cancel <gray>to stop"));
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if(!waitingPlayers.containsKey(uuid))
            return;

        event.setCancelled(true);
        Component messageComponent = event.message();
        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent);

        Consumer<String> callback = waitingPlayers.remove(uuid);
        Bukkit.getScheduler().runTask(PickaxeLoans.getPlugin(), () -> callback.accept(message));

    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        waitingPlayers.remove(uuid);
    }
}
