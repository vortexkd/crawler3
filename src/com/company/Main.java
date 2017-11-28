package com.company;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {

        System.out.println("記事を探してま～す");

        sourceArticlesFromDate(2016, 5,31,6,0); // 2016年12月の記事の一覧

        System.out.println("見つけました～多分");

    }

    private static void sourceArticlesFromDate(int intYear, int intMonth, int intDay, int intHour, int intMinute) throws IOException, InterruptedException, ParseException{

        String month = String.format("%02d" , intMonth);
        String day = String.format("%02d" , intDay);
        String previousDay = String.format("%02d" , (intDay-1));
        String hour = String.format("%02d" , intHour);
        String minute = String.format("%02d" , intMinute);
        String year = String.valueOf(intYear);

        String dateTimeStamp = month + "/" + day + " " + hour + ":" + minute;
        String dateMarker = year + "年" + month + "月" + day + "日";
        String previousDayMarker = year + "年" + month + "月" + previousDay + "日";

        String forwardSlash = "/";
        String urlMain = "http://news.mynavi.jp/list/headline/" + intYear + "/" + month + "/?page=";
        String siteName = "http://" + urlMain.split(forwardSlash)[2];
        String pageDateClass = "thumb-s__update";
        String elementClass = "thumb-s__item";

        String noArticlesNotice = "この時間に投稿されている記事がありませんでした！";

        String elementDateClass = Article.getElementDateClass();

        int waitTime = 1000;
        int page = 0;

        List<Article> articles = new ArrayList<>();

        while (true) {
            page += 1;
            String url = urlMain + page;

            Document doc = Jsoup.connect(url).get();
            Elements dateElements = doc.getElementsByClass(pageDateClass);

            //日付が正しくないときにスキップ、これでもったいない時間を過ごさない
            if (!correctDate(dateElements, dateMarker)) {
                continue;
            }

            Elements elements = doc.getElementsByClass(elementClass);
            if (elements.size() == 0) {
                break;
            }

            for (Element element : elements) {
                if (dateTimeStamp.equals(element.getElementsByClass(elementDateClass).text())) {
                    Article newArticle = new Article(element, siteName, year);
                    if (!articles.contains(newArticle)) {
                        articles.add(newArticle);
                    }
                }
            }
            if(passedDate(dateElements, previousDayMarker)) {
                break;
            }
            Thread.sleep(waitTime);
        }
        if (articles.size() == 0) {
            System.out.println(noArticlesNotice);
        }

        for(Article a : articles) {
            updateArticleInfo(a,a.getUrl());
            Thread.sleep(waitTime);
        }

        writeToHTML(articles);

    }


    private static void updateArticleInfo(Article article, String url) throws IOException{

        Document doc = Jsoup.connect(url).get();
        String articleBodyClass = "article-body";
        Elements articleElementList = doc.getElementsByClass(articleBodyClass);

        String source;
        
        for(Element element : articleElementList) {
            source = element.getElementsByTag("img").attr("src");
            article.setImgUrl(url + source);
            Elements paragraphList = element.getElementsByTag("p");
            for (Element paragraph : paragraphList) {
                article.addBody(paragraph.text());
            }

        }

    }

    private static void writeToHTML(List<Article> articleList) throws IOException{
        List<String> htmlOutput = getHTMLHeaderTemplate();
        String footer = "</body></html>";
        String filePath = System.getProperty("user.dir") + "/files/mynavi_articles_compilation.html";
        String utf8 = "UTF-8";

        for (Article article : articleList) {
            htmlOutput.add(getArticleDiv(article));
        }

        htmlOutput.add(footer);

        PrintWriter pw = new PrintWriter(filePath,utf8);

        for (String line : htmlOutput) {
            pw.println(line);
        }

        pw.close();
    }

    private static List<String> getHTMLHeaderTemplate() throws IOException {
        String path = System.getProperty("user.dir") + "/files/mynavi_article_page_header.txt";
        List<String> output = new ArrayList<>();
        File file = new File(path);
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader text = new BufferedReader(isr);
            String line;
            while ((line = text.readLine()) != null) {
                output.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    private static String getArticleDiv(Article article) {
        String path = System.getProperty("user.dir") + "/files/div_text.txt";
        String timePlaceholder = "<!-- $time -->";
        String titlePlaceholder = "<!-- $title -->";
        String imgTagPlaceholder = "<!-- $imgTag -->";
        String bodyPlaceHolder = "<!-- $articleBody -->";

        File file = new File(path);
        StringBuilder output = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader text = new BufferedReader(isr);
            String line;
            while ((line = text.readLine()) != null) {
                output.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String articleDiv = output.toString();

        articleDiv = articleDiv.replace(timePlaceholder,article.getPostTimeString());
        articleDiv = articleDiv.replace(titlePlaceholder,article.getTitle());
        articleDiv = articleDiv.replace(imgTagPlaceholder, article.getImgTag());
        articleDiv = articleDiv.replace(bodyPlaceHolder,article.getBody());

        return articleDiv;
    }


    private static boolean correctDate (Elements dateElements, String dateMarker) {
        // can be made to skip pages based on distance between current day and desired day.
        boolean correctDay = false;
        for(Element d : dateElements) {
            if(d.text().contains(dateMarker)) {
                correctDay = true;
            }
        }
        return correctDay;
    }

    private static boolean passedDate (Elements dateElements, String previousDayMarker) {
        boolean passed = false;
        for(Element d : dateElements) {
            if(d.text().contains(previousDayMarker)) {
                passed = true;
            }
        }
        return passed;
    }

}
