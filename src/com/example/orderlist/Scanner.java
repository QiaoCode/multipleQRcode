package com.example.orderlist;

import java.util.List;
import android.graphics.Bitmap;
import android.util.Log;

//扫描图片得到圆心坐标list
public class Scanner {

	/** Total width of image */
	protected int w;

	/** Total height of image */
	protected int h;

	/** Holds processed binary pixel data */
	protected int[] data;

	/** Candidate code count */
	protected int ccount;

	/** Number of candidates tested */
	protected int tcount;

	/** Maximum width of a TopCode unit in pixels */
	protected int maxu;// 这是一个可变值，后期可能需要改变

	/**
	 * Default constructor
	 */
	public Scanner() {
		this.w = 0;
		this.h = 0;
		this.data = null;
		this.ccount = 0;
		this.tcount = 0;
		this.maxu = 100;
	}

	/**
	 * 扫描照片，得到圆心坐标list,list中存放的是圆心坐标类
	 */
	public List<CenterCoordinates> scan(Bitmap image) {
		this.w = image.getWidth();
		this.h = image.getHeight();
		if (data == null || data.length < w * h) {
			this.data = new int[w * h];
		}
		image.getPixels(this.data, 0, w, 0, 0, w, h);

		// DELETE下一行
		Log.i("data", "num:" + data.length);
		threshold(); // run the adaptive threshold filter
		return findCodes(); // scan for topcodes
	}

	/**
	 * 返回照片的宽
	 */
	public int getImageWidth() {
		return this.w;
	}

	/**
	 * 返回照片的高
	 */
	public int getImageHeight() {
		return this.h;
	}

	/**
	 * 设置最大直径
	 */
	public void setMaxCodeDiameter(int diameter) {
		float f = diameter / 8.0f;
		this.maxu = (int) Math.ceil(f);
	}

	/**
	 * 返回候选点的数量
	 */
	protected int getCandidateCount() {
		return this.ccount;
	}

	/**
	 * 返回通过测试的点的数量
	 */
	protected int getTestedCount() {
		return this.tcount;
	}

	/**
	 * 得到像素点(x,y)的二值
	 */
	protected int getBW3x3(int x, int y) {
		if (x < 1 || x > w - 2 || y < 1 || y >= h - 2)
			return 0;
		int pixel, sum = 0;

		for (int j = y - 1; j <= y + 1; j++) {
			for (int i = x - 1; i <= x + 1; i++) {
				pixel = data[j * w + i];
				sum += ((pixel >> 24) & 0x01);
			}
		}
		return (sum >= 5) ? 1 : 0;
	}

	/**
	 * 扫描得到候选点的函数(count++&&候选点的标记)
	 */
	protected void threshold() {

		int pixel, r, g, b, a;
		int threshold, sum = 128;
		int s = 30;
		int k;
		int b1, w1, b2, level, dk;

		this.ccount = 0;

		for (int j = 0; j < h; j++) {
			level = b1 = b2 = w1 = 0;

			// ----------------------------------------
			// 左右一条龙式扫描
			// ----------------------------------------
			k = (j % 2 == 0) ? 0 : w - 1;
			k += (j * w);

			for (int i = 0; i < w; i++) {

				// ----------------------------------------
				// 得到k点的灰度值
				// ----------------------------------------
				pixel = data[k];
				r = (pixel >> 16) & 0xff;
				g = (pixel >> 8) & 0xff;
				b = pixel & 0xff;
				a = (r + g + b) / 3;

				// ----------------------------------------
				// Calculate sum as an approximate sum
				// of the last s pixels
				// ----------------------------------------
				sum += a - (sum / s);

				// ----------------------------------------
				// Factor in sum from the previous row
				// ----------------------------------------
				if (k >= w) {
					threshold = (sum + (data[k - w] & 0xffffff)) / (2 * s);
				} else {
					threshold = sum / s;
				}

				// ----------------------------------------
				// 得到k点的二值
				// ----------------------------------------
				double f = 0.85;
				f = 0.975;
				a = (a < threshold * f) ? 0 : 1;

				// ----------------------------------------
				// 通过a和sum重新生成data[k]
				// ----------------------------------------
				data[k] = (a << 24) + (sum & 0xffffff);

				switch (level) {

				// On a white region. No black pixels yet
				case 0:
					if (a == 0) { // First black encountered
						level = 1;
						b1 = 1;
						w1 = 0;
						b2 = 0;
					}
					break;

				// On first black region
				case 1:
					if (a == 0) {
						b1++;
					} else {
						level = 2;
						w1 = 1;
					}
					break;

				// On second white region (bulls-eye of a code?)
				case 2:
					if (a == 0) {
						level = 3;
						b2 = 1;
					} else {
						w1++;
					}
					break;

				// On second black region
				case 3:
					if (a == 0) {
						b2++;
					}
					// This could be a top code
					else {
						int mask;
						if (b1 >= 2
								&& b2 >= 2
								&& // less than 2 pixels... not interested
								b1 <= maxu && b2 <= maxu && w1 <= (maxu + maxu)
								&& Math.abs(b1 + b2 - w1) <= (b1 + b2)
								&& Math.abs(b1 + b2 - w1) <= w1
								&& Math.abs(b1 - b2) <= b1
								&& Math.abs(b1 - b2) <= b2) {
							mask = 0x2000000;

							dk = 1 + b2 + w1 / 2;
							if (j % 2 == 0) {
								dk = k - dk;
							} else {
								dk = k + dk;
							}

							data[dk - 1] |= mask;
							data[dk] |= mask;
							data[dk + 1] |= mask;
							ccount += 3; // count candidate codes
						}
						b1 = b2;
						w1 = 1;
						b2 = 0;
						level = 2;
					}
					break;
				}

				k += (j % 2 == 0) ? 1 : -1;
			}
		}
	}

