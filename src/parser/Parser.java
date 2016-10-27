/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import productDAO.*;

/**
 *
 * @author user
 */
public class Parser {

    /**
     * @param args the command line arguments
     */
    static ArrayList<String> linksPagesArrList = new ArrayList<String>();
    static ArrayList<String> linksProductsArrList = new ArrayList<String>();

    static ArrayList<String> linksProducts;

    static Connection con;

    static String lastProdCaption;

    private static ArrayList<String> getLinksProducts(String URL) throws IOException {

        if (con == null) {
            try {
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/fb7967y9_chamo", "root", "root");
            } catch (SQLException ex) {
                Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        PageDAO pageConn = new PageDAO(con);
        String caption = pageConn.getLastProductCaption();
        String linkProd;
        ArrayList<String> currLinksProdArrList = new ArrayList<String>();

        Document doc = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .get();
        Elements linksProducts = doc.getElementsByClass("product");
        if (lastProdCaption != "") {

            for (Element linkProdEl : linksProducts) {
                if (pageConn.getPageByText(linkProdEl.text()) != null) {
                    System.out.println("страница уже существует в базе");
                    continue;
                }
                linkProd = linkProdEl.attr("abs:href");
                linksProductsArrList.add(linkProd);
                currLinksProdArrList.add(linkProd);
            }
        }

        return currLinksProdArrList;
    }

    private static String getLinkNextPage(String URL) {

        Document doc;
        try {
            doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/51.0.2704.79 Chrome/51.0.2704.79 Safari/537.36")
                    //.referrer("http://www.google.com")
                    .get();
        } catch (IOException ex) {

            //Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("страница Али не получена");

            return null;
        }
        Elements linksProducts = doc.getElementsByClass("page-next");
        if (linksProducts.size() == 0) {
            return "";
        }
        String nextPage = linksProducts.get(0).attr("abs:href");

        return nextPage;

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        String[] categoryURLs = new String[5];

// Women's Clothing & Accessories
        categoryURLs[0] = "http://www.aliexpress.com/w/wholesale-boho.html?initiative_id=AS_20160823025413&site=glo&g=y&SearchText=boho&CatId=100003109&isrefine=y";
// Jewelry & Accessories
        categoryURLs[1] = "http://www.aliexpress.com/w/wholesale-boho.html?site=glo&g=y&SearchText=boho&CatId=1509&isrefine=y";
// Home Decor
        categoryURLs[2] = "http://www.aliexpress.com/w/wholesale-boho.html?site=glo&g=y&SearchText=boho&needQuery=n&CatId=3710&isrefine=y";
// Luggage & Bags
        categoryURLs[3] = "http://www.aliexpress.com/w/wholesale-boho.html?initiative_id=AS_20160823005026&site=glo&g=y&SearchText=boho&needQuery=n&CatId=1524&isrefine=y";
// Shoes
        categoryURLs[4] = "http://www.aliexpress.com/w/wholesale-boho.html?initiative_id=AS_20160823005026&site=glo&g=y&SearchText=boho&needQuery=n&CatId=322&isrefine=y";

        //ArrayList<String> links = getNextLinksPage(categoryURLs[0]);
        for (String currCatURL : categoryURLs) {
            parse(currCatURL, true);
        }
    }

    private static void parse(String URL, boolean setProxy) {

        ProxySetter newProxy = new ProxySetter();

        if (setProxy) {
            newProxy.setNewProxy();
        }

        try {
            linksProducts = getLinksProducts(URL);
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            parse(URL, true);
        }

        while (true) {
            try {
                if (parseProducts(linksProducts)) {
                    break;
                }
            } catch (IOException ex) {
                Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String linkNextPage = null;

        while (true) {
            linkNextPage = getLinkNextPage(URL);
            if (Objects.isNull(linkNextPage)) {
                newProxy.setNewProxy();
            } else {
                break;
            }
        }

        parse(linkNextPage, false);

    }

    private static boolean parseProducts(ArrayList<String> nLinksProducts) throws IOException {

        ProxySetter newProxy = new ProxySetter();
        ArrayList<String> unparsedURL = new ArrayList<>();
        Iterator<String> iter = nLinksProducts.iterator();

        while (iter.hasNext()) {

            String prodURL = iter.next();

            Document doc = null;
            try {
                doc = Jsoup.connect(prodURL)
                        .timeout(5000)
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/51.0.2704.79 Chrome/51.0.2704.79 Safari/537.36")
                        //.referrer("http://www.google.com")
                        .get();
            } catch (IOException ex) {
                //Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("страница Али не получена");
                newProxy.setNewProxy();
                continue;
            }
            // если выкидывает на главную
            if (doc.title().equals("Buy Products Online from China Wholesalers at Aliexpress.com")) {
                newProxy.setNewProxy();
                continue;
            }

            System.out.println(doc.title());
            try {
                parseToBase(doc);
            } catch (SQLException ex) {
                Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            }
            iter.remove();
        }

        if (linksProducts.size() > 0) {
            parseProducts(linksProducts);
        }
        return true;

    }

    private static void parseToBase(Document doc) throws SQLException {

        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/fb7967y9_chamo", "root", "root");
        String url = "";
        String urlPrev = "";

        ArrayList<String[]> catArr = new ArrayList<String[]>();
        HashMap catHashMap = new HashMap();
        Elements categories = doc.select("div.ui-breadcrumb div.container a");
        ListIterator<Element> iter = categories.listIterator();
        PageDAO PageConn = new PageDAO(con);

        // предыд. элемент в цикле
        Page pagePrev = null;

        while (iter.hasNext()) {

            Element el = iter.next();
            // завершаем итерацию если в ссылке нет слова "category" 
            if (!el.attr("abs:href").contains("category")) {
                continue;
            }
            // получить страницу из базы для проверки её наличия
            Page page = PageConn.getPageByCaption(el.select("a").text());
            Page pageIns = new Page();

            String[] urlSplit = el.attr("abs:href").split("/");
            url = urlPrev + "/" + urlSplit[urlSplit.length - 1].replace(".html", "").toLowerCase();

            // если страница существует в базе
            if (page != null) {
                pagePrev = PageConn.getPageByCaption(el.select("a").text());
                urlPrev = url;
                continue;
            }

            if (pagePrev != null) {

                int PrevPageId = pagePrev.getId();
                pageIns.setPid(PrevPageId);

            }

            pageIns.setCaption(el.select("a").text());

            pageIns.setUrl(url + "/");
            PageConn.updatePage(pageIns);

            // текущий элемент - в след. итерации как предыдущий
            pagePrev = PageConn.getPageByCaption(el.select("a").text());
            urlPrev = url;

        }

        //Запись страницы товара
        Page pageProd = new Page();
        pageProd.setPid(pagePrev.getId());
        pageProd.setCaption(doc.title().replaceAll(" from.*", ""));
        pageProd.setDescription(doc.select("[name=description]").attr("content").replaceAll(" at Ali.*", "").replace("from China ", ""));
        pageProd.setKeywords(doc.select("[name=keywords]").attr("content").replaceAll(", China.*", ""));
        String[] urlSplit = doc.select("link[rel=canonical]").attr("href").split("/");
        url = urlPrev + "/" + urlSplit[urlSplit.length - 2] + "/";
        pageProd.setUrl(url.toLowerCase());
        pageProd.setText(doc.select("h1.product-name").text());
        PageConn.updatePage(pageProd);

        //Заполнение характеристик товара
        FeaturesDAO featuresConn = new FeaturesDAO(con);
        Features features = new Features();
        features.setPid(PageConn.getPageByCaption(pageProd.getCaption()).getId());

        String price = doc.select("del.p-del-price-content").text();
        //Если нет скидки
        if(price.equals("")){
            price = doc.select("div.p-price-content").text();
        }
        features.setKey("price");
        features.setValue(price);
        featuresConn.updatePage(features);
        Elements attributes = doc.select("#j-product-info-sku dl.p-property-item");
        for (Element nAtt : attributes) {
            features.setKey(nAtt.select("dt.p-item-title").text());
            Elements images = nAtt.select("dd.p-item-main li.item-sku-image a");
            if (images.size() > 0) {
                features.setValue(nAtt.select("dd.p-item-main li.item-sku-image a").html());
            } else {
                features.setValue(" " + nAtt.select("dd.p-item-main li").text());
            }
            featuresConn.updatePage(features);
        }

        //Картинки
        Elements imagesThumb = doc.select("ul.image-thumb-list img");
        int i = 0;
        for (Element img : imagesThumb) {
            i++;
            features.setKey("thumb_" + i);
            features.setValue(img.attr("src"));
            featuresConn.updatePage(features);
        }
        
        //Доп. характеристики
        
        Elements addAttributes = doc.select("ul.product-property-list li.property-item");
        for (Element currAttr : addAttributes) {
            features.setKey(currAttr.select("span.propery-title").text());
            features.setValue(currAttr.select("span.propery-des").text());
            featuresConn.updatePage(features);
        }

    }

}