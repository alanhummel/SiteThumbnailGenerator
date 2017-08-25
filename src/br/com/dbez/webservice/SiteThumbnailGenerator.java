package br.com.dbez.webservice;

import javax.jws.WebMethod;  
import javax.jws.WebService; 

@WebService  
public interface SiteThumbnailGenerator {
	 @WebMethod public String generateThumbnail(String name);
	 
}