	/**
	 * 扫描bitmap找到圆环保存到list<CenyerCoordinates>
	 */
	protected List<CenterCoordinates> findCodes() {
		this.tcount = 0;
		List<CenterCoordinates> list = new java.util.ArrayList<CenterCoordinates>();
		CenterCoordinates cootmp = new CenterCoordinates();

		int k = w * 2;
		for (int j = 2; j < h - 2; j++) { // j是纵坐标
			for (int i = 0; i < w; i++) { // i是横坐标
				if ((data[k] & 0x2000000) > 0) {
					if ((data[k - 1] & 0x2000000) > 0
							&& (data[k + 1] & 0x2000000) > 0
							&& (data[k - w] & 0x2000000) > 0
							&& (data[k + w] & 0x2000000) > 0) {

						// 判重
						if (!overlaps(list, i, j)) {
							this.tcount++;

							// 圆心校正
							int tmpx = i;// 用来保存当前圆心坐标
							int tmpy = j;

							int up = (ydist(tmpx, tmpy, -1)
									+ ydist(tmpx - 1, tmpy, -1) + ydist(
									tmpx + 1, tmpy, -1));

							int down = (ydist(tmpx, tmpy, 1)
									+ ydist(tmpx - 1, tmpy, 1) + ydist(
									tmpx + 1, tmpy, 1));

							int left = (xdist(tmpx, tmpy, -1)
									+ xdist(tmpx, tmpy - 1, -1) + xdist(tmpx,
									tmpy + 1, -1));

							int right = (xdist(tmpx, tmpy, 1)
									+ xdist(tmpx, tmpy - 1, 1) + xdist(tmpx,
									tmpy + 1, 1));

							float newx = tmpx / 1.0f + (right - left) / 6.0f;
							float newy = tmpy / 1.0f + (down - up) / 6.0f;

							// 校正之后的圆心
							tmpx = (int) Math.round(newx);
							tmpy = (int) Math.round(newy);
							// 得到该圆环unit
							float unit = readUnit(tmpx, tmpy);
							// 判断unit的合法性
							if (unit > 0 && unit < maxu) {
								// 判断此候选圆环的合法性
								if (isValid(tmpx, tmpy, unit)) {
									cootmp.setX(tmpx);
									cootmp.setY(tmpy);
									cootmp.setUnit(unit);

									list.add(cootmp);
									cootmp = new CenterCoordinates();
								}
							}
						}
					}
				}
				k++;
			}
		}

		// 除去list中的干扰项，unit过小或者过大
		float sum = 0.0f, average = 0.0f;
		for (int i = 0; i < list.size(); i++) {
			sum += list.get(i).getUnit();
		}
		average = sum / list.size();

		float tmpUnit = 0.0f;
		for (int i = 0; i < list.size();) {
			tmpUnit = list.get(i).getUnit();
			if (tmpUnit <= 0.75f * average || tmpUnit >= 1.25f * average)
			{
				list.remove(i);
			}
			else
			{
				i++;
			}
		}
		Log.i("flag", "count:" + list.size());
		return list;
	}

	/**
	 * 判重
	 */
	protected boolean overlaps(List<CenterCoordinates> list, int x, int y) {

		for (int i = 0; i < list.size(); i++) {

			int tmpx = list.get(i).getX();
			int tmpy = list.get(i).getY();
			float unit = list.get(i).getUnit();
			if (((tmpx - x) * (tmpx - x) + (tmpy - y) * (tmpy - y)) <= unit
					* unit)

				return true;
		}
		return false;
	}

