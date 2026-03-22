package io.github.haoyongqiang1999.agent;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.Message;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Sinks;

import java.util.stream.Stream;

@Component
public class DesignerReactAgent {
    @Resource(name = "designer")
    ReactAgent designerReactAgent;
    @Resource
    @Lazy
    AnalyzerReactAgent analyzerReactAgent;
    String promptToThink = "你是一个软件设计者，你需要理解产品经理给你的输入并和产品经理进行探讨，直到完全理解和认可。1）在这一步，你不应该开始通过 SSH 连接远程服务器并执行 shell 命令;2）你的工作区是/media/aitest0321，除非迫不得已，否则你不应该处理其他路径;3）你应该充分和产品经理讨论需求；4）如果你已经完全理解和认可，请直接说'OK,GET IT!'";
    String promptToDesign = "你是一个软件设计者，你需要根据产品经理的输入，进行开发；1）通过 SSH 连接远程服务器并执行 shell 命令，以完成开发任务；2）你的工作区是/media/aitest0321，除非迫不得已，否则你不应该处理其他路径";

    public String call(String msg) throws Exception {
        System.out.println(analyzerReactAgent);
        RunnableConfig config = RunnableConfig.builder()
                .threadId("designerReactAgentThreadId")
                .build();
        try {
            return "designer: " + designerReactAgent.call(msg,config).getText();
        } catch (GraphRunnerException e) {
            System.out.println(e.getMessage());
        }
        return "designer: error";
    }

    public void think(String msg, String threadId, Sinks.Many<String> sink) throws Exception {
        StringBuilder text = new StringBuilder();
        sink.tryEmitNext("designer：开始思考...<br>");
        try {
            designerReactAgent.setSystemPrompt(promptToThink);
            Stream<Message> stream = designerReactAgent.streamMessages(
                    msg,
                    RunnableConfig.builder().threadId("designer" + threadId).build()
            ).toStream();
            stream.forEach(message -> {
                sink.tryEmitNext(message.getText());
                text.append(message.getText());
                }
            );
            sink.tryEmitNext("<br>");
        } catch (GraphRunnerException e) {
            System.out.println(e.getMessage());
        }
        if (text.toString().contains("OK,GET IT!")) {
            design(msg, threadId, sink);
        }
        else {
            analyzerReactAgent.analyzeForDesigner(text.toString(), threadId, sink);
        }
    }

    private void design(String msg, String threadId, Sinks.Many<String> sink) {
        sink.tryEmitNext("designer: 开始设计...<br>");
        try {
            designerReactAgent.setSystemPrompt(promptToDesign);
            Stream<Message> stream = designerReactAgent.streamMessages(msg, RunnableConfig.builder()
                    .threadId("designer" + threadId)
                    .build()).toStream();
            stream.forEach(message -> {
                sink.tryEmitNext(message.getText());
            });
            sink.tryEmitNext("<br>");
            sink.tryEmitComplete();
        } catch (GraphRunnerException e) {
            System.out.println(e.getMessage());
        }
    }

    private void logToConsole(String text) {
        System.out.println("designer: " + text);
    }
}
