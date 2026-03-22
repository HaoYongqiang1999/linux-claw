package io.github.haoyongqiang1999.agent;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class AnalyzerReactAgent {
    @Resource(name = "analyzer")
    ReactAgent anaylzerReactAgent;
    @Resource
    DesignerReactAgent designerReactAgent;

    String systemPromptForUser = "你是一个软件产品经理，负责接收用户模糊的需求并翻译为一段清晰可行的需求。具体要求如下：1）你应该直接生成需求描述，不应该对用户的需求进行任何反问或者其他无关回应；2）你的回答应该是一段普通文字，不包含任何特殊格式。";
    String systemPromptForDesigner = "你是一个软件产品经理，负责根据软件设计者对当前需求的疑问，在解答疑问的同时给出完整的需求描述。具体要求如下：1）你应该根据软件设计者的疑问重新生成需求描述，且生成的需求描述必须是完整的，而不应该只包含对疑问的回应；2）你的回答应该是一段普通文字，不包含任何特殊格式；3）你不应该反问软件设计者，或者进行其他无关回应。强调：你的回答永远应该是一份完整的需求描述。";
    public String call(String msg, String threadId) throws Exception {
        try {
            return anaylzerReactAgent.call(msg,RunnableConfig.builder()
                    .threadId(threadId)
                    .build()).getText();
        } catch (GraphRunnerException e) {
            System.out.println(e.getMessage());
        }
        return "error";
    }

    public void analyzeForUser(String msg, String threadId, Sinks.Many<String> sink) throws Exception {
        sink.tryEmitNext("analyzer: 开始为用户分析...<br>");
        anaylzerReactAgent.setSystemPrompt(systemPromptForUser);
        Stream<Message> stream = anaylzerReactAgent.streamMessages(
           msg,
           RunnableConfig.builder().threadId("analyzer" + threadId).build()
        ).toStream();
        stream.forEach(message -> {
            sink.tryEmitNext(message.getText());
        });
        sink.tryEmitNext("<br>");
        designerReactAgent.think(msg, threadId, sink);
    }


    public void analyzeForDesigner(String msg, String threadId, Sinks.Many<String> sinks) throws Exception {
        sinks.tryEmitNext("analyzer: 开始为designer分析...<br>");
        StringBuilder text = new StringBuilder();
        anaylzerReactAgent.setSystemPrompt(systemPromptForDesigner);
        Stream<Message> stream = anaylzerReactAgent.streamMessages(msg, RunnableConfig.builder()
                .threadId("analyzer" + threadId)
                .build()).toStream();
        stream.forEach(message -> {
            sinks.tryEmitNext(message.getText());
            text.append(message.getText());
        });
        sinks.tryEmitNext("<br>");
        designerReactAgent.think(text.toString(), threadId, sinks);
    }

}
