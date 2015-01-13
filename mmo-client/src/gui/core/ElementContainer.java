package gui.core;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;

import java.util.*;

public class ElementContainer extends Element {
	private List<Element> elements = new ArrayList<Element>();
	private List<LayoutData> layout = new ArrayList<LayoutData>();
	private boolean equalColumnsWidth = false;

	public ElementContainer(Vector2f position, Vector2f size, Vector4f imageCords, Vector4f resizeBorders) {
		super(position, size, imageCords, resizeBorders);
	}

	public final void addElement(Element element) {
		elements.add(element);
		element.setElementParent(this);

		if (element.getScreen() == null && screen != null)
			element.initialize(screen);

		attachChild(element);
	}

	public void setLayout(LayoutData layoutData){
		layout.clear();
		layout.add(layoutData);

		LayoutData previous = layoutData.previous;

		while (previous != null){
			layout.add(previous);

			previous = previous.previous;
		}

		Collections.reverse(layout);

		layout();
	}

	public final List<Element> getElements() {
		return elements;
	}

	public void setEqualColumnsWidth(boolean equalColumnsWidth) {
		this.equalColumnsWidth = equalColumnsWidth;
	}

	@Override
	public void update(float tpf){
		for (Element element: getElements())
			element.update(tpf);
	}

	private Element getElementById(String id){
		for (Element element: getElements())
			if (element.getId() != null && element.getId().equals(id))
				return element;

		return null;
	}

	private boolean canFit(Cell[][] filledCells, int x, int y, int width, int height){
		int endX = x + width;
		int endY = y + height;

		for (; x < endX; x++)
			for (; y < endY; y++)
				if (filledCells[x][y] != null)
					return false;

		return true;
	}

	private GridData calculateGridSize(){
		GridData size = new GridData();

		int gridColumns = 0;
		int gridRows = layout.size() > 0 ? 1 : 0;

		int rowLength = 0;
		int additionalRows = 0;

		Map<Integer, Integer> additionalColumns = new HashMap<Integer, Integer>();

		for (LayoutData data: layout){
			rowLength += data.spanX + data.skipBefore + data.skipAfter;

			for (int row = 1; row < data.spanY; row++){
				Integer got = additionalColumns.get(gridRows + row - 1);

				additionalColumns.put(gridRows + row - 1, got == null ? data.spanX : got + data.spanX);
			}

			additionalRows = Math.max(additionalRows, data.spanY - 1);

			gridColumns = Math.max(gridColumns, rowLength);

			for (int i = 0; i < data.wrap; i++){
				Integer add = additionalColumns.get(gridRows - 1);
				gridColumns = Math.max(gridColumns, rowLength + (add == null ? 0 : add));
				additionalRows = Math.max(0, additionalRows - 1);

				rowLength = 0;
				gridRows++;
			}
		}

		gridRows += additionalRows;

		size.columns = gridColumns;
		size.rows = gridRows;

		return size;
	}

	private void calculateCellPositions(GridData grid){
		Cell[][] cells = new Cell[grid.columns][grid.rows]; // Indicates if cell is filled

		int startRow = 0;
		int startColumn = 0;

		for (LayoutData data: layout) {
			int column = startColumn;
			int row = startRow;

			while (cells[column][row] != null){
				column++;

				if (column == grid.columns){
					column = 0;
					row++;
				}
			}

			column += data.skipBefore;

			if (cells[column][row] != null)
				break;

			for (int i = 0; i < data.wrap; i++) {
				startRow++;
				startColumn = 0;
			}

			if (data.wrap == 0) {
				startColumn = column + data.skipAfter + 1;
			}

			for (int x = 0; x < data.spanX; x++)
				for (int y = 0; y < data.spanY; y++) {
					Cell cell = new Cell();
					cell.data = data;
					cell.subCell = x != 0 || y != 0;

					cells[column + x][row + y] = cell;
				}
		}

		for (int y = 0; y < grid.rows; y++){
			for (int x = 0; x < grid.columns; x++){
				System.out.print((cells[x][y] != null ? cells[x][y].data.id : "   ") + " ");
			}
			System.out.println();
		}

		grid.cells = cells;
	}

	private void calculateSizeData(GridData grid){
		Vector2f size = getSize();
		
		float rowHeight[] = new float[grid.rows];
		float columnWidth[] = new float[grid.columns];

		for (int column = 0; column < grid.columns; column++) {
			float maxWidth = 0f;

			for (int row = 0; row < grid.rows; row++) {
				Cell cell = grid.cells[column][row];

				if (cell != null)
					maxWidth = Math.max(maxWidth, cell.data.calculateSize(size).cellSize.x);
			}

			columnWidth[column] = maxWidth;
		}

		for (int row = 0; row < grid.rows; row++) {
			float maxHeight = 0f;

			for (int column = 0; column < grid.columns; column++) {
				Cell cell = grid.cells[column][row];

				if (cell != null)
					maxHeight = Math.max(maxHeight, cell.data.calculateSize(size).cellSize.y);
			}

			rowHeight[row] = maxHeight;
		}

		grid.rowHeight = rowHeight;
		grid.columnWidth = columnWidth;
	}
	
	private void placeElements(GridData grid){
		Vector2f size = getSize();

		float y = 0;
		for (int row = 0; row < grid.rows; row++) {

			float x = 0;
			for (int column = 0; column < grid.columns; x += grid.columnWidth[column], column++) {
				Cell cell = grid.cells[column][row];

				if (cell == null || cell.subCell)
					continue;

				LayoutData.CalculatedSize calculatedSize = cell.data.calculateSize(size);
				Element element = getElementById(cell.data.id);

				if (element == null)
					continue;

				Vector2f elementSize = calculatedSize.elementSize;

				if (cell.data.fillX) {
					elementSize.x = 0f;

					for (int i = 0; i < cell.data.spanX; i++)
						elementSize.x += grid.columnWidth[column + i];
				}

				if (cell.data.fillY)
					elementSize.y = grid.rowHeight[row];

				element.setPosition(V.f(x, y));
				element.setSize(calculatedSize.elementSize);
			}

			y += grid.rowHeight[row];
		}
	}

	private void layout(){
		GridData grid = calculateGridSize();
		
		calculateCellPositions(grid);
		calculateSizeData(grid);
		placeElements(grid);
	}

	private static class Cell {
		LayoutData data;
		boolean subCell;
	}

	private static class GridData {
		int columns, rows;
		Cell[][] cells;
		float rowHeight[];
		float columnWidth[];
	}
}
