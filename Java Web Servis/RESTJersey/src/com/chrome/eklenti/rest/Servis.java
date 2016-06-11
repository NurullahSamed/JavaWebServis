package com.chrome.eklenti.rest;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.zemberek.araclar.turkce.YaziBirimi;
import net.zemberek.araclar.turkce.YaziIsleyici;
import net.zemberek.erisim.Zemberek;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;
import net.zemberek.yapi.Kelime;
import net.zemberek.yapi.KelimeTipi;
 
@Path("/servis")
public class Servis {
	//Global De�i�ken
    /*
     * @GET annotation bu methodun HTTP GET isteklerini i�leyece�ini g�steriyor.
     */
    @POST
    /*
     * @Path annotation servisin hizmet verdi�i adresi g�sterir. Bir servis
     * i�inde farkl� i�lemleri ger�ekle�tiren hizmetler bulunabilir.
     */
    @Path("/id")
    /*
     * @Produces annotation istek kar��s�nda istemci g�nderilecek veri tipini
     * g�sterir. Hizmetin hangi veri tipiyle cevaplayaca�� belirlenir.
     */
    //@Produces(MediaType.APPLICATION_JSON)
    /*
     * @PathParam annotation @Path i�inde g�sterilen parametrenin ad�n�
     * g�sterir. Servisi kullan�rken yollanacak parametreler bu i�aretle
     * belirlenir.
     */
    public String postString(String paramID) throws SQLException {
    	
    	Connection baglanti = null;
    	Statement statement = null;
    	ResultSet resultSet = null;
    	
        String id = paramID;
        String[] kelimeler = id.split(" ");
        //System.out.print(id);
        
        Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());


        

       	baglanti = DriverManager.getConnection("jdbc:sqlserver://localhost:49480;databasename=text;user=secili_text;password=parola");
    	statement = baglanti.createStatement();
    		
  
        for(int j = 0 ;j < kelimeler.length;j++)
        {
        	 boolean uygun = false;
        	 
 
    		//Stop word kontol�
        	 boolean stpword = stopWords(kelimeler[j]);
    		 
    		 
        	 zemberek.asciidenTurkceye(kelimeler[j]);
        	 //T�rk�e ve isim tipindeki kelimeleri almak i�in
        	 if(zemberek.kelimeDenetle(kelimeler[j]))
        	 {

    	         Kelime[] ad = zemberek.kelimeCozumle(kelimeler[j]);
    	         String calisilanKelime [] = ad[0].toString().split(" ");
    	         if(calisilanKelime[4].indexOf("ISIM") != -1 || calisilanKelime[4].indexOf("ZAMAN") != -1 || calisilanKelime[4].indexOf("SIFAT") != -1)
    	         {
    	        	 uygun = true;
    	         } 
        	 }
      
        	 //System.out.println(kelimeler[j]);
        	 if(uygun && stpword)
        	 {
        		 Kelime[] ayrisimlar2 = zemberek.kelimeCozumle(kelimeler[j]);
        		 String [] kokler = ayrisimlar2[0].kok().toString().split(" ");
        		 //System.out.println(kokler[0]);
        		 
        		 //List<String[]> ayrisimlar = zemberek.kelimeAyristir(kelimeler[j]);
            	 //String[] kokler = ayrisimlar.get(0);
            	 statement.execute("insert into kokler (kokler) values('"+ kokler[0]+"')");
            	 
            	 resultSet = statement.executeQuery("select * from yazi");
            	 
            	 boolean bulundu = false;
            	 while (resultSet.next())
            	 {
            		 String siradaki = resultSet.getString("yazi");
            		 //System.out.println(siradaki);
            		 if(siradaki.equals(kokler[0])) 
            		 {
            			 bulundu = true;
            			 break;
            		 }
            	 }
            	 
            	 if(bulundu == false) //Gelen k�k daha �nce bulunmad� ise
            	 {
            		 statement.execute("insert into yazi (yazi,frekans) values('"+ kokler[0]+"',1)");
     
            	 }
            	 
            	 else
            	 {
            		 int frekans = resultSet.getInt("frekans");
            		 frekans++;
            		 statement.execute("update yazi set frekans = "+frekans+" where yazi ='" +kokler[0]+"'");
            	 }
            	 
        	 }
        }
        
       

