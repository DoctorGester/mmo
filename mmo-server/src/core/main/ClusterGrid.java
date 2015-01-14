package core.main;

import shared.map.CardMaster;
import shared.map.Hero;

import java.util.ArrayList;
import java.util.List;

public class ClusterGrid {
	private ClusterCell clusterCells[];
	private int width, height;

	public ClusterGrid(int width, int height){
		this.width = width;
		this.height = height;

		clusterCells = new ClusterCell[width * height];

		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++){
				int index = y * width + x;
				clusterCells[index] = new ClusterCell(x, y);
			}
	}

	private int clamp(int n, int l, int h){
		return (n > h ? h : (n < l ? l : n));
	}

	private ClusterCell getCell(int x, int y){
		return clusterCells[y * width + x];
	}

	private ClusterCell getCellByRealCords(int x, int y){
		int rx = clamp((int) (x / ClusterCell.CELL_SIZE), 0, width),
			ry = clamp((int) (y / ClusterCell.CELL_SIZE), 0, height);
		return getCell(rx, ry);
	}

	public void updateCardMaster(ServerCardMaster cardMaster){
		ServerHero hero = cardMaster.getHero();
		ClusterCell position = hero.getClusterPosition();
		int heroX = (int) hero.getX(),
			heroY = (int) hero.getY();

		ClusterCell real = getCellByRealCords(heroX, heroY);

		if (position != real){
			if (position != null)
				position.getCardMastersInside().remove(cardMaster);
			real.getCardMastersInside().add(cardMaster);

			hero.setClusterPosition(real);
		}
	}

    public void removeCardMaster(ServerCardMaster cardMaster){
        ServerHero hero = cardMaster.getHero();
        ClusterCell position = hero.getClusterPosition();

        position.getCardMastersInside().remove(cardMaster);
    }

	private static final List<ServerCardMaster> zeroList = new ArrayList<ServerCardMaster>(0);

	public List<ServerCardMaster> getHeroesInRadiusOf(CardMaster cardMaster, float radius){
		Hero hero = cardMaster.getHero();
		float heroX = hero.getX(),
			  heroY = hero.getY(),
			  radSquare = radius * radius;

		ClusterCell topLeft = getCellByRealCords((int) (heroX - radius), (int) (heroY - radius)),
					botRight = getCellByRealCords((int) (heroX + radius), (int) (heroY + radius));

		List<ServerCardMaster> cardMasters = null;

		for(int y = topLeft.getY(); y <= botRight.getY(); y++)
			for(int x = topLeft.getX(); x <= botRight.getX(); x++){
				ClusterCell toCheck = getCell(x, y);

				for(ServerCardMaster cmInCell: toCheck.getCardMastersInside()){
					Hero heroInCell = cmInCell.getHero();
					float distance = (heroInCell.getX() - heroX) * (heroInCell.getX() - heroX) +
									 (heroInCell.getY() - heroY) * (heroInCell.getY() - heroY);

					if (distance <= radSquare){
						if (cardMasters == null)
							cardMasters = new ArrayList<ServerCardMaster>();

						cardMasters.add(cmInCell);
					}
				}
			}

		if (cardMasters == null)
			return zeroList;

		return cardMasters;
	}
}
