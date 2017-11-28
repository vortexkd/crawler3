package com.company;

import org.jsoup.nodes.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Article implements Comparable<Article>{

    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm";
    private static final String ELEMENT_TITLE_CLASS = "thumb-s__tit";
    private static final String ELEMENT_URL_CLASS = "thumb-s__tit-link";
    private static final String ELEMENT_CATEGORY_CLASS = "thumb-s__category";
    private static final String ELEMENT_DATE_CLASS = "thumb-s__date";
    private static final String HREF = "href";
    private static final String HTTP = "http";
    private static final String DOT = ".";
    private static final String FORWARD_SLASH =  "/";
    private static final String P_TAG = "<p>";
    private static final String P_TAG_CLOSED = "</p>";
    private static final String LINE_SEP = System.getProperty("line.separator");



    private final String title;
    private final String category;
    private final String postTimeString;
    private final Date postTime;
    private final String url;

    //for printed article
    private List<String> body = new ArrayList<>();
    private String imgUrl;

    static String getElementDateClass() {
        return ELEMENT_DATE_CLASS;
    }

    static String urlMaker(String relativeLocation, String base){
        if(relativeLocation.equals("") || relativeLocation == null){
            return "";
        }
        if(relativeLocation.contains(HTTP) || relativeLocation.contains(DOT)){
            return relativeLocation;
        }else {
            return base+relativeLocation;
        }
    }

    Article(Element el, String baseUrl, String year) throws ParseException{
        SimpleDateFormat d = new SimpleDateFormat(DATE_PATTERN);
        this.title = el.getElementsByClass(ELEMENT_TITLE_CLASS).text();
        this.url = Article.urlMaker(el.getElementsByClass(ELEMENT_URL_CLASS).attr(HREF).toString(), baseUrl);
        this.category = el.getElementsByClass(ELEMENT_CATEGORY_CLASS).text();
        this.postTimeString = year + FORWARD_SLASH + el.getElementsByClass(ELEMENT_DATE_CLASS).text();
        this.postTime = d.parse(year + FORWARD_SLASH + el.getElementsByClass(ELEMENT_DATE_CLASS).text());

    }

    String getUrl() {
        return url;
    }

    String getTitle() {
        return title;
    }

    String getPostTimeString() {
        return postTimeString;
    }

    String getBody() {
        StringBuilder bodyOutput = new StringBuilder();
        for (String paragraph : this.body) {
            bodyOutput.append(P_TAG + paragraph + P_TAG_CLOSED + LINE_SEP);
        }
        return bodyOutput.toString();
    }

    String getImgTag() {
        return "<img src=\"" + this.imgUrl + "\" />";
    }

    Date getPostTime() {
        return postTime;
    }

    void addBody(String paragraph) {
        this.body.add(paragraph); //need to sanitize.
    }

    void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Override
    public String toString() {
        return "\"" +  this.title + "\",\"" + this.category + "\",\"" + this.postTimeString + "\",\"" + this.url + "\"";
    }


    @Override
    public int compareTo(Article other) {
        return this.postTime.compareTo(other.getPostTime());
    }
}
