package org.madduck.galleryfix;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private Button btnFixDate = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnFixDate = (Button) findViewById(R.id.btn_fix_date);
		btnFixDate.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == btnFixDate.getId()) {
			Intent intent = new Intent("org.openintents.action.PICK_DIRECTORY");
			startActivityForResult(intent, 1);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == 1) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				fixFolder(data.getDataString().replaceFirst("file:///", "")
						.replaceFirst("folder:///", ""));
			}
		}
	}

	private boolean fixFolder(String folderPath) {

		File folder = new File(folderPath);

		// List files
		List<String> files = new LinkedList<String>();
		if (folder.exists()) {
			for (File f : folder.listFiles()) {
				if (f.isFile()) {
					files.add(f.getAbsolutePath());
				}
			}
		}

		Collections.sort(files);

		Log.i("toto", files.toString());

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		Log.i("toto", files.size() + "");

		for (String filename : files) {

			try {
				ExifInterface i = new ExifInterface(filename);

				// 2012:12:28 19:09:31
				// 2012:12:26 19:54:42
				SimpleDateFormat dateFormatter = new SimpleDateFormat(
						"yyyy:MM:dd");
				SimpleDateFormat timeFormatter = new SimpleDateFormat(
						"HH:mm:ss");

				Date date = new Date(calendar.getTimeInMillis());
				
				i.setAttribute(ExifInterface.TAG_DATETIME,
						dateFormatter.format(date) + " " + timeFormatter.format(date));
				i.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, dateFormatter.format(date));
				i.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, timeFormatter.format(date));

				// i.setAttribute(ExifInterface.TAG_DATETIME,
				// "2020:12:28 19:09:51");

				Log.i("toto",
						filename + " : "
								+ i.getAttribute(ExifInterface.TAG_DATETIME));

				i.saveAttributes();

			} catch (IOException e) {
				Log.e("toto", "Throw");
				e.printStackTrace();
			}

			calendar.add(Calendar.DATE, -1);
		}

		sendBroadcast(new Intent(
				Intent.ACTION_MEDIA_MOUNTED,
				Uri.parse("file://" + Environment.getExternalStorageDirectory())));

		Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();

		return true;
	}
}
