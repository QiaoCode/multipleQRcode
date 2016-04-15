package com.example.orderlist;

//用来存放圆心坐标的类
public class CenterCoordinates {

	private int x;//圆心x坐标
	private int y;//圆心y坐标
	private float unit;//圆环宽度（内圆半径）

	public CenterCoordinates() {
		this.x = 0;
		this.y = 0;
		this.unit = 0.0f;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setUnit(float unit) {
		this.unit = unit;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public float getUnit() {
		return this.unit;
	}

}
