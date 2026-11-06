import { h } from '../dom.js';

const COLUMNS = [
  'ID', 'Город', 'Координаты', 'Площадь', 'Население', 'Высота',
  'Климат', 'Правление', 'Уровень жизни', 'Возраст губернатора', 'Создан', 'Действия',
];

function cityValues(city) {
  return [
    city.id,
    city.name,
    `${city.coordinates?.x ?? '—'}, ${city.coordinates?.y ?? '—'}`,
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
    ...cityValues(city).map((value, index) => h('td', { key: index }, value ?? '—')),
    h('td', { className: 'row-actions' },
      h('button', { type: 'button', className: 'secondary', onClick: () => onOpen(city.id) }, 'Открыть'),
      h('button', { type: 'button', className: 'danger', onClick: () => onDelete(city.id) }, 'Удалить')));
}

export function CitiesTable({ cities, onOpen, onDelete }) {
  return h('div', { className: 'table-wrap' },
    h('table', null,
      h('thead', null, h('tr', null, ...COLUMNS.map(title => h('th', { key: title }, title)))),
      h('tbody', null,
        cities.length
          ? cities.map(city => h(CityRow, { key: city.id, city, onOpen, onDelete }))
          : h('tr', null, h('td', { className: 'empty', colSpan: COLUMNS.length }, 'Города не найдены')))));
}
