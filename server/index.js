const { setTimeout: setTimeoutPromises } = require('timers/promises');
const http = require('http');
const { createWSServer } = require('./server');

const PING_INTERVAL_MS = 30000;
const socketState = {};

const sendPings = () => {
    for (const socketId in socketState) {
        const stateForSocket = socketState[socketId];
        stateForSocket.socket.ping();
    }
};

const sendPingsOnInterval = async () => {
    sendPings();
    await setTimeoutPromises(PING_INTERVAL_MS);
    void sendPingsOnInterval();
};

const main = () => {
    const wsServer = createWSServer(socketState);
    const server = http.createServer((req, res) => {
        res.writeHead(200, { 'Content-Type': 'text/plain' });
        res.end();
    });
    server.on('upgrade', (request, socket, head) => {
        wsServer.handleUpgrade(request, socket, head, (ws) => {
            wsServer.emit('connection', ws, request);
        });
    });
    void sendPingsOnInterval();
    server.listen(4000, () => {
        console.log('Websocket server listening on port 4000');
    });
};

main();