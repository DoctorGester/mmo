package core.board;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import core.graphics.SpecialEffect;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.main.CardMaster;
import core.ui.BattleController;
import program.main.Program;
import program.main.Util;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class Board {
	public static final int OWNED_UNITS = 3;

	public static final ColorRGBA PLAYER_COLORS[] = new ColorRGBA[]{
			ColorRGBA.Red,
			ColorRGBA.Blue,
			ColorRGBA.Cyan,
			ColorRGBA.Yellow,
			ColorRGBA.Orange
	};

	public static final int STATE_WAIT_FOR_ORDER = 0x00,
							STATE_WAIT_FOR_PICK = 0x04,
							STATE_WAIT_FOR_PLACEMENT = 0x05;

	public static final int GAME_OVER_WIN = 0x00,
							GAME_OVER_DRAW = 0x01;

	public static final float DEFAULT_FACING[] = {
		FastMath.HALF_PI,
		-FastMath.HALF_PI,
		FastMath.HALF_PI,
		-FastMath.HALF_PI
	};

	private Cell[] cells;

	private int id;
	private int width, height;
	
	private Rectangle[] placementArea;

	private int state = STATE_WAIT_FOR_PICK;

	private int turnNumber;

	private CardMaster currentTurning;

	private List<Unit> units;
	private List<CardMaster> cardMasters;
	private List<CardMaster> cardMastersMadeTurn;

	private List<Buff> buffs;
	private List<Alliance> alliances;
	private Map<CardMaster, Alliance> cardMasterAllianceMap;

	private float timeRemaining;
	private float turnTime;

	private boolean placementFinishedLocal;

	public Board(int width, int height){
		units = new CopyOnWriteArrayList<Unit>();
		cardMasters = new CopyOnWriteArrayList<CardMaster>();
		cardMastersMadeTurn = new CopyOnWriteArrayList<CardMaster>();
		buffs = new ArrayList<Buff>();
		alliances = new ArrayList<Alliance>();

		cardMasterAllianceMap = new HashMap<CardMaster, Alliance>();

		this.width = width;
		this.height = height;

		cells = new Cell[width * height];

		// Filling cell array with actual empty cells
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				cells[y * width + x] = new Cell(this, x, y);
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
        Buff buff = new Buff(this, buffData, timesToRepeat, period, initialDelay, data);
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
		addEffect(id, Util.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(unit).getNode());
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

		for(Iterator<Buff> iterator = buffs.iterator(); iterator.hasNext(); ){
			Buff buff = iterator.next();
			buff.update();
			if (buff.hasEnded())
				iterator.remove();
		}

		endTurnForCardMaster(currentTurning);
		selectTurningPlayer();

		List<TurnResults> results = new ArrayList<TurnResults>();

		if (state != STATE_WAIT_FOR_PICK)
			for (Unit u: units){
				results.add(u.calculateTurnParameters());
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
}
