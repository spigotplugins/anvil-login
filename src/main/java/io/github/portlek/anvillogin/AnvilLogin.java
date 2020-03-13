package io.github.portlek.anvillogin;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnvilLogin extends JavaPlugin implements Listener {

    private AuthMeApi authmeApi;

    private String insert;

    private String wrongPassword;

    @Override
    public void onEnable() {
        if (this.getServer().getPluginManager().getPlugin("AuthMe") == null) {
            return;
        }

        this.authmeApi = AuthMeApi.getInstance();

        this.saveDefaultConfig();

        this.insert = this.c(this.getConfig().getString("insert"));
        this.wrongPassword = this.c(this.getConfig().getString("wrong-password"));

        for (final Player player : this.getServer().getOnlinePlayers()) {
            this.ask(player);
        }

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    private String c(final String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void ask(final Player player) {
        if (this.authmeApi.isRegistered(player.getName())) {
            this.openLogin(player);
        } else {
            this.openRegister(player);
        }
    }

    private void openLogin(final Player p) {
        final AnvilGUI.Builder builder = new AnvilGUI.Builder()
            .onComplete((player, s) -> {
                if (!this.authmeApi.checkPassword(player.getName(), s)) {
                    return AnvilGUI.Response.text(this.wrongPassword);
                }

                this.authmeApi.forceLogin(player);

                return AnvilGUI.Response.close();
            })
            .preventClose()
            .text(this.insert)
            .plugin(this);

        this.getServer().getScheduler().runTask(this, () -> builder.open(p));
    }

    private void openRegister(final Player p) {
        final AnvilGUI.Builder builder = new AnvilGUI.Builder()
            .onComplete((player, s) -> {
                this.authmeApi.registerPlayer(player.getName(), s);

                if (!this.authmeApi.isAuthenticated(player)) {
                    this.authmeApi.forceLogin(player);
                }

                return AnvilGUI.Response.close();
            })
            .preventClose()
            .text(this.insert)
            .plugin(this);

        this.getServer().getScheduler().runTask(this, () -> builder.open(p));
    }

    @EventHandler
    public void join(final PlayerJoinEvent event) {
        this.ask(event.getPlayer());
    }

}
