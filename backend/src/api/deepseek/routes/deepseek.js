module.exports = {
  routes: [
    // 非流式问答接口
    {
      method: "POST",
      path: "/deepseek/ask-trade-question",
      handler: "deepseek.askTradeQuestion",
      config: {
        auth: false,
        policies: []
      }
    },
    // 流式问答接口
    {
      method: "POST",
      path: "/deepseek/ask-trade-question-stream",
      handler: "deepseek.askTradeQuestionStream",
      config: {
        auth: false,
        policies: []
      }
    },
    // 测试接口
    {
      method: "GET",
      path: "/deepseek/qwer",
      handler: "deepseek.qwer",
      config: {
        auth: false,
        policies: []
      }
    }
  ]
};