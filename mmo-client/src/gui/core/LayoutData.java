package gui.core;

import com.jme3.math.Vector2f;

public class LayoutData {
	protected LayoutData previous;

	public enum Align {
		LEFT,
		CENTER,
		RIGHT
	}

	public enum VAlign {
		TOP,
		CENTER,
		BOTTOM
	}

	public class CalculatedSize {
		public Vector2f elementSize;
		public Vector2f cellSize;
	}

	protected String id;
	protected int spanX = 1, spanY = 1, skipBefore = 0, skipAfter = 0;
	protected int wrap = 0;
	protected boolean fillX = false, fillY = false;

	protected int pixelWidth;
	protected int pixelHeight;
	protected float relativeWidth;
	protected float relativeHeight;

	protected int columnPixelWidth;
	protected int columnPixelHeight;
	protected float columnRelativeWidth;
	protected float columnRelativeHeight;

	protected Align align = Align.LEFT;
	protected VAlign valign = VAlign.CENTER;

	public LayoutData(String id){
		this.id = id;
	}

	public LayoutData e(String id){
		LayoutData result = new LayoutData(id);

		result.previous = this;

		return result;
	}

	public LayoutData c(String id){
		LayoutData result = new LayoutData(id);

		result.wrap = wrap;

		result.fillX = fillX;
		result.fillY = fillY;

		result.spanX = spanX;
		result.spanY = spanY;

		result.align = align;
		result.valign = valign;

		result.skipAfter = skipAfter;
		result.skipBefore = skipBefore;

		result.pixelWidth = pixelWidth;
		result.pixelHeight = pixelHeight;
		result.relativeWidth = relativeWidth;
		result.relativeHeight = relativeHeight;

		result.columnPixelWidth = columnPixelWidth;
		result.columnPixelHeight = columnPixelHeight;
		result.columnRelativeWidth = columnRelativeWidth;
		result.columnRelativeHeight = columnRelativeHeight;

		result.previous = this;

		return result;
	}

	public LayoutData align(Align align){
		this.align = align;
		return this;
	}

	public LayoutData valign(VAlign valign){
		this.valign = valign;
		return this;
	}

	public LayoutData fill(){
		return fillX().fillY();
	}

	public LayoutData fillX(){
		return fillX(true);
	}

	public LayoutData fillY(){
		return fillY(true);
	}

	public LayoutData fillX(boolean fill){
		fillX = fill;
		return this;
	}

	public LayoutData fillY(boolean fill){
		fillY = fill;
		return this;
	}

	public LayoutData w(int pixelWidth){
		this.pixelWidth = pixelWidth;
		return this;
	}

	public LayoutData h(int pixelHeight){
		this.pixelHeight = pixelHeight;
		return this;
	}

	public LayoutData w(float relativeWidth){
		this.relativeWidth = relativeWidth;
		return this;
	}

	public LayoutData h(float relativeHeight){
		this.relativeHeight = relativeHeight;
		return this;
	}

	// If not explicitly set will always use element size parameters

	public LayoutData cw(int columnPixelWidth){
		this.columnPixelWidth = columnPixelWidth;
		return this;
	}

	public LayoutData ch(int columnPixelHeight){
		this.columnPixelHeight = columnPixelHeight;
		return this;
	}

	public LayoutData cw(float columnRelativeWidth){
		this.columnRelativeWidth = columnRelativeWidth;
		return this;
	}

	public LayoutData ch(float columnRelativeHeight){
		this.columnRelativeHeight = columnRelativeHeight;
		return this;
	}

	public LayoutData skipAfter(){
		skipAfter++;
		return this;
	}

	public LayoutData skipBefore(){
		skipBefore++;
		return this;
	}

	public LayoutData skip(int skipBefore, int skipAfter){
		this.skipAfter = skipAfter;
		this.skipBefore = skipBefore;
		return this;
	}

	public LayoutData wrap(){
		return wrap(1);
	}

	public LayoutData wrap(int times){
		this.wrap = times;
		return this;
	}

	public LayoutData span(){
		return span(spanX + 1, spanY + 1);
	}

	public LayoutData span(int x, int y){
		return spanX(x).spanY(y);
	}

	public LayoutData spanX(int x){
		spanX = x;
		return this;
	}

	public LayoutData spanY(int y){
		spanY = y;
		return this;
	}

	public CalculatedSize calculateSize(Vector2f parent){
		CalculatedSize result = new CalculatedSize();

		float ew = pixelWidth;

		if (pixelWidth == 0)
			ew = parent.x * columnRelativeHeight;

		float eh = pixelHeight;

		if (pixelHeight == 0)
			eh = parent.y * relativeHeight;

		result.elementSize = new Vector2f(ew, eh);
		result.cellSize = new Vector2f(result.elementSize);

		if (columnRelativeWidth != 0f)
			result.cellSize.x = parent.x * columnRelativeWidth;

		if (columnRelativeHeight != 0f)
			result.cellSize.y = parent.y * columnRelativeHeight;

		if (columnPixelWidth != 0)
			result.cellSize.x = columnPixelWidth;

		if (columnPixelHeight != 0)
			result.cellSize.y = columnPixelHeight;

		result.elementSize.x = Math.min(result.elementSize.x, result.cellSize.x);
		result.elementSize.y = Math.min(result.elementSize.y, result.cellSize.y);

		result.cellSize.x /= spanX;
		result.cellSize.y /= spanY;

		return result;
	}
}
