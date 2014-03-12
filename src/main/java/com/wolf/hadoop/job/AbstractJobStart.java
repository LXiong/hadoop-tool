package com.wolf.hadoop.job;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author aladdin
 */
public abstract class AbstractJobStart extends Configured implements Tool {

    private final String parameterPath = "parameter.json";
    private final Map<String, ParameterEntity> parameterMap = new HashMap<String, ParameterEntity>(16, 1);

    public AbstractJobStart() {
        //读取参数文件
        String path = AbstractJobStart.class.getClassLoader().getResource(this.parameterPath).getPath();
        if (path.isEmpty()) {
            System.out.println("--not find parameter.json.");
        } else {
            System.out.println("read properties path:" + path);
            JsonNode rootNode = null;
            File file = new File(path);
            ObjectMapper mapper = new ObjectMapper();
            try {
                rootNode = mapper.readValue(file, JsonNode.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (rootNode != null) {
                //读数据
                Map.Entry<String, JsonNode> entry;
                String name;
                String value;
                String describe;
                Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.getFields();
                JsonNode jsonNode;
                ParameterEntity parameterEntity;
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    name = entry.getKey();
                    jsonNode = entry.getValue().get("value");
                    if (jsonNode == null) {
                        value = "";
                    } else {
                        value = jsonNode.getTextValue();
                    }
                    jsonNode = entry.getValue().get("describe");
                    if (jsonNode == null) {
                        describe = "";
                    } else {
                        describe = jsonNode.getTextValue();
                    }
                    parameterEntity = new ParameterEntity(name, describe);
                    parameterEntity.setValue(value);
                    this.parameterMap.put(name, parameterEntity);
                }
            }
        }
    }

    public final String getParameter(String name) {
        return this.parameterMap.get(name).getValue();
    }

    /**
     * 实例化job
     *
     * @return
     * @throws Exception
     */
    public abstract Job createJob() throws Exception;

    /**
     * 获取必要参数
     *
     * @return
     */
    public abstract String[] getValidateParameter();

    @Override
    public final int run(String[] args) throws Exception {
        int result = 0;//return
        if (args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) {
            //帮助说明
            this.parameterMap.entrySet();
            Set<Entry<String, ParameterEntity>> entrySet = this.parameterMap.entrySet();
            for (Entry<String, ParameterEntity> entry : entrySet) {
                System.out.println("--name:" + entry.getKey() + " value:" + entry.getValue().getValue());
                System.out.println("----describe:" + entry.getValue().getDescribe());
            }
        } else {
            //处理输入参数
            String text;
            String[] para;
            ParameterEntity parameterEntity;
            for (int index = 0; index < args.length; index++) {
                text = args[index];
                para = text.split("=");
                if (para.length == 2) {
                    //符合要求的参数格式
                    parameterEntity = this.parameterMap.get(para[0]);
                    if (parameterEntity != null && para[1].isEmpty() == false) {
                        parameterEntity.setValue(para[1]);
                    }
                }
            }
            //验证参数
            String[] paras = this.getValidateParameter();
            String name;
            String value;
            for (int index = 0; index < paras.length; index++) {
                name = paras[index];
                value = this.getParameter(name);
                if (value == null || value.isEmpty()) {
                    String message = "miss parameter:" + name;
                    throw new RuntimeException(message);
                } else {
                    System.out.println("parameter info----name:" + name + "  value:" + value);
                }
            }
            //创建job，并执行
            Job job = this.createJob();
            //关闭预测执行
            job.setMapSpeculativeExecution(false);
            job.setReduceSpeculativeExecution(false);
            result = job.waitForCompletion(true) ? 0 : 1;
        }
        return result;
    }
}
