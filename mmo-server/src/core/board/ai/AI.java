package core.board.ai;

import core.board.ServerBoard;
import core.board.TurnManager;
import core.main.ServerCardMaster;
import shared.board.Cell;
import shared.board.Unit;
import shared.items.ItemTypes;
import shared.items.filters.TypeFilter;
import shared.items.types.CardItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author doc
 */
public class AI implements Runnable {
	private static final int MAX_AI_THREADS = 32;

	private static final float HEAT_COEFFICIENT = 0.25f;

	protected boolean calculatingHeatMaps;

	protected ServerCardMaster cardMaster;
	protected VirtualTurn turn;
	protected VirtualBoard virtualBoard;
	protected ServerBoard realBoard;

	private List<VirtualTurn> allTurns;

	private boolean working = false;

	private static ExecutorService threadPool = Executors.newFixedThreadPool(MAX_AI_THREADS);

	public AI(ServerCardMaster cardMaster) {
		this.cardMaster = cardMaster;
	}

	public void setBoard(ServerBoard board){
		realBoard = board;
		virtualBoard = new VirtualBoard(this, board.getWidth(), board.getHeight());

		//createFrame();
	}

	private void checkBuffs(){
		int times = 0;
		while(times < 16 && virtualBoard.buffs.size() != 0){
			virtualBoard.nextTurn();
			times++;
		}
	}

	private Map<VirtualCell, Float> calculateHeatMap(VirtualUnit unit){
		Map<VirtualCell, Float> heatMap = new HashMap<VirtualCell, Float>();

		virtualBoard.snapshot(realBoard);

		unit.setState(Unit.STATE_WAIT);
		unit.setCurrentActionPoints(unit.getMaxActionPoints());

		unit.importance = 0;

		for (VirtualCell cell: virtualBoard.cells) {
			if (cell.getContentsType() != Cell.CONTENTS_EMPTY){
				heatMap.put(cell, 0f);
				continue;
			}

			float weight = 0;

			unit.setPosition(cell);

			for (VirtualUnit enemy : virtualBoard.units) {
				if (realBoard.areAllies(enemy.getOwner(), cardMaster))
					continue;

				boolean canBeAttacked = (Boolean) enemy.callFunction("onCheckAttack", enemy, virtualBoard, unit, enemy.getPosition());

				if (!canBeAttacked)
					weight += enemy.getTotalAttackDamage();

				boolean canAttack = (Boolean) unit.callFunction("onCheckAttack", unit, virtualBoard, enemy, cell);

				if (canAttack)
					weight += unit.getTotalAttackDamage() * 1.25f;
			}

			heatMap.put(cell, weight);
		}

		/*int turns = 0;

		for (VirtualCell cell: virtualBoard.cells){
			float weight = 0;

			turn = new VirtualTurn(virtualBoard, unit);
			turn.setTarget(cell);
			turn.setType(VirtualTurn.TURN_MOVE);

			unit.move(cell);
			checkBuffs();

			weight += turn.getWeight();
			turns++;

			virtualBoard.snapshot(realBoard);

			unit.move(cell);
			unit.setState(Unit.STATE_WAIT);
			unit.setCurrentActionPoints(unit.getMaxActionPoints());

			for(VirtualUnit target: virtualBoard.units){
				if (target.getState() == Unit.STATE_DEAD)
					continue;

				if (target != unit && unit.attack(target)){
					checkBuffs();

					weight += turn.getWeight();
					turns++;

					virtualBoard.snapshot(realBoard);
					unit.move(cell);
					unit.setState(Unit.STATE_WAIT);
					unit.setCurrentActionPoints(unit.getMaxActionPoints());
				}
			}

			for (VirtualCell castTo: virtualBoard.cells){
				for (VirtualSpell spell: unit.spells){
					if (unit.cast(spell, castTo)){
						checkBuffs();

						weight += turn.getWeight();
						turns++;

						virtualBoard.snapshot(realBoard);
						unit.move(cell);
						unit.setState(Unit.STATE_WAIT);
						unit.setCurrentActionPoints(unit.getMaxActionPoints());
					}
				}
			}

			unit.importance += weight;
			heatMap.put(cell, weight);
		}

		unit.importance /= turns;*/

		return heatMap;
	}

