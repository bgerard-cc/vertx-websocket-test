const WebSocket = require('ws');
const crypto = require("crypto");

const createWSServer = (socketState) => {
    const server = new WebSocket.Server({noServer: true});
    server.on('connection', (ws) => {
        console.log('Client connected');
        const socketId = crypto.randomBytes(3 * 5).toString('base64');
        socketState[socketId] = {socket: ws};

        const message = {
            type: 'welcome',
            content: 'Hello! You are connected to the WebSocket server.',
            timestamp: new Date(),
        };
        ws.send(JSON.stringify(message));

        // Force close the connection after 45s
        setTimeout(() => {
            ws.terminate();
        }, 45000);

        ws.on('message', (message) => {
            console.log(`Received message: ${message}`);
        });

        ws.on('close', () => {
            console.log('Client disconnected');
            delete socketState[socketId];
        });

        ws.on('error', (error) => {
            console.error(`WebSocket error: ${error}`);
        });

        ws.on('ping', () => {
            console.log('got ping');
        });

        ws.on('pong', () => {
            console.log('got pong');
        });
    });
    return server;
};

module.exports = {
    createWSServer,
};