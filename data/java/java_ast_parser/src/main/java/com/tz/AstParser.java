package com.tz;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.*;

public class AstParser {

    public static String toAST(String source){
        try{
            CompilationUnit cu = JavaParser.parse("class DUMMY {" + source + "}");
            Node node = (Node) cu.getChildNodes().get(0).getChildNodes().get(1);
            MyAST myAST = new MyAST();
            String output =  myAST.output(node);
            return output;
        }catch (ParseProblemException var4) {
            return interfaceCase(source);
        }
    }

    public static String interfaceCase(String source){
        try{
            CompilationUnit cu = JavaParser.parse("interface DUMMY {" + source + "}");
            Node node = (Node) ((Node) cu.getChildNodes().get(0)).getChildNodes().get(1);
            MyAST myAST = new MyAST();
            String output =  myAST.output(node);
            return output;
        }catch (ParseProblemException var4){
            System.out.println("error:" + var4);
            return null;
        }
    }

}
