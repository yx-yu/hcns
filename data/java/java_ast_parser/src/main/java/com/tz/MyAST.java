package com.tz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.javaparser.utils.Utils;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.javaparser.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class MyAST {
    private int nodeCount;

    public String output(Node node){
        this.nodeCount = 0;
        HashMap<Integer, MyNode> nodes = new LinkedHashMap<>();
        this.output(node, null, "root", nodes);
        return JSON.toJSONString(nodes.values());
    }

    public void output(Node node, Integer parentId, String name, HashMap<Integer, MyNode> output){
        Utils.assertNotNull(node);
        NodeMetaModel metaModel = node.getMetaModel();
        List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();
        List<PropertyMetaModel> attributes = (List)allPropertyMetaModels.stream().filter(PropertyMetaModel::isAttribute).filter(PropertyMetaModel::isSingular).collect(Collectors.toList());
        List<PropertyMetaModel> subNodes = (List)allPropertyMetaModels.stream().filter(PropertyMetaModel::isNode).filter(PropertyMetaModel::isSingular).collect(Collectors.toList());
        List<PropertyMetaModel> subLists = (List)allPropertyMetaModels.stream().filter(PropertyMetaModel::isNodeList).collect(Collectors.toList());
        Integer ndId = this.nextNodeId();

        MyNode myNode = new MyNode();
        Optional<Range> range = node.getRange();
        if(range.isPresent()){
            myNode.setLineNum(range.get().begin.line);
        }
        myNode.setType( metaModel.getTypeName());
        if(parentId != null){
            output.get(parentId).addChild(ndId);
        }
        output.put(ndId, myNode);

        Iterator var11 = attributes.iterator();

        PropertyMetaModel sl;
        while(var11.hasNext()) {
            sl = (PropertyMetaModel)var11.next();
            String slName = sl.getName();
            String value = sl.getValue(node).toString();

            if("identifier".equals(slName)){
                myNode.setValue(value);
            }else if("value".equals(slName)){
                if(isNumber(value)) {
                    myNode.setValue(value);
                }else{
                    myNode.setType(myNode.type + " (STR)");
                }
            }else{
                myNode.setType(myNode.type + " (" + value + ")");
            }
        }

        var11 = subNodes.iterator();

        while(var11.hasNext()) {
            sl = (PropertyMetaModel)var11.next();
            Node nd = (Node)sl.getValue(node);
            if (nd != null) {
                this.output(nd, ndId, sl.getName(), output);
            }
        }

        var11 = subLists.iterator();

        while(true) {
            NodeList nl;
            do {
                do {
                    if (!var11.hasNext()) {
                        return;
                    }

                    sl = (PropertyMetaModel)var11.next();
                    nl = (NodeList)sl.getValue(node);
                } while(nl == null);
            } while(!nl.isNonEmpty());

            Integer ndLstId = this.nextNodeId();

            MyNode n = new MyNode();
            n.setType(sl.getName());
            n.setLineNum(myNode.getLineNum());
            myNode.addChild(ndLstId);
            output.put(ndLstId, n);

            String slName = sl.getName().substring(0, sl.getName().length() - 1);
            Iterator var16 = nl.iterator();

            while(var16.hasNext()) {
                Node nd = (Node)var16.next();
                this.output(nd, ndLstId, slName, output);
            }
        }
    }

    private Integer nextNodeId() {
        return this.nodeCount++;
    }

    public  boolean isNumber(String s){//合法数字返回true
        //这个正则表达式能够过滤0.0.0、8-99这种不合法的数字
        String reg="^[-\\\\+]?([0-9]+\\\\.?)?[0-9]+$";
        return  s.matches(reg);
    }

    public static class MyNode{
        private String type;
        private String value;
        private List<Integer> children;

        private Integer lineNum;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Integer getLineNum() {
            return lineNum;
        }

        public void setLineNum(Integer lineNum) {
            this.lineNum = lineNum;
        }

        public List<Integer> getChildren(){return children;}

        public void addChild(Integer childId){
            if(null == children){
                children = new ArrayList<>();
            }
            children.add(childId);
        }

        public void setChildren(List<Integer> children) {
            this.children = children;
        }

    }

    public static void main(String[] args){
         MyAST.MyNode m = new MyAST.MyNode();
        System.out.println(JSON.toJSONString(m));
    }
}