	private float weightFromHeatMap(Map<VirtualCell, Float> heatMap, VirtualCell cell, int actionPoints){
		Wave heatWave = new Wave(virtualBoard, cell, actionPoints * 4); // TODO wave cache/optimization
		heatWave.calculate();
		float weight = 0f;

		for(Map.Entry<VirtualCell, Float> entry: heatMap.entrySet()){
			int length = heatWave.getPathing(entry.getKey());

			if (length > 0) {
				int turnsNeeded = Math.max((int) Math.ceil(length / (actionPoints - 1)), 1);
				weight += entry.getValue() / turnsNeeded;
			}
		}

		return heatMap.get(cell) + weight * HEAT_COEFFICIENT;
	}

	private void guessAllTurns(VirtualUnit unit, Map<VirtualCell, Float> heatMap){
		Wave wave = unit.getWave();

		for(VirtualCell cell: wave.allAvailableCells()){
			float heatMapWeight = weightFromHeatMap(heatMap, cell, unit.getMaxActionPoints());

			virtualBoard.snapshot(realBoard);

			turn = new VirtualTurn(virtualBoard, unit);
			turn.setTarget(cell);
			turn.setType(VirtualTurn.TURN_MOVE);
			turn.modWeight(heatMapWeight);

			unit.move(cell);
			checkBuffs();

			allTurns.add(turn);
			virtualBoard.snapshot(realBoard);

			unit.move(cell);

			for(VirtualUnit target: virtualBoard.units){
				if (target.getState() == Unit.STATE_DEAD)
					continue;

				if (target != unit){
					turn = new VirtualTurn(virtualBoard, unit);
					turn.setMoveToExecute(cell);
					turn.setTarget(target.getPosition());
					turn.setType(VirtualTurn.TURN_ATTACK);
					turn.modWeight(heatMapWeight);

					if (unit.attack(target)){
						checkBuffs();
						allTurns.add(turn);
						virtualBoard.snapshot(realBoard);
						unit.move(cell);
					}
				}
			}

			for (VirtualCell castTo: virtualBoard.cells){
				for (VirtualSpell spell: unit.spells){
					turn = new VirtualTurn(virtualBoard, unit);
					turn.setMoveToExecute(cell);
					turn.setTarget(castTo);
					turn.setType(VirtualTurn.TURN_CAST);
					turn.setSpell(spell);
					turn.modWeight(heatMapWeight);

					if (unit.cast(spell, castTo)){
						checkBuffs();
						allTurns.add(turn);
						virtualBoard.snapshot(realBoard);
						unit.move(cell);
					}
				}
			}
		}
	}

	private void decideTurn(){
		virtualBoard.snapshot(realBoard);

		for (VirtualUnit unit: virtualBoard.units)
			unit.calculateWave();

		calculatingHeatMaps = true;
		Map<VirtualUnit, Map<VirtualCell, Float>> unitHeatMaps = new HashMap<VirtualUnit, Map<VirtualCell, Float>>();
		for (VirtualUnit unit: virtualBoard.units){
			Map<VirtualCell, Float> heatMap = calculateHeatMap(unit); // It also calculates importance
			unitHeatMaps.put(unit, heatMap);
		}

		//setHeatMaps(unitHeatMaps);

		calculatingHeatMaps = false;

		allTurns = new LinkedList<VirtualTurn>();
		for (VirtualUnit unit: virtualBoard.units)
			if (unit.getOwner() == cardMaster && unit.getUnit().getState() == Unit.STATE_WAIT)
				guessAllTurns(unit, unitHeatMaps.get(unit));

		virtualBoard.snapshot(realBoard);

		Collections.sort(allTurns);

		working = false;

		if (allTurns.size() != 0){
			VirtualTurn executingTurn = allTurns.get(0);
			if (!executingTurn.execute() || executingTurn.getType() == VirtualTurn.TURN_MOVE)
				TurnManager.getInstance().skip(realBoard, cardMaster);
		} else {
			TurnManager.getInstance().skip(realBoard, cardMaster);
		}
	}

	public void run(){
		decideTurn();
	}

	public void makeTurn(){
		if (working)
			return;
		working = true;
		threadPool.execute(this);
	}

