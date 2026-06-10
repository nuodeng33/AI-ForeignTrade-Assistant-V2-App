'use strict';

module.exports = {
  routes: [
    // 覆盖内置的 /auth/local/register 路由，指向我们的自定义控制器
    {
      method: 'POST',
      path: '/auth/local/register',
      handler: 'user.register',
      config: {
        middlewares: ['plugin::users-permissions.rateLimit'],
        prefix: '',
      },
    },
    // 覆盖内置的 /auth/local 登录路由，确保返回头像
    {
      method: 'POST',
      path: '/auth/local',
      handler: 'user.login',
      config: {
        middlewares: ['plugin::users-permissions.rateLimit'],
        prefix: '',
      },
    },
    // 新增一个获取当前用户资料的端点
    {
      method: 'GET',
      path: '/users/me',
      handler: 'user.me',
      config: {
        prefix: '',
        policies: ['plugin::users-permissions.isAuthenticated'] // 需要认证
      },
    }
  ],
};