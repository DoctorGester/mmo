package core.board;

import core.board.interfaces.*;
import core.main.CardMaster;
import core.main.ChatChannel;
import core.main.GameClient;
import core.main.inventory.ItemTypes;
import core.main.inventory.filters.TypeFilter;
import core.main.inventory.items.CardItem;
import core.main.inventory.items.SpellCardItem;
import program.main.Program;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class BoardImpl implements Board {
	private Cell[] cells;

	private int id;
	private int width, height;

	private Rectangle[] placementArea;
	
	private int state = STATE_WAIT_FOR_PICK;

	private AtomicInteger turnNumber = new AtomicInteger(-1);

	private CardMaster currentTurning;

	private List<Unit> units;
	private List<CardMaster> cardMasters;
	private List<CardMaster> cardMastersMadeTurn;

	private Set<CardMaster> playersFinishedPlacement;

	private List<Buff> buffs;
	private List<Alliance> alliances;
	private Map<CardMaster, Alliance> cardMasterAllianceMap;

	private Map<CardMaster, List<CardItem>> pickedCards = new HashMap<CardMaster, List<CardItem>>();
	private Map<CardMaster, List<SpellCardItem>> castCards = new HashMap<CardMaster, List<SpellCardItem>>();

	private float timeRemaining;
	private float turnTime;

	private ChatChannel chat;

	public BoardImpl(int width, int height){
		alliances = new ArrayList<Alliance>();
		units = new CopyOnWriteArrayList<Unit>();
		cardMasters = new CopyOnWriteArrayList<CardMaster>();
		cardMastersMadeTurn = new CopyOnWriteArrayList<CardMaster>();
		buffs = new ArrayList<Buff>();

		playersFinishedPlacement = new CopyOnWriteArraySet<CardMaster>();

		cardMasterAllianceMap = new HashMap<CardMaster, Alliance>();
		
		this.width = width;
		this.height = height;
		
		cells = new Cell[width * height];
		
		// Filling cell array with actual empty cells
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				cells[y * width + x] = new CellImpl(this, x, y);

		chat = Program.getInstance().getChatController().createChannel("Battle");
	}

	public void setPlacementArea(Rectangle[] placementArea) {
		this.placementArea = placementArea;
	}

	public Rectangle[] getPlacementArea() {
		return placementArea;
	}

	// These are used by scripts

    public Buff addBuff(String id, int timesToRepeat, int period, int initialDelay, Object data){
        BuffData buffData = Program.getInstance().getBuffScriptById(id);
        Buff buff = new BuffImpl(this, buffData, timesToRepeat, period, initialDelay, data);
        buffs.add(buff);
        return buff;
    }

    public Buff addBuff(String id, int timesToRepeat, int period, Object data){
        return addBuff(id, timesToRepeat, period, period, data);
    }

    public Buff addBuff(String id, int timesToRepeat, int period){
        return addBuff(id, timesToRepeat, period, period);
    }

	public short getTurnNumber(){
		return turnNumber.shortValue();
	}

	public Alliance getAllianceById(int id){
		for (Alliance alliance: alliances)
			if (id == alliance.getId())
				return alliance;
		return null;
	}
	
	public void selectTurningPlayer(){
		// If turn series has ended, clear madeTurn list
		if (cardMasters.size() == cardMastersMadeTurn.size())
			cardMastersMadeTurn.clear();
		
		// Next turning player selection
		for(CardMaster cardMaster: cardMasters){
			if (!cardMastersMadeTurn.contains(cardMaster)){
				currentTurning = cardMaster;
				break;
			}
		}
	}

	public void playerFinishedPlacement(CardMaster cardMaster){
		playersFinishedPlacement.add(cardMaster);
		if (playersFinishedPlacement.size() == cardMasters.size()){
			setState(STATE_WAIT_FOR_ORDER);
			incTurn();
			TurnManager.getInstance().finishPlacement(this);
			nextTurn();
		}
	}

	/**
	 * It's just a wrapper for {@link #nextTurn} which also increments turnNumber
	 */
	public void skipTurn(){
		incTurn();
		nextTurn();
	}
	
	public CardMaster getCurrentTurningPlayer(){
		return currentTurning;
	}
	
	public int getState() {
		return state;
	}
	
	private void setState(int state) {
		this.state = state;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

	public void addAlliance(Alliance alliance){
		alliances.add(alliance);
		for (CardMaster player: alliance.getAlliance()){
			Alliance old = cardMasterAllianceMap.get(player);

			if (old != null)
				old.getAlliance().remove(player);

			cardMasterAllianceMap.put(player, alliance);
		}
	}

	public boolean areAllies(CardMaster ... masters){
		List<CardMaster> list = Arrays.asList(masters);
		for (Alliance alliance: alliances)
			if (alliance.getAlliance().containsAll(list))
				return true;
		return false;
	}
	
	public void addCardMaster(CardMaster cm){
		cm.setBattleId(cardMasters.size());
		cardMasters.add(cm);
		
		if (currentTurning == null)
			currentTurning = cm;

		GameClient client = Program.getInstance().getGameClientByCardMaster(cm);
		if (client != null)
			Program.getInstance().getChatController().joinChannel(client, chat.getId());
	}
	
	public void addUnit(Unit u) {
		units.add(u);
	}
	
	public void removeUnit(Unit u){
		units.remove(u);
	}
	
	// This function is dangerous but fast, contains no checks
	public Cell getCell(int x, int y){
		return cells[y * width + x];
	}

	public Cell getCellChecked(int x, int y){
		if (x < 0 || x >= width || y < 0 || y >= height)
			return null;
		return cells[y * width + x];
	}
	
	public List<CardMaster> getCardMasters(){
		return cardMasters;
	}
	
	public List<Unit> getUnits(){
		return units;
	}
	
	private void endTurnForCardMaster(CardMaster cardMaster){
		cardMastersMadeTurn.add(cardMaster);
		cardMaster.setUsedUnit(null);
	}

	public static boolean pointInsideArea(Cell point, Rectangle area){
		int x = point.getX(),
			y = point.getY();
		return (x >= area.x && x <= area.x + area.width &&
				y >= area.y && y < area.y + area.height);
	}

	private int getUnitAmount(CardMaster cardMaster){
		int result = 0;
		for(Unit unit: units){
			if (unit.getOwner() == cardMaster)
				result++;
		}
		return result;
	}

	private void addCastCard(CardMaster master, SpellCardItem card){
		List<SpellCardItem> cards = castCards.get(master);

		if (cards == null){
			cards = new ArrayList<SpellCardItem>();
			castCards.put(master, cards);
		}

		cards.add(card);
	}

	private void addPickedCard(CardMaster master, CardItem card){
		List<CardItem> cards = pickedCards.get(master);

		if (cards == null){
			cards = new ArrayList<CardItem>();
			pickedCards.put(master, cards);
		}

		cards.add(card);
	}

	private boolean hasCastCard(CardMaster master, SpellCardItem card){
		return castCards.get(master) != null && castCards.get(master).contains(card);
	}

	private boolean hasPickedCard(CardMaster master, CardItem card){
		return pickedCards.get(master) != null && pickedCards.get(master).contains(card);
	}

	public List<CardItem> getPickedCards(CardMaster cardMaster){
		return pickedCards.get(cardMaster);
	}

	public synchronized boolean handlePlacementOrder(CardMaster owner, Cell selected, Cell order){
		if (selected == null || order == null)
			return false;

		// Exit if board is not in the placement state
		if (state != STATE_WAIT_FOR_PLACEMENT)
			return false;

		if (selected == order)
			return false;

		// Exit if selected cell doesn't contain unit
		if (selected.getContentsType() != Cell.CONTENTS_UNIT)
			return false;

		// Exit if cell is occupied
		if (order.getContentsType() != Cell.CONTENTS_EMPTY)
			return false;

		// Exit if cell is not in the placement area
		if (!pointInsideArea(order, placementArea[owner.getBattleId()]))
			return false;

		Unit unit = selected.getUnit();

		// Exit if selected unit is not owned by this cardMaster
		if (owner != unit.getOwner())
			return false;

		unit.setPosition(order);
		incTurn();

		return true;
	}

	public synchronized Unit handlePickOrder(CardMaster cardMaster, CardItem card, UnitData unitData){
		// Exit if board is not in the picking stage
		if (state != STATE_WAIT_FOR_PICK)
			return null;

		// Exit if order initiator is not picking now
		if (cardMaster != currentTurning)
			return null;

		if (hasPickedCard(cardMaster, card))
			return null;

		int unitAmount = getUnitAmount(cardMaster);
		if (unitAmount == OWNED_UNITS)
			return null;

		Rectangle area = placementArea[cardMaster.getBattleId()];

		for(int y = area.y; y < area.y + area.height; y++)
			for(int x = area.x; x < area.x + area.width; x++){
				if (getCell(x, y).getContentsType() == Cell.CONTENTS_EMPTY){

					addPickedCard(cardMaster, card);

					Unit unit = new UnitImpl(cardMaster, unitData, getCell(x, y));
					// Switch board state if pick stage has ended
					if (units.size() == cardMasters.size() * OWNED_UNITS){
						setState(STATE_WAIT_FOR_PLACEMENT);
					}

					incTurn();
					nextTurn();
					return unit;
				}
			}
		return null;
	}

	public synchronized boolean handleCastOrder(CardMaster owner, Cell selected, Cell target, int spell){
		if (selected == null || target == null)
			return false;

		// Exit if board is not in idle state
		if (state != STATE_WAIT_FOR_ORDER)
			return false;

		// Exit if another cardMaster is making a turn now
		if (currentTurning != owner)
			return false;

		// Exit if selected cell doesn't contain unit
		if (selected.getContentsType() != Cell.CONTENTS_UNIT)
			return false;

		Unit unit = selected.getUnit();

		// Exit if selected unit is not owned by this cardMaster
		if (owner != unit.getOwner())
			return false;

		// Exit if unit is dead or resting
		if (unit.getState() == Unit.STATE_DEAD || unit.getState() == Unit.STATE_REST)
			return false;

		// Check stun and silence status
		if (unit.controlApplied(ControlType.SILENCE) || unit.controlApplied(ControlType.STUN))
			return false;

		// Exit if cardMaster already has used another unit
		if (owner.getUsedUnit() != null && owner.getUsedUnit() != unit)
			return false;

		boolean result = selected.getUnit().castSpell(spell, target);
		if (result)
			incTurn();
		return result;
	}

	// Main function for handling attack/move orders
	public synchronized boolean handleSimpleOrder(CardMaster owner, Cell selected, Cell order){
		if (selected == null || order == null)
			return false;

		// Exit if board is not in idle state
		if (state != STATE_WAIT_FOR_ORDER)
			return false;

		// Exit if clicked cell is selected cell
		if (selected == order)
			return false;
		
		// Exit if another cardMaster is making a turn now
		if (currentTurning != owner)
			return false;
		
		// Exit if selected cell doesn't contain unit
		if (selected.getContentsType() != Cell.CONTENTS_UNIT)
			return false;
		
		Unit unit = selected.getUnit();

		// Exit if selected unit is not owned by this cardMaster
		if (owner != unit.getOwner())
			return false;

		// Exit if unit is dead or resting
		if (unit.getState() == Unit.STATE_DEAD || unit.getState() == Unit.STATE_REST)
			return false;

		// Check if unit is stunned
		if (unit.controlApplied(ControlType.STUN))
			return false;

		// Exit if cardMaster already has used another unit
		if (owner.getUsedUnit() != null && owner.getUsedUnit() != unit)
			return false;

		if (order.getContentsType() == Cell.CONTENTS_UNIT){
			return handleAttack(unit, order.getUnit());
		} else if (order.getContentsType() == Cell.CONTENTS_EMPTY){
			return handleMove(unit, order);
		}
		return true;
	}

	public boolean handleCastCardSpellOrder(CardMaster caster, int cardId){
		// Exit if board is not in idle state
		if (state != STATE_WAIT_FOR_ORDER)
			return false;

		// Exit if another cardMaster is making a turn now
		if (currentTurning != caster)
			return false;

		// Can't cast card spells after moving units
		if (caster.getUsedUnit() != null)
			return false;

		SpellCardItem spellCard = caster.getInventory().findById(cardId, SpellCardItem.class);

		if (spellCard == null)
			return false;

		if (hasCastCard(caster, spellCard))
			return false;

		CardSpellData data = Program.getInstance().getCardSpellDataById(spellCard.getSpellId());

		if (data == null)
			return false;

		CardSpell spell = new CardSpell(data, caster, this);
		spell.invokeScript();

		addCastCard(caster, spellCard);
		incTurn();

		return true;
	}

	private boolean handleAttack(Unit attacker, Unit target){
		if (attacker.controlApplied(ControlType.DISARM))
			return false;

		Object result = attacker.callEvent(Unit.SCRIPT_EVENT_CHECK_ATTACK, target, attacker.getPosition());
		if ((Boolean) result){
			attacker.getOwner().setUsedUnit(attacker);
			attacker.callEvent(Unit.SCRIPT_EVENT_PERFORM_ATTACK, target, attacker.getTotalAttackDamage());
			attacker.callEvent(Unit.SCRIPT_EVENT_ATTACK_END, target);
			incTurn();
		}
		return (Boolean) result;
	}
	
	private boolean handleMove(Unit unit, Cell target){
		if (unit.controlApplied(ControlType.ROOT))
			return false;

		Path path = new Path(unit.getPosition(), target, unit.getCurrentActionPoints());
		if (path.find()){
			unit.setCurrentActionPoints(unit.getCurrentActionPoints() - (path.getLength() - 1));
			unit.getOwner().setUsedUnit(unit);

			unit.callEvent(Unit.SCRIPT_EVENT_WALK_START);
			unit.setPosition(target);
			unit.callEvent(Unit.SCRIPT_EVENT_WALK_END);
			incTurn();
			return true;
		}
		return false;
	}

	private void autoSkipTurnCheck(){
		boolean skip = units.size() > 0;

		for (Unit u: units){
			if (u.getState() == Unit.STATE_WAIT){
				skip = false;
				break;
			}
		}

		if (skip)
			nextTurn();
	}

	public void unitDies(Unit unit){
		Alliance alliance = cardMasterAllianceMap.get(unit.getOwner());
		if (alliance.killUnit())
			alliances.remove(alliance);
	}

	public void nextTurn(){
		timeRemaining = turnTime;
		TurnManager.getInstance().time(this, timeRemaining);

		if (currentTurning.getUsedUnit() != null)
			currentTurning.getUsedUnit().setState(Unit.STATE_REST);

		for(Iterator<Buff> iterator = buffs.iterator(); iterator.hasNext(); ){
			Buff buff = iterator.next();
			buff.update();
			if (buff.hasEnded())
				iterator.remove();
		}

		endTurnForCardMaster(currentTurning);
		selectTurningPlayer();

		if (state != STATE_WAIT_FOR_PICK)
			for (Unit u: units){
				u.calculateTurnParameters();
				u.callEvent(Unit.SCRIPT_EVENT_TURN_END);
			}

		// Check if turn can be skipped once more
		// TODO add some delay there
		autoSkipTurnCheck();
	}

	public void checkGameOver(){
		// Checking if game is over
		if (alliances.size() <= 1){
			incTurn();
			if (alliances.size() == 1)
				TurnManager.getInstance().gameOver(this, GAME_OVER_WIN, alliances.get(0));
			else
				TurnManager.getInstance().gameOver(this, GAME_OVER_DRAW, null);
			setState(STATE_GAME_IS_OVER);

			Program.getInstance().getChatController().destroyChannel(chat.getId());
		}
	}

	public void setTimeRemaining(float timeRemaining) {
		this.timeRemaining = timeRemaining;
	}

	public void setTurnTime(float turnTime) {
		this.turnTime = turnTime;
		timeRemaining = turnTime;
	}

	public void update(float tpf){
		if (timeRemaining > 0f && state != STATE_GAME_IS_OVER){
			timeRemaining -= tpf;

			if (timeRemaining <= 0f)
				switch (state){
					case STATE_WAIT_FOR_PLACEMENT:
						for (CardMaster cardMaster: cardMasters)
							playerFinishedPlacement(cardMaster);
						break;
					case STATE_WAIT_FOR_ORDER:
						TurnManager.getInstance().skip(this, currentTurning);
						break;
					case STATE_WAIT_FOR_PICK:
						// Picking random card
						List<CardItem> cards = currentTurning.getInventory().filter(CardItem.class, new TypeFilter(ItemTypes.CARD));

						if (pickedCards.get(currentTurning) != null)
							cards.removeAll(pickedCards.get(currentTurning));

						int random = (int) (Math.random() * cards.size());
						CardItem card = cards.get(random);

						TurnManager.getInstance().pick(this, currentTurning, card.getId());
						break;
				}
		}
	}

	public int computeHash(){
		int result = 0;

		for (Unit u: units){
			result += u.getCurrentHealth()
					+ u.getState()
					+ u.getAttackDamage()
					+ u.getOwner().getBattleId()
					+ u.getCurrentActionPoints()
					+ u.getUnitData().getId()
					+ u.getPosition().getX()
					+ u.getPosition().getY()
					+ u.getBonusAttackDamage()
					+ u.getDamageDealt()
					+ u.getHealDone();

			for (Spell spell: u.getSpells())
				result += spell.getCoolDownLeft() +
						  spell.getSpellData().getId().hashCode();
		}

		result += currentTurning.getBattleId();

		return result;
	}

	private int incTurn(){
		return turnNumber.incrementAndGet();
	}
}
