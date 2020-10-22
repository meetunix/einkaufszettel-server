package de.nachtsieb.einkaufszettelServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class ResourceLoader {
	
	
    public File getFileFromResources(String fileName) {

        ClassLoader classLoader = getClass().getClassLoader();

        URL resource = classLoader.getResource(fileName);
        
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }

    }

    public String getFileasString(File file) throws IOException {

        if (file == null) return null;

        try (FileReader reader = new FileReader(file);
             BufferedReader br = new BufferedReader(reader)) {
        	
        
        	StringBuilder sb = new StringBuilder();
        	
            String line;
            while ((line = br.readLine()) != null) {
            	sb.append(line);
            }

        	return sb.toString();
        }
    }

}
