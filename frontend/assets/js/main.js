import { createRoot } from 'react-dom/client';
import { h } from './dom.js';
import { App } from './App.js';

createRoot(document.getElementById('root')).render(h(App));
