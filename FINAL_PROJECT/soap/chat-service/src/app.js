const fs = require('fs');
const path = require('path');
const express = require('express');
const soap = require('soap');

const services = require('./service');

const PORT = Number(process.env.SERVICE_PORT || 9002);
const WSDL_PATH = path.join(__dirname, '..', 'wsdl', 'chat.wsdl');
const wsdl = fs.readFileSync(WSDL_PATH, 'utf8');

const app = express();

app.get('/', (_req, res) => {
  res.type('text/plain').send('ChatService SOAP (Node.js node-soap). WSDL at /chat?wsdl');
});

const server = app.listen(PORT, () => {
  console.log(`[soap-chat-node] listening on :${PORT}`);
  console.log(`[soap-chat-node] WSDL at http://localhost:${PORT}/chat?wsdl`);
});

soap.listen(server, '/chat', services, wsdl, () => {
  console.log('[soap-chat-node] SOAP endpoint /chat ready');
});
