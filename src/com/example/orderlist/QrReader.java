package com.example.orderlist;

import java.util.Hashtable;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

public class QrReader {

	// ��������������,����String���
	public String parseQRcodeBitmap(Bitmap bitmap) {
		// ����ת������UTF-8
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8");

		Result result = null;
		RGBLuminanceSource rgbLuminanceSource;
		BinaryBitmap binaryBitmap;
		MultiFormatReader multiFormatReader;
		// �������ν���
		for (int i = 3; i > 0 && result == null; i--) {
			// �½�һ��RGBLuminanceSource���󣬽�bitmap�����˶���
			rgbLuminanceSource = new RGBLuminanceSource(bitmap);
			// ��rgbLuminanceSourceת���ɶ�����ͼƬ
			//rgbLuminanceSource.
			//PlanarYUVLuminanceSource
			binaryBitmap = new BinaryBitmap(new HybridBinarizer(
					rgbLuminanceSource));
			
			// ��ʼ����������
			multiFormatReader = new MultiFormatReader();
			// ��ʼ����

			try {
				result = multiFormatReader.decode(binaryBitmap, hints);
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				multiFormatReader.reset();
			}
		}

		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}

		if (result != null) {
			String str = result.getText();
			return str;
		} else
			// ��������
			return "fail";
	}

}
