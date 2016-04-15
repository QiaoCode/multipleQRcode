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

	// 解析单个条形码,返回String结果
	public String parseQRcodeBitmap(Bitmap bitmap) {
		// 解析转换类型UTF-8
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8");

		Result result = null;
		RGBLuminanceSource rgbLuminanceSource;
		BinaryBitmap binaryBitmap;
		MultiFormatReader multiFormatReader;
		// 尝试三次解析
		for (int i = 3; i > 0 && result == null; i--) {
			// 新建一个RGBLuminanceSource对象，将bitmap传给此对象
			rgbLuminanceSource = new RGBLuminanceSource(bitmap);
			// 将rgbLuminanceSource转换成二进制图片
			//rgbLuminanceSource.
			//PlanarYUVLuminanceSource
			binaryBitmap = new BinaryBitmap(new HybridBinarizer(
					rgbLuminanceSource));
			
			// 初始化解析对象
			multiFormatReader = new MultiFormatReader();
			// 开始解析

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
			// 重新拍照
			return "fail";
	}

}
