package org.golang.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class WViewActivity extends Activity {

	WebView webView;

	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Fixed Portrait orientation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		/*this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);*/


		setContentView(R.layout.webview);
		WebView webView = (WebView) findViewById(R.id.webView1);

		initWebView(webView);
		webView.setWebViewClient(new WebViewClient( ) {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				Log.d("JavaGoWV: ", url);

				final Pattern p = Pattern.compile("dcoinKey&password=(.*)$");
				final Matcher m = p.matcher(url);
				if (m.find()) {
					try {
						Thread thread = new Thread(new Runnable(){
							@Override
							public void run() {
								try {
										//File root = android.os.Environment.getExternalStorageDirectory();
										File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
										Log.d("JavaGoWV", "dir " + dir);

										URL keyUrl = new URL("http://127.0.0.1:8089/ajax?controllerName=dcoinKey&first=1"); //you can write here any link
										//URL keyUrl = new URL("http://yandex.ru/"); //you can write here any link
										File file = new File(dir, "dcoin-key.png");

										long startTime = System.currentTimeMillis();
										Log.d("JavaGoWV", "download begining");
										Log.d("JavaGoWV", "download keyUrl:" + keyUrl);

										/* Open a connection to that URL. */
										URLConnection ucon = keyUrl.openConnection();

										Log.d("JavaGoWV", "0");
										/*
										* Define InputStreams to read from the URLConnection.
										*/
										InputStream is = ucon.getInputStream();

										Log.d("JavaGoWV", "01");

										BufferedInputStream bis = new BufferedInputStream(is);

										Log.d("JavaGoWV", "1");
									   /*
										* Read bytes to the Buffer until there is nothing more to read(-1).
										*/
										ByteArrayOutputStream baf = new ByteArrayOutputStream(5000);
                                        int current;
                                        while ((current = bis.read()) != -1) {
                                            baf.write(current);
                                        }

										Log.d("JavaGoWV", "2");
										/* Convert the Bytes read to a String. */
										FileOutputStream fos = new FileOutputStream(file);
										fos.write(baf.toByteArray());
										fos.flush();
										fos.close();

										Log.d("JavaGoWV", "3");
										if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
											Intent mediaScanIntent = new Intent(
													Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
											Uri contentUri = Uri.fromFile(file);
											mediaScanIntent.setData(contentUri);
											WViewActivity.this.sendBroadcast(mediaScanIntent);
										} else {
											sendBroadcast(new Intent(
													Intent.ACTION_MEDIA_MOUNTED,
													Uri.parse("file://"
															+ Environment.getExternalStorageDirectory())));
										}

										Log.d("JavaGoWV", "4");
								} catch (Exception e) {
									Log.e("JavaGoWV error 0", e.toString());
									e.printStackTrace();
								}
							}
						});
						thread.start();


					} catch (Exception e) {
						Log.e("JavaGoWV error", e.toString());
						e.printStackTrace();
					}
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				Log.e("JavaGoWV", "shouldOverrideUrlLoading " + url);

				if (url.endsWith(".mp4")) {
					Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(in);
					return true;
				} else {
					return false;
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Log.d("JavaGoWV", "failed: " + failingUrl + ", error code: " + errorCode + " [" + description + "]");
			}
		});

		SystemClock.sleep(1500);
		//if (MyService.DcoinStarted(8089)) {
			webView.loadUrl("http://localhost:8089/");
		//}



	}

	private final static Object methodInvoke(Object obj, String method, Class<?>[] parameterTypes, Object[] args) {
		try {
			Method m = obj.getClass().getMethod(method, new Class[] { boolean.class });
			m.invoke(obj, args);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void initWebView(WebView webView) {

		WebSettings settings = webView.getSettings();

		settings.setJavaScriptEnabled(true);
		settings.setAllowFileAccess(true);
		settings.setDomStorageEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		settings.setSupportZoom(true);
		// settings.setPluginsEnabled(true);
		methodInvoke(settings, "setPluginsEnabled", new Class[] { boolean.class }, new Object[] { true });
		// settings.setPluginState(PluginState.ON);
		methodInvoke(settings, "setPluginState", new Class[]{PluginState.class }, new Object[] { PluginState.ON});
		// settings.setPluginsEnabled(true);
		methodInvoke(settings, "setPluginsEnabled", new Class[]{ boolean.class }, new Object[]{true});
		// settings.setAllowUniversalAccessFromFileURLs(true);
		methodInvoke(settings, "setAllowUniversalAccessFromFileURLs", new Class[] { boolean.class }, new Object[] { true });
		// settings.setAllowFileAccessFromFileURLs(true);
		methodInvoke(settings, "setAllowFileAccessFromFileURLs", new Class[] { boolean.class }, new Object[] { true });

		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webView.clearHistory();
		webView.clearFormData();
		webView.clearCache(true);

		webView.setWebChromeClient(new MyWebChromeClient());
		// webView.setDownloadListener(downloadListener);
	}

	UploadHandler mUploadHandler;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (requestCode == Controller.FILE_SELECTED) {
			// Chose a file from the file picker.
			if (mUploadHandler != null) {
				mUploadHandler.onResult(resultCode, intent);
			}
		}

		super.onActivityResult(requestCode, resultCode, intent);
	}

	class MyWebChromeClient extends WebChromeClient {
		public MyWebChromeClient() {

		}

		@Override
		public boolean onConsoleMessage(ConsoleMessage cm)
		{
			Log.d("JavaGoWV", String.format("%s @ %d: %s",
					cm.message(), cm.lineNumber(), cm.sourceId()));
			return true;
		}


		@Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }


		private String getTitleFromUrl(String url) {
			String title = url;
			try {
				URL urlObj = new URL(url);
				String host = urlObj.getHost();
				if (host != null && !host.isEmpty()) {
					return urlObj.getProtocol() + "://" + host;
				}
				if (url.startsWith("file:")) {
					String fileName = urlObj.getFile();
					if (fileName != null && !fileName.isEmpty()) {
						return fileName;
					}
				}
			} catch (Exception e) {
				// ignore
			}

			return title;
		}


		public void onLoadStarted(WebView view, String url) {
			Log.d("Go", "WebView onLoadStarted: " + url);
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {

		    if (message.contains("location")) return false;

			String newTitle = getTitleFromUrl(url);

			new AlertDialog.Builder(WViewActivity.this).setTitle(newTitle).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					result.confirm();
				}
			}).setCancelable(false).create().show();
			return true;
			// return super.onJsAlert(view, url, message, result);
		}

		@Override
		public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            if (message.contains("location")) {
                return false;
            }

			String newTitle = getTitleFromUrl(url);

			new AlertDialog.Builder(WViewActivity.this).setTitle(newTitle).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					result.confirm();
				}
			}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					result.cancel();
				}
			}).setCancelable(false).create().show();
			return true;

			// return super.onJsConfirm(view, url, message, result);
		}

		// Android 2.x
		public void openFileChooser(ValueCallback<Uri> uploadMsg) {
			openFileChooser(uploadMsg, "");
		}

		// Android 3.0
		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
			openFileChooser(uploadMsg, "", "filesystem");
		}

		// Android 4.1
		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
			mUploadHandler = new UploadHandler(new Controller());
			mUploadHandler.openFileChooser(uploadMsg, acceptType, capture);
		}
	};

	class Controller {
		final static int FILE_SELECTED = 4;

		Activity getActivity() {
			return WViewActivity.this;
		}
	}


    public class UploadHandler {

        private String mCameraFilePath;

        private final static String IMAGE_MIME_TYPE = "image/*";
        private final static String VIDEO_MIME_TYPE = "video/*";
        private final static String AUDIO_MIME_TYPE = "audio/*";
        private final static String FILE_PROVIDER_AUTHORITY = "com.jam.webview.dcoinwebview.fileProvider";
        /*
         * The Object used to inform the WebView of the file to upload.
         */
        private ValueCallback<Uri[]> mUploadMessage;
        private boolean mHandled;
        private Controller mController;
        private WebChromeClient.FileChooserParams mParams;
        private Uri mCapturedMedia;
        public UploadHandler(Controller controller) {
            mController = controller;
        }


        boolean handled() {
            return mHandled;
        }


        void onResult(int resultCode, Intent intent) {
            if (mUploadMessage == null) return;

            if (intent == null) {
                mUploadMessage.onReceiveValue(null);
                return;
            }
            Uri[] uris;
            // As the media capture is always supported, we can't use
            // FileChooserParams.parseResult().
            uris = parseResult(resultCode, intent);
            mUploadMessage.onReceiveValue(uris);
            mHandled = true;
        }


        void openFileChooser(ValueCallback callback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (mUploadMessage != null) {
                // Already a file picker operation in progress.
                return;
            }
            mUploadMessage = callback;
            mParams = fileChooserParams;
            Intent[] captureIntents = createCaptureIntent();

            Intent intent = null;
            // Go to the media capture directly if capture is specified, this is the
            // preferred way.

            if (fileChooserParams.isCaptureEnabled() && captureIntents.length == 1) {
                intent = captureIntents[0];
            } else {
                intent = new Intent(Intent.ACTION_CHOOSER);
                intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, captureIntents);
                intent.putExtra(Intent.EXTRA_INTENT, fileChooserParams.createIntent());
            }
            startActivity(intent);
        }

        private Uri[] parseResult(int resultCode, Intent intent) {
            if (resultCode == Activity.RESULT_CANCELED) {
                return null;
            }
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                    : intent.getData();
            // As we ask the camera to save the result of the user taking
            // a picture, the camera application does not return anything other
            // than RESULT_OK. So we need to check whether the file we expected
            // was written to disk in the in the case that we
            // did not get an intent returned but did get a RESULT_OK. If it was,
            // we assume that this result has came back from the camera.
            if (result == null && intent == null && resultCode == Activity.RESULT_OK
                    && mCapturedMedia != null) {
                result = mCapturedMedia;
            }
            Uri[] uris = null;
            if (result != null) {
                uris = new Uri[1];
                uris[0] = result;
            }
            return uris;
        }


        void openFileChooser(ValueCallback uploadMsg, String acceptType, String capture) {

            Log.d("JavaGoWV", "openFileChooser ValueCallback");
            mUploadMessage = uploadMsg;
            final String mediaSourceKey = "capture";
            final String mediaSourceValueCamera = "camera";
            final String mediaSourceValueFileSystem = "filesystem";
            final String mediaSourceValueCamcorder = "camcorder";
            final String mediaSourceValueMicrophone = "microphone";
            // According to the spec, media source can be 'filesystem' or 'camera' or 'camcorder'
            // or 'microphone' and the default value should be 'filesystem'.
            String mediaSource = mediaSourceValueFileSystem;
            if (mUploadMessage != null) {
                // Already a file picker operation in progress.
                return;
            }

            // Parse the accept type.
            String params[] = acceptType.split(";");
            String mimeType = params[0];
            if (capture.length() > 0) {
                mediaSource = capture;
            }

            Log.d("JavaGoWV", "openFileChooser 1 ");
            if (capture.equals(mediaSourceValueFileSystem)) {
                // To maintain backwards compatibility with the previous implementation
                // of the media capture API, if the value of the 'capture' attribute is
                // "filesystem", we should examine the accept-type for a MIME type that
                // may specify a different capture value.
                for (String p : params) {
                    String[] keyValue = p.split("=");
                    if (keyValue.length == 2) {
                        // Process key=value parameters.
                        if (mediaSourceKey.equals(keyValue[0])) {
                            mediaSource = keyValue[1];
                        }
                    }
                }
            }
            Log.d("JavaGoWV", "openFileChooser 2 ");
            //Ensure it is not still set from a previous upload.
            mCameraFilePath = null;
            switch (mimeType) {
                case IMAGE_MIME_TYPE:
                    if (mediaSource.equals(mediaSourceValueCamera)) {
                        Log.d("JavaGoWV", "001");
                        // Specified 'image/*' and requested the camera, so go ahead and launch the
                        // camera directly.
                        startActivity(createCameraIntent());
                        return;
                    } else {
                        // Specified just 'image/*', capture=filesystem, or an invalid capture parameter.
                        // In all these cases we show a traditional picker filetered on accept type
                        // so launch an intent for both the Camera and image/* OPENABLE.
                        Log.d("JavaGoWV", "chooser 4");
                        Intent chooser = createChooserIntent(createCameraIntent());
                        chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(IMAGE_MIME_TYPE));
                        startActivity(chooser);
                        return;
                    }
                case VIDEO_MIME_TYPE:
                    if (mediaSource.equals(mediaSourceValueCamcorder)) {
                        // Specified 'video/*' and requested the camcorder, so go ahead and launch the
                        // camcorder directly.
                        startActivity(createCamcorderIntent());
                        return;
                    } else {
                        // Specified just 'video/*', capture=filesystem or an invalid capture parameter.
                        // In all these cases we show an intent for the traditional file picker, filtered
                        // on accept type so launch an intent for both camcorder and video/* OPENABLE.
                        Log.d("JavaGoWV", "chooser 3");
                        Intent chooser = createChooserIntent(createCamcorderIntent());
                        chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(VIDEO_MIME_TYPE));
                        startActivity(chooser);
                        return;
                    }
                case AUDIO_MIME_TYPE:
                    if (mediaSource.equals(mediaSourceValueMicrophone)) {
                        // Specified 'audio/*' and requested microphone, so go ahead and launch the sound
                        // recorder.
                        startActivity(createSoundRecorderIntent());
                        return;
                    } else {
                        // Specified just 'audio/*',  capture=filesystem of an invalid capture parameter.
                        // In all these cases so go ahead and launch an intent for both the sound
                        // recorder and audio/* OPENABLE.
                        Log.d("JavaGoWV", "chooser 2");
                        Intent chooser = createChooserIntent(createSoundRecorderIntent());
                        chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(AUDIO_MIME_TYPE));
                        startActivity(chooser);
                        return;
                    }
            }
            // No special handling based on the accept type was necessary, so trigger the default
            // file upload chooser.
            Log.d("JavaGoWV", "createDefaultOpenableIntent");