	/**
	 * 返回 像素点(x,y)往上/下（d=0/1）相同颜色的像素点个数
	 */
	protected int ydist(int x, int y, int d) {
		int sample;
		int start = getBW3x3(x, y);

		for (int j = y + d; j > 1 && j < h - 1; j += d) {
			sample = getBW3x3(x, j);
			if (start + sample == 1) {
				return (d > 0) ? j - y : y - j;
			}
		}
		return -1;
	}

	/**
	 * 返回 像素点(x,y)往左/右（d=0/1）相同颜色的像素点个数
	 */
	protected int xdist(int x, int y, int d) {
		int sample;
		int start = getBW3x3(x, y);

		for (int i = x + d; i > 1 && i < w - 1; i += d) {
			sample = getBW3x3(i, y);
			if (start + sample == 1) {
				return (d > 0) ? i - x : x - i;
			}
		}
		return -1;
	}

	// 返回候选圆（sx,sy）对应的unit的大小
	protected float readUnit(int sx, int sy) {
		int iwidth = w;
		int iheight = h;

		boolean whiteL = true;
		boolean whiteR = true;
		boolean whiteU = true;
		boolean whiteD = true;
		int sample;
		int distL = 0, distR = 0, distU = 0, distD = 0;

		for (int i = 1; true; i++) {

			// 取点最多到边缘，如果取到了边缘？错误
			if (sx - i < 1 || sx + i >= iwidth - 1 || sy - i < 1
					|| sy + i >= iheight - 1 || i > 100) {
				return -1;
			}

			// 左边的样本点
			sample = getBW3x3(sx - i, sy); // ======1时是白点

			if (distL <= 0) {
				if (whiteL && sample == 0)
					whiteL = false;

				else if (!whiteL && sample == 1)
					distL = i;

			}

			// 右边的样本点
			sample = getBW3x3(sx + i, sy);
			if (distR <= 0) {
				if (whiteR && sample == 0) {
					whiteR = false;
				} else if (!whiteR && sample == 1) {
					distR = i;
				}
			}

			// 上边的样本点
			sample = getBW3x3(sx, sy - i);
			if (distU <= 0) {

				if (whiteU && sample == 0) {
					whiteU = false;
				} else if (!whiteU && sample == 1) {
					distU = i;
				}
			}

			// 下边的样本点
			sample = getBW3x3(sx, sy + i);
			if (distD <= 0) {
				if (whiteD && sample == 0) {
					whiteD = false;
				} else if (!whiteD && sample == 1) {
					distD = i;
				}
			}

			if (distR > 0 && distL > 0 && distU > 0 && distD > 0) {
				float u = (distR + distL + distU + distD) / 8.0f;
				// 对上下左右四个数据检测，若大小差不多才可以通过
				if (Math.abs(distR - distL) > u || Math.abs(distR - distD) > u
						|| Math.abs(distR - distU) > u
						|| Math.abs(distL - distD) > u
						|| Math.abs(distL - distU) > u
						|| Math.abs(distU - distD) > u) {
					return -1;
				} else
					return u;
			}
		}
	}

	// hw 当前圆心1.5unit半径外8个点 ，若至少有7个黑色，通过验证
	public boolean isValid(int x, int y, float unit) {
		int count = 0;
		// 测试8个像素点
		int tmpx1 = (int) Math.round(x / 1.0f - 1.5f * unit);
		int tmpx2 = (int) Math.round(x / 1.0f + 1.5f * unit);
		int tmpx3 = (int) Math.round(x / 1.0f - 1.5f * unit * 1.414 / 2);
		int tmpx4 = (int) Math.round(x / 1.0f + 1.5f * unit * 1.414 / 2);
		int tmpy1 = (int) Math.round(y / 1.0f - 1.5f * unit);
		int tmpy2 = (int) Math.round(y / 1.0f + 1.5f * unit);
		int tmpy3 = (int) Math.round(y / 1.0f - 1.5f * unit * 1.414 / 2);
		int tmpy4 = (int) Math.round(y / 1.0f + 1.5f * unit * 1.414 / 2);

		if (tmpx1 < 0 || tmpy1 < 0 || tmpx2 > w || tmpy2 > h)
			return false;

		if (getBW3x3(tmpx1, y) == 0)
			count++;
		if (getBW3x3(tmpx2, y) == 0)
			count++;
		if (getBW3x3(x, tmpy1) == 0)
			count++;
		if (getBW3x3(x, tmpy2) == 0)
			count++;
		if (getBW3x3(tmpx3, tmpy3) == 0)
			count++;
		if (getBW3x3(tmpx3, tmpy4) == 0)
			count++;
		if (getBW3x3(tmpx4, tmpy3) == 0)
			count++;
		if (getBW3x3(tmpx4, tmpy4) == 0)
			count++;

		if (count > 6)
			return true;
		else
			return false;
	}
}
