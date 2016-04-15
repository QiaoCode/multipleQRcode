package com.example.orderlist;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MainActivity extends Activity {

	/*
	 * java.lang.Object/android.view.View/android.widget.ImageView����
	 * ��ֱ֪�����ࣺImageButton, QuickContactBadge
	 * ��ʾ����ͼ������ͼ�ꡣImageView����Լ��ظ�����Դ��ͼƬ������Դ��ͼƬ�⣩��
	 * ��Ҫ����ͼ��ĳߴ磬�ȱ�������������������ʹ�ã����ṩ�������ź���ɫ����Ⱦ��������ʾѡ�
	 */
	ImageView imgFavorite;
	// ԴͼƬ�ı���·��
	private String path = Environment.getExternalStorageDirectory()
			+ File.separator;
	private String fileName;
	List<String> listStr = new ArrayList<String>();
	// DELETE
	private static String TAG = "LIST";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imgFavorite = (ImageView) findViewById(R.id.imageView1);
		imgFavorite.setOnClickListener(new OnClickListener() {
			@Override
			// ���ĵõ���Ƭ���浽����
			public void onClick(View v) {
				File file = new File(path);
				if (!file.exists()) {
					file.mkdir();
				}
				fileName = "bpsrc.jpg";
				// �����������
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// ���ĵõ���Ƭ���浽����
				intent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(new File(path + fileName)));
				startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Activity.DEFAULT_KEYS_DIALER: {
			File file = new File(path + fileName);
			break;
		}
		}
		// ��ԭʼ��Ƭѹ���õ�����Ҫ�����bitmap
		Bitmap bmp = getSmallBitmap(path + fileName);
		// ��ѹ�����ͼƬ���浽����
		CutBitmap cutb = new CutBitmap();
		cutb.saveBitmap(bmp, 100);

		// ����Ƭ�е�ָ��浽listStr��
		listStr = result(bmp);
	}

	// ��bitmap�е�ָ��ɨ����������浽list��
	protected List<String> result(Bitmap bp) {
		List<String> tmp = new ArrayList<String>();
		Scanner scanner = new Scanner();
		// �õ�Բ����������
		List<CenterCoordinates> listCoo = scanner.scan(bp);
		// �õ��и���ʵ��
		Log.i("listCoo", "size:" + listCoo.size());

		Bitmap bitmap;
		CutBitmap cutbitmap = new CutBitmap();
		// �õ��������ȡ��ʵ��
		QrReader reader = new QrReader();

		String str;
		for (int i = 0; i < listCoo.size(); i++) {
			bitmap = cutbitmap.cut(bp, listCoo.get(i));
			cutbitmap.saveBitmap(bitmap, i);
			str = reader.parseQRcodeBitmap(bitmap);
			if (str != null)
				tmp.add(str);
		}

		return tmp;
	}

	// ѹ��ͼƬ
	public static Bitmap getSmallBitmap(String filePath) {

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// ����ѹ������
		
			options.inSampleSize = calculateInSampleSize(options,1000,800);

		Log.i("InSampleSize", ":" + options.inSampleSize);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		Bitmap bm = BitmapFactory.decodeFile(filePath, options);
		if (bm == null) {
			return null;
		}
		int degree = readPictureDegree(filePath);
		bm = rotateBitmap(bm, degree);
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.JPEG, 30, baos);

		} finally {
			try {
				if (baos != null)
					baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bm;

	}

	// ����ѹ������
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// ԭͼ�Ŀ�͸�
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}

		return inSampleSize;
	}

	// ��תͼƬ
	private static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
		if (bitmap == null)
			return null;

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		// Setting post rotate to 90
		Matrix mtx = new Matrix();
		mtx.postRotate(rotate);
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}

	// ��ת�Ƕ�
	private static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
