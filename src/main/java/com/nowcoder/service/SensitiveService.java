package com.nowcoder.service;


import org.apache.commons.lang.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import sun.text.normalizer.Trie;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤服务
 */
@Service
public class SensitiveService implements InitializingBean{
    private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);
    /**
     * 默认敏感词替换符
     */
    private static final String DEFAULT_REPLACEMENT = "***";
    @Override
    public void afterPropertiesSet() throws Exception {
        rootNode = new TrieNode();
        try {
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("SensitiveWords.txt");
            InputStreamReader read = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                lineTxt = lineTxt.trim();
                addWord(lineTxt);
            }
            read.close();
        } catch (Exception e) {
            logger.error("读取敏感词文件失败" + e.getMessage());
        }
    }

    /**
     *@Author LeonWang
     *@Description 添加敏感词
     */
    private void addWord(String lineTxt){
        if (lineTxt==null){
            return;
        }

        TrieNode tempNode = rootNode;

        for (int i = 0;i<lineTxt.length();i++){

            char c = lineTxt.charAt(i);
            if (isSymbol(c)){
                continue;
            }
            TrieNode node = tempNode.getSubNode(c);

            if (node==null){
                node = new TrieNode();
                tempNode.addSubNode(c,node);
            }
            //说明当树上已经有了当前位置要加字符的值
            tempNode = node;

            //到了敏感词的末尾，把末尾节点的end设置为true
            if (i==lineTxt.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 判断是否是一个符号
     */
    private boolean isSymbol(char c) {
        int ic = (int) c;
        // 0x2E80-0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }

    /**
     *@Author LeonWang
     *@Description 敏感词过滤的具体操作
     */
    public String filter(String text) {
        int position = 0;
        int begin = 0;
        TrieNode tempNode = rootNode;
        StringBuilder sb = new StringBuilder();
        String replacement = DEFAULT_REPLACEMENT;
        while (position<text.length()){
            //取出字符串当前字符
            char c = text.charAt(position);

            if (isSymbol(c)){
                if (tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            //从根节点开始，看下一个字符是否是当前字符
            tempNode = tempNode.getSubNode(c);
            if (tempNode==null){
                //不是，就说明不包含铭感词，直接加入结果
                sb.append(text.charAt(begin));
                position = begin+1;
                begin = position;
                tempNode = rootNode;
            }else if (tempNode.isKeywordEnd()){//到了敏感词结尾，返回替代敏感词字符串，position原来在铭感词最后+1，从下一个位置开始
                sb.append(replacement);
                position = position+1;
                begin = position;
                tempNode = rootNode;
            }else {
                position++;//包含敏感词的字符，但不能判断是否是敏感词
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    private class TrieNode{

        //key代表下一个字符，value代表节点
        private Map<Character,TrieNode> map = new HashMap<Character,TrieNode>();

        //true代表敏感词结尾
        private boolean end = false;

        public void addSubNode(Character key,TrieNode node){
            map.put(key,node);
        }

        public boolean isKeywordEnd(){
            return end;
        }

        //获取下一个节点
        public TrieNode getSubNode(Character key){
            return map.get(key);
        }

        public void setKeywordEnd(boolean end){
            this.end = end;
        }
    }

    private TrieNode rootNode = new TrieNode();

    public static void main(String[] argv) {
        SensitiveService s = new SensitiveService();
        s.addWord("色情");
        s.addWord("好色");
        System.out.print(s.filter("你好 la 色 情 a"));
    }
}
