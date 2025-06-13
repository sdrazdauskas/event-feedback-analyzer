# Stage 1: Build the Angular app
FROM node:24-alpine AS build
WORKDIR /app
COPY . .
RUN npm ci
RUN npm run build -- --configuration production

# Stage 2: Serve the app with http-server
FROM node:24-alpine
WORKDIR /app
RUN npm install -g http-server
COPY --from=build /app/dist/frontend/browser /app/dist/frontend/browser
EXPOSE 8080
CMD ["http-server", "dist/frontend/browser", "-p", "8080"]
