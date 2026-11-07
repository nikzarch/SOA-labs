import { h, field } from '../dom.js';

export function CityLookup({ lookupId, setLookupId, onFind, onDelete }) {
  return h('section', { className: 'id-tools', 'aria-labelledby': 'id-tools-title' },
    h('h3', { id: 'id-tools-title' }, '\u041f\u043e\u0438\u0441\u043a \u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u0438\u0435 \u043f\u043e ID'),
    h('form', {
      className: 'city-lookup-form',
      onSubmit: event => {
        event.preventDefault();
        onFind(lookupId);
      },
    },
    field('ID', h('input', {
      type: 'number', min: 1, required: true, value: lookupId,
      onChange: event => setLookupId(event.target.value),
      placeholder: '\u041d\u0430\u043f\u0440\u0438\u043c\u0435\u0440, 12',
    })),
    h('button', { type: 'submit', className: 'secondary' }, '\u041d\u0430\u0439\u0442\u0438'),
    h('button', {
      type: 'button', className: 'danger', disabled: !lookupId,
      onClick: () => onDelete(lookupId),
    }, '\u0423\u0434\u0430\u043b\u0438\u0442\u044c')));
}
