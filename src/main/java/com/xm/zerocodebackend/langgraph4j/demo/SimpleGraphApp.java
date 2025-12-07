package com.xm.zerocodebackend.langgraph4j.demo;

import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 简单图应用
 * 用于演示如何使用 LangGraph4j 构建和运行一个简单的图
 */
public class SimpleGraphApp {

    public static void main(String[] args) throws GraphStateException {

        // 初始化节点
        GreeterNode greeterNode = new GreeterNode();
        ResponderNode responderNode = new ResponderNode();

        // 定义图结构
        var stateGraph = new StateGraph<>(SimpleState.SCHEMA, initData -> new SimpleState(initData))
                .addNode("greeter", node_async(greeterNode))
                .addNode("responder", node_async(responderNode))
                // 定义边
                // START -> greeter -> responder -> END
                .addEdge(START, "greeter") // 从问候节点开始
                .addEdge("greeter", "responder")
                .addEdge("responder", END) // 响应节点后结束
        ;

        // 编译图
        var compiledGraph = stateGraph.compile();

        // 打印图
        GraphRepresentation domeGraph = stateGraph.getGraph(GraphRepresentation.Type.MERMAID, "dome", true);
        System.out.println(domeGraph.toString());

        /**
         * 运行图
         * `stream` 方法返回 AsyncGenerator。
         * 为了简单起见，我们将收集结果。在真实的应用程序中，您可以处理它们当他们到达时。
         * 在这里，执行后的最终状态是感兴趣的项。
         */
        for (var item : compiledGraph.stream(Map.of(SimpleState.MESSAGES_KEY, "让我们开始吧！"))) {
            // 打印每个状态
            System.out.println(item);
        }

    }
}