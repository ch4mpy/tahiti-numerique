FROM node:18-alpine

RUN npm install --global pm2

WORKDIR /app

ENV NODE_ENV production
ENV NEXT_TELEMETRY_DISABLED 1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --chown=nextjs:nodejs ./.next ./.next
COPY ./node_modules ./node_modules
COPY ./package.json ./package.json
COPY ./next.config.js ./next.config.js
RUN npm install --production

USER nextjs

EXPOSE 3000

ENV PORT 3000

CMD ["pm2-runtime", "npm", "--", "start"]