package core.main;

import shared.items.Item;
import shared.items.types.UnitCardItem;
import shared.map.CardMaster;

import java.util.Arrays;

public class Npc {
    private ServerCardMaster cardMaster;

    public Npc(){
        cardMaster = new ServerCardMaster();
        cardMaster.setName("Undefined");
		cardMaster.setType(CardMaster.TYPE_NPC);
    }

	public void createDefaultInventory(){
		ServerInventory inventory = cardMaster.getInventory();

		UnitCardItem itemOne, itemTwo, itemThree;
		itemOne = new UnitCardItem();
		itemTwo = new UnitCardItem();
		itemThree = new UnitCardItem();

		itemOne.setUnitId("angel");
		itemTwo.setUnitId("lizardman");
		itemThree.setUnitId("kobold");

		inventory.addItems(Arrays.<Item>asList(itemOne, itemTwo, itemThree));

		ItemDatabase.getInstance().registerItem(itemOne);
		ItemDatabase.getInstance().registerItem(itemTwo);
		ItemDatabase.getInstance().registerItem(itemThree);
	}

    public ServerCardMaster getCardMaster(){
        return cardMaster;
    }
}
