package io.github.haoyongqiang1999.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AgentsConfig
{
    @Bean
    public DashScopeApi dashScopeApi()
    {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("aliQwen-api"))
                .build();
        return dashScopeApi;
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean(name = "analyzer")
    public ReactAgent analyzerReactAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("analyzer")
                .model(chatModel)
                .saver(new MemorySaver())
                .build();
    }

    @Bean(name = "designer")
    public ReactAgent designerReactAgent(ChatModel chatModel, ToolCallbackProvider tools) {
        return ReactAgent.builder()
                .name("designer")
                .model(chatModel)
                .toolCallbackProviders(tools)
                .saver(new MemorySaver())
                .build();
    }
}
