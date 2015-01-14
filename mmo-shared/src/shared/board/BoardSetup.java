package shared.board;

import shared.other.DataUtil;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author doc
 */
public class BoardSetup {
	private int width, height;

	private List<Rectangle> placementAreas = new LinkedList<Rectangle>();
	private List<Integer[]> alliances = new LinkedList<Integer[]>();

	private int alliancesInnerLength = 0;
	private boolean shuffle;

	private float turnTime = 60f;

	public int getWidth() {
		return width;
	}

	public BoardSetup setWidth(int width) {
		this.width = width;
		return this;
	}

	public int getHeight() {
		return height;
	}

	public BoardSetup setHeight(int height) {
		this.height = height;
		return this;
	}

	public boolean isShuffle() {
		return shuffle;
	}

	public BoardSetup setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
		return this;
	}

	public BoardSetup addPlacementArea(Rectangle rectangle){
		placementAreas.add(rectangle);
		return this;
	}

	public BoardSetup addAlliance(Integer[] alliance){
		alliancesInnerLength += alliance.length;
		alliances.add(alliance);
		return this;
	}

	public Rectangle[] getPlacementAreas(){
		return placementAreas.toArray(new Rectangle[placementAreas.size()]);
	}

	public List<Integer[]> getAlliances() {
		return alliances;
	}

	public float getTurnTime() {
		return turnTime;
	}

	public BoardSetup setTurnTime(float turnTime) {
		this.turnTime = turnTime;
		return this;
	}

	/*
		Da format:
			1 byte for width
			1 byte for height
			1 byte for placementAreas.size() = n
			n times:
				1 byte for x
				1 byte for y
				1 byte for width
				1 byte for height

			1 byte for alliances.size() = m
			m times:
				1 byte for alliance(m).length = i
				i times:
					1 byte for player(i) battle id

			4 bytes for turnTime
	 */
	public BoardSetup fromBytes(DataInputStream stream) throws IOException{
		setWidth(stream.readByte());
		setHeight(stream.readByte());

		int amount = stream.readByte();

		for (byte b = 0; b < amount; b++){
			addPlacementArea(
				new Rectangle(
					stream.readByte(),
					stream.readByte(),
					stream.readByte(),
					stream.readByte()
				)
			);
		}

		int allianceAmount = stream.readByte();
		for(int i = 0; i < allianceAmount; i++){
			int idAmount = stream.readByte();
			Integer alliance[] = new Integer[idAmount];

			for (int j = 0; j < idAmount; j++){
				int id = stream.readByte();
				alliance[j] = id;
			}

			addAlliance(alliance);
		}

		setTurnTime(stream.readFloat());

		return this;
	}

	public byte[] toBytes() throws IOException{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		stream.write(width);
		stream.write(height);
		stream.write(placementAreas.size());

		for (Rectangle area: placementAreas){
			stream.write(area.x);
			stream.write(area.y);
			stream.write(area.width);
			stream.write(area.height);
		}

		stream.write(alliances.size());

		for (Integer[] alliance: alliances) {
			stream.write(alliance.length);

			for (Integer player: alliance)
				stream.write(player);
		}

		stream.write(DataUtil.floatToByte(turnTime));

		return stream.toByteArray();
	}


}
