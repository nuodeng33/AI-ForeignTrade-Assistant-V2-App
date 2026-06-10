'use strict';

module.exports = {
  // 使用Strapi内置的注册方法（不需要自定义）
  async register(ctx) {
    try {
      const { username, email, password } = ctx.request.body;
      
      // 基础验证
      if (!username || !email || !password) {
        return ctx.badRequest('用户名、邮箱和密码是必填项');
      }
      
      // 直接调用Strapi内置的用户注册服务
      const user = await strapi.plugin('users-permissions').service('user').add({
        username,
        email,
        password,
        confirmed: true, // 自动确认，不需要邮件验证
        role: 1, // 默认关联到 'Authenticated' 角色
      });
      
      // 生成JWT令牌
      const jwt = strapi.plugin('users-permissions').service('jwt').issue({
        id: user.id,
      });
      
      // 返回简单的结果
      ctx.send({
        jwt,
        user: {
          id: user.id,
          username: user.username,
          email: user.email,
        }
      });
      
    } catch (err) {
      // 简单错误处理
      console.log('注册错误:', err.message);
      
      if (err.message.includes('email already taken')) {
        return ctx.badRequest('邮箱已被注册');
      }
      if (err.message.includes('username already taken')) {
        return ctx.badRequest('用户名已被使用');
      }
      
      return ctx.badRequest('注册失败，请稍后重试');
    }
  }
};