package main.models;

import main.core.Stat;

import java.util.Map;

/**
 * @author doc
 */
public class PlayerInfo {
	private int id;
	private String name;
	private InventoryModel inventory;
	private Map<Stat, Integer> stats;
	private Map<FactionModel, Integer> reputation;

	public InventoryModel getInventory() {
		return inventory;
	}

	public Map<FactionModel, Integer> getReputation() {
		return reputation;
	}

	public Map<Stat, Integer> getStats() {
		return stats;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