        return "<Kullanici>" + "<ID>" + id + "</ID>" + "</Kullanici>";

    }
 
    @GET
@Path("/string2")
    @Produces(MediaType.TEXT_PLAIN)
    public String getString() throws SQLException {
       	Connection baglanti = null;
    	Statement statement = null;
    	ResultSet resultSet = null;
    	
	String sonuc = "";
    	
    	baglanti = DriverManager.getConnection("jdbc:sqlserver://localhost:49480;databasename=text;user=secili_text;password=parola");
    	statement = baglanti.createStatement();
    	resultSet = statement.executeQuery("select * from kokler");
    	 while (resultSet.next())
    	 {
    		 sonuc = sonuc+" "+resultSet.getString("kokler");
    	 }
    	 
    	 statement.execute("delete from kokler");
    	
 
        return sonuc;
    }
    
    @GET
    @Path("/tumtext")
    @Produces(MediaType.TEXT_PLAIN)
    public String getTumText() throws SQLException{
    	
    	Connection baglanti = null;
    	Statement statement = null;
    	ResultSet resultSet = null;
    	
    	
    	baglanti = DriverManager.getConnection("jdbc:sqlserver://localhost:49480;databasename=text;user=secili_text;password=parola");
    	statement = baglanti.createStatement();
    	resultSet = statement.executeQuery("select * from yazi ORDER BY frekans DESC ");
    	resultSet.next();
    	String sonuc = "";
    	for(int i = 0 ; i <5 ; i++)
    	{
    		sonuc = sonuc+" "+ resultSet.getString("yazi");
    		resultSet.next();
    	}
    	
    	
    	return sonuc;
    	
    }
    
    public boolean stopWords (String kelime){
		
    	String stopwords  [] = {"a", "acaba","alt�","ama","ancak","art�k","asla","asl�nda","az","b","bana","bazen","baz�","baz�lar�"
    			, "baz�s�","belki","ben","beni","benim","be�","bile","bir","bir�o�u","bir�ok","bir�oklar�","biri","birisi","birka�",
    			"birka��","bir�ey","bir�eyi","biz","bize","bizi","bizim","b�yle","b�ylece","bu","buna",
    			"bunda","bundan","bunu","bunun","burada","b�t�n","c","�","�o�u","�o�una",
    			"�o�unu","�ok","��nk�","d","da","daha","de","de�il","demek","di�er","di�eri",
    			"di�erleri","diye","dokuz","dolay�","d�rt","e","elbette","en","f","fakat","falan","felan","filan",
    			"g","gene","gibi","�","h","h�l�","hangi","hangisi","hani","hatta","hem",
    			"hen�z","hep","hepsi","hepsine","hepsini","her","her biri","herkes","herkese",
    			"herkesi","hi�","hi� kimse","hi�biri","hi�birine","hi�birini","�","i","i�in",
    			"i�inde","iki","ile","ise","i�te","j","k","ka�","kadar","kendi","kendine",
    			"kendini","ki","kim","kime","kimi","kimin","kimisi","l","m","madem","m�","mi",
    			"mu","m�","n","nas�l","ne","ne kadar","ne zaman","neden","nedir","nerde","nerede",
    			"nereden","nereye","nesi","neyse","ni�in","niye","o","on","ona","ondan",
    			"onlar","onlara","onlardan","onlar�n","onlar�n","onu","onun","orada","oysa",
    			"oysaki","�","�b�r�","�n","�nce","�t�r�","�yle","p","r","ra�men","s","sana",
    			"sekiz","sen","senden","seni","senin","siz","sizden","size","sizi","sizin",
    			"son","sonra","�","�ayet","�ey","�eyden","�eye","�eyi","�eyler","�imdi",
    			"��yle","�u","�una","�unda","�undan","�unlar","�unu","�unun","t","tabi","tamam",
    			"t�m","t�m�","u","�","��","�zere","v","var","ve","veya","veyahut","y","ya","ya da",
    			"yani","yedi","yerine","yine","yoksa","z","zaten","zira"};
    	
    	for(int i = 0 ; i < stopwords.length;i++)
    	{
    		if(kelime.equals(stopwords[i]))
    		{
    			return false;
    		}
    	}
    	
    	return true;
    	
    	
    }
    
 
}