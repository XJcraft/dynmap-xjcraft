package org.jim.xj.dynmap;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.jim.bukkit.audit.AuditPlugin;
import org.jim.bukkit.audit.PlayerMeta;
import org.jim.bukkit.audit.Status;
import org.jim.bukkit.audit.apply.ApplyHelper;

public class DynmapXJPlugin extends JavaPlugin implements Runnable, Listener {

	private MarkerAPI markerapi;
	private boolean enable = false;
	private MarkerSetWrapper homeMarkers;
	private MarkerSetWrapper baseMarkers;
	private BukkitTask task;

	// private int period = 20 * 60 * 60;// 1h

	@Override
	public void onLoad() {

	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		Plugin dynmap = pm.getPlugin("dynmap");
		if (dynmap == null) {
			severe("Cannot find dynmap!");
			return;
		}
		Plugin xjcraft = pm.getPlugin("XJCraftAudit");
		if(xjcraft == null){
			severe("Cannot find xjcraft!");
			return;
		}
		DynmapAPI api = (DynmapAPI) dynmap; /* Get API */
		markerapi = api.getMarkerAPI();
		enable = true;
		activate();

	}

	private void activate() {
		String key = "xjcraft.markerset.home";
		String key2 = "xjcraft.markerset.base";
		homeMarkers = new MarkerSetWrapper(markerapi, key, "Homes",
				markerapi.getMarkerIcon("house"));
		baseMarkers = new MarkerSetWrapper(markerapi, key2, "Basements",
				markerapi.getMarkerIcon("bighouse"));
		Bukkit.getScheduler().runTaskAsynchronously(this, this);
	}

	@Override
	public void run() {
		update();
	}

	private Location getTownLocation(OfflinePlayer player, PlayerMeta meta) {
		if (meta != null && meta.getStatus() != Status.UNAPPLIED) {
			return meta.getLocation("town-cmdlocation");
		}
		return null;
	}

	private Location getBaseLocation(OfflinePlayer player, PlayerMeta meta) {
		if (meta != null && meta.getStatus() != Status.UNAPPLIED) {
			return meta.getLocation("base-cmdlocation");
		}
		return null;
	}

	private void update() {
		Map<String, Marker> homemap = new HashMap<String, Marker>();
		Map<String, Marker> basemap = new HashMap<String, Marker>();
		ApplyHelper helper = AuditPlugin.getPlugin().helper;
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if (!player.isBanned()) {
				PlayerMeta meta = helper.getPlayerMeta(player.getName());
				Location loc = getTownLocation(player, meta);
				Location loc2 = getBaseLocation(player, meta);
				String id = player.getName();
				if (loc != null) {
					Marker m = homeMarkers.remove(id);

					if (m == null) { /* Not found? Need new one */
						m = homeMarkers.create(id, player.getName() + "(home)",
								loc);
					}
					homemap.put(id, m);

				}
				if (loc2 != null) {
					Marker b = baseMarkers.remove(id);
					if (b == null) {
						b = baseMarkers.create(id, player.getName()
								+ "(basement)", loc2);
					}
					basemap.put(id, b);
				}
			}
		}
		/* And replace with new map */
		homeMarkers.clear();
		homeMarkers.setMarkers(homemap);
		baseMarkers.clear();
		baseMarkers.setMarkers(basemap);
	}

	@Override
	public void onDisable() {
		if (homeMarkers != null)
			homeMarkers.delete();
		if (baseMarkers != null)
			baseMarkers.delete();
		if (task != null) {
			task.cancel();
		}
	}

	public void severe(String msg) {
		getLogger().log(Level.SEVERE, msg);
	}

}
