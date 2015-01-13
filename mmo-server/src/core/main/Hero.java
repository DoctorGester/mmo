package core.main;

import program.main.Program;

import java.util.List;

public class Hero {
	public static final int ORDER_STOP = 0,
							ORDER_MOVE = 1,
							ORDER_ATTACK = 2;

	private float x, y;

	private int physicalSize = 16;
	
	private int order = ORDER_STOP;
	
	private Vector2f path[];
	private int currentPathTarget;
	
	private Program program;
    private CardMaster owner;

	private ClusterCell clusterPosition;
	
	public Hero(CardMaster owner){
		this.program = Program.getInstance();
        this.owner = owner;
	}

	public ClusterCell getClusterPosition() {
		return clusterPosition;
	}

	public void setClusterPosition(ClusterCell clusterPosition) {
		this.clusterPosition = clusterPosition;
	}

	public void setPath(Vector2f[] path){
		this.path = path;
		currentPathTarget = 0;
	}
	
	public int getCurrentPathTarget() {
		return currentPathTarget;
	}
	
	public Vector2f[] getPath() {
		return path;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	public int getOrder() {
		return order;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public void setY(float y) {
		this.y = y;
	}

	public int getPhysicalSize() {
		return physicalSize;
	}
	
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
		List<GameClient> players = program.getGameClients();
		
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
				
				tempVector.addLocal(tempVector2.subLocal(tempVector).normalizeLocal().multLocal(physicalSize));
				
				tempVector2.set(h.getX(), h.getY());
				if (checkCircleLineIntersection(tempVector, path[currentPathTarget], tempVector2, h.getPhysicalSize()))
					return true;
			}
		}
		return false;
	}*/

    private boolean checkIntersection(float x, float y){
        List<CardMaster> close = program.getClusterGrid().getHeroesInRadiusOf(owner, physicalSize * 2);
        for(CardMaster cardMaster: close){
            Hero hero = cardMaster.getHero();

            tempVector.set(x - hero.getX(), y - hero.getY());
            if (tempVector.length() < hero.physicalSize + physicalSize){
                tempVector.normalizeLocal();
                tempVector.multLocal(hero.physicalSize + physicalSize + 4);
                this.x = hero.x + tempVector.x;
                this.y = hero.y + tempVector.y;
                return true;
            }
        }
        return false;
    }
	
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

					currentPathTarget++;
					break;
				}
				float ex = dx / len,
					  ey = dy / len;
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
