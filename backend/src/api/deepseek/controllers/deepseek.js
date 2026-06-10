const axios = require('axios');
const { PassThrough } = require('stream');

module.exports = ({ strapi }) => ({
  // 核心：外贸行业问答接口（非流式版本）
  async askTradeQuestion(ctx) {
    try {
      // 1. 读取并校验前端参数
      const { question, history = [] } = ctx.request.body;
      
      // 校验问题参数
      if (!question || typeof question !== 'string') {
        return ctx.badRequest('参数错误：请传入有效的外贸相关问题文本');
      }
      // 校验上下文格式
      if (!Array.isArray(history)) {
        return ctx.badRequest('上下文（history）必须为数组格式');
      }

      // 2. 构造DeepSeek请求头
      const deepseekHeaders = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${process.env.DEEPSEEK_API_KEY}`
      };

      // 3. 外贸行业限定System Prompt
      const systemPrompt = `
        你是专业的外贸行业智能助手，严格遵守以下规则：
        1. 仅回答与外贸相关的问题；
        2. 若问题与外贸无关，直接回复："抱歉，我仅专注于外贸行业问题解答，无法回答该问题。";
        3. 回答需专业、简洁、准确；
        4. 必须基于历史对话上下文回答问题。
      `.trim();

      // 4. 构造DeepSeek请求参数
      const requestData = { 
        model: "deepseek-chat",
        messages: [
          { role: "system", content: systemPrompt },
          ...history,
          { role: "user", content: question }
        ],
        temperature: 0.7,
        max_tokens: 2000,
        stream: false
      };

      // 5. 调用DeepSeek API
      const response = await axios.post(
        process.env.DEEPSEEK_API_URL || "https://api.deepseek.com/v1/chat/completions",
        requestData,
        { 
          headers: deepseekHeaders,
          timeout: 30000
        }
      );

      // 6. 处理响应
      const answer = response.data.choices[0].message.content.trim();
      const newHistory = [
        ...history,
        { role: "user", content: question },
        { role: "assistant", content: answer }
      ];

      // 7. 返回前端结果
      ctx.body = {
        success: true,
        data: {
          question: question,
          answer: answer,
          newHistory: newHistory
        }
      };

    } catch (error) {
      console.error("DeepSeek调用失败:", error.message);
      
      if (error.response) {
        // DeepSeek API返回错误
        return ctx.internalServerError({
          success: false,
          message: `DeepSeek API错误：${error.response.status}`
        });
      } else if (error.request) {
        // 网络错误
        return ctx.internalServerError({
          success: false,
          message: "无法连接到DeepSeek服务器"
        });
      } else {
        // 代码逻辑错误
        return ctx.internalServerError({
          success: false,
          message: `服务器逻辑错误：${error.message}`
        });
      }
    }
  },

  // 流式问答接口
  async askTradeQuestionStream(ctx) {
    try {
      // 1. 读取并校验前端参数
      const { question, history = [] } = ctx.request.body;
      
      if (!question || typeof question !== 'string') {
        return ctx.badRequest('参数错误：请传入有效的外贸相关问题文本');
      }
      if (!Array.isArray(history)) {
        return ctx.badRequest('上下文（history）必须为数组格式');
      }

      // 2. 设置SSE响应头
      ctx.set({
        'Content-Type': 'text/event-stream',
        'Cache-Control': 'no-cache',
        'Connection': 'keep-alive',
        'X-Accel-Buffering': 'no'
      });
      ctx.status = 200;

      // 3. 创建可写流
      const stream = new PassThrough();
      ctx.body = stream;

      // 4. 外贸行业限定System Prompt
      const systemPrompt = `你是专业的外贸行业智能助手，仅回答外贸相关问题。回答需专业、简洁、准确。`;

      // 5. 构造DeepSeek流式请求参数
      const requestData = { 
        model: "deepseek-chat",
        messages: [
          { role: "system", content: systemPrompt },
          ...history,
          { role: "user", content: question }
        ],
        temperature: 0.7,
        max_tokens: 2000,
        stream: true
      };

      // 6. DeepSeek API请求头
      const deepseekHeaders = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${process.env.DEEPSEEK_API_KEY}`,
        'Accept': 'text/event-stream'
      };

      // 7. 调用DeepSeek流式API
      const response = await axios.post(
        process.env.DEEPSEEK_API_URL || "https://api.deepseek.com/v1/chat/completions",
        requestData,
        { 
          headers: deepseekHeaders,
          responseType: 'stream',
          timeout: 60000
        }
      );

      let buffer = '';
      let fullAnswer = '';
      
      // 8. 处理DeepSeek的流式响应
      response.data.on('data', (chunk) => {
        buffer += chunk.toString();
        
        // 按行分割处理
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';
        
        for (const line of lines) {
          if (line.trim() === '') continue;
          
          if (line.startsWith('data: ')) {
            const data = line.substring(6).trim();
            
            if (data === '[DONE]') {
              // 流式传输完成
              stream.write(`event: done\ndata: {}\n\n`);
              
              // 生成新上下文并发送
              const newHistory = [
                ...history,
                { role: "user", content: question },
                { role: "assistant", content: fullAnswer }
              ];
              
              stream.write(`event: history\ndata: ${JSON.stringify({ newHistory })}\n\n`);
              stream.end();
              return;
            }
            
            try {
              const parsed = JSON.parse(data);
              if (parsed.choices && parsed.choices[0].delta && parsed.choices[0].delta.content) {
                const content = parsed.choices[0].delta.content;
                fullAnswer += content;
                
                // 发送内容块到前端
                stream.write(`data: ${JSON.stringify({ content })}\n\n`);
              }
            } catch (e) {
              // 忽略解析错误
              console.log('解析JSON失败:', e.message);
            }
          }
        }
      });

      // 9. 错误处理
      response.data.on('error', (error) => {
        console.error('DeepSeek流式响应错误:', error);
        stream.write(`event: error\ndata: ${JSON.stringify({ 
          message: '流式响应中断',
          error: error.message 
        })}\n\n`);
        stream.end();
      });

      response.data.on('end', () => {
        if (!stream.writableEnded) {
          stream.write(`event: done\ndata: {}\n\n`);
          stream.end();
        }
      });

      // 10. 超时处理
      setTimeout(() => {
        if (!stream.writableEnded) {
          stream.write(`event: error\ndata: ${JSON.stringify({ 
            message: '响应超时'
          })}\n\n`);
          stream.end();
        }
      }, 55000);

    } catch (error) {
      console.error("DeepSeek流式调用失败:", error.message);
      
      if (!ctx.headerSent) {
        return ctx.internalServerError({
          success: false,
          message: `服务器错误: ${error.message}`
        });
      }
    }
  },

  // 测试接口
  async qwer(ctx) {
    ctx.body = {
      success: true,
      message: 'Hello World!'
    };
  }
});