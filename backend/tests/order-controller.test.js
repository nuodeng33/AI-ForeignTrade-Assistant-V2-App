'use strict';

const assert = require('node:assert/strict');
const Module = require('node:module');
const test = require('node:test');

let activeStrapi;
let nextId = 1;
const controllerPath = require.resolve('../src/api/order/controllers/order.js');
const originalLoad = Module._load;

const matchesFilter = (order, filters) => {
  if (!filters || Object.keys(filters).length === 0) return true;

  if (Array.isArray(filters.$and)) {
    return filters.$and.every((filter) => matchesFilter(order, filter));
  }

  if (filters.documentId && filters.documentId.$eq !== order.documentId) {
    return false;
  }

  if (filters.user && filters.user.id && filters.user.id.$eq !== order.user.id) {
    return false;
  }

  return true;
};

const createMockStrapi = (initialOrders = []) => {
  const orders = initialOrders.map((order) => ({ ...order, user: { ...order.user } }));

  return {
    orders,
    documents(uid) {
      assert.equal(uid, 'api::order.order');
      return {
        async findFirst({ filters }) {
          return orders.find((order) => matchesFilter(order, filters)) || null;
        },
        async delete({ documentId }) {
          const index = orders.findIndex((order) => order.documentId === documentId);
          if (index === -1) return null;
          const [deletedOrder] = orders.splice(index, 1);
          return deletedOrder;
        },
      };
    },
  };
};

const createBaseController = () => ({
  async find(ctx) {
    return {
      data: activeStrapi.orders.filter((order) => matchesFilter(order, ctx.query.filters)),
    };
  },
  async create(ctx) {
    const data = ctx.request.body.data;
    const order = {
      id: nextId++,
      documentId: `order-${nextId}`,
      orderNumber: data.orderNumber,
      goodsInfo: data.goodsInfo,
      status: data.status,
      createTime: data.createTime,
      createdAt: data.createTime,
      user: { id: data.user },
    };
    activeStrapi.orders.push(order);
    return { data: order };
  },
  async sanitizeOutput(entity) {
    return entity;
  },
  transformResponse(entity) {
    return { data: entity };
  },
});

Module._load = function patchedLoad(request, parent, isMain) {
  if (request === '@strapi/strapi') {
    return {
      factories: {
        createCoreController(uid, factory) {
          assert.equal(uid, 'api::order.order');
          const baseController = createBaseController();
          const customController = factory({ strapi: activeStrapi });
          Object.setPrototypeOf(customController, baseController);
          return customController;
        },
      },
    };
  }

  return originalLoad.apply(this, arguments);
};

test.after(() => {
  Module._load = originalLoad;
});

const loadController = (strapi) => {
  activeStrapi = strapi;
  delete require.cache[controllerPath];
  return require(controllerPath);
};

const createCtx = ({ userId, bodyData = {}, params = {}, query = {} } = {}) => ({
  state: { user: userId ? { id: userId } : null },
  request: { body: { data: bodyData } },
  params,
  query,
  status: 200,
  unauthorized(message) {
    this.status = 401;
    return { error: 'Unauthorized', message };
  },
  notFound(message) {
    this.status = 404;
    return { error: 'NotFound', message };
  },
});

const createOrderForUserA = () => ({
  id: 1,
  documentId: 'order-a',
  orderNumber: 'ORD-A',
  goodsInfo: 'A user goods',
  status: '待发货',
  createTime: '2026-06-11T00:00:00.000Z',
  createdAt: '2026-06-11T00:00:00.000Z',
  user: { id: 101 },
});

test('create binds an order to the current user', async () => {
  const strapi = createMockStrapi();
  const controller = loadController(strapi);
  const ctx = createCtx({
    userId: 101,
    bodyData: {
      orderNumber: 'ORD-CREATE-A',
      goodsInfo: 'A user new goods',
      status: '待发货',
      createTime: '2026-06-11T00:00:00.000Z',
      user: 202,
    },
  });

  const response = await controller.create(ctx);

  assert.equal(ctx.request.body.data.user, 101);
  assert.equal(response.data.user.id, 101);
  assert.equal(strapi.orders[0].user.id, 101);
});

test('user B cannot see user A orders in the order list', async () => {
  const strapi = createMockStrapi([createOrderForUserA()]);
  const controller = loadController(strapi);
  const ctx = createCtx({ userId: 202 });

  const response = await controller.find(ctx);

  assert.deepEqual(response.data, []);
});

test('user B receives 404 when fetching user A order detail by documentId', async () => {
  const strapi = createMockStrapi([createOrderForUserA()]);
  const controller = loadController(strapi);
  const ctx = createCtx({ userId: 202, params: { documentId: 'order-a' } });

  const response = await controller.findOne(ctx);

  assert.equal(ctx.status, 404);
  assert.deepEqual(response, { error: 'NotFound', message: 'Order not found' });
});

test('user B cannot delete user A order and the order remains', async () => {
  const strapi = createMockStrapi([createOrderForUserA()]);
  const controller = loadController(strapi);
  const ctx = createCtx({ userId: 202, params: { documentId: 'order-a' } });

  const response = await controller.delete(ctx);

  assert.equal(ctx.status, 404);
  assert.deepEqual(response, { error: 'NotFound', message: 'Order not found' });
  assert.equal(strapi.orders.length, 1);
  assert.equal(strapi.orders[0].documentId, 'order-a');
});

test('user A can delete their own order', async () => {
  const strapi = createMockStrapi([createOrderForUserA()]);
  const controller = loadController(strapi);
  const ctx = createCtx({ userId: 101, params: { documentId: 'order-a' } });

  const response = await controller.delete(ctx);

  assert.equal(ctx.status, 200);
  assert.equal(response.data.documentId, 'order-a');
  assert.equal(strapi.orders.length, 0);
});

test('order update always returns 405', async () => {
  const strapi = createMockStrapi([createOrderForUserA()]);
  const controller = loadController(strapi);
  const ctx = createCtx({ userId: 101, params: { documentId: 'order-a' } });

  const response = await controller.update(ctx);

  assert.equal(ctx.status, 405);
  assert.deepEqual(response, {
    error: 'ORDER_UPDATE_DISABLED',
    message: 'Order update is currently disabled.',
  });
});
