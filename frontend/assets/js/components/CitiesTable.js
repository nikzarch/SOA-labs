import { h } from '../dom.js';

const COLUMNS = [
  'ID', '\u0413\u043e\u0440\u043e\u0434', '\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b',
  '\u041f\u043b\u043e\u0449\u0430\u0434\u044c', '\u041d\u0430\u0441\u0435\u043b\u0435\u043d\u0438\u0435',
  '\u0412\u044b\u0441\u043e\u0442\u0430', '\u041a\u043b\u0438\u043c\u0430\u0442',
  '\u041f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435', '\u0423\u0440\u043e\u0432\u0435\u043d\u044c \u0436\u0438\u0437\u043d\u0438',
  '\u0412\u043e\u0437\u0440\u0430\u0441\u0442 \u0433\u0443\u0431\u0435\u0440\u043d\u0430\u0442\u043e\u0440\u0430',
  '\u0421\u043e\u0437\u0434\u0430\u043d', '\u0414\u0435\u0439\u0441\u0442\u0432\u0438\u044f',
];

function cityValues(city) {
  return [
    city.id,
    city.name,
    `${city.coordinates?.x ?? '\u2014'}, ${city.coordinates?.y ?? '\u2014'}`,
    city.area,
    city.population,
    city.metersAboveSeaLevel,
    city.climate,
    city.government,
    city.standardOfLiving,
    city.governor?.age,
    city.creationDate,
  ];
}

function CityRow({ city, onOpen, onDelete }) {
  return h('tr', null,
    ...cityValues(city).map((value, index) => h('td', { key: index }, value ?? '\u2014')),
    h('td', { className: 'row-actions' },
      h('button', { type: 'button', className: 'secondary', onClick: () => onOpen(city.id) }, '\u0418\u0437\u043c\u0435\u043d\u0438\u0442\u044c'),
      h('button', { type: 'button', className: 'danger', onClick: () => onDelete(city.id) }, '\u0423\u0434\u0430\u043b\u0438\u0442\u044c')));
}

export function CitiesTable({ cities, onOpen, onDelete }) {
  return h('div', { className: 'table-wrap' },
    h('table', null,
      h('thead', null, h('tr', null, ...COLUMNS.map(title => h('th', { key: title }, title)))),
      h('tbody', null,
        cities.length
          ? cities.map(city => h(CityRow, { key: city.id, city, onOpen, onDelete }))
          : h('tr', null, h('td', { className: 'empty', colSpan: COLUMNS.length }, '\u0413\u043e\u0440\u043e\u0434\u044b \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b')))));
}
