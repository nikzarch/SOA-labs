import { h } from '../dom.js';

export function Toast({ message }) {
  if (!message) return null;
  return h('div', { className: 'toast', role: 'status' }, message);
}

export function DeleteDialog({ cityId, busy, onCancel, onConfirm }) {
  if (cityId === null) return null;
  return h('div', {
    className: 'modal-backdrop',
    onMouseDown: event => { if (event.target === event.currentTarget) onCancel(); },
  },
  h('section', {
    className: 'modal', role: 'dialog', 'aria-modal': 'true', 'aria-labelledby': 'delete-title',
  },
  h('h2', { id: 'delete-title' }, 'Удалить город?'),
  h('p', null, `Город №${cityId} будет удалён. Это действие нельзя отменить.`),
  h('div', { className: 'actions' },
    h('button', { type: 'button', className: 'secondary', onClick: onCancel }, 'Отмена'),
    h('button', { type: 'button', className: 'danger', onClick: onConfirm, disabled: busy }, 'Удалить'))));
}
