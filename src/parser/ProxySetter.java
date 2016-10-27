/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ProxySetter {

    static String testLink = "http://google.com"; // указываем сайт для проверки, т.е.  
    // страницу к которой будем обращаться
    // через прокси
    public static int timeout = 5000; // время ожидания ответа (в милисекундах) - для функции check()

    public boolean useGimmeproxyAPI = true;

    public boolean useLocalFiles = true;

    public String filePath = System.getProperty("user.dir") + "/validProxyEN/";
    
    public static ArrayList<String[]> proxyList = new ArrayList<String[]>();
    

    // устанавливается пркси как системный параметр. прокси берется из api Gimmeproxy
    // если прокси недоступно или не на английском, рекурсивно вызываеся снова: берется новое прокси из api Gimmeproxy, снова проверяется
    public void setNewProxy() {

        String[] proxyArr = null;

        if (useLocalFiles) {
            readDir(filePath);
            Iterator<String[]> iter = proxyList.iterator();
            proxyArr = iter.next();
            iter.remove();
        } else if (useGimmeproxyAPI) {
            while (true) {
                try {
                    proxyArr = getProxyFromAPIGimmeproxy();
                    break;
                } catch (IOException ex) {
                    Logger.getLogger(ProxySetter.class.getName()).log(Level.SEVERE, null, ex);
                    resetProxy();
                }
            }
        }
        boolean isProxyValid = setProxyEN(proxyArr[0], proxyArr[1], proxyArr[2]);

        if (!isProxyValid) {
            resetProxy();
            setNewProxy();
        } else if(useLocalFiles){
            Date now = new Date();
            try {
                String urlDir = filePath;
                File folder = new File(urlDir);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                saveArrToFile(urlDir+DateFormat.getDateInstance().format(now), proxyArr);
            } catch (IOException ex) {
                Logger.getLogger(ProxySetter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
  
    // читается файл со списком прокси, возвращается коллекция массивов прокси
    private static ArrayList<String[]> readProxyFile(File filePath) {

        String line;
        
        ArrayList<String[]> nProxyList = new ArrayList<String[]>();

        String[] currProxyArr = new String[3];
        BufferedReader readFromFile = null;
        try {
            readFromFile = new BufferedReader(new FileReader(filePath));
            while (true) {
                line = readFromFile.readLine();
                if (line == null) {
                    break;
                }
                currProxyArr = line.split(":");
                nProxyList.add(currProxyArr);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProxySetter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProxySetter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (readFromFile != null) {

                try {
                    readFromFile.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProxySetter.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

        return nProxyList;

    }
    
    public static ArrayList<String[]> readDir(String Path){
        File folder = new File(Path);
        // Если это каталог
        if (folder.isDirectory()) {
            for (File f : folder.listFiles()) 

               proxyList.addAll(readProxyFile(f));
                
                }
        return proxyList;
    }

    // проверяется прокси на доступность
    private static boolean check(String pHost, int pPort, String pType) {
        SocketAddress addr = new InetSocketAddress(pHost, pPort);
        Proxy.Type _pType = (pType.equals("HTTP") ? Proxy.Type.HTTP : Proxy.Type.SOCKS);
        Proxy httpProxy = new Proxy(_pType, addr);
        HttpURLConnection urlConn = null;
        URL url;
        try {
            url = new URL(testLink);
            urlConn = (HttpURLConnection) url.openConnection(httpProxy);
            urlConn.setConnectTimeout(timeout);
            urlConn.connect();
            return (urlConn.getResponseCode() == 200);
        } catch (SocketException e) {
            return false;
        } catch (SocketTimeoutException e) {
            return false;
        } catch (Exception e) {
            System.out.print("Error: " + e);
            return false;
        }
    }
 
    // сбрасывает системные настройки прокси
    private void resetProxy() {

           
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");

    }

   // Проверяет, на английском-ли языке страница алиекспресс
    private static boolean setProxyEN(String pHost, String pPort, String pType) {

        boolean result = false;
       
// Не работает ни хуя
//         Setup proxy
//        Proxy proxy = new Proxy( //
//                pType == "HTTP" ? Proxy.Type.HTTP : Proxy.Type.SOCKS, //
//                InetSocketAddress.createUnresolved(pHost, Integer.parseInt(pPort)) //
//        );
// ставим системные параметры прокси. Вот они работают!
        if (pType.equals("http")) {
            System.setProperty("http.proxyHost", pHost);
            System.setProperty("http.proxyPort", pPort);
        } else {
            System.setProperty("socksProxyHost", pHost);
            System.setProperty("socksProxyPort", pPort);
        }

        Document doc = null;
        try {
            doc = Jsoup
                    .connect("http://www.aliexpress.com")
                    //.proxy(proxy) // - это то что не работает ни хуя, doc == null
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/51.0.2704.79 Chrome/51.0.2704.79 Safari/537.36")
                    //.referrer("http://www.google.com")
                    .get();
        } catch (IOException ex) {
            // ошибка при получении страницы, возвращается false
            //Logger.getLogger(ProxyES.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Нерабочий прокси - " + pHost + ":" + pPort + " " + pType);
            return result = false;
        }

        // это строка для проверки, на английском языке-ли сайт
        String strTitle = "AliExpress.com - Online Shopping for Electronics, Fashion, Home & Garden, Toys & Sports, Automobiles from China.";

        // вот сравниваем строку заголовка страницы со строкой которая должна быть в английской версии
        if (doc.title().equals(strTitle)) {
            result = true; // тогда true
        } else{
            System.out.println("Не английская страница, " + pHost + ":" + pPort + " " + pType);
        }
            

        return result;
    }

    private static String[] getProxyFromAPIGimmeproxy() throws MalformedURLException, IOException {

        URL url = new URL("http://gimmeproxy.com/api/getProxy?anonymityLevel=1&country=us");

        String pIpText = "";
        String pPortText = "";
        String pProtocolText = "";

        // get URL content
        URLConnection conn = null;
        try {
            conn = url.openConnection();
        } catch (IOException ex) {
            //Logger.getLogger(ProxyES.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("не удалось соединиться с API Gymmeproxy");
        }

        // open the stream and put it into BufferedReader
        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

        String inputLine;
        while ((inputLine = br.readLine()) != null) {
            if (inputLine.contains("\"ip\":")) {
                pIpText = inputLine.replaceAll("[^\\d.]", "");
            }
            if (inputLine.contains("\"port\":")) {
                pPortText = inputLine.replaceAll("[^\\d]", "");
            }
            if (inputLine.contains("\"protocol\":")) {
                pProtocolText = inputLine.replaceAll("^.*\": \"|\",$", "");
            }
        }
        br.close();

        String[] nProxyArray = {pIpText, pPortText, pProtocolText};

        return nProxyArray;

    }
    
        private static void saveArrToFile(String URL, String[] proxy) throws IOException {
        FileWriter writer = new FileWriter(URL, true);
        
            writer.write(proxy[0] + ":" + proxy[1] + ":" + proxy[2] + "\n");
        
        writer.close();

    }

}
