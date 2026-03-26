FROM node:20-alpine AS builder

WORKDIR /app

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm build

# ── Production ──
FROM node:20-alpine

WORKDIR /app

RUN addgroup -g 1001 -S mentoria && adduser -u 1001 -S mentoria -G mentoria

COPY --from=builder /app/.output ./.output

USER mentoria

EXPOSE 3000

ENV HOST=0.0.0.0
ENV PORT=3000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost:3000/ || exit 1

CMD ["node", ".output/server/index.mjs"]
