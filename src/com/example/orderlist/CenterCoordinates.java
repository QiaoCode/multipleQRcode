package com.example.orderlist;

//�������Բ���������
public class CenterCoordinates {

	private int x;//Բ��x����
	private int y;//Բ��y����
	private float unit;//Բ����ȣ���Բ�뾶��

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
