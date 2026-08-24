/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.internal;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import vazkii.psi.compampac.BlockPosCompat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 3D Vector class for Psi spell calculations.
 * Backported to 1.7.10 from 1.21.1.
 * 
 * This is a mutable vector class - operations modify the vector in place
 * and return 'this' for chaining.
 */
public class Vector3 {
	public static final Vector3 zero = new Vector3();
	
	public double x;
	public double y;
	public double z;

	/**
	 * Creates a zero vector (0, 0, 0)
	 */
	public Vector3() {
		this(0, 0, 0);
	}

	/**
	 * Creates a vector with the specified coordinates
	 */
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Copy constructor
	 */
	public Vector3(Vector3 vec) {
		this(vec.x, vec.y, vec.z);
	}

	/**
	 * Creates a vector from a Minecraft Vec3
	 */
	public Vector3(Vec3 vec) {
		this(vec.xCoord, vec.yCoord, vec.zCoord);
	}

	/**
	 * Creates a vector from an entity's position
	 */
	public static Vector3 fromEntity(Entity e) {
		return new Vector3(e.posX, e.posY, e.posZ);
	}

	/**
	 * Creates a vector from a BlockPos-like coordinate
	 */
	public static Vector3 fromBlockPos(BlockPosCompat pos) {
		return new Vector3(pos.x, pos.y, pos.z);
	}

	/**
	 * Creates a vector from a Minecraft Vec3
	 */
	public static Vector3 fromVec3(Vec3 vec3) {
		return new Vector3(vec3.xCoord, vec3.yCoord, vec3.zCoord);
	}

	/**
	 * Creates a copy of this vector
	 */
	public Vector3 copy() {
		return new Vector3(this);
	}

	/**
	 * Sets this vector's components
	 */
	public Vector3 set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	/**
	 * Copies another vector's components into this vector
	 */
	public Vector3 set(Vector3 vec) {
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
		return this;
	}

	/**
	 * Calculates the dot product with another vector
	 */
	public double dotProduct(Vector3 vec) {
		double d = vec.x * x + vec.y * y + vec.z * z;

		// Clamp to [-1, 1] range for floating point errors
		if(d > 1 && d < 1.00001) {
			d = 1;
		} else if(d < -1 && d > -1.00001) {
			d = -1;
		}
		return d;
	}

	/**
	 * Sets this vector to the cross product of this × vec
	 * WARNING: Modifies this vector!
	 */
	public Vector3 crossProduct(Vector3 vec) {
		double newX = y * vec.z - z * vec.y;
		double newY = z * vec.x - x * vec.z;
		double newZ = x * vec.y - y * vec.x;
		x = newX;
		y = newY;
		z = newZ;
		return this;
	}

	/**
	 * Adds components to this vector
	 */
	public Vector3 add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	/**
	 * Adds another vector to this vector
	 */
	public Vector3 add(Vector3 vec) {
		return add(vec.x, vec.y, vec.z);
	}

	/**
	 * Subtracts another vector from this vector (alias for subtract)
	 */
	public Vector3 sub(Vector3 vec) {
		return subtract(vec);
	}

	/**
	 * Subtracts another vector from this vector
	 */
	public Vector3 subtract(Vector3 vec) {
		x -= vec.x;
		y -= vec.y;
		z -= vec.z;
		return this;
	}

	/**
	 * Multiplies this vector by a scalar
	 */
	public Vector3 multiply(double scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}

	/**
	 * Calculates the magnitude (length) of this vector
	 */
	public double mag() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * Calculates the squared magnitude of this vector (faster than mag())
	 */
	public double magSquared() {
		return x * x + y * y + z * z;
	}

	/**
	 * Normalizes this vector to unit length (magnitude 1)
	 */
	public Vector3 normalize() {
		double d = mag();
		if(d != 0) {
			multiply(1.0 / d);
		}
		return this;
	}

	/**
	 * Calculates the distance to another vector
	 */
	public double distanceTo(Vector3 other) {
		double dx = x - other.x;
		double dy = y - other.y;
		double dz = z - other.z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Calculates the squared distance to another vector (faster than distanceTo)
	 */
	public double distanceSquaredTo(Vector3 other) {
		double dx = x - other.x;
		double dy = y - other.y;
		double dz = z - other.z;
		return dx * dx + dy * dy + dz * dz;
	}

	@Override
	public String toString() {
		MathContext cont = new MathContext(5, RoundingMode.HALF_UP);
		return "Vector[" 
			+ new BigDecimal(x, cont) + ", " 
			+ new BigDecimal(y, cont) + ", " 
			+ new BigDecimal(z, cont) + "]";
	}

	/**
	 * Converts this vector to a Minecraft Vec3
	 */
	public Vec3 toVec3() {
		return Vec3.createVectorHelper(x, y, z);
	}

	/**
	 * Converts this vector to a BlockPos, rounding coordinates
	 */
	public BlockPosCompat toBlockPos() {
		return new BlockPosCompat(
			(int) Math.round(x),
			(int) Math.round(y),
			(int) Math.round(z)
		);
	}

	/**
	 * Checks if this vector is zero (all components are 0)
	 */
	public boolean isZero() {
		return x == 0 && y == 0 && z == 0;
	}

	/**
	 * Checks if this vector is axial (only one component is non-zero)
	 */
	public boolean isAxial() {
		return x == 0 ? y == 0 || z == 0 : y == 0 && z == 0;
	}

	/**
	 * Negates all components of this vector
	 */
	public Vector3 negate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	/**
	 * Projects this vector onto another vector
	 */
	public Vector3 project(Vector3 b) {
		double l = b.magSquared();
		if(l == 0) {
			set(0, 0, 0);
			return this;
		}

		double m = dotProduct(b) / l;
		set(b).multiply(m);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Vector3)) {
			return false;
		}
		Vector3 v = (Vector3) o;
		return x == v.x && y == v.y && z == v.z;
	}

	@Override
	public int hashCode() {
		long xBits = Double.doubleToLongBits(x);
		long yBits = Double.doubleToLongBits(y);
		long zBits = Double.doubleToLongBits(z);
		
		int result = (int) (xBits ^ (xBits >>> 32));
		result = 31 * result + (int) (yBits ^ (yBits >>> 32));
		result = 31 * result + (int) (zBits ^ (zBits >>> 32));
		return result;
	}
}
