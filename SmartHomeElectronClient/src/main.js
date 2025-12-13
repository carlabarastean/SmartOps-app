const { app, BrowserWindow, Menu, ipcMain, dialog } = require('electron');
const path = require('path');
const axios = require('axios');


const API_BASE_URL = 'http://localhost:8083/api';

class SmartHomeElectronApp {
    constructor() {
        this.mainWindow = null;
        this.setupEventHandlers();
    }

    setupEventHandlers() {
        app.whenReady().then(() => {
            this.createMainWindow();
            this.createMenu();
            this.setupIpcHandlers();
        });

        app.on('window-all-closed', () => {
            if (process.platform !== 'darwin') {
                app.quit();
            }
        });

        app.on('activate', () => {
            if (BrowserWindow.getAllWindows().length === 0) {
                this.createMainWindow();
            }
        });
    }

    createMainWindow() {
        this.mainWindow = new BrowserWindow({
            width: 1200,
            height: 800,
            minWidth: 800,
            minHeight: 600,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            },
            icon: path.join(__dirname, 'assets', 'icon.png'),
            title: 'Smart Home Dashboard - Desktop Client',
            show: false // Don't show until ready
        });

        this.mainWindow.loadFile(path.join(__dirname, 'renderer', 'index.html'));

        // Show window when ready
        this.mainWindow.once('ready-to-show', () => {
            this.mainWindow.show();

            // Open DevTools in development
            if (process.env.NODE_ENV === 'development') {
                this.mainWindow.webContents.openDevTools();
            }
        });

        this.mainWindow.on('closed', () => {
            this.mainWindow = null;
        });
    }

    createMenu() {
        const template = [
            {
                label: 'Smart Home',
                submenu: [
                    {
                        label: 'Refresh Data',
                        accelerator: 'CmdOrCtrl+R',
                        click: () => {
                            this.mainWindow.webContents.send('refresh-data');
                        }
                    },
                    {
                        label: 'Settings',
                        accelerator: 'CmdOrCtrl+,',
                        click: () => {
                            this.mainWindow.webContents.send('show-settings');
                        }
                    },
                    { type: 'separator' },
                    {
                        label: 'Quit',
                        accelerator: process.platform === 'darwin' ? 'Cmd+Q' : 'Ctrl+Q',
                        click: () => {
                            app.quit();
                        }
                    }
                ]
            },
            {
                label: 'View',
                submenu: [
                    {
                        label: 'Dashboard',
                        accelerator: 'CmdOrCtrl+1',
                        click: () => {
                            this.mainWindow.webContents.send('navigate-to', 'dashboard');
                        }
                    },
                    {
                        label: 'Devices',
                        accelerator: 'CmdOrCtrl+2',
                        click: () => {
                            this.mainWindow.webContents.send('navigate-to', 'devices');
                        }
                    },
                    {
                        label: 'AI Insights',
                        accelerator: 'CmdOrCtrl+3',
                        click: () => {
                            this.mainWindow.webContents.send('navigate-to', 'ai');
                        }
                    },
                    { type: 'separator' },
                    {
                        label: 'Toggle Developer Tools',
                        accelerator: process.platform === 'darwin' ? 'Alt+Cmd+I' : 'Ctrl+Shift+I',
                        click: () => {
                            this.mainWindow.webContents.toggleDevTools();
                        }
                    }
                ]
            },
            {
                label: 'Help',
                submenu: [
                    {
                        label: 'About Smart Home',
                        click: () => {
                            dialog.showMessageBox(this.mainWindow, {
                                type: 'info',
                                title: 'About Smart Home Dashboard',
                                message: 'Smart Home IoT Dashboard',
                                detail: 'Desktop client for managing your smart home devices\\nVersion 1.0.0\\n\\nBuilt with Electron'
                            });
                        }
                    }
                ]
            }
        ];

        const menu = Menu.buildFromTemplate(template);
        Menu.setApplicationMenu(menu);
    }

    setupIpcHandlers() {
        // Get all devices
        ipcMain.handle('api-get-devices', async () => {
            try {
                const response = await axios.get(`${API_BASE_URL}/devices`);
                return { success: true, data: response.data };
            } catch (error) {
                console.error('Error fetching devices:', error.message);
                return { success: false, error: error.message };
            }
        });

        // Get active devices only
        ipcMain.handle('api-get-active-devices', async () => {
            try {
                const response = await axios.get(`${API_BASE_URL}/devices`);
                const activeDevices = response.data.filter(device => device.status === true);
                return { success: true, data: activeDevices };
            } catch (error) {
                console.error('Error fetching active devices:', error.message);
                return { success: false, error: error.message };
            }
        });

        // Get users
        ipcMain.handle('api-get-users', async () => {
            try {
                const response = await axios.get(`${API_BASE_URL}/users`);
                return { success: true, data: response.data };
            } catch (error) {
                console.error('Error fetching users:', error.message);
                return { success: false, error: error.message };
            }
        });

        // Get temperature trend
        ipcMain.handle('api-get-temperature', async () => {
            try {
                const response = await axios.get(`${API_BASE_URL}/temperature/trend`);
                return { success: true, data: response.data };
            } catch (error) {
                console.error('Error fetching temperature:', error.message);
                return { success: false, error: error.message };
            }
        });

        // Get AI suggestions
        ipcMain.handle('api-get-ai-suggestions', async (event, userId) => {
            try {
                const response = await axios.get(`${API_BASE_URL}/ai/suggestions/${userId || 1}`);
                return { success: true, data: response.data };
            } catch (error) {
                console.error('Error fetching AI suggestions:', error.message);
                return { success: false, error: error.message };
            }
        });

        // Send email simulation
        ipcMain.handle('simulate-email', async (event, emailData) => {
            const timestamp = new Date().toLocaleString();
            const emailContent = `
=== SMART HOME STATUS REPORT ===
Generated: ${timestamp}
Recipient: ${emailData.recipient}

System Status: ${emailData.systemStatus}
Total Devices: ${emailData.totalDevices}
Active Devices: ${emailData.activeDevices}
Temperature: ${emailData.temperature}°C

${emailData.details}

---
Smart Home Dashboard Desktop Client
This is a simulated email report.
            `;

            console.log('📧 EMAIL SIMULATION:', emailContent);

            // Show in dialog as well
            dialog.showMessageBox(this.mainWindow, {
                type: 'info',
                title: '📧 Email Sent (Simulation)',
                message: 'Status Report Email Simulation',
                detail: `Email sent to: ${emailData.recipient}\\n\\nContent preview:\\n${emailContent.substring(0, 200)}...`,
                buttons: ['OK']
            });

            return { success: true, message: 'Email simulation completed' };
        });

        // Test backend connection
        ipcMain.handle('test-connection', async () => {
            try {
                const response = await axios.get(`${API_BASE_URL}/devices`, { timeout: 5000 });
                return {
                    success: true,
                    message: 'Backend connected successfully',
                    deviceCount: response.data.length
                };
            } catch (error) {
                return {
                    success: false,
                    error: `Backend connection failed: ${error.message}`
                };
            }
        });
    }
}

// Initialize the application
new SmartHomeElectronApp();
