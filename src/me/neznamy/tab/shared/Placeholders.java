package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Placeholders {

	public static List<Placeholder> playerPlaceholders;
	public static List<Placeholder> serverPlaceholders;
	public static ConcurrentHashMap<String, Integer> online = new ConcurrentHashMap<String, Integer>();

	static {
		online.put("other", 0);
		for (int i=5; i<=14; i++) online.put("1-" + i + "-x", 0);
	}
	public static List<Placeholder> getAll(){
		List<Placeholder> list = new ArrayList<Placeholder>();
		list.addAll(playerPlaceholders);
		list.addAll(serverPlaceholders);
		return list;
	}
	public static void recalculateOnlineVersions() {
		online.put("other", 0);
		for (int i=5; i<=14; i++) online.put("1-" + i + "-x", 0);
		for (ITabPlayer p : Shared.getPlayers()){
			String group = "1-"+p.getVersion().getMinorVersion()+"-x";
			if (online.containsKey(group)) {
				online.put(group, online.get(group)+1);
			} else {
				online.put("other", online.get("other")+1);
			}
		}
	}
	//code taken from bukkit, so it can work on bungee too
	public static String color(String textToTranslate){
		if (textToTranslate == null) return null;
		if (!textToTranslate.contains("&")) return textToTranslate;
		char[] b = textToTranslate.toCharArray();
		for (int i = 0; i < b.length - 1; i++) {
			if ((b[i] == '&') && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[(i + 1)]) > -1)){
				b[i] = '§';
				b[(i + 1)] = Character.toLowerCase(b[(i + 1)]);
			}
		}
		return new String(b);
	}
	//code taken from bukkit, so it can work on bungee too
	public static String getLastColors(String input) {
		String result = "";
		int length = input.length();
		for (int index = length - 1; index > -1; index--){
			char section = input.charAt(index);
			if ((section == '§') && (index < length - 1)){
				char c = input.charAt(index + 1);
				if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(c+"")) {
					result = "§" + c + result;
					if ("0123456789AaBbCcDdEeFfRr".contains(c+"")) {
						break;
					}
				}
			}
		}
		return result;
	}
	public static String replaceAllPlaceholders(String string, ITabPlayer p) {
		for (Placeholder pl : Property.detectPlaceholders(string, true)) {
			if (string.contains(pl.getIdentifier())) string = pl.set(string, p);
		}
		string = PluginHooks.PlaceholderAPI_setPlaceholders(p, string);
		for (String removed : Configs.removeStrings) {
			string = string.replace(removed, "");
		}
		string = color(string);
		return string;
	}
}