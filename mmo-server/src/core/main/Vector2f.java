package core.main;

public strictfp class Vector2f {
	public float x, y;

	public Vector2f() {
	}

	/**
	 * Create a new vector based on an angle
	 * 
	 * @param theta
	 *            The angle of the vector in degrees
	 */
	public Vector2f(double theta) {
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
	public Vector2f add(double theta) {
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
	public Vector2f sub(double theta) {
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

	public Vector2f(Vector2f other) {
		this(other.getX(), other.getY());
	}

	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void set(Vector2f other) {
		set(other.getX(), other.getY());
	}

	public float dot(Vector2f other) {
		return (x * other.getX()) + (y * other.getY());
	}
	
	public Vector2f set(float x, float y) {
		this.x = x;
		this.y = y;

		return this;
	}

	public Vector2f getPerpendicular() {
		return new Vector2f(-y, x);
	}

	public Vector2f negate() {
		return new Vector2f(-x, -y);
	}

	public Vector2f negateLocal() {
		x = -x;
		y = -y;
		return this;
	}

	public Vector2f add(Vector2f v){
		return new Vector2f(v.x + x, v.y + y);
	}

	public Vector2f addLocal(Vector2f v) {
		x += v.getX();
		y += v.getY();

		return this;
	}

	public Vector2f subLocal(Vector2f v) {
		x -= v.getX();
		y -= v.getY();

		return this;
	}

	public Vector2f multLocal(float a) {
		x *= a;
		y *= a;

		return this;
	}

	public Vector2f normalizeLocal() {
		float l = length();

		if (l == 0) {
			return this;
		}

		x /= l;
		y /= l;
		return this;
	}

	public Vector2f getNormal() {
		Vector2f cp = clone();
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
	public void projectOntoUnit(Vector2f b, Vector2f result) {
		float dp = b.dot(this);

		result.x = dp * b.getX();
		result.y = dp * b.getY();

	}

	public Vector2f clone() {
		return new Vector2f(x, y);
	}
	
	public String toString() {
		return "[Vector2f " + x + "," + y + " (" + length() + ")]";
	}

	public float distance(Vector2f other) {
		return (float) Math.sqrt(distanceSquared(other));
	}

	public float distanceSquared(Vector2f other) {
		float dx = other.getX() - getX();
		float dy = other.getY() - getY();

		return (dx * dx) + (dy * dy);
	}

	public boolean equals(Object other) {
		if (other instanceof Vector2f) {
			Vector2f o = ((Vector2f) other);
			return (o.x == x) && (o.y == y);
		}

		return false;
	}
}