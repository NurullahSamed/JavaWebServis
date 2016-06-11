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
	//Global Deðiþken
    /*
     * @GET annotation bu methodun HTTP GET isteklerini iþleyeceðini gösteriyor.
     */
    @POST
    /*
     * @Path annotation servisin hizmet verdiði adresi gösterir. Bir servis
     * içinde farklý iþlemleri gerçekleþtiren hizmetler bulunabilir.
     */
    @Path("/id")
    /*
     * @Produces annotation istek karþýsýnda istemci gönderilecek veri tipini
     * gösterir. Hizmetin hangi veri tipiyle cevaplayacaðý belirlenir.
     */
    //@Produces(MediaType.APPLICATION_JSON)
    /*
     * @PathParam annotation @Path içinde gösterilen parametrenin adýný
     * gösterir. Servisi kullanýrken yollanacak parametreler bu iþaretle
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
        	 
 
    		//Stop word kontolü
        	 boolean stpword = stopWords(kelimeler[j]);
    		 
    		 
        	 zemberek.asciidenTurkceye(kelimeler[j]);
        	 //Türkçe ve isim tipindeki kelimeleri almak için
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
            	 
            	 if(bulundu == false) //Gelen kök daha önce bulunmadý ise
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
		
    	String stopwords  [] = {"a", "acaba","altý","ama","ancak","artýk","asla","aslýnda","az","b","bana","bazen","bazý","bazýlarý"
    			, "bazýsý","belki","ben","beni","benim","beþ","bile","bir","birçoðu","birçok","birçoklarý","biri","birisi","birkaç",
    			"birkaçý","birþey","birþeyi","biz","bize","bizi","bizim","böyle","böylece","bu","buna",
    			"bunda","bundan","bunu","bunun","burada","bütün","c","ç","çoðu","çoðuna",
    			"çoðunu","çok","çünkü","d","da","daha","de","deðil","demek","diðer","diðeri",
    			"diðerleri","diye","dokuz","dolayý","dört","e","elbette","en","f","fakat","falan","felan","filan",
    			"g","gene","gibi","ð","h","hâlâ","hangi","hangisi","hani","hatta","hem",
    			"henüz","hep","hepsi","hepsine","hepsini","her","her biri","herkes","herkese",
    			"herkesi","hiç","hiç kimse","hiçbiri","hiçbirine","hiçbirini","ý","i","için",
    			"içinde","iki","ile","ise","iþte","j","k","kaç","kadar","kendi","kendine",
    			"kendini","ki","kim","kime","kimi","kimin","kimisi","l","m","madem","mý","mi",
    			"mu","mü","n","nasýl","ne","ne kadar","ne zaman","neden","nedir","nerde","nerede",
    			"nereden","nereye","nesi","neyse","niçin","niye","o","on","ona","ondan",
    			"onlar","onlara","onlardan","onlarýn","onlarýn","onu","onun","orada","oysa",
    			"oysaki","ö","öbürü","ön","önce","ötürü","öyle","p","r","raðmen","s","sana",
    			"sekiz","sen","senden","seni","senin","siz","sizden","size","sizi","sizin",
    			"son","sonra","þ","þayet","þey","þeyden","þeye","þeyi","þeyler","þimdi",
    			"þöyle","þu","þuna","þunda","þundan","þunlar","þunu","þunun","t","tabi","tamam",
    			"tüm","tümü","u","ü","üç","üzere","v","var","ve","veya","veyahut","y","ya","ya da",
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