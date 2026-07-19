package io.github.ethan23.pickaxeLoans.gui;

import io.github.ethan23.pickaxeLoans.PickaxeLoans;
import io.github.ethan23.pickaxeLoans.util.ColorTextBuilder;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Collects a single line of chat input from a player and hands it to a
 * callback.
 *
 * <p>Threading: {@link AsyncChatEvent} fires off the main thread, so the
 * captured callback is re-scheduled onto the main thread before it runs —
 * callbacks may safely touch Bukkit state (open inventories, modify items).
 * A pending request is dropped if the player quits before answering.
 */
public class PlayerInputListener implements Listener {

    private final ConcurrentHashMap<UUID, Consumer<String>> waitingPlayers = new ConcurrentHashMap<>();

    /**
     * Registers a callback for the player's next chat message. That message
     * is consumed (canceled) instead of being broadcast to chat.
     *
     * @param player the player to collect input from
     * @param callback receives the message as plain text, on the main thread
     */
    public void requestInput(Player player, Consumer<String> callback) {
        waitingPlayers.put(player.getUniqueId(), callback);
        player.sendMessage(ColorTextBuilder.parse("<gray>Type <red>cancel <gray>to stop"));
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

    /** Drops any pending input request so quitting players do not leak stale callbacks. */
    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        waitingPlayers.remove(uuid);
    }
}
