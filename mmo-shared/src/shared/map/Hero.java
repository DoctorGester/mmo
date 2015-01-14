package shared.map;

import shared.other.Vector2;

/**
 * Created by Toaru Shoujo on 1/14/2015.
 */
public interface Hero {
	int ORDER_STOP = 0;
	int ORDER_MOVE = 1;
	int ORDER_ATTACK = 2;

	void setPath(Vector2[] path);

	int getCurrentPathTarget();

	Vector2[] getPath();

	float getX();

	float getY();

	void setOrder(int order);

	int getOrder();

	void setX(float x);

	void setY(float y);

	Vector2 getFacing();

	int getPhysicalSize();

	void setPhysicalSize(int physicalSize);

	void update();
}
