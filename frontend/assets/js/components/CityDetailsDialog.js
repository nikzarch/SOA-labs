import { h } from '../dom.js';

const DETAILS = [
  ['name', '\u0413\u043e\u0440\u043e\u0434'],
  ['id', 'ID'],
  ['coordinates', '\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b'],
  ['area', '\u041f\u043b\u043e\u0449\u0430\u0434\u044c'],
  ['population', '\u041d\u0430\u0441\u0435\u043b\u0435\u043d\u0438\u0435'],
  ['metersAboveSeaLevel', '\u0412\u044b\u0441\u043e\u0442\u0430 \u043d\u0430\u0434 \u0443\u0440\u043e\u0432\u043d\u0435\u043c \u043c\u043e\u0440\u044f'],
  ['climate', '\u041a\u043b\u0438\u043c\u0430\u0442'],
  ['government', '\u0424\u043e\u0440\u043c\u0430 \u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f'],
  ['standardOfLiving', '\u0423\u0440\u043e\u0432\u0435\u043d\u044c \u0436\u0438\u0437\u043d\u0438'],
  ['governor', '\u0412\u043e\u0437\u0440\u0430\u0441\u0442 \u0433\u0443\u0431\u0435\u0440\u043d\u0430\u0442\u043e\u0440\u0430'],
  ['creationDate', '\u0414\u0430\u0442\u0430 \u0441\u043e\u0437\u0434\u0430\u043d\u0438\u044f'],
];

function valueOf(city, key) {
  if (key === 'coordinates') return `${city.coordinates?.x ?? '\u2014'}, ${city.coordinates?.y ?? '\u2014'}`;
  if (key === 'governor') return city.governor?.age ?? '\u2014';
  return city[key] ?? '\u2014';
}

export function CityDetailsDialog({ city, onClose, onEdit }) {
  if (!city) return null;
  return h('div', {
    className: 'modal-backdrop',
    onMouseDown: event => { if (event.target === event.currentTarget) onClose(); },
  },
  h('section', { className: 'modal city-details-modal', role: 'dialog', 'aria-modal': 'true', 'aria-labelledby': 'found-city-title' },
    h('div', { className: 'section-heading' },
      h('h2', { id: 'found-city-title' }, `\u0413\u043e\u0440\u043e\u0434 #${city.id}`),
      h('button', { type: 'button', className: 'secondary', onClick: onClose, 'aria-label': 'Close' }, '\u00d7')),
    h('dl', { className: 'city-details-grid' }, ...DETAILS.flatMap(([key, label]) => [
      h('dt', { key: `${key}-label` }, label),
      h('dd', { key: `${key}-value` }, valueOf(city, key)),
    ])),
    h('div', { className: 'actions modal-actions' },
      h('button', { type: 'button', className: 'secondary', onClick: onClose }, '\u0417\u0430\u043a\u0440\u044b\u0442\u044c'),
      h('button', { type: 'button', onClick: onEdit }, '\u0420\u0435\u0434\u0430\u043a\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c'))));
}
