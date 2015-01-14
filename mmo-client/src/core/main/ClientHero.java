package core.main;

import com.jme3.math.Vector2f;
import program.main.Program;
import shared.map.CardMaster;
import shared.map.Hero;
import shared.other.Vector2;

import java.util.ArrayList;
import java.util.List;

public class ClientHero implements Hero {

	private float x, y;

	private Vector2f facing = new Vector2f(0, 1),
					 facingCurrent = new Vector2f(0, 1);
	private float facingStep = 0f;
	
	private int physicalSize = 16;
	
	private int order = ORDER_STOP;
	
	private Vector2 path[];
	private int currentPathTarget;
	
	private Program program;
	
	public ClientHero(){
		this.program = Program.getInstance();
	}
	
	@Override
	public void setPath(Vector2[] path){
		this.path = path;
		currentPathTarget = 0;
		facingStep = 0f;
		facing.set(facingCurrent);
	}
	
	@Override
	public int getCurrentPathTarget() {
		return currentPathTarget;
	}
	
	@Override
	public Vector2[] getPath() {
		return path;
	}
	
	@Override
	public float getX() {
		return x;
	}
	
	@Override
	public float getY() {
		return y;
	}
	
	@Override
	public void setOrder(int order) {
		this.order = order;
	}
	
	@Override
	public int getOrder() {
		return order;
	}
	
	@Override
	public void setX(float x) {
		this.x = x;
	}
	
	@Override
	public void setY(float y) {
		this.y = y;
	}

	@Override
	public Vector2 getFacing(){
		return new Vector2(facingCurrent.x, facingCurrent.y);
	}

	@Override
	public int getPhysicalSize() {
		return physicalSize;
	}
	
	@Override
	public void setPhysicalSize(int physicalSize) {
		this.physicalSize = physicalSize;
	}
	
	private boolean checkCircleLineIntersection(Vector2f lstart, Vector2f lend, Vector2f center, float radius) {
        float baX = lend.getX() - lstart.getX();
        float baY = lend.getY() - lstart.getY();
        float caX = center.getX() - lstart.getX();
        float caY = center.getY() - lstart.getY();

        float a = baX * baX + baY * baY;
        float bBy2 = baX * caX + baY * caY;
        float c = caX * caX + caY * caY - radius * radius;

        float pBy2 = bBy2 / a;
        float q = c / a;

        float disc = pBy2 * pBy2 - q;
        
        return disc >= 0;
    }
	
	private Vector2f tempVector = new Vector2f(),
					 tempVector2 = new Vector2f();
	
	/*private boolean checkIntersection(){
		// TODO No support for collisions with npcs lol
		// TODO Implement cluster search there
		List<GameClient> players = program.getVisibleRealPlayers();
		
		for(GameClient gc: players){
			Hero h = gc.getCardMaster().getHero();
			
			// Exit if we are checking ourself lol
			if (h == this)
				continue;
			
			float dist = (float) Point2D.distance(x, y, h.getX(), h.getY()) - physicalSize - h.getPhysicalSize();
			
			// If collision circles intersect, check if units is moving in the direction of collided unit
			if (dist <= 0){
				tempVector.set(x, y);
				tempVector2.set(path[path.length - 1]);
				
				tempVector.add(tempVector2.subtractLocal(tempVector).normalizeLocal().multLocal(physicalSize));
				
				tempVector2.set(h.getX(), h.getY());
				if (checkCircleLineIntersection(tempVector, path[currentPathTarget], tempVector2, h.getPhysicalSize()))
					return true;
			}
		}
		return false;
	}*/

    private boolean checkIntersection(float x, float y){
        List<ClientCardMaster> close = new ArrayList<ClientCardMaster>();

        for(ClientCardMaster player: program.getVisiblePlayers())
            close.add(player);

        for(ClientCardMaster cardMaster: close){
            ClientHero hero = cardMaster.getHero();

            if (hero == this)
                continue;

            tempVector.set(x - hero.getX(), y - hero.getY());
            if (tempVector.length() < hero.physicalSize + physicalSize){
                if (tempVector.length() == 0)
                    tempVector = Vector2f.UNIT_XY;
                tempVector.normalizeLocal();
                tempVector.multLocal(hero.physicalSize + physicalSize + 4);
                this.x = hero.x + tempVector.x;
                this.y = hero.y + tempVector.y;
                return true;
            }
        }

        return false;
    }

	@Override
	public void update() {
		switch(order){
			case ORDER_MOVE:{
				// TODO can be optimized by storing vx and vy

				// Vector between current position and order point
				float dx = path[currentPathTarget].getX() - x,
					  dy = path[currentPathTarget].getY() - y,
					  len = (float) Math.sqrt(dx * dx + dy * dy);
				
				if (len < 3){
					// Exit if order point is reached
					if (currentPathTarget == path.length - 1){
						order = ORDER_STOP;
						break;
					}

					facingStep = 0f;
					facing.set(facingCurrent);

					currentPathTarget++;
					break;
				}

				// Normalized vector
				float ex = dx / len,
					  ey = dy / len;

				// Update facing
				//facingCurrent.set(DataUtil.slerp(facing, new Vector2f(ex, ey), facingStep));
				facingCurrent.interpolate(facing, new Vector2f(ex, ey), facingStep);
				//facingCurrent.interpolate(facing, new Vector2f(ex, ey), facingStep);
				facingStep = Math.min(facingStep + 0.05f, 1f);

				// Final velocity
				float vx = ex * 3,
					  vy = ey * 3;

                //if (!checkIntersection(x + vx, y + vy)){
                    x += vx;
                    y += vy;
                //}
			}
		}
	}
}
