FROM node:22-alpine
WORKDIR /app
COPY package.json tsconfig.json ./
COPY src ./src
RUN npm install
RUN npm run build
EXPOSE 8080
CMD ["npm", "run", "start"]
