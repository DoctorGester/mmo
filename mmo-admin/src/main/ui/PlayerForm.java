package main.ui;

import main.core.ItemTypes;
import main.core.Program;
import main.core.Stat;
import main.models.FactionModel;
import main.models.ItemModel;
import main.models.PlayerInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Vector;

/**
 * @author doc
 */
public class PlayerForm {
	private JRadioButton byNameRadioButton;
	private JRadioButton byIDRadioButton;
	private JTextField searchField;
	private JButton searchButton;
	private JTable statsTable;
	private JTable reputationTable;
	private JTable itemsTable;
	private JButton saveStatsButton;
	private JButton saveFractionsButton;
	private JButton addItemButton;
	private JButton saveItemsButton;
	private JPanel basePanel;
	private JButton deleteButton;

	public PlayerForm() {
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (byNameRadioButton.isSelected())
					Program.getInstance().getApi().requestPlayerInfo(searchField.getText());

				if (byIDRadioButton.isSelected())
					Program.getInstance().getApi().requestPlayerInfo(Integer.valueOf(searchField.getText()));
			}
		});
	}

	public JPanel getBasePanel() {
		return basePanel;
	}

	public JButton getSearchButton() {
		return searchButton;
	}

	public JTextField getSearchField() {
		return searchField;
	}

	public JTable getStatsTable() {
		return statsTable;
	}

	public JTable getItemsTable() {
		return itemsTable;
	}

	public JTable getReputationTable() {
		return reputationTable;
	}

	public void fillPlayerInfo(PlayerInfo playerInfo){
		fillStatsTable(playerInfo);
		fillItemsTable(playerInfo);
		fillReputationTable(playerInfo);
	}

	public void fillReputationTable(PlayerInfo playerInfo){
		Vector<String> columnNames = new Vector<String>();

		columnNames.add("ID");
		columnNames.add("Name");
		columnNames.add("Reputation");

		Vector<Vector<Object>> rows = new Vector<Vector<Object>>();

		for (Map.Entry<FactionModel, Integer> pair: playerInfo.getReputation().entrySet()){
			Vector<Object> row = new Vector<Object>();
			row.add(pair.getKey().getId());
			row.add(pair.getKey().getName());
			row.add(pair.getValue());

			rows.add(row);
		}


		Collections.sort(rows, new Comparator<Vector<Object>>() {
			@Override
			public int compare(Vector<Object> first, Vector<Object> second) {
				return Integer.compare((Integer) first.get(0), (Integer) second.get(0));
			}
		});

		reputationTable.getTableHeader().setReorderingAllowed(false);
		reputationTable.setModel(new DefaultTableModel(rows, columnNames){
			public boolean isCellEditable(int row, int column){
				return column == 2;
			}
		});
	}

	public void fillItemsTable(PlayerInfo playerInfo){
		Vector<String> columnNames = new Vector<String>();

		columnNames.add("ID");
		columnNames.add("Name");
		columnNames.add("Type");

		Vector<Vector<Object>> rows = new Vector<Vector<Object>>();

		for (ItemModel item: playerInfo.getInventory().getItems()){
			Vector<Object> row = new Vector<Object>();
			row.add(item.getId());
			row.add(item.getName());
			row.add(ItemTypes.NAME[item.getType()]);

			rows.add(row);
		}

		itemsTable.getTableHeader().setReorderingAllowed(false);
		itemsTable.setModel(new DefaultTableModel(rows, columnNames){
			public boolean isCellEditable(int row, int column){
				return column != 0;
			}
		});
	}

	private void fillStatsTable(PlayerInfo playerInfo) {
		Vector<String> columnNames = new Vector<String>();

		columnNames.add("Stat");
		columnNames.add("Value");

		Vector<Vector<Object>> rows = new Vector<Vector<Object>>();

		for (Map.Entry<Stat, Integer> pair: playerInfo.getStats().entrySet()){
			Vector<Object> row = new Vector<Object>();
			row.add(pair.getKey().getName());
			row.add(pair.getValue());

			rows.add(row);
		}

		statsTable.getTableHeader().setReorderingAllowed(false);
		statsTable.setModel(new DefaultTableModel(rows, columnNames){
			public boolean isCellEditable(int row, int column){
				return column == 1;
			}
		});
	}
}
