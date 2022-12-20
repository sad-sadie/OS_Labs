package com.my;

import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.IOException;

public class FileReader {

    public ArrayList<String> readFile(String fileName) {
        ArrayList<String> tokens = new ArrayList<>();

        try {
            Scanner sf = new Scanner(new File(fileName + ".txt"));
            while(sf.hasNext())
                tokens.add(sf.next());
        } catch (IOException e){
            Main.log.error(e);
        }

        return tokens;
    }
}