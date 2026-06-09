package me.angelique.angelNCore.commands;

import me.angelique.angelNCore.AngelNCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupCommand implements CommandExecutor {

    private final AngelNCore plugin;

    public BackupCommand(AngelNCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        File dbFile = new File(plugin.getDataFolder(), "economy.db");
        if (!dbFile.exists()) {
            sender.sendMessage("\u00a7cDatabase file not found.");
            return true;
        }
        File backupDir = new File(plugin.getDataFolder(), "backups");
        backupDir.mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File backupFile = new File(backupDir, "economy-" + timestamp + ".db");
        try {
            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            sender.sendMessage("\u00a7aBackup saved: " + backupFile.getName());
            cleanOldBackups(backupDir, 10);
        } catch (Exception e) {
            sender.sendMessage("\u00a7cBackup failed: " + e.getMessage());
        }
        return true;
    }

    public static void backupOnDisable(AngelNCore plugin) {
        try {
            File dbFile = new File(plugin.getDataFolder(), "economy.db");
            if (!dbFile.exists()) return;
            File backupDir = new File(plugin.getDataFolder(), "backups");
            backupDir.mkdirs();
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            File backupFile = new File(backupDir, "economy-shutdown-" + timestamp + ".db");
            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Auto-backup saved: " + backupFile.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Auto-backup failed: " + e.getMessage());
        }
    }

    private void cleanOldBackups(File dir, int keep) {
        File[] files = dir.listFiles((d, name) -> name.startsWith("economy-") && name.endsWith(".db"));
        if (files == null || files.length <= keep) return;
        java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        for (int i = keep; i < files.length; i++) {
            files[i].delete();
        }
    }
}
