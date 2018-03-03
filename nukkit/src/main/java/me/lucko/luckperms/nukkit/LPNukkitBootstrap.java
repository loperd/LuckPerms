/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.nukkit;

import me.lucko.luckperms.api.platform.PlatformType;
import me.lucko.luckperms.common.dependencies.classloader.PluginClassLoader;
import me.lucko.luckperms.common.dependencies.classloader.ReflectionClassLoader;
import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * Bootstrap plugin for LuckPerms running on Nukkit.
 */
public class LPNukkitBootstrap extends PluginBase implements LuckPermsBootstrap {

    /**
     * A scheduler adapter for the platform
     */
    private final NukkitSchedulerAdapter schedulerAdapter;

    /**
     * The plugin classloader
     */
    private final PluginClassLoader classLoader;

    /**
     * The plugin instance
     */
    private final LPNukkitPlugin plugin;

    /**
     * The time when the plugin was enabled
     */
    private long startTime;

    // load/enable latches
    private final CountDownLatch loadLatch = new CountDownLatch(1);
    private final CountDownLatch enableLatch = new CountDownLatch(1);

    public LPNukkitBootstrap() {
        this.schedulerAdapter = new NukkitSchedulerAdapter(this);
        this.classLoader = new ReflectionClassLoader(this);
        this.plugin = new LPNukkitPlugin(this);
    }

    // provide adapters

    @Override
    public NukkitSchedulerAdapter getScheduler() {
        return this.schedulerAdapter;
    }

    @Override
    public PluginClassLoader getPluginClassLoader() {
        return this.classLoader;
    }

    // lifecycle

    @Override
    public void onLoad() {
        try {
            this.plugin.load();
        } finally {
            this.loadLatch.countDown();
        }
    }

    @Override
    public void onEnable() {
        this.startTime = System.currentTimeMillis();
        try {
            this.plugin.enable();
        } finally {
            this.enableLatch.countDown();
        }
    }

    @Override
    public void onDisable() {
        this.plugin.disable();
    }

    @Override
    public CountDownLatch getEnableLatch() {
        return this.enableLatch;
    }

    @Override
    public CountDownLatch getLoadLatch() {
        return this.loadLatch;
    }

    // provide information about the plugin

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public long getStartupTime() {
        return this.startTime;
    }

    // provide information about the platform

    @Override
    public PlatformType getType() {
        return PlatformType.NUKKIT;
    }

    @Override
    public String getServerBrand() {
        return getServer().getName();
    }

    @Override
    public String getServerVersion() {
        return getServer().getVersion() + " - " + getServer().getNukkitVersion();
    }

    @Override
    public String getServerName() {
        return getServer().getServerUniqueId().toString();
    }

    @Override
    public File getDataDirectory() {
        return getDataFolder();
    }

    @Override
    public InputStream getResourceStream(String path) {
        return getResource(path);
    }

    @Nullable
    @Override
    public Player getPlayer(UUID uuid) {
        return getServer().getOnlinePlayers().get(uuid);
    }

    @Override
    public Optional<UUID> lookupUuid(String username) {
        return Optional.empty();
    }

    @Override
    public int getPlayerCount() {
        return getServer().getOnlinePlayers().size();
    }

    @Override
    public Stream<String> getPlayerList() {
        return getServer().getOnlinePlayers().values().stream().map(Player::getName);
    }

    @Override
    public Stream<UUID> getOnlinePlayers() {
        return getServer().getOnlinePlayers().values().stream().map(Player::getUniqueId);
    }

    @Override
    public boolean isPlayerOnline(UUID uuid) {
        Player player = getServer().getOnlinePlayers().get(uuid);
        return player != null && player.isOnline();
    }
}
