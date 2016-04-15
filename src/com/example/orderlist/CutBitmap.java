package com.example.orderlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

public class CutBitmap {

	private int x, y;// ����������ͼƬ��ʼ����

	// Ĭ�Ϲ��캯��
	public CutBitmap() {
		this.x = 0;
		this.y = 0;
	}

	// �и���ʼ��ͳ��ȿ�Ⱥ�����Ҫ���ģ�ȡ����Բ�����������λ�ù�ϵ�ʹ�С��ϵ
	public Bitmap cut(Bitmap imageSource, CenterCoordinates coo) {
		// �õ�Բ��unit
		float unit = coo.getUnit();

		// �õ�Բ��coo��Ӧbitmap�����Ͻǵ�һ�����ص�����
		x = (int) Math.floor(coo.getX() - 4 * unit);
		y = (int) Math.floor(coo.getY() - 8 * unit);

		// ��ֹ���Ͻǵ�һ�����ص�Խ��
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;

		// �õ�Ҫ���е���ͼ��ĳ��ȺͿ��
		int lengthW = (int) Math.ceil(8 * unit);
		int lengthH = (int) Math.ceil(6 * unit);

		// ��ȡͼƬ�Ŀ�͸�
		int w = imageSource.getWidth();
		int h = imageSource.getHeight();

		// ע����е�ʱ���ܳ���ԭʼԴ��ͼƬ��Χ
		Bitmap bitmap;

		if (x + lengthW > w) {
			lengthW = w - x;
		}
		if (y + lengthH > h) {
			lengthH = h - y;
		}
		bitmap = Bitmap.createBitmap(imageSource, x, y, lengthW, lengthH);
		Log.i("childBitmapHeight",""+bitmap.getHeight());
		Log.i("childBitmapWidth",""+bitmap.getWidth());
		return bitmap;
	}

	// DELETE
	public void saveBitmap(Bitmap bm, int n) {
		Log.i("img", "����ͼƬ");
		String fileName = Environment.getExternalStorageDirectory()
				+ File.separator;
		File f = new File(fileName + n + ".png");
		if (f.exists()) {
			f.delete();
		}
		try {
			f.createNewFile();
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			Log.i("img", "�Ѿ�����");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}