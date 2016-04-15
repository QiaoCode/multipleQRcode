package com.example.orderlist;

import java.util.List;
import android.graphics.Bitmap;
import android.util.Log;

//ɨ��ͼƬ�õ�Բ������list
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
	protected int maxu;// ����һ���ɱ�ֵ�����ڿ�����Ҫ�ı�

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
	 * ɨ����Ƭ���õ�Բ������list,list�д�ŵ���Բ��������
	 */
	public List<CenterCoordinates> scan(Bitmap image) {
		this.w = image.getWidth();
		this.h = image.getHeight();
		if (data == null || data.length < w * h) {
			this.data = new int[w * h];
		}
		image.getPixels(this.data, 0, w, 0, 0, w, h);

		// DELETE��һ��
		Log.i("data", "num:" + data.length);
		threshold(); // run the adaptive threshold filter
		return findCodes(); // scan for topcodes
	}

	/**
	 * ������Ƭ�Ŀ�
	 */
	public int getImageWidth() {
		return this.w;
	}

	/**
	 * ������Ƭ�ĸ�
	 */
	public int getImageHeight() {
		return this.h;
	}

	/**
	 * �������ֱ��
	 */
	public void setMaxCodeDiameter(int diameter) {
		float f = diameter / 8.0f;
		this.maxu = (int) Math.ceil(f);
	}

	/**
	 * ���غ�ѡ�������
	 */
	protected int getCandidateCount() {
		return this.ccount;
	}

	/**
	 * ����ͨ�����Եĵ������
	 */
	protected int getTestedCount() {
		return this.tcount;
	}

	/**
	 * �õ����ص�(x,y)�Ķ�ֵ
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
	 * ɨ��õ���ѡ��ĺ���(count++&&��ѡ��ı��)
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
			// ����һ����ʽɨ��
			// ----------------------------------------
			k = (j % 2 == 0) ? 0 : w - 1;
			k += (j * w);

			for (int i = 0; i < w; i++) {

				// ----------------------------------------
				// �õ�k��ĻҶ�ֵ
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
				// �õ�k��Ķ�ֵ
				// ----------------------------------------
				double f = 0.85;
				f = 0.975;
				a = (a < threshold * f) ? 0 : 1;

				// ----------------------------------------
				// ͨ��a��sum��������data[k]
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
	 * ɨ��bitmap�ҵ�Բ�����浽list<CenyerCoordinates>
	 */
	protected List<CenterCoordinates> findCodes() {
		this.tcount = 0;
		List<CenterCoordinates> list = new java.util.ArrayList<CenterCoordinates>();
		CenterCoordinates cootmp = new CenterCoordinates();

		int k = w * 2;
		for (int j = 2; j < h - 2; j++) { // j��������
			for (int i = 0; i < w; i++) { // i�Ǻ�����
				if ((data[k] & 0x2000000) > 0) {
					if ((data[k - 1] & 0x2000000) > 0
							&& (data[k + 1] & 0x2000000) > 0
							&& (data[k - w] & 0x2000000) > 0
							&& (data[k + w] & 0x2000000) > 0) {

						// ����
						if (!overlaps(list, i, j)) {
							this.tcount++;

							// Բ��У��
							int tmpx = i;// �������浱ǰԲ������
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

							// У��֮���Բ��
							tmpx = (int) Math.round(newx);
							tmpy = (int) Math.round(newy);
							// �õ���Բ��unit
							float unit = readUnit(tmpx, tmpy);
							// �ж�unit�ĺϷ���
							if (unit > 0 && unit < maxu) {
								// �жϴ˺�ѡԲ���ĺϷ���
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

		// ��ȥlist�еĸ����unit��С���߹���
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
	 * ����
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
	 * ���� ���ص�(x,y)����/�£�d=0/1����ͬ��ɫ�����ص����
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
	 * ���� ���ص�(x,y)����/�ң�d=0/1����ͬ��ɫ�����ص����
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

	// ���غ�ѡԲ��sx,sy����Ӧ��unit�Ĵ�С
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

			// ȡ����ൽ��Ե�����ȡ���˱�Ե������
			if (sx - i < 1 || sx + i >= iwidth - 1 || sy - i < 1
					|| sy + i >= iheight - 1 || i > 100) {
				return -1;
			}

			// ��ߵ�������
			sample = getBW3x3(sx - i, sy); // ======1ʱ�ǰ׵�

			if (distL <= 0) {
				if (whiteL && sample == 0)
					whiteL = false;

				else if (!whiteL && sample == 1)
					distL = i;

			}

			// �ұߵ�������
			sample = getBW3x3(sx + i, sy);
			if (distR <= 0) {
				if (whiteR && sample == 0) {
					whiteR = false;
				} else if (!whiteR && sample == 1) {
					distR = i;
				}
			}

			// �ϱߵ�������
			sample = getBW3x3(sx, sy - i);
			if (distU <= 0) {

				if (whiteU && sample == 0) {
					whiteU = false;
				} else if (!whiteU && sample == 1) {
					distU = i;
				}
			}

			// �±ߵ�������
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
				// �����������ĸ����ݼ�⣬����С���ſ���ͨ��
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

	// hw ��ǰԲ��1.5unit�뾶��8���� ����������7����ɫ��ͨ����֤
	public boolean isValid(int x, int y, float unit) {
		int count = 0;
		// ����8�����ص�
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
