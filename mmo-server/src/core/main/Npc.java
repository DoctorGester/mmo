package core.main;

import core.main.inventory.Inventory;
import core.main.inventory.Item;
import core.main.inventory.ItemDatabase;
import core.main.inventory.items.CardItem;
import program.main.Program;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Npc {
    private CardMaster cardMaster;

    public Npc(){
        cardMaster = new CardMaster();
        cardMaster.setName("Undefined");
		cardMaster.setType(CardMaster.TYPE_NPC);

		createDefaultInventory();
    }

	private void createDefaultInventory(){
		Inventory inventory = cardMaster.getInventory();

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

    public CardMaster getCardMaster(){
        return cardMaster;
    }
}
