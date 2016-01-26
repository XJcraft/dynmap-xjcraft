package org.jim.xj.dynmap;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

public class MarkerSetWrapper {

	private MarkerSet set;
	private Map<String, Marker> markers = new HashMap<String, Marker>();
	private MarkerIcon icon;

	public MarkerSetWrapper(MarkerAPI markerapi, String key, String label,
			MarkerIcon icon) {
		this.icon = icon;
		set = markerapi.getMarkerSet(key);
		if (set == null)
			set = markerapi.createMarkerSet(key, label, null, false);
		else
			set.setMarkerSetLabel(label);
		set.setLayerPriority(10);
		set.setHideByDefault(false);
	}

	public MarkerSet getSet() {
		return set;
	}

	public void setSet(MarkerSet set) {
		this.set = set;
	}

	public Map<String, Marker> getMarkers() {
		return markers;
	}

	public void setMarkers(Map<String, Marker> markers) {
		this.markers = markers;
	}

	public Marker create(String id, String label, Location loc) {
		Marker m = set.createMarker(id, label, loc.getWorld().getName(),
				loc.getX(), loc.getY(), loc.getZ(), icon, false);
		//System.out.println("create: " + label + "," + loc);
		return m;
	}

	public Marker remove(String id) {
		return markers.remove(id);
	}

	public void clear() {
		for (Marker oldm : markers.values()) {
			oldm.deleteMarker();
		}
		markers.clear();
	}

	public void delete() {
		set.deleteMarkerSet();
	}
}
