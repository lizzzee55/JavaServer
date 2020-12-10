package ru.classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Exception;
import java.util.Map;
import java.util.HashMap;
import com.yahoo.platform.yui.compressor.*;


public class Template {
    private static Template	_instance;

    public static Template getInstance()
    {
        if (_instance == null)
            _instance = new Template();
        return _instance;
    }

    private Map<String, String> Tpl = new HashMap<String, String>();

    public String getFile(String path) throws IOException {
        System.out.println(" "+path);
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuffer stringBuffer = new StringBuffer();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuffer.append(line);
            stringBuffer.append("\n");
        }
        fileReader.close();
        return stringBuffer.toString();
    }

    public String reloadTemplates() {
        String result = "";
        try {
            for(String tp: this.Tpl.keySet()) {
                String tplUpdate = this.getFile(tp);
                this.Tpl.put(tp, tplUpdate);
                result += " -- "+tp+": reloaded<br />\n";
            }
        } catch (Exception e) {
            result += e.toString();
        }

        return result;
    }

    public String render(String path) throws Exception  {

        String tpl = this.Tpl.get(path);
        if(tpl != null) {
            return tpl;
        } else {
            tpl = this.getFile(path);
            this.Tpl.put(path, tpl);
            return tpl;
        }
    }

}