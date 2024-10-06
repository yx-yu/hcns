package com.tz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Parser {
    public static void forSummarization(String fileName, String dirName) throws FileNotFoundException {
        ArrayList<String[]> data = read(fileName);
        if(!dirName.endsWith("/")){
            dirName = dirName + "/";
        }

        Iterator var4 = data.iterator();

        List<String> asts = new ArrayList<>();
        List<String> codes = new ArrayList<>();

        int cnt = 0;
        while(var4.hasNext()) {
            String[] line = (String[]) var4.next();
            String code = line[0];
            String ast = AstParser.toAST(code);
            asts.add(ast);

            code = code.replaceAll("\n"," DCNL ");
            code = code.replaceAll("\t", " DCSP ");
            codes.add(code);
        }

        write(asts, dirName + "ast.original");
        write(codes, dirName + "code.original");
        System.out.println("COMPLETE");
    }

    public static void write(List<String> data, String fileName){
        Path path = Paths.get(fileName);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)){
            for(String d: data){
                bufferedWriter.write(d + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String[]> read(String fileName) throws FileNotFoundException{
        Scanner sc = new Scanner(new File(fileName));

        ArrayList res = new ArrayList();
        while(sc.hasNextLine()){
            String line = sc.nextLine().trim();
            if(line.length() != 0){
                JSONObject json = JSON.parseObject(line);
                String code = json.getString("code");
                String nl = json.getString("comment");

                res.add(new String[]{code, nl});
            }
        }
        return res;
    }



    public static void main(String[] args) throws FileNotFoundException{
        int index = 0;
        String fileName = null;
        String dirName = null;

        while(index < args.length) {
            if (args[index].equals("-f")) {
                fileName = args[index + 1].trim();
                index += 2;
            } else if (args[index].equals("-d")) {
                dirName = args[index + 1].trim();
                index += 2;
            } else {
                System.out.println(args[index++]);
            }
        }

        if (fileName == null) {
            throw new RuntimeException("Please specify the input file: -f [filename]");
        } else if (dirName == null) {
            throw new RuntimeException("Please specify the output directory: -d [dirname]");
        } else {
            forSummarization(fileName, dirName);
        }
    }
}