	public void pickCard(){
		List<CardItem> cards = cardMaster.getInventory().filter(CardItem.class, new TypeFilter(ItemTypes.CREATURE_CARD));
		if (realBoard.getPickedCards(cardMaster) != null)
			cards.removeAll(realBoard.getPickedCards(cardMaster));

		TurnManager.getInstance().pick(realBoard, cardMaster, cards.get(0).getId());
	}

	public void doPlacement(){
		realBoard.playerFinishedPlacement(cardMaster);
	}

	private JFrame frame;
	private JMenu unitMenu;
	private TestPanel panel;
	private Map<VirtualUnit, Map<VirtualCell, Float>> maps;

	private String[] names = new String[] {
			"ang",
			"mino",
			"kob",
			"eye",
			"liz"
	};

	private class TestPanel extends JPanel {
		public static final int CELL = 64;
		private VirtualUnit unit;
		private Map<VirtualCell, Float> heatMap;

		public TestPanel(){
			setSize(new java.awt.Dimension(realBoard.getWidth() * CELL, realBoard.getHeight() * CELL));
		}

		private void drawXCenteredString(Graphics g, String text, int x, int y, int width){
			int textWidth = g.getFontMetrics().stringWidth(text);

			g.drawString(text, x * width + (width - textWidth) / 2, y);
		}

		@Override
		public void paint(Graphics g){
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());

			if (heatMap != null) {
				float maxValue = Float.MIN_VALUE;

				for (VirtualCell cell: unit.getWave().allAvailableCells())
					maxValue = Math.max(weightFromHeatMap(heatMap, cell, unit.getMaxActionPoints()), maxValue);

				for (VirtualCell cell: unit.getWave().allAvailableCells()) {
					float weight = weightFromHeatMap(heatMap, cell, unit.getMaxActionPoints());

					g.setColor(new Color(weight / maxValue, 0.3f, 0));
					g.fillRoundRect(cell.getX() * CELL, cell.getY() * CELL, CELL, CELL, 4, 4);
				}
			}

			g.setColor(Color.BLACK);

			for (int x = 0; x < realBoard.getWidth(); x++)
				for (int y = 0; y < realBoard.getHeight(); y++){
					g.drawRoundRect(x * CELL, y * CELL, CELL, CELL, 4, 4);
				}

			for (VirtualUnit unit: virtualBoard.units){
				int h = g.getFontMetrics().getHeight();

				int x = unit.getPosition().getX() * CELL;
				int y = unit.getPosition().getY() * CELL;

				drawXCenteredString(g, names[unit.getUnitData().getId()], x, y + h, CELL);
				drawXCenteredString(g, String.valueOf(unit.importance), x, y + h * 2, CELL);
				drawXCenteredString(g, String.valueOf(unit.getState()), x, y + h * 3, CELL);
			}

		}

		private void setHeatMap(VirtualUnit unit, Map<VirtualCell, Float> map){
			this.unit = unit;
			this.heatMap = map;

			repaint();
		}

	}

	private void createFrame(){
		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setTitle(cardMaster.getName() + " - AI DEBUG");
		frame.setSize(new java.awt.Dimension(400, 400));
		//frame.setResizable(false);
		frame.setVisible(true);

		JMenuBar bar = new JMenuBar();

		frame.setJMenuBar(bar);

		unitMenu = new JMenu("Unit");

		bar.add(unitMenu);

		panel = new TestPanel();

		/*JScrollPane pane = new JScrollPane(panel);
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);*/

		frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.getContentPane().add(panel);

		frame.repaint();
	}

	private void setHeatMaps(Map<VirtualUnit, Map<VirtualCell, Float>> maps){
		this.maps = maps;

		unitMenu.removeAll();

		for (Map.Entry<VirtualUnit, Map<VirtualCell, Float>> entry: maps.entrySet()){
			final VirtualUnit unit = entry.getKey();
			final Map<VirtualCell, Float> map = entry.getValue();

			JMenuItem item = new JMenuItem(names[unit.getUnitData().getId()] + " (" + unit.getUnitData().getId() + ")");

			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					panel.setHeatMap(unit, map);
				}
			});

			unitMenu.add(item);
		}
	}
}