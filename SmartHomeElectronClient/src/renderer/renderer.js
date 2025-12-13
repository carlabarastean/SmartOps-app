
const { ipcRenderer } = require('electron');

class SmartHomeRenderer {
    constructor() {
        this.currentView = 'dashboard';
        this.devices = [];
        this.users = [];
        this.temperatureData = [];

        this.initializeEventListeners();
        this.testConnection();
        this.loadDashboardData();
    }

    initializeEventListeners() {

        document.querySelectorAll('.nav-item').forEach(item => {
            item.addEventListener('click', () => {
                const view = item.getAttribute('data-view');
                this.switchView(view);
            });
        });


        document.getElementById('refreshBtn').addEventListener('click', () => {
            this.refreshAllData();
        });


        document.querySelectorAll('input[name="deviceFilter"]').forEach(radio => {
            radio.addEventListener('change', () => {
                this.filterDevices(radio.value);
            });
        });


        document.getElementById('getAIBtn').addEventListener('click', () => {
            this.getAISuggestions();
        });


        document.getElementById('emailForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.sendStatusReport();
        });


        ipcRenderer.on('refresh-data', () => {
            this.refreshAllData();
        });

        ipcRenderer.on('navigate-to', (event, view) => {
            this.switchView(view);
        });

        ipcRenderer.on('show-settings', () => {
            this.showSettings();
        });
    }

    async testConnection() {
        try {
            const result = await ipcRenderer.invoke('test-connection');
            this.updateConnectionStatus(result.success, result.message, result.deviceCount);
        } catch (error) {
            this.updateConnectionStatus(false, 'Connection test failed');
        }
    }

    updateConnectionStatus(connected, message, deviceCount = 0) {
        const statusElement = document.getElementById('connectionStatus');

        if (connected) {
            statusElement.className = 'connection-status connected';
            statusElement.innerHTML = `
                <i class="fas fa-circle"></i>
                <span>Connected (${deviceCount} devices)</span>
            `;
        } else {
            statusElement.className = 'connection-status disconnected';
            statusElement.innerHTML = `
                <i class="fas fa-circle"></i>
                <span>Offline</span>
            `;
        }
    }

    switchView(viewName) {

        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });
        document.querySelector(`[data-view="${viewName}"]`).classList.add('active');


        document.querySelectorAll('.view').forEach(view => {
            view.classList.remove('active');
        });
        document.getElementById(`${viewName}View`).classList.add('active');

        this.currentView = viewName;


        switch (viewName) {
            case 'dashboard':
                this.loadDashboardData();
                break;
            case 'devices':
                this.loadDevices();
                break;
            case 'ai':

                break;
            case 'status':

                break;
        }
    }

    showLoading(show = true) {
        const overlay = document.getElementById('loadingOverlay');
        if (show) {
            overlay.classList.remove('hidden');
        } else {
            overlay.classList.add('hidden');
        }
    }

    async loadDashboardData() {
        this.showLoading(true);

        try {

            const [devicesResult, usersResult, temperatureResult] = await Promise.all([
                ipcRenderer.invoke('api-get-devices'),
                ipcRenderer.invoke('api-get-users'),
                ipcRenderer.invoke('api-get-temperature')
            ]);

            if (devicesResult.success) {
                this.devices = devicesResult.data;
                this.updateDashboardStats();
                this.updateRecentActivity();
            }

            if (usersResult.success) {
                this.users = usersResult.data;
                this.updateDashboardStats();
            }

            if (temperatureResult.success) {
                this.temperatureData = temperatureResult.data;
                this.updateDashboardStats();
            }

        } catch (error) {
            console.error('Error loading dashboard data:', error);
        } finally {
            this.showLoading(false);
        }
    }

    updateDashboardStats() {
        const activeDevices = this.devices.filter(d => d.status).length;
        const currentTemp = this.temperatureData.length > 0 ?
            this.temperatureData[0].temperature : null;

        document.getElementById('totalDevicesCount').textContent = this.devices.length;
        document.getElementById('activeDevicesCount').textContent = activeDevices;
        document.getElementById('currentTemperature').textContent =
            currentTemp ? `${currentTemp}°C` : 'N/A';
        document.getElementById('usersCount').textContent = this.users.length;
    }

    updateRecentActivity() {
        const activityList = document.getElementById('recentActivity');
        const activities = [];


        this.devices.slice(0, 5).forEach(device => {
            const status = device.status ? 'activated' : 'deactivated';
            activities.push({
                time: new Date().toLocaleTimeString(),
                description: `${device.name} ${status} in ${device.room?.name || 'Unknown Room'}`,
                type: device.status ? 'success' : 'info'
            });
        });

        activityList.innerHTML = activities.map(activity => `
            <div class="activity-item">
                <div class="activity-time">${activity.time}</div>
                <div class="activity-description">${activity.description}</div>
            </div>
        `).join('');
    }

    async loadDevices() {
        this.showLoading(true);

        try {
            const result = await ipcRenderer.invoke('api-get-devices');
            if (result.success) {
                this.devices = result.data;
                this.displayDevices(this.devices);
                this.updateConnectionStatus(true, 'Connected', this.devices.length);
            } else {
                this.updateConnectionStatus(false, result.error);
            }
        } catch (error) {
            console.error('Error loading devices:', error);
            this.updateConnectionStatus(false, 'Failed to load devices');
        } finally {
            this.showLoading(false);
        }
    }

    displayDevices(devices) {
        const deviceGrid = document.getElementById('deviceGrid');

        if (devices.length === 0) {
            deviceGrid.innerHTML = `
                <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #b0b0b0;">
                    <i class="fas fa-plug" style="font-size: 48px; margin-bottom: 16px; opacity: 0.5;"></i>
                    <p>No devices found</p>
                </div>
            `;
            return;
        }

        deviceGrid.innerHTML = devices.map(device => {
            const deviceIcon = this.getDeviceIcon(device.type);
            const statusClass = device.status ? 'active' : 'inactive';
            const statusText = device.status ? 'Active' : 'Inactive';

            return `
                <div class="device-card ${statusClass}">
                    <div class="device-header">
                        <div class="device-type">
                            <i class="${deviceIcon}"></i>
                            ${device.type}
                        </div>
                        <div class="device-status ${statusClass}">${statusText}</div>
                    </div>
                    <div class="device-name">${device.name}</div>
                    <div class="device-room">${device.room?.name || 'Unknown Room'}</div>
                    ${device.value !== null && device.value !== undefined ? 
                        `<div class="device-value">Value: ${device.value}${this.getDeviceUnit(device.type)}</div>` : 
                        ''
                    }
                </div>
            `;
        }).join('');
    }

    getDeviceIcon(type) {
        switch (type) {
            case 'LIGHT': return 'fas fa-lightbulb';
            case 'THERMOSTAT': return 'fas fa-thermometer-half';
            case 'LOCK': return 'fas fa-lock';
            default: return 'fas fa-microchip';
        }
    }

    getDeviceUnit(type) {
        switch (type) {
            case 'THERMOSTAT': return '°C';
            case 'LIGHT': return '%';
            default: return '';
        }
    }

    filterDevices(filter) {
        let filteredDevices;

        if (filter === 'active') {
            filteredDevices = this.devices.filter(d => d.status);
        } else {
            filteredDevices = this.devices;
        }

        this.displayDevices(filteredDevices);
    }

    async getAISuggestions() {
        const aiBtn = document.getElementById('getAIBtn');
        const aiResponse = document.getElementById('aiResponse');
        const aiContent = document.getElementById('aiContent');

        aiBtn.disabled = true;
        aiBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generating...';

        try {
            const result = await ipcRenderer.invoke('api-get-ai-suggestions', 1);

            if (result.success) {
                aiContent.textContent = result.data.suggestion || 'No suggestions available.';
                aiResponse.style.display = 'block';
            } else {
                aiContent.textContent = `Error: ${result.error}`;
                aiResponse.style.display = 'block';
            }
        } catch (error) {
            console.error('Error getting AI suggestions:', error);
            aiContent.textContent = 'Failed to get AI suggestions. Please check your connection.';
            aiResponse.style.display = 'block';
        } finally {
            aiBtn.disabled = false;
            aiBtn.innerHTML = '<i class="fas fa-brain"></i> Generate Smart Recommendations';
        }
    }

    async sendStatusReport() {
        const recipientEmail = document.getElementById('recipientEmail').value;
        const emailDetails = document.getElementById('emailDetails').value;

        const activeDevices = this.devices.filter(d => d.status).length;
        const currentTemp = this.temperatureData.length > 0 ?
            this.temperatureData[0].temperature : 'N/A';

        const emailData = {
            recipient: recipientEmail,
            systemStatus: activeDevices > 0 ? 'ONLINE' : 'OFFLINE',
            totalDevices: this.devices.length,
            activeDevices: activeDevices,
            temperature: currentTemp,
            details: emailDetails || 'No additional details provided.'
        };

        try {
            const result = await ipcRenderer.invoke('simulate-email', emailData);

            if (result.success) {

                document.getElementById('emailDetails').value = '';


                this.showNotification('Email simulation completed successfully!', 'success');
            }
        } catch (error) {
            console.error('Error sending email:', error);
            this.showNotification('Failed to send email simulation.', 'error');
        }
    }

    showNotification(message, type = 'info') {

        const notification = document.createElement('div');
        notification.style.cssText = `
            position: fixed;
            top: 80px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            z-index: 1001;
            animation: slideIn 0.3s ease;
            max-width: 300px;
        `;

        switch (type) {
            case 'success':
                notification.style.background = 'linear-gradient(135deg, #4caf50, #45a049)';
                break;
            case 'error':
                notification.style.background = 'linear-gradient(135deg, #f44336, #d32f2f)';
                break;
            default:
                notification.style.background = 'linear-gradient(135deg, #2196f3, #1976d2)';
        }

        notification.textContent = message;
        document.body.appendChild(notification);


        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, 3000);
    }

    async refreshAllData() {
        const refreshBtn = document.getElementById('refreshBtn');
        refreshBtn.disabled = true;
        refreshBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Refreshing...';

        await this.testConnection();

        switch (this.currentView) {
            case 'dashboard':
                await this.loadDashboardData();
                break;
            case 'devices':
                await this.loadDevices();
                break;
        }

        refreshBtn.disabled = false;
        refreshBtn.innerHTML = '<i class="fas fa-sync-alt"></i> Refresh';

        this.showNotification('Data refreshed successfully!', 'success');
    }

    showSettings() {
        this.showNotification('Settings panel coming soon!', 'info');
    }
}


document.addEventListener('DOMContentLoaded', () => {
    new SmartHomeRenderer();
});


const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);
