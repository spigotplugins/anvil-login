package io.github.portlek.anvillogin;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AnvilLogin extends JavaPlugin implements Listener {

    private AuthMeApi authmeApi;

    private String insert;

    private String wrongPassword;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("AuthMe") == null)
            return;

        authmeApi = AuthMeApi.getInstance();

        saveDefaultConfig();

        insert = c(getConfig().getString("insert"));
        wrongPassword = c(getConfig().getString("wrong-password"));

        for (Player player : getServer().getOnlinePlayers())
            ask(player);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        ask(event.getPlayer());
    }

    private void ask(Player player) {
        if (authmeApi.isRegistered(player.getName()))
            openLogin(player);
        else
            openRegister(player);
    }

    private void openRegister(Player p) {
        new AnvilGUI.Builder()
            .onComplete((player, s) -> {
                authmeApi.registerPlayer(player.getName(), s);

                if (!authmeApi.isAuthenticated(player))
                    authmeApi.forceLogin(player);

                return AnvilGUI.Response.close();
            })
            .preventClose()
            .text(insert)
            .plugin(this)
            .open(p);
    }

    private void openLogin(Player p) {
        new AnvilGUI.Builder()
            .onComplete((player, s) -> {
                if (!authmeApi.checkPassword(player.getName(), s))
                    return AnvilGUI.Response.text(wrongPassword);

                authmeApi.forceLogin(player);

                return AnvilGUI.Response.close();
            })
            .preventClose()
            .text(insert)
            .plugin(this)
            .open(p);
    }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
