package dk.incipio.photogallery;

import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FlickerFetchr {
    private static final String TAG = "FlickerFetchr";
    public static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String ENDPOINT = "https://api.flickr.com/services/rest";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";
    private static final String PARAM_EXTRAS = "extras";
    private static final String PARAM_TEXT = "text";
    private static final String EXTRA_SMALL_URL = "url_s";
    private static final String XML_PHOTO = "photo";

    public byte[] getUrlBytes(String urlSpec) throws IOException {

        // Create URL object from string and attempt to open connection
        // Casting to HttpURLConnection gives convenient access to HTTP specific interfaces for
        // working with request methods, response codes, streaming methods etc
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // create a real connection now - gives us  access to response code
            // (use getOutPutStream for POST requests instead)
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode()!= HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead=0;
            byte[] buffer = new byte[1024];
            while ((bytesRead=in.read(buffer))>0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();

        } finally {
            connection.disconnect();
        }
    }

    public String getURL(String urlSpec) throws IOException {
        // Convert the byteArray read from the web address to a String
        return new String(getUrlBytes(urlSpec));
    }

    public ArrayList<GalleryItem> downloadGalleryItems(String url) {
        ArrayList<GalleryItem> items=new ArrayList<GalleryItem>();

        try {

            String xmlString = getURL(url);
            Log.i(TAG, "Received XML: "+xmlString);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));

            parseItems(items, parser);

        } catch (IOException e) {
            Log.e(TAG, "fetchItems fail ",e);
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "fetchItems pullparserexception", xppe);
        }

        return items;
    }

    public ArrayList<GalleryItem> fetchItems() {
        // Create a properly escaped url string for the request
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_GET_RECENT)
                .appendQueryParameter("api_key", Secret.API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .build().toString();

        return downloadGalleryItems(url);
    }


    public ArrayList<GalleryItem> search(String query) {
        // Create a properly escaped url string for the request
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_SEARCH)
                .appendQueryParameter("api_key", Secret.API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .appendQueryParameter(PARAM_TEXT, query)
                .build().toString();

        return downloadGalleryItems(url);
    }


    public void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.next();
        while (eventType!=XmlPullParser.END_DOCUMENT) {
            if (eventType==XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())) {
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);

                GalleryItem item = new GalleryItem();
                item.setId(id);
                item.setCaption(caption);
                item.setUrl(smallUrl);
                items.add(item);
            }

            eventType = parser.next();
        }
    }
}
