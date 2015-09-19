package dk.incipio.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ThumbnailDownLoader<Token> extends HandlerThread {
    private static final String TAG = "ThumbnailDownLoader";
    private static final int MESSAGE_DOWNLOAD = 0;

    Handler mHandler;
    Handler mResponseHandler;
    Map<Token,String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    Listener<Token> mListener;


    public interface Listener<Token> {
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }
    @Override
    @SuppressLint("HandlerLeak")
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    @SuppressWarnings("unchecked")
                    Token token = (Token) msg.obj;
                    Log.i(TAG,"Got a request for URL: "+ requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    public ThumbnailDownLoader(Handler responseHandler) {

        super(TAG);
        mResponseHandler = responseHandler;
    }

    public void queueThumbnail(Token token, String url) {
        Log.d(TAG, "queueThumbnail() called with: " + "token = [" + token + "], url = [" + url + "]");
        
        requestMap.put(token, url);
        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
    }

    private void handleRequest(final Token token) {
        try {
            final String url = requestMap.get(token);
            if (url == null) {
                return;
            }

            byte[] bitmapBytes = new FlickerFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (requestMap.get(token)!=url) {
                        return;
                    }
                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, bitmap);
                }
            });
        }  catch (IOException ioe) {
            Log.e(TAG, "Error downloadeing image", ioe);
        }
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }


}
