package io.github.haoyongqiang1999.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import io.github.haoyongqiang1999.service.ShellExeService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ToolCallbackProviderConfig {
    @Bean
    public ToolCallbackProvider shellExeToolCallbackProvider(ShellExeService shellExeService) {
        return MethodToolCallbackProvider.builder().toolObjects(shellExeService).build();
    }
}