/*
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
					+ "/Android/data/org.golang.app/files/");
			Log.d("JavaGoWV", "path ="+Environment.getExternalStorageDirectory().getPath() + "/Android/data/org.golang.app/files/");
			intent.setDataAndType(uri, "**");
			//startActivity(Intent.createChooser(intent, "Open folder"));*/
            startActivity(createDefaultOpenableIntent());
        }

        private Intent createDefaultOpenableIntent() {
            // Create and return a chooser with the default OPENABLE
            // actions including the camera, camcorder and sound
            // recorder where available.
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                    + "/download/");
            i.setDataAndType(uri, "*/*");
            Log.d("JavaGoWV", "chooser 0");
            Intent chooser = createChooserIntent(createCameraIntent(), createCamcorderIntent(),
                    createSoundRecorderIntent());
            chooser.putExtra(Intent.EXTRA_INTENT, i);
            return chooser;
        }


        private Intent createChooserIntent(Intent... intents) {

            Log.d("JavaGoWV", "createChooserIntent");
            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
            chooser.putExtra(Intent.EXTRA_TITLE,
                    mController.getActivity().getResources()
                            .getString(R.string.choose_upload));
            Log.d("JavaGoWV", "return chooser");
            return chooser;
        }


        private Intent createOpenableIntent(String type) {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType(type);
            return i;
        }


        private Intent createCameraIntent() {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File externalDataDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM);
            File cameraDataDir = new File(externalDataDir.getAbsolutePath() +
                    File.separator + "browser-photos");
            cameraDataDir.mkdirs();
            mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator +
                    System.currentTimeMillis() + ".jpg";
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
            return cameraIntent;
        }


        private void startActivity(Intent intent) {
            try {
                mController.getActivity().startActivityForResult(intent, Controller.FILE_SELECTED);
            } catch (ActivityNotFoundException e) {
                // No installed app was able to handle the intent that
                // we sent, so file upload is effectively disabled.
                Toast.makeText(mController.getActivity(), R.string.uploads_disabled,
                        Toast.LENGTH_LONG).show();
            }
        }


        private Intent[] createCaptureIntent() {
            String mimeType = "*/*";
            String[] acceptTypes = mParams.getAcceptTypes();
            if ( acceptTypes != null && acceptTypes.length > 0) {
                mimeType = acceptTypes[0];
            }
            Intent[] intents;
            if (mimeType.equals(IMAGE_MIME_TYPE)) {
                intents = new Intent[1];
                intents[0] = createCameraIntent(createTempFileContentUri(".jpg"));
            } else if (mimeType.equals(VIDEO_MIME_TYPE)) {
                intents = new Intent[1];
                intents[0] = createCamcorderIntent();
            } else if (mimeType.equals(AUDIO_MIME_TYPE)) {
                intents = new Intent[1];
                intents[0] = createSoundRecorderIntent();
            } else {
                intents = new Intent[3];
                intents[0] = createCameraIntent(createTempFileContentUri(".jpg"));
                intents[1] = createCamcorderIntent();
                intents[2] = createSoundRecorderIntent();
            }
            return intents;
        }


        private Uri createTempFileContentUri(String suffix) {

            try {
                File mediaPath = new File(mController.getActivity().getFilesDir(), "files");
                if (!mediaPath.exists() && !mediaPath.mkdir()) {
                    throw new RuntimeException("Folder cannot be created.");
                }
                File mediaFile = File.createTempFile(
                        String.valueOf(System.currentTimeMillis()), suffix, mediaPath);
                return FileProvider.getUriForFile(mController.getActivity(),
                        FILE_PROVIDER_AUTHORITY, mediaFile);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }


        private Intent createCameraIntent(Uri contentUri) {
            if (contentUri == null) throw new IllegalArgumentException();
            mCapturedMedia = contentUri;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedMedia);
            intent.setClipData(ClipData.newUri(mController.getActivity().getContentResolver(),
                    FILE_PROVIDER_AUTHORITY, mCapturedMedia));
            return intent;
        }


        private Intent createCamcorderIntent() {
            return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        }


        private Intent createSoundRecorderIntent() {
            return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        }
    }
}