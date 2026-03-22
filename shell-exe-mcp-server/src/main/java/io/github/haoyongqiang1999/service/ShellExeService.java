package io.github.haoyongqiang1999.service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


@Service
public class ShellExeService {
    @Tool(description = "通过 SSH 连接远程服务器并执行 shell 命令")
    public String shell(@ToolParam(description = "要执行的 shell 命令") String command)
    {
        String host = "";
        String username = "";
        String password = "";
        int port = 22;
        Session session = null;
        ChannelExec channel = null;
        
        try {
            // 创建 JSch 对象
            JSch jsch = new JSch();
            
            // 获取 session 对象
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            
            // 设置第一次连接时是否检查 host key
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            
            // 设置超时时间（毫秒）
            session.setTimeout(30000);
            
            // 连接 session
            session.connect();
            
            // 打开执行 channel
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            
            // 连接 channel
            channel.connect();
            
            // 获取命令输出流和错误流
            InputStream in = channel.getInputStream();
            InputStream err = channel.getErrStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(err, StandardCharsets.UTF_8));
            
            // 读取输出结果
            StringBuilder result = new StringBuilder();
            StringBuilder errorResult = new StringBuilder();
            String line;
            
            // 使用超时机制读取输出（最多等待 30 秒）
            long startTime = System.currentTimeMillis();
            long timeout = 30000; // 30 秒
            
            while (channel.isConnected() && (System.currentTimeMillis() - startTime) < timeout) {
                // 读取标准输出
                while (in.available() > 0) {
                    line = reader.readLine();
                    if (line != null) {
                        result.append(line).append("<br>");
                    }
                }
                
                // 读取错误输出
                while (err.available() > 0) {
                    line = errReader.readLine();
                    if (line != null) {
                        errorResult.append(line).append("<br>");
                    }
                }
                
                // 短暂等待
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            // 读取最后剩余的数据
            while (in.available() > 0 || reader.ready()) {
                line = reader.readLine();
                if (line != null) {
                    result.append(line).append("<br>");
                }
            }
            
            // 断开连接
            channel.disconnect();
            
            // 如果有错误输出，追加到结果中
            if (errorResult.length() > 0) {
                result.append("\n错误输出:\n").append(errorResult.toString());
            }
            
            return result.length() > 0 ? result.toString() : "命令执行成功，无输出";
            
        } catch (Exception e) {
            return "执行失败：" + e.getMessage();
        } finally {
            // 关闭资源
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
