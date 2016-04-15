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

	private int x, y;// 用来保存子图片起始坐标

	// 默认构造函数
	public CutBitmap() {
		this.x = 0;
		this.y = 0;
	}

	// 切割起始点和长度宽度后期需要更改，取决于圆环和条形码的位置关系和大小关系
	public Bitmap cut(Bitmap imageSource, CenterCoordinates coo) {
		// 得到圆环unit
		float unit = coo.getUnit();

		// 得到圆心coo对应bitmap的左上角第一个像素点坐标
		x = (int) Math.floor(coo.getX() - 4 * unit);
		y = (int) Math.floor(coo.getY() - 8 * unit);

		// 防止左上角第一个像素点越界
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;

		// 得到要剪切的子图像的长度和宽度
		int lengthW = (int) Math.ceil(8 * unit);
		int lengthH = (int) Math.ceil(6 * unit);

		// 获取图片的宽和高
		int w = imageSource.getWidth();
		int h = imageSource.getHeight();

		// 注意剪切的时候不能超过原始源的图片范围
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
		Log.i("img", "保存图片");
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
			Log.i("img", "已经保存");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}