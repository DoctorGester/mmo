package shared.other;

public strictfp class Vector2 {
	public float x, y;

	public Vector2() {
	}

	/**
	 * Create a new vector based on an angle
	 * 
	 * @param theta
	 *            The angle of the vector in degrees
	 */
	public Vector2(double theta) {
		x = 1;
		y = 0;
		setTheta(theta);
	}

	/**
	 * Calculate the components of the vectors based on a angle
	 * 
	 * @param theta
	 *            The angle to calculate the components from (in degrees)
	 */
	public void setTheta(double theta) {
		// Next lines are to prevent numbers like -1.8369701E-16
		// when working with negative numbers
		if ((theta < -360) || (theta > 360)) {
			theta = theta % 360;
		}
		if (theta < 0) {
			theta = 360 + theta;
		}
		double oldTheta = getTheta();
		if ((theta < -360) || (theta > 360)) {
			oldTheta = oldTheta % 360;
		}
		if (theta < 0) {
			oldTheta = 360 + oldTheta;
		}

		float len = length();
		x = len * (float) Math.cos(StrictMath.toRadians(theta));
		y = len * (float) Math.sin(StrictMath.toRadians(theta));
	}

	/**
	 * Adjust this vector by a given angle
	 * 
	 * @param theta
	 *            The angle to adjust the angle by (in degrees)
	 * @return This vector - useful for chaining operations
	 * 
	 */
	public Vector2 add(double theta) {
		setTheta(getTheta() + theta);

		return this;
	}

	/**
	 * Adjust this vector by a given angle
	 * 
	 * @param theta
	 *            The angle to adjust the angle by (in degrees)
	 * @return This vector - useful for chaining operations
	 */
	public Vector2 sub(double theta) {
		setTheta(getTheta() - theta);

		return this;
	}

	/**
	 * Get the angle this vector is at
	 * 
	 * @return The angle this vector is at (in degrees)
	 */
	public double getTheta() {
		double theta = StrictMath.toDegrees(StrictMath.atan2(y, x));
		if ((theta < -360) || (theta > 360)) {
			theta = theta % 360;
		}
		if (theta < 0) {
			theta = 360 + theta;
		}

		return theta;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public Vector2(Vector2 other) {
		this(other.getX(), other.getY());
	}

	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void set(Vector2 other) {
		set(other.getX(), other.getY());
	}

	public float dot(Vector2 other) {
		return (x * other.getX()) + (y * other.getY());
	}
	
	public Vector2 set(float x, float y) {
		this.x = x;
		this.y = y;

		return this;
	}

	public Vector2 getPerpendicular() {
		return new Vector2(-y, x);
	}

	public Vector2 negate() {
		return new Vector2(-x, -y);
	}

	public Vector2 negateLocal() {
		x = -x;
		y = -y;
		return this;
	}

	public Vector2 add(Vector2 v){
		return new Vector2(v.x + x, v.y + y);
	}

	public Vector2 addLocal(Vector2 v) {
		x += v.getX();
		y += v.getY();

		return this;
	}

	public Vector2 subLocal(Vector2 v) {
		x -= v.getX();
		y -= v.getY();

		return this;
	}

	public Vector2 multLocal(float a) {
		x *= a;
		y *= a;

		return this;
	}

	public Vector2 normalizeLocal() {
		float l = length();

		if (l == 0) {
			return this;
		}

		x /= l;
		y /= l;
		return this;
	}

	public Vector2 getNormal() {
		Vector2 cp = clone();
		cp.normalizeLocal();
		return cp;
	}

	public float lengthSquared() {
		return (x * x) + (y * y);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	/**
	 * Project this vector onto another
	 * 
	 * @param b The vector to project onto
	 * @param result The projected vector
	 */
	public void projectOntoUnit(Vector2 b, Vector2 result) {
		float dp = b.dot(this);

		result.x = dp * b.getX();
		result.y = dp * b.getY();

	}

	public Vector2 clone() {
		return new Vector2(x, y);
	}
	
	public String toString() {
		return "[Vector2f " + x + "," + y + " (" + length() + ")]";
	}

	public float distance(Vector2 other) {
		return (float) Math.sqrt(distanceSquared(other));
	}

	public float distanceSquared(Vector2 other) {
		float dx = other.getX() - getX();
		float dy = other.getY() - getY();

		return (dx * dx) + (dy * dy);
	}

	public boolean equals(Object other) {
		if (other instanceof Vector2) {
			Vector2 o = ((Vector2) other);
			return (o.x == x) && (o.y == y);
		}

		return false;
	}
}