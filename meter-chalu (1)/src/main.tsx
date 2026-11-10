import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';
import '@livekit/components-styles';

// Ensure no stale service worker serves cached assets (Task 4)
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.getRegistrations().then(registrations => {
    for (const registration of registrations) {
      registration.unregister().then(unregistered => {
        if (unregistered) console.log('[SW] Unregistered old service worker');
      });
    }
  });
  if ('caches' in window) {
    caches.keys().then(keys => {
      for (const key of keys) {
        caches.delete(key);
      }
    });
  }
}

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
