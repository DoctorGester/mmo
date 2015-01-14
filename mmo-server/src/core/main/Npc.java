package core.main;

import shared.items.Item;
import shared.items.types.CardItem;
import shared.map.CardMaster;

import java.util.Arrays;

public class Npc {
    private ServerCardMaster cardMaster;

    public Npc(){
        cardMaster = new ServerCardMaster();
        cardMaster.setName("Undefined");
		cardMaster.setType(CardMaster.TYPE_NPC);

		createDefaultInventory();
    }

	private void createDefaultInventory(){
		ServerInventory inventory = cardMaster.getInventory();

		CardItem itemOne, itemTwo, itemThree;
		itemOne = new CardItem();
		itemTwo = new CardItem();
		itemThree = new CardItem();

		itemOne.setUnitId(1);
		itemThree.setUnitId(1);

		inventory.addItems(Arrays.<Item>asList(itemOne, itemTwo, itemThree));

		ItemDatabase.getInstance().registerItem(itemOne);
		ItemDatabase.getInstance().registerItem(itemTwo);
		ItemDatabase.getInstance().registerItem(itemThree);
	}

    public ServerCardMaster getCardMaster(){
        return cardMaster;
    }
}
