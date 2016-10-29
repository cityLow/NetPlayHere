package com.hl.netplayhere.sensitive;

import android.content.Context;

import com.hl.netplayhere.sensitive.conf.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * 
 * 简单的敏感词处理器，从配置文件读取敏感词初始化，<br>
 * 使用者只需要在classpath放置sensitive-word.properties敏感词文件即可
 * 
 * @author hailin0@yeah.net
 * @createDate 2016年5月22日
 *
 */
public class SimpleKWSeekerProcessor extends KWSeekerManage {

    private static volatile SimpleKWSeekerProcessor instance;

    /**
     * 获取实例
     * 
     * @return
     */
    public static SimpleKWSeekerProcessor newInstance(Context context) {
        if (null == instance) {
            synchronized (SimpleKWSeekerProcessor.class) {
                if (null == instance) {
                    instance = new SimpleKWSeekerProcessor(context);
                }
            }
        }
        return instance;
    }

    /**
     * 私有构造器
     */
    private SimpleKWSeekerProcessor(Context context) {
        initialize(context);
    }

    /**
     * 初始化敏感词
     */
    private void initialize(Context context) {
        Map<String, String> map = Config.newInstance(context).getAll();
        Set<Entry<String, String>> entrySet = map.entrySet();

        Map<String, KWSeeker> seekers = new HashMap<String, KWSeeker>();
        Set<KeyWord> kws;

        for (Entry<String, String> entry : entrySet) {
            String[] words = entry.getValue().split(",");
            kws = new HashSet<>();
            for (String word : words) {
                kws.add(new KeyWord(word));
            }
            seekers.put(entry.getKey(), new KWSeeker(kws));
        }
        this.seekers.putAll(seekers);
    }
}
