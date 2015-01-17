package core.board;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import core.graphics.SpecialEffect;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.ui.BattleController;
import program.main.Program;
import program.main.SceneUtil;
import shared.board.*;
import shared.board.data.BuffData;
import shared.items.types.CardItem;
import shared.map.CardMaster;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class ClientBoard implements Board {
	public static final int OWNED_UNITS = 3;

	public static final ColorRGBA PLAYER_COLORS[] = new ColorRGBA[]{
			ColorRGBA.Red,
			ColorRGBA.Blue,
			ColorRGBA.Cyan,
			ColorRGBA.Yellow,
			ColorRGBA.Orange
	};

	public static final float DEFAULT_FACING[] = {
		FastMath.HALF_PI,
		-FastMath.HALF_PI,
		FastMath.HALF_PI,
		-FastMath.HALF_PI
	};

	private ClientCell[] cells;

	private int id;
	private int width, height;
	
	private Rectangle[] placementArea;

	private int state = STATE_WAIT_FOR_PICK;

	private short turnNumber;

	private CardMaster currentTurning;

	private List<ClientUnit> units;
	private List<CardMaster> cardMasters;
	private List<CardMaster> cardMastersMadeTurn;

	private List<Buff> buffs;
	private List<Alliance> alliances;
	private Map<CardMaster, Alliance> cardMasterAllianceMap;

	private float timeRemaining;
	private float turnTime;

	private boolean placementFinishedLocal;

	public ClientBoard(int width, int height){
		units = new CopyOnWriteArrayList<ClientUnit>();
		cardMasters = new CopyOnWriteArrayList<CardMaster>();
		cardMastersMadeTurn = new CopyOnWriteArrayList<CardMaster>();
		buffs = new ArrayList<Buff>();
		alliances = new ArrayList<Alliance>();

		cardMasterAllianceMap = new HashMap<CardMaster, Alliance>();

		this.width = width;
		this.height = height;

		cells = new ClientCell[width * height];

		// Filling cell array with actual empty cells
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				cells[y * width + x] = new ClientCell(this, x, y);
	}

	public void setPlacementArea(Rectangle[] placementArea) {
		this.placementArea = placementArea;
	}

	public Rectangle[] getPlacementArea() {
		return placementArea;
	}

	// These are used by scripts

    @SuppressWarnings("unused")
    public Buff addBuff(String id, int timesToRepeat, int period, int initialDelay, Object data){
        BuffData buffData = Program.getInstance().getBuffScriptById(id);
        Buff buff = new ClientBuff(this, buffData, timesToRepeat, period, initialDelay, data);
        buffs.add(buff);
        return buff;
    }

    @SuppressWarnings("unused")
    public Buff addBuff(String id, int timesToRepeat, int period, Object data){
        return addBuff(id, timesToRepeat, period, period, data);
    }

    @SuppressWarnings("unused")
    public Buff addBuff(String id, int timesToRepeat, int period){
        return addBuff(id, timesToRepeat, period, period);
    }

	@SuppressWarnings("unused")
	/**
	 * Just a utility wrapper method to be used by scripts
	 */
	public void addEffect(String id, Object ... args){
		Program.getInstance().getMainFrame().addEffect(new SpecialEffect(id, args));
	}

	public void addEffect(String id, Unit unit){
		addEffect(id, SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(unit).getNode());
	}

	public Alliance getAllianceById(int id){
		for (Alliance alliance: alliances)
			if (id == alliance.getId())
				return alliance;
		return null;
	}

	public void finishPlacementLocal(){
		placementFinishedLocal = true;
	}

	public boolean isPlacementFinishedLocal(){
		return placementFinishedLocal;
	}

	public void finishPlacementPhase(){
		setState(STATE_WAIT_FOR_ORDER);
		nextTurn();
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

	public CardMaster getCurrentTurningPlayer(){
		return currentTurning;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
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
	}

	@Override
	public void playerFinishedPlacement(CardMaster cardMaster) {
		throw new NotImplementedException();
	}

	public void addUnit(Unit u) {
		units.add((ClientUnit) u);
	}

	public void removeUnit(Unit u){
		if (u instanceof ClientUnit)
			units.remove(u);
	}

	@Override
	public void skipTurn() {
		throw new NotImplementedException();
	}

	// This function is dangerous but fast, contains no checks
	public ClientCell getCell(int x, int y){
		return cells[y * width + x];
	}

	public ClientCell getCellChecked(int x, int y){
		if (x < 0 || x >= width || y < 0 || y >= height)
			return null;
		return cells[y * width + x];
	}

	public List<CardMaster> getCardMasters(){
		return cardMasters;
	}

	@Override
	public List<? extends CardItem> getPickedCards(CardMaster cardMaster) {
		throw new NotImplementedException();
	}

	public List<ClientUnit> getUnits(){
		return units;
	}

	public List<ClientUnit> getClientUnits() {
		return units;
	}

	private void endTurnForCardMaster(CardMaster cardMaster){
		cardMastersMadeTurn.add(cardMaster);
		cardMaster.setUsedUnit(null);
	}

	private void autoSkipTurnCheck(){
		boolean skip = units.size() > 0;

		for (Unit u: units){
			if (u.getState() == STATE_WAIT_FOR_ORDER){
				skip = false;
				break;
			}
		}

		if (skip)
			nextTurn();
	}

	public synchronized void nextTurn(){
		if (currentTurning.getUsedUnit() != null)
			currentTurning.getUsedUnit().setState(Unit.STATE_REST);

		// The following loop is used to avoid concurrent modification exception,
		// since buffs might be added in the process of updating
		// noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < buffs.size(); i++) {
			Buff buff = buffs.get(i);
			buff.update();
		}

		for(Iterator<Buff> iterator = buffs.iterator(); iterator.hasNext(); ){
			Buff buff = iterator.next();
			if (buff.hasEnded()) {
				buff.end();
				iterator.remove();
			}
		}

		endTurnForCardMaster(currentTurning);
		selectTurningPlayer();

		List<TurnResults> results = new ArrayList<TurnResults>();

		if (state != STATE_WAIT_FOR_PICK)
			for (ClientUnit u: units){
				results.add(u.getTurnResults());

				u.calculateTurnParameters();
				u.resetWavePath();
				u.callEvent(Unit.SCRIPT_EVENT_TURN_END);
			}

		BattleController controller = Program.getInstance().getBattleController();
		controller.onNextTurn(controller.getBattleState(id), results);

		// Check if turn can be skipped once more
		// TODO add some delay there
		autoSkipTurnCheck();

		turnNumber++;
	}

	public void setTurnTime(float turnTime) {
		this.turnTime = turnTime;
		timeRemaining = turnTime;
	}

	public float getTimeRemaining() {
		return timeRemaining;
	}

	public void setTimeRemaining(float timeRemaining) {
		this.timeRemaining = timeRemaining;
	}

	public void update(float tpf){
		if (timeRemaining > 0f){
			timeRemaining -= tpf;

			if (timeRemaining < 0f)
				timeRemaining = 0f;
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
		}

		result += currentTurning.getBattleId();

		return result;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public short getTurnNumber() {
		return turnNumber;
	}
}
