import { createApp } from 'vue';
import App from './App.vue';
import { router } from './router';
import { setGatewayBase } from '@cutepet/api-client';
import './styles.css';

setGatewayBase(import.meta.env.VITE_GATEWAY_BASE_URL);
createApp(App).use(router).mount('#app');
