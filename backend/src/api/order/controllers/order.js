'use strict';

/**
 * order controller
 */

const { createCoreController } = require('@strapi/strapi').factories;

const currentUserId = (ctx) => ctx.state.user && ctx.state.user.id;

const requireUser = (ctx) => {
  const userId = currentUserId(ctx);
  if (!userId) {
    ctx.unauthorized('Authentication is required to access orders');
    return null;
  }
  return userId;
};

const userFilter = (userId) => ({ user: { id: { $eq: userId } } });

const documentFilter = (documentId, userId) => ({
  documentId: { $eq: documentId },
  ...userFilter(userId),
});

const mergeFilters = (existingFilters, requiredFilters) => {
  if (!existingFilters || Object.keys(existingFilters).length === 0) {
    return requiredFilters;
  }

  return {
    $and: [existingFilters, requiredFilters],
  };
};

module.exports = createCoreController('api::order.order', ({ strapi }) => ({
  async find(ctx) {
    const userId = requireUser(ctx);
    if (!userId) return;

    ctx.query = {
      ...ctx.query,
      filters: mergeFilters(ctx.query.filters, userFilter(userId)),
    };

    return super.find(ctx);
  },

  async findOne(ctx) {
    const userId = requireUser(ctx);
    if (!userId) return;

    const documentId = ctx.params.documentId || ctx.params.id;
    const order = await strapi.documents('api::order.order').findFirst({
      filters: documentFilter(documentId, userId),
    });

    if (!order) {
      return ctx.notFound('Order not found');
    }

    const sanitizedOrder = await this.sanitizeOutput(order, ctx);
    return this.transformResponse(sanitizedOrder);
  },

  async create(ctx) {
    const userId = requireUser(ctx);
    if (!userId) return;

    ctx.request.body = {
      ...ctx.request.body,
      data: {
        ...(ctx.request.body && ctx.request.body.data ? ctx.request.body.data : {}),
        user: userId,
      },
    };

    return super.create(ctx);
  },

  async delete(ctx) {
    const userId = requireUser(ctx);
    if (!userId) return;

    const documentId = ctx.params.documentId || ctx.params.id;
    const order = await strapi.documents('api::order.order').findFirst({
      filters: documentFilter(documentId, userId),
    });

    if (!order) {
      return ctx.notFound('Order not found');
    }

    const deletedOrder = await strapi.documents('api::order.order').delete({
      documentId: order.documentId,
    });

    const sanitizedOrder = await this.sanitizeOutput(deletedOrder, ctx);
    return this.transformResponse(sanitizedOrder);
  },
}));
