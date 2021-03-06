package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;

public class BelowName implements SimpleFeature{

	private static final String objectivename = "TAB-BelowName";
	private static final int DisplaySlot = 2;
	
	private int refresh;
	public static String number;
	public static String text;
	private Property textProperty;
	
	@Override
	public void load() {
		refresh =  Configs.config.getInt("belowname.refresh-interval-milliseconds", 200);
		if (refresh < 50) Shared.errorManager.refreshTooLow("BelowName", refresh);
		textProperty = new Property(null, text, null);
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledBelowname) continue;
			PacketAPI.registerScoreboardObjective(p, objectivename, textProperty.get(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		}
		Shared.cpu.startRepeatingMeasuredTask(refresh, "refreshing belowname", "Belowname", new Runnable() {
			public void run(){
				for (ITabPlayer p : Shared.getPlayers()){
					if (p.disabledBelowname) continue;
					if (p.properties.get("belowname-number").isUpdateNeeded()) {
						for (ITabPlayer all : Shared.getPlayers()) PacketAPI.setScoreboardScore(all, p.getName(), objectivename, getNumber(p));
					}
				}
				if (textProperty.isUpdateNeeded()) {
					for (ITabPlayer all : Shared.getPlayers()) {
						PacketAPI.changeScoreboardObjectiveTitle(all, objectivename, textProperty.get(), EnumScoreboardHealthDisplay.INTEGER);
					}
				}
			}
		});
	}
	@Override
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledBelowname) continue;
			PacketAPI.unregisterScoreboardObjective(p, objectivename);
		}
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		if (connectedPlayer.disabledBelowname) return;
		PacketAPI.registerScoreboardObjective(connectedPlayer, objectivename, textProperty.get(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		for (ITabPlayer all : Shared.getPlayers()){
			PacketAPI.setScoreboardScore(all, connectedPlayer.getName(), objectivename, getNumber(connectedPlayer));
			PacketAPI.setScoreboardScore(connectedPlayer, all.getName(), objectivename, getNumber(all));
		}
	}
	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		PacketAPI.unregisterScoreboardObjective(disconnectedPlayer, objectivename);
	}
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.disabledBelowname && !p.isDisabledWorld(Configs.disabledBelowname, from)) {
			onQuit(p);
		}
		if (!p.disabledBelowname && p.isDisabledWorld(Configs.disabledBelowname, from)) {
			onJoin(p);
		}
	}
	private int getNumber(ITabPlayer p) {
		return Shared.errorManager.parseInteger(p.properties.get("belowname-number").get(), 0, "BelowName");
	}
}