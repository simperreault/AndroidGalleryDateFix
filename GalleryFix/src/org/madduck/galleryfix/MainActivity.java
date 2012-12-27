package org.madduck.galleryfix;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
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
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) {
		// Check which request we're responding to
		if (requestCode == 1) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {

				fixFolder(data.getDataString().replaceFirst("file:///", "")
						.replaceFirst("folder:///", ""));

			}
		}
	}

	@SuppressWarnings("unchecked")
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
		
		Button btnFix = (Button) findViewById(R.id.btn_fix_date);
		btnFix.setVisibility(8);
		
		ProgressBar bar = (ProgressBar) findViewById(R.id.progress_fix);
		bar.setVisibility(0);
		
		FixFolderTask task = new FixFolderTask(this, bar, btnFix);
		task.execute(files);

		return true;
	}

	@SuppressLint({ "SimpleDateFormat", "DefaultLocale" })
	private class FixFolderTask extends AsyncTask<List<String>, Integer, Long> {

		private Context context;
		private ProgressBar progressBar;
		private Button btnFix;

		public FixFolderTask(Context context, ProgressBar progressBar, Button btnFix) {
			this.context = context;
			this.progressBar = progressBar;
			this.btnFix = btnFix;
		}

		protected Long doInBackground(List<String>... allFiles) {
			
			List<String> files = allFiles[0];
			Collections.sort(files);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			
			progressBar.setMax(files.size());

			int i = 1;
			for (String filename : files) {
				publishProgress(i);
				++i;
				
				if (!filename.toLowerCase().endsWith(".png") && !filename.toLowerCase().endsWith(".jpg"))
				{
					continue;
				}
				
				Log.i("Gallery Fix", "Doing " + filename);
				
				
				try {
					ExifInterface exif = new ExifInterface(filename);

					SimpleDateFormat dateFormatter = new SimpleDateFormat(
							"yyyy:MM:dd");
					SimpleDateFormat timeFormatter = new SimpleDateFormat(
							"HH:mm:ss");

					Date date = new Date(calendar.getTimeInMillis());

					exif.setAttribute(
							ExifInterface.TAG_DATETIME,
							dateFormatter.format(date) + " "
									+ timeFormatter.format(date));

					exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, "");
					exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, "");

					exif.saveAttributes();

				} catch (IOException e) {
					Log.e("Gallery Fix", "Throw : " + e.getMessage());
					e.printStackTrace();
				}

				calendar.add(Calendar.DATE, -1);
			}

			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://"
							+ Environment.getExternalStorageDirectory())));

			return 0L;
		}

		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}

		protected void onPostExecute(Long result) {
			Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
			
			progressBar.setVisibility(8);
			btnFix.setVisibility(0);
		}
	}
}
