package core.handlers.admin;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import core.main.ServerCardMaster;
import shared.items.Inventory;
import shared.items.Item;
import shared.map.Faction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author doc
 */
public class WhiteListExclusionStrategy implements ExclusionStrategy {
	private static final String MODEL_PACKAGE = "core.handlers.admin.models";

	private Map<Class<?>, Set<String>> whiteList = new HashMap<Class<?>, Set<String>>();

	public WhiteListExclusionStrategy(){
		addField(ServerCardMaster.class, "id");
		addField(ServerCardMaster.class, "name");
		addField(ServerCardMaster.class, "reputation");
		addField(ServerCardMaster.class, "stats");
		addField(ServerCardMaster.class, "inventory");

		addField(Inventory.class, "items");

		addField(Item.class, "id");
		addField(Item.class, "type");
		addField(Item.class, "name");

		addField(Faction.class, "id");
		addField(Faction.class, "name");
	}

	public void addField(Class<?> type, String field){
		Set<String> set = whiteList.get(type);

		if (set == null) {
			set = new HashSet<String>();
			whiteList.put(type, set);
		}

		set.add(field);
	}

	@Override
	public boolean shouldSkipField(FieldAttributes fieldAttributes) {
		if (fieldAttributes.getDeclaringClass().getPackage().getName().equals(MODEL_PACKAGE))
			return false;

		Set<String> set = whiteList.get(fieldAttributes.getDeclaringClass());
		return set != null && !set.contains(fieldAttributes.getName());
	}

	@Override
	public boolean shouldSkipClass(Class<?> aClass) {
		return false;
	}
}
