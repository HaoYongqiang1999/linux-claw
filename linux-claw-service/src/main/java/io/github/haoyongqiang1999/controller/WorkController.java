package io.github.haoyongqiang1999.controller;

import cn.hutool.core.lang.UUID;
import io.github.haoyongqiang1999.agent.AnalyzerReactAgent;
import io.github.haoyongqiang1999.agent.DesignerReactAgent;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class WorkController {
    @Resource
    AnalyzerReactAgent analyzerReactAgent;

    @Resource
    DesignerReactAgent designerReactAgent;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    @GetMapping("/work")
    public Flux<String> work(@RequestParam(name = "msg",defaultValue="你是谁") String msg) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        String threadId = UUID.randomUUID().toString();
        executorService.submit(() -> {
            try {
                analyzerReactAgent.analyzeForUser(msg, threadId, sink);
            } catch (Exception e) {
                sink.tryEmitError(e);
            }
        });
        return sink.asFlux();
    }
}
