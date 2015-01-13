package core.main;

import program.main.Program;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameClient {
	private static final int MAX_TIME_ONLINE = 100;

	private Program program;
	private Client client;
	private int timeOnlineLeft;
	private boolean stillOnline;

	private int visionHash = 0;
	private boolean playersInSightChanged;
	private byte playersInSightByte[] = new byte[0];

	private int id;

	// Client data
	private CardMaster cardMaster;

	private Set<CardMaster> playersInSight;
	private Set<ChatChannel> chatChannels;

	public GameClient(Client client) {
		this.client = client;
		this.program = Program.getInstance();
		timeOnlineLeft = MAX_TIME_ONLINE;

		cardMaster = new CardMaster();
		cardMaster.setType(CardMaster.TYPE_PLAYER);

		playersInSight = new HashSet<CardMaster>();
		chatChannels = new HashSet<ChatChannel>();
	}

	public Set<ChatChannel> getChatChannels() {
		return chatChannels;
	}

	public void updateTime() {
		timeOnlineLeft = Math.max(0, timeOnlineLeft - 1);
		stillOnline = timeOnlineLeft != 0;
	}

	public boolean isPlayersInSightChanged() {
		return playersInSightChanged;
	}

	public boolean isStillOnline() {
		return stillOnline;
	}

	public Set<CardMaster> getPlayersInSight() {
		return playersInSight;
	}

	public Client getClient() {
		return client;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void updateOnline() {
		timeOnlineLeft = MAX_TIME_ONLINE;
	}

	public void updatePlayersInSight() {
		// Update list of visible players
		// Complete list of all cardMasters in sight
		List<CardMaster> cmsInSight = program.getClusterGrid().getHeroesInRadiusOf(cardMaster, 1000);

		// First, remove players who had gone out of sight
		if (cmsInSight.size() > 0)
			for (CardMaster cardMasterInSight: playersInSight)
				if (!cmsInSight.contains(cardMasterInSight))
					cardMasterInSight.getWhoCanSeeMe().remove(this);

		// Next, add players who are now in sight
		playersInSight.clear();
		playersInSight.addAll(cmsInSight);

		for (CardMaster cardMasterInSight: cmsInSight)
			cardMasterInSight.getWhoCanSeeMe().add(this);

		// Fill array of ids, if it was changed, update playersInSightByte
		int idArray[] = new int[cmsInSight.size()];
		int amount = 0;

		for (CardMaster cardMaster : playersInSight)
			idArray[amount++] = cardMaster.getId();

		Arrays.sort(idArray);

		// Calculating and checking hash
		int hash = Arrays.hashCode(idArray);
		playersInSightChanged = (hash != visionHash);
		visionHash = hash;

		if (playersInSightChanged)
			playersInSightByte = DataUtil.intToVarInt(idArray);
	}

	public CardMaster getCardMaster() {
		return cardMaster;
	}

	public byte[] getPlayersInSightByte() {
		return playersInSightByte;
	}
}
